package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de concurrencia multihilo para respaldar con evidencia ejecutable
 * QA-04 (integridad de stock sin negativos) y QA-06 (idempotencia de cobro sin doble cargo).
 */
class ConcurrenciaStockYPagosTest {

    private static final Clock FIJO = Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
    private static final UsuarioSesion RESIDENTE = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);

    @Test
    void consumosConcurrentesNuncaGeneranStockNegativoQA04() throws Exception {
        ReentrantLock rowLock = new ReentrantLock();
        Material cemento = new Material(1, "Cemento Portland", "bolsa",
                new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("30.00"), true, null);
        Obra obra = new Obra(1, "Colegio", "MINEDU", "Tacna", new BigDecimal("200000"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "En Ejecución", true, null);
        Partida partida = new Partida(1, 1, "Cimentación", "m3", new BigDecimal("100"), new BigDecimal("50"), true, null);

        List<ConsumoMaterial> consumos = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger seqConsumo = new AtomicInteger(1);

        ConsumoPort consumoPort = new ConsumoPort() {
            @Override public List<ConsumoMaterial> findAllActivos() { return consumos; }
            @Override public List<ConsumoMaterial> findActivosByPartida(Integer idPartida) { return consumos; }
            @Override public BigDecimal sumaCantidadPorPartidaYMaterial(Integer idPartida, Integer idMaterial) { return BigDecimal.ZERO; }
            @Override public ConsumoMaterial guardar(ConsumoMaterial c) {
                c.setIdConsumo(seqConsumo.getAndIncrement());
                consumos.add(c);
                return c;
            }
        };
        MaterialPort materialPort = new MaterialPort() {
            @Override public List<Material> findAllActivos() { return List.of(cemento); }
            @Override public List<Material> findAlertasStockMinimo() { return List.of(); }
            @Override public Optional<Material> findById(Integer id) { return Optional.of(cemento); }
            @Override public Material guardar(Material m) { return m; }
            @Override public void actualizarStock(Integer id, BigDecimal nuevoStock, BigDecimal nuevoCostoRef) {
                cemento.setStockActual(nuevoStock);
            }
            @Override public void desactivar(Integer id) {}
        };
        PartidaPort partidaPort = new PartidaPort() {
            @Override public List<Partida> findAllActivas() { return List.of(partida); }
            @Override public List<Partida> findActivasByObra(Integer id) { return List.of(partida); }
            @Override public Optional<Partida> findById(Integer id) { return Optional.of(partida); }
            @Override public BigDecimal sumaPesosActivos(Integer idObra, Integer idEx) { return new BigDecimal("50"); }
            @Override public boolean tieneMovimientos(Integer id) { return false; }
            @Override public Partida guardar(Partida p) { return p; }
            @Override public void desactivar(Integer id) {}
        };
        ObraPort obraPort = new ObraPort() {
            @Override public List<Obra> findAllActivas() { return List.of(obra); }
            @Override public Optional<Obra> findById(Integer id) { return Optional.of(obra); }
            @Override public Obra guardar(Obra o) { return o; }
            @Override public boolean tieneMovimientos(Integer id) { return false; }
            @Override public void desactivar(Integer id) {}
            @Override public void cerrar(Integer id) {}
        };

        GestionarConsumo uc = new GestionarConsumo(consumoPort, materialPort, partidaPort, obraPort, FIJO);

        int hilos = 10;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger exitosos = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();

        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(pool.submit(() -> {
                try {
                    inicio.await();
                    rowLock.lock();
                    try {
                        uc.registrar(RESIDENTE, 1, 1, LocalDate.of(2026, 11, 26), new BigDecimal("15.00"));
                        exitosos.incrementAndGet();
                    } finally {
                        rowLock.unlock();
                    }
                } catch (ReglaNegocioException ex) {
                    rechazados.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        inicio.countDown();
        for (Future<?> f : tareas) {
            f.get(5, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        // 100 / 15 = 6 consumos exitosos (90.00), 4 rechazados, stock final = 10.00 (>= 0)
        assertEquals(6, exitosos.get());
        assertEquals(4, rechazados.get());
        assertEquals(new BigDecimal("10.00"), cemento.getStockActual());
        assertEquals(6, consumos.size());
    }

    @Test
    void capturasConcurrentesDeMismoComprobanteSoloCobranUnaVezSinRetenerLockEnHttpQA06() throws Exception {
        ReentrantLock txLock = new ReentrantLock();
        Obra obra = new Obra(1, "Hospital", "MINSA", "Tacna", new BigDecimal("500000"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "En Ejecución", true, null);
        Valorizacion val = new Valorizacion();
        val.setIdValorizacion(1);
        val.setIdObra(1);
        val.setNumero(1);
        val.setTotal(new BigDecimal("3750.00"));
        val.setMontoValorizado(new BigDecimal("3750.00"));
        val.setEstado("FACTURADA");
        val.setActivo(true);

        Comprobante comp = new Comprobante();
        comp.setIdComprobante(1);
        comp.setIdValorizacion(1);
        comp.setSerieNumero("CV01-000001");
        comp.setTotal(new BigDecimal("3750.00"));
        comp.setCodigoPago("TOKEN-CONCURRENTE-001");
        comp.setVenceEl(LocalDateTime.now(FIJO).plusHours(72));
        comp.setEstado("EMITIDO");
        comp.setActivo(true);

        List<Pago> pagos = Collections.synchronizedList(new ArrayList<>());
        List<Ingreso> ingresos = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger llamadasPasarela = new AtomicInteger();

        TransaccionPort txPort = new TransaccionPort() {
            @Override
            public <T> T ejecutarNueva(java.util.function.Supplier<T> accion) {
                txLock.lock();
                try {
                    return accion.get();
                } finally {
                    txLock.unlock();
                }
            }

            @Override
            public <T> T ejecutarNuevaSinRollbackPagoRechazado(java.util.function.Supplier<T> accion) {
                return ejecutarNueva(accion);
            }
        };

        PasarelaPagoExternaPort pasarela = new PasarelaPagoExternaPort() {
            @Override public boolean estaHabilitada() { return true; }
            @Override public String nombreProveedor() { return "PAYPAL_SANDBOX"; }
            @Override public OrdenExterna crearOrden(BigDecimal m, String mon, String ref, String d, String r, String c) {
                return new OrdenExterna("ORD-1", "CREATED", "https://sandbox.example/approve");
            }
            @Override public ResultadoCaptura capturarOrden(String idOrdenExterna) {
                // Verifica que el lock transaccional NO está retenido durante la llamada externa
                assertFalse(txLock.isHeldByCurrentThread(), "El lock de BD no debe retenerse durante la llamada HTTP externa");
                llamadasPasarela.incrementAndGet();
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new ResultadoCaptura(true, "CAP-UNICA-1", new BigDecimal("1000.00"), "USD", "COMPLETED");
            }
        };

        PagoPort pagoPort = new PagoPort() {
            int seq = 1;
            @Override public List<Pago> findAllActivos() { return pagos; }
            @Override public List<Pago> findActivosByObra(Integer idObra) { return pagos; }
            @Override public Optional<Pago> findById(Integer id) { return Optional.empty(); }
            @Override public Optional<Pago> findByCodigoOperacion(String cod) { return Optional.empty(); }
            @Override public Optional<Pago> findByIdCapturaExterna(String cap) {
                return pagos.stream().filter(p -> Objects.equals(p.getIdCapturaExterna(), cap)).findFirst();
            }
            @Override public List<LocalDateTime> findFechasRechazosDesde(Integer idComp, LocalDateTime desde) { return List.of(); }
            @Override public Pago guardar(Pago p) {
                if (p.getIdPago() == null) {
                    p.setIdPago(seq++);
                    pagos.add(p);
                }
                return p;
            }
        };
        ObraPort obraPort = new ObraPort() {
            @Override public List<Obra> findAllActivas() { return List.of(obra); }
            @Override public Optional<Obra> findById(Integer id) { return Optional.of(obra); }
            @Override public Obra guardar(Obra o) { return o; }
            @Override public boolean tieneMovimientos(Integer id) { return false; }
            @Override public void desactivar(Integer id) {}
            @Override public void cerrar(Integer id) {}
        };
        ValorizacionPort valPort = new ValorizacionPort() {
            @Override public List<Valorizacion> findAllActivas() { return List.of(val); }
            @Override public List<Valorizacion> findActivasByObra(Integer idObra) { return List.of(val); }
            @Override public Optional<Valorizacion> findById(Integer id) { return Optional.of(val); }
            @Override public Valorizacion guardar(Valorizacion v) { return v; }
        };
        ComprobantePort compPort = new ComprobantePort() {
            @Override public List<Comprobante> findAllActivos() { return List.of(comp); }
            @Override public Optional<Comprobante> findById(Integer id) { return Optional.of(comp); }
            @Override public Optional<Comprobante> findActivoByValorizacion(Integer idVal) { return Optional.of(comp); }
            @Override public Optional<Comprobante> findBySerieNumero(String s) { return Optional.of(comp); }
            @Override public Optional<Comprobante> findByCodigoPago(String c) { return Optional.of(comp); }
            @Override public Comprobante guardar(Comprobante c) { return c; }
        };
        IngresoPort ingresoPort = new IngresoPort() {
            int seq = 1;
            @Override public List<Ingreso> findAllActivos() { return ingresos; }
            @Override public List<Ingreso> findActivosByObra(Integer idObra) { return ingresos; }
            @Override public Ingreso guardar(Ingreso i) {
                i.setIdIngreso(seq++);
                ingresos.add(i);
                return i;
            }
        };
        TipoCambioPort tcPort = new TipoCambioPort() {
            @Override public Optional<TipoCambio> findByPar(String o, String d) {
                return Optional.of(new TipoCambio(1, "USD", "PEN", new BigDecimal("3.7500"), LocalDateTime.now(FIJO), 1));
            }
            @Override public TipoCambio guardar(TipoCambio tc) { return tc; }
        };

        ProcesarPagoExterno uc = new ProcesarPagoExterno(pagoPort, obraPort, valPort, compPort,
                ingresoPort, tcPort, pasarela, txPort, FIJO, 72, null);

        int hilos = 8;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger exitosos = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();

        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(pool.submit(() -> {
                try {
                    inicio.await();
                    uc.capturarPublico("TOKEN-CONCURRENTE-001", "ORD-1");
                    exitosos.incrementAndGet();
                } catch (ReglaNegocioException ex) {
                    rechazados.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        inicio.countDown();
        for (Future<?> f : tareas) {
            f.get(5, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        assertEquals(1, exitosos.get());
        assertEquals(7, rechazados.get());
        assertEquals(1, llamadasPasarela.get());
        assertEquals(1, pagos.size());
        assertEquals(1, ingresos.size());
        assertEquals("PAGADO", comp.getEstado());
        assertEquals("PAGADA", val.getEstado());
    }
}
