package com.sigoc.sigoc_web.infrastructure;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de integración real contra MySQL 8 InnoDB (Testcontainers) para verificar
 * bloqueos pesimistas SELECT ... FOR UPDATE (PESSIMISTIC_WRITE) en stock de materiales (QA-04)
 * y reserva atómica de comprobante en estado PENDIENTE_CAPTURA (QA-06).
 * Se omite automáticamente si Docker no está disponible en el entorno local.
 */
@Testcontainers(disabledWithoutDocker = true)
class ConcurrenciaMySqlInnoDbIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("sigoc_it")
            .withUsername("sigoc")
            .withPassword("sigoc_test");

    @Test
    void diezHilosConSelectForUpdateEnInnoDbNoGeneranStockNegativoNiDeadlocks() throws Exception {
        try (Connection setup = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             Statement st = setup.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS material_it (
                        id_material INT PRIMARY KEY,
                        stock_actual DECIMAL(10,2) NOT NULL
                    ) ENGINE=InnoDB
                    """);
            st.execute("DELETE FROM material_it");
            st.execute("INSERT INTO material_it (id_material, stock_actual) VALUES (1, 100.00)");
        }

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
                    try (Connection cn = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
                        cn.setAutoCommit(false);
                        try (PreparedStatement lock = cn.prepareStatement(
                                "SELECT stock_actual FROM material_it WHERE id_material = 1 FOR UPDATE");
                             ResultSet rs = lock.executeQuery()) {
                            rs.next();
                            BigDecimal stock = rs.getBigDecimal(1);
                            if (stock.compareTo(new BigDecimal("15.00")) >= 0) {
                                try (PreparedStatement upd = cn.prepareStatement(
                                        "UPDATE material_it SET stock_actual = ? WHERE id_material = 1")) {
                                    upd.setBigDecimal(1, stock.subtract(new BigDecimal("15.00")));
                                    upd.executeUpdate();
                                }
                                cn.commit();
                                exitos.incrementAndGet();
                            } else {
                                cn.rollback();
                                rechazos.incrementAndGet();
                            }
                        }
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

        BigDecimal stockFinal;
        try (Connection check = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             Statement st = check.createStatement();
             ResultSet rs = st.executeQuery("SELECT stock_actual FROM material_it WHERE id_material = 1")) {
            rs.next();
            stockFinal = rs.getBigDecimal(1);
        }

        assertEquals(0, erroresInesperados.get(), "No debe ocurrir ningún deadlock ni error SQL en InnoDB");
        assertEquals(6, exitos.get(), "Solo 6 consumos de 15.00 caben en stock 100.00");
        assertEquals(4, rechazos.get(), "Los 4 restantes deben ser rechazados");
        assertEquals(0, new BigDecimal("10.00").compareTo(stockFinal), "Stock final exacto en InnoDB: 10.00");
    }

    @Test
    void ochoHilosCompitiendoPorReservarPendienteCapturaEnInnoDbSoloAceptanUno() throws Exception {
        try (Connection setup = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             Statement st = setup.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS comprobante_it (
                        id_comprobante INT PRIMARY KEY,
                        estado VARCHAR(25) NOT NULL
                    ) ENGINE=InnoDB
                    """);
            st.execute("DELETE FROM comprobante_it");
            st.execute("INSERT INTO comprobante_it (id_comprobante, estado) VALUES (1, 'EMITIDO')");
        }

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
                    try (Connection cn = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
                        cn.setAutoCommit(false);
                        try (PreparedStatement lock = cn.prepareStatement(
                                "SELECT estado FROM comprobante_it WHERE id_comprobante = 1 FOR UPDATE");
                             ResultSet rs = lock.executeQuery()) {
                            rs.next();
                            if ("EMITIDO".equals(rs.getString(1))) {
                                try (PreparedStatement upd = cn.prepareStatement(
                                        "UPDATE comprobante_it SET estado = 'PENDIENTE_CAPTURA' WHERE id_comprobante = 1")) {
                                    upd.executeUpdate();
                                }
                                cn.commit();
                                ganadores.incrementAndGet();
                            } else {
                                cn.rollback();
                                bloqueados.incrementAndGet();
                            }
                        }
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

        assertEquals(0, errores.get());
        assertEquals(1, ganadores.get(), "Solo un hilo puede transicionar EMITIDO -> PENDIENTE_CAPTURA");
        assertEquals(7, bloqueados.get());
    }
}
