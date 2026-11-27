package com.sigoc.sigoc_web.infrastructure;

import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.TransaccionPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de integración real contra MySQL 8 InnoDB (Testcontainers) ejecutando los
 * adaptadores JPA reales ({@link MaterialPort}, {@link ComprobantePort} y {@link TransaccionPort})
 * con bloqueos pesimistas {@code @Lock(LockModeType.PESSIMISTIC_WRITE)} (QA-04 y QA-06).
 * Se ejecuta automáticamente en {@code ./mvnw verify} (Failsafe / CI GitHub Actions) y se
 * omite de forma segura si Docker no está disponible en la estación local.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ConcurrenciaMySqlInnoDbIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("sigoc_it")
            .withUsername("sigoc")
            .withPassword("sigoc_test");

    @DynamicPropertySource
    static void configurarMySql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MaterialPort materialPort;

    @Autowired
    private ComprobantePort comprobantePort;

    @Autowired
    private TransaccionPort transaccionPort;

    @Test
    void diezHilosConMaterialJpaAdapterPessimisticWriteEnInnoDbNoGeneranStockNegativoNiDeadlocks() throws Exception {
        Material creado = materialPort.guardar(new Material(
                null, "Cemento Portland Tipo I IT", "Bolsa",
                new BigDecimal("100.00"), new BigDecimal("10.00"),
                new BigDecimal("28.50"), true, null));
        Integer idMaterial = creado.getIdMaterial();

        int hilos = 10;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazos = new AtomicInteger();
        AtomicInteger erroresInesperados = new AtomicInteger();

        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(pool.submit(() -> {
                try {
                    inicio.await();
                    boolean ok = transaccionPort.ejecutarNueva(() -> {
                        Material bloqueado = materialPort.findByIdParaActualizar(idMaterial).orElseThrow();
                        BigDecimal consumo = new BigDecimal("15.00");
                        if (bloqueado.getStockActual().compareTo(consumo) >= 0) {
                            materialPort.actualizarStock(idMaterial, bloqueado.getStockActual().subtract(consumo), null);
                            return true;
                        }
                        return false;
                    });
                    if (ok) {
                        exitos.incrementAndGet();
                    } else {
                        rechazos.incrementAndGet();
                    }
                } catch (Exception ex) {
                    erroresInesperados.incrementAndGet();
                }
            }));
        }
        inicio.countDown();
        for (Future<?> f : tareas) {
            f.get();
        }
        pool.shutdown();

        Material finalDb = materialPort.findById(idMaterial).orElseThrow();
        assertEquals(0, erroresInesperados.get(), "No debe ocurrir ningún deadlock ni error JPA en InnoDB");
        assertEquals(6, exitos.get(), "Solo 6 consumos de 15.00 caben en stock 100.00");
        assertEquals(4, rechazos.get(), "Los 4 restantes deben ser rechazados");
        assertEquals(0, new BigDecimal("10.00").compareTo(finalDb.getStockActual()), "Stock final exacto: 10.00");
    }

    @Test
    void ochoHilosConComprobanteJpaAdapterPessimisticWriteSoloReservanPendienteCapturaUnaVez() throws Exception {
        Comprobante creado = comprobantePort.guardar(new Comprobante(
                null, 999, "COMPROBANTE_INTERNO", "CV01", 9001, "CV01-009001",
                LocalDate.now(), new BigDecimal("1000.00"), new BigDecimal("180.00"), new BigDecimal("1180.00"),
                "TOKEN-IT-9001", LocalDateTime.now().plusHours(72), "EMITIDO", true, null));
        Integer idComprobante = creado.getIdComprobante();

        int hilos = 8;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger ganadores = new AtomicInteger();
        AtomicInteger bloqueados = new AtomicInteger();
        AtomicInteger errores = new AtomicInteger();

        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(pool.submit(() -> {
                try {
                    inicio.await();
                    boolean reservado = transaccionPort.ejecutarNueva(() -> {
                        Comprobante bloqueado = comprobantePort.findByIdParaActualizar(idComprobante).orElseThrow();
                        if ("EMITIDO".equals(bloqueado.getEstado())) {
                            bloqueado.setEstado("PENDIENTE_CAPTURA");
                            comprobantePort.guardar(bloqueado);
                            return true;
                        }
                        return false;
                    });
                    if (reservado) {
                        ganadores.incrementAndGet();
                    } else {
                        bloqueados.incrementAndGet();
                    }
                } catch (Exception ex) {
                    errores.incrementAndGet();
                }
            }));
        }
        inicio.countDown();
        for (Future<?> f : tareas) {
            f.get();
        }
        pool.shutdown();

        Comprobante finalDb = comprobantePort.findById(idComprobante).orElseThrow();
        assertEquals(0, errores.get());
        assertEquals(1, ganadores.get(), "Solo un hilo puede transicionar EMITIDO -> PENDIENTE_CAPTURA");
        assertEquals(7, bloqueados.get());
        assertEquals("PENDIENTE_CAPTURA", finalDb.getEstado());
    }
}
