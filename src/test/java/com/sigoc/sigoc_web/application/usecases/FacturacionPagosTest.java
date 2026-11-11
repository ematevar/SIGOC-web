package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;
import com.sigoc.sigoc_web.infrastructure.adapters.ComprobantePdfAdapter;
import com.sigoc.sigoc_web.infrastructure.adapters.PasarelaSimuladaAdapter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FacturacionPagosTest {

    private static final Clock FIJO = Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"), ZoneOffset.UTC);
    private static final UsuarioSesion ADMIN = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private static final UsuarioSesion RESIDENTE = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private static final UsuarioSesion ALMACENERO = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);
    private static final UsuarioSesion CONTADOR = new UsuarioSesion(4, "cont", "Contador", RolSistema.CONTADOR, false);

    @Test
    void validadorSigocValidaDniRucTelefonoSerieYPeriodo() {
        assertDoesNotThrow(() -> ValidadorSigoc.validarDni("74859612"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarDni("00000000"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarDni("1234567A"));

        assertDoesNotThrow(() -> ValidadorSigoc.validarRuc("20601234567"));
        assertDoesNotThrow(() -> ValidadorSigoc.validarRuc("10748596123"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarRuc("99601234567"));

        assertDoesNotThrow(() -> ValidadorSigoc.validarTelefono("952123456"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarTelefono("952-12345"));

        assertDoesNotThrow(() -> ValidadorSigoc.validarSerieComprobante("CV01-000001"));
        assertDoesNotThrow(() -> ValidadorSigoc.validarSerieComprobante("F001-000125"));
        assertDoesNotThrow(() -> ValidadorSigoc.validarSerieComprobante("B001-000001"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarSerieComprobante("FAC-123"));

        assertDoesNotThrow(() -> ValidadorSigoc.validarPeriodo("2026-11"));
        assertThrows(ReglaNegocioException.class, () -> ValidadorSigoc.validarPeriodo("2026-13"));
    }

    @Test
    void guardarClienteValidaDocumentosYDuplicadosSoloAdmin() {
        ClientesFake clientePort = new ClientesFake();
        GuardarCliente uc = new GuardarCliente(clientePort);

        assertThrows(AccesoDenegadoException.class, () ->
                uc.ejecutar(RESIDENTE, null, "DNI", "74859612", "Ana Torres", "952123456", null, null));

        assertThrows(ReglaNegocioException.class, () ->
                uc.ejecutar(ADMIN, null, "DNI", "74859612", "Ana 123", "952123456", null, null));

        Cliente c = uc.ejecutar(ADMIN, null, "ruc", "20601234567", "Constructora Andes SAC", "952123456", "a@b.pe", "Lima");
        assertEquals("RUC", c.getTipoDocumento());
        assertThrows(ReglaNegocioException.class, () ->
                uc.ejecutar(ADMIN, null, "RUC", "20601234567", "Otra SAC", "952123456", null, null));
    }

    @Test
    void flujoAutomaticoValorizacionCv01PdfYPagoConIntegridad() {
        ObraPort obraPort = obraOperable(1);
        PartidasFake partidaPort = new PartidasFake(1);
        AvancesFake avancePort = new AvancesFake();
        // Partida 1 (peso 60%) al 50% y Partida 2 (peso 40%) al 50% => Avance acumulado = 50%
        avancePort.datos.add(new AvancePartida(1, 1, LocalDate.of(2026, 10, 15), new BigDecimal("50.00"), "Ok", 2, true, null));
        avancePort.datos.add(new AvancePartida(2, 2, LocalDate.of(2026, 10, 20), new BigDecimal("50.00"), "Ok", 2, true, null));

        ValorizacionesFake valPort = new ValorizacionesFake();
        ComprobantesFake compPort = new ComprobantesFake();
        PagosFake pagoPort = new PagosFake();
        IngresosFake ingresoPort = new IngresosFake();
        TipoCambioFake tcPort = new TipoCambioFake();
        PasarelaFake pasarelaPort = new PasarelaFake();

        RegistrarValorizacion regVal = new RegistrarValorizacion(valPort, obraPort, partidaPort, avancePort, FIJO);
        EmitirComprobante emComp = new EmitirComprobante(compPort, valPort, FIJO, 72);
        GenerarPdfComprobante pdfUc = new GenerarPdfComprobante(compPort, valPort, obraPort, new ClientesFake(), new ComprobantePdfAdapter());
        ProcesarPago procPago = new ProcesarPago(pagoPort, obraPort, valPort, compPort, ingresoPort, tcPort, pasarelaPort, FIJO);

        // Residente o Contador calculan valorización automática; Almacenero denegado
        assertThrows(AccesoDenegadoException.class, () ->
                regVal.ejecutar(ALMACENERO, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null));

        Valorizacion val = regVal.ejecutar(RESIDENTE, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null);
        // Presupuesto 500,000 * 50% = 250,000 subtotal + 45,000 IGV = 295,000 total
        assertEquals(1, val.getNumero());
        assertEquals(new BigDecimal("50.00"), val.getAvanceAcumulado());
        assertEquals(new BigDecimal("50.00"), val.getPorcentajeAvancePeriodo());
        assertEquals(new BigDecimal("250000.00"), val.getSubtotal());
        assertEquals(new BigDecimal("45000.00"), val.getIgv());
        assertEquals(new BigDecimal("295000.00"), val.getTotal());
        assertEquals("PENDIENTE", val.getEstado());

        // RN-20: periodo solapado rechazado
        assertThrows(ReglaNegocioException.class, () ->
                regVal.ejecutar(CONTADOR, 1, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 11, 5), null));

        // Emitir comprobante CV01 automático con enlace de 72h
        Comprobante comp = emComp.ejecutar(CONTADOR, val.getIdValorizacion(), null, null);
        assertEquals("CV01-000001", comp.getSerieNumero());
        assertEquals("COMPROBANTE_VALORIZACION", comp.getTipoComprobante());
        assertNotNull(comp.getCodigoPago());
        assertTrue(comp.getCodigoPago().length() >= 32);
        assertEquals(LocalDateTime.now(FIJO).plusHours(72), comp.getVenceEl());
        assertEquals(new BigDecimal("295000.00"), comp.getTotal());
        assertEquals("FACTURADA", valPort.findById(val.getIdValorizacion()).orElseThrow().getEstado());

        // Generar PDF interno CV01 válido
        GenerarPdfComprobante.ResultadoPdf pdf = pdfUc.ejecutar(CONTADOR, comp.getIdComprobante());
        assertTrue(pdf.contenido().length > 500);
        assertTrue(new String(pdf.contenido(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF-"));

        // C02: el servidor rechaza un monto manipulado distinto al del comprobante
        assertThrows(ReglaNegocioException.class, () ->
                procPago.ejecutar(CONTADOR, 1, val.getIdValorizacion(), comp.getIdComprobante(),
                        new BigDecimal("1.00"), "TRANSFERENCIA_SIMULADA", "OP-TAMPER"));

        // No permite doble facturación
        assertThrows(ReglaNegocioException.class, () ->
                emComp.ejecutar(ADMIN, val.getIdValorizacion(), null, null));

        // Pago público con tarjeta de prueba terminada en 0002 -> PagoRechazadoException + conserva RECHAZADO sin Ingreso
        assertThrows(PagoRechazadoException.class, () ->
                procPago.cobrarTarjetaSimuladaPublica(comp.getCodigoPago(), "4000000000000002", "JUAN PEREZ", "12/28", "123"));
        assertEquals(1, pagoPort.datos.size());
        assertEquals("RECHAZADO", pagoPort.datos.get(1).getEstado());
        assertEquals("0002", pagoPort.datos.get(1).getUltimos4());
        assertEquals(0, ingresoPort.datos.size());

        // Renovar enlace actualiza token e invalida el anterior
        String linkAnterior = comp.getCodigoPago();
        Comprobante renovado = emComp.renovarEnlace(CONTADOR, comp.getIdComprobante());
        assertNotEquals(linkAnterior, renovado.getCodigoPago());

        // Cobro público aprobado mediante PayPal Sandbox (con T.C. 3.75 => 295000 / 3.75 = 78666.67 USD)
        Pago pago = procPago.capturarPayPalPublico(renovado.getCodigoPago(), "ORDER-PP-1");
        assertEquals("APROBADO", pago.getEstado());
        assertEquals("PAYPAL_SANDBOX", pago.getPasarela());
        assertEquals(1, ingresoPort.datos.size());
        assertEquals(pago.getIdPago(), ingresoPort.datos.get(0).getIdPago());
        assertEquals(ingresoPort.datos.get(0).getIdIngreso(), pago.getIdIngresoGenerado());
        assertEquals("PAGADA", valPort.findById(val.getIdValorizacion()).orElseThrow().getEstado());
        assertEquals("PAGADO", compPort.findById(comp.getIdComprobante()).orElseThrow().getEstado());

        // Idempotencia de captura PayPal y bloqueo de re-pago
        assertThrows(ReglaNegocioException.class, () ->
                procPago.capturarPayPalPublico(renovado.getCodigoPago(), "ORDER-PP-1"));
    }

    @Test
    void contadorActualizaTipoCambioYResidenteNo() {
        TipoCambioFake tcPort = new TipoCambioFake();
        GestionarTipoCambio tcUc = new GestionarTipoCambio(tcPort, FIJO);

        assertThrows(AccesoDenegadoException.class, () -> tcUc.actualizar(RESIDENTE, new BigDecimal("3.80")));
        TipoCambio tc = tcUc.actualizar(CONTADOR, new BigDecimal("3.785"));
        assertEquals(new BigDecimal("3.7850"), tc.getValor());
        assertEquals(CONTADOR.idUsuario(), tc.getIdUsuarioRegistro());
    }

    private static ObraPort obraOperable(int id) {
        Obra obra = new Obra(id, "Hospital Norte", "MINSA", "Piura", new BigDecimal("500000"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "En Ejecución", true, null);
        return new ObraPort() {
            @Override public List<Obra> findAllActivas() { return List.of(obra); }
            @Override public Optional<Obra> findById(Integer i) { return Objects.equals(i, id) ? Optional.of(obra) : Optional.empty(); }
            @Override public Obra guardar(Obra o) { return o; }
            @Override public boolean tieneMovimientos(Integer idObra) { return false; }
            @Override public void cerrar(Integer idObra) {}
            @Override public void desactivar(Integer idObra) {}
        };
    }

    private static final class PartidasFake implements PartidaPort {
        private final List<Partida> datos;
        PartidasFake(int idObra) {
            datos = List.of(
                    new Partida(1, idObra, "Cimentación", "m3", new BigDecimal("100"), new BigDecimal("60"), true, null),
                    new Partida(2, idObra, "Estructuras", "m2", new BigDecimal("200"), new BigDecimal("40"), true, null)
            );
        }
        @Override public List<Partida> findAllActivas() { return datos; }
        @Override public List<Partida> findActivasByObra(Integer idObra) { return datos; }
        @Override public Optional<Partida> findById(Integer id) { return datos.stream().filter(p -> Objects.equals(p.getIdPartida(), id)).findFirst(); }
        @Override public BigDecimal sumaPesosActivos(Integer idObra, Integer idEx) { return new BigDecimal("100"); }
        @Override public boolean tieneMovimientos(Integer id) { return false; }
        @Override public Partida guardar(Partida p) { return p; }
        @Override public void desactivar(Integer id) {}
    }

    private static final class AvancesFake implements AvancePartidaPort {
        final List<AvancePartida> datos = new ArrayList<>();
        @Override public List<AvancePartida> findActivosByPartida(Integer idPartida) { return datos; }
        @Override public List<AvancePartida> findActivosByObra(Integer idObra) { return datos; }
        @Override public List<AvancePartida> findAllActivos() { return datos; }
        @Override public Optional<AvancePartida> findUltimoActivoByPartida(Integer idPartida) { return Optional.empty(); }
        @Override public AvancePartida guardar(AvancePartida a) { datos.add(a); return a; }
    }

    private static final class ClientesFake implements ClientePort {
        final Map<Integer, Cliente> datos = new LinkedHashMap<>();
        int seq = 1;
        @Override public List<Cliente> findAllActivos() { return datos.values().stream().filter(Cliente::isActivo).toList(); }
        @Override public Optional<Cliente> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public boolean existsByNumeroDocumento(String doc, Integer idEx) {
            return datos.values().stream().anyMatch(c -> c.getNumeroDocumento().equals(doc) && !Objects.equals(c.getIdCliente(), idEx));
        }
        @Override public Cliente guardar(Cliente c) { if (c.getIdCliente() == null) c.setIdCliente(seq++); datos.put(c.getIdCliente(), c); return c; }
        @Override public void desactivar(Integer id) { datos.get(id).setActivo(false); }
    }

    private static final class ValorizacionesFake implements ValorizacionPort {
        final Map<Integer, Valorizacion> datos = new LinkedHashMap<>();
        int seq = 1;
        @Override public List<Valorizacion> findAllActivas() { return List.copyOf(datos.values()); }
        @Override public List<Valorizacion> findActivasByObra(Integer idObra) { return List.copyOf(datos.values()); }
        @Override public Optional<Valorizacion> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Valorizacion guardar(Valorizacion v) { if (v.getIdValorizacion() == null) v.setIdValorizacion(seq++); datos.put(v.getIdValorizacion(), v); return v; }
    }

    private static final class ComprobantesFake implements ComprobantePort {
        final Map<Integer, Comprobante> datos = new LinkedHashMap<>();
        int seq = 1;
        @Override public List<Comprobante> findAllActivos() { return List.copyOf(datos.values()); }
        @Override public Optional<Comprobante> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Optional<Comprobante> findActivoByValorizacion(Integer idVal) {
            return datos.values().stream().filter(c -> c.isActivo() && Objects.equals(c.getIdValorizacion(), idVal)).findFirst();
        }
        @Override public Optional<Comprobante> findBySerieNumero(String s) {
            return datos.values().stream().filter(c -> c.getSerieNumero().equals(s)).findFirst();
        }
        @Override public Optional<Comprobante> findByCodigoPago(String cod) {
            return datos.values().stream().filter(c -> c.isActivo() && Objects.equals(c.getCodigoPago(), cod)).findFirst();
        }
        @Override public Comprobante guardar(Comprobante c) { if (c.getIdComprobante() == null) c.setIdComprobante(seq++); datos.put(c.getIdComprobante(), c); return c; }
    }

    private static final class PagosFake implements PagoPort {
        final Map<Integer, Pago> datos = new LinkedHashMap<>();
        int seq = 1;
        @Override public List<Pago> findAllActivos() { return List.copyOf(datos.values()); }
        @Override public List<Pago> findActivosByObra(Integer idObra) { return List.copyOf(datos.values()); }
        @Override public Optional<Pago> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Optional<Pago> findByCodigoOperacion(String cod) {
            return datos.values().stream().filter(p -> p.getCodigoOperacion().equals(cod)).findFirst();
        }
        @Override public Optional<Pago> findByIdCapturaExterna(String cap) {
            return datos.values().stream().filter(p -> Objects.equals(p.getIdCapturaExterna(), cap)).findFirst();
        }
        @Override public Pago guardar(Pago p) { if (p.getIdPago() == null) p.setIdPago(seq++); datos.put(p.getIdPago(), p); return p; }
    }

    private static final class IngresosFake implements IngresoPort {
        final List<Ingreso> datos = new ArrayList<>();
        int seq = 1;
        @Override public List<Ingreso> findAllActivos() { return datos; }
        @Override public List<Ingreso> findActivosByObra(Integer idObra) { return datos; }
        @Override public Ingreso guardar(Ingreso i) { i.setIdIngreso(seq++); datos.add(i); return i; }
    }

    private static final class TipoCambioFake implements TipoCambioPort {
        TipoCambio actual = new TipoCambio(1, "USD", "PEN", new BigDecimal("3.7500"), LocalDateTime.now(FIJO), 1);
        @Override public Optional<TipoCambio> findByPar(String o, String d) { return Optional.ofNullable(actual); }
        @Override public TipoCambio guardar(TipoCambio tc) { tc.setIdTipoCambio(1); actual = tc; return tc; }
    }

    private static final class PasarelaFake implements PasarelaPagoPort {
        private final PasarelaSimuladaAdapter simulada = new PasarelaSimuladaAdapter(FIJO);
        @Override public boolean paypalHabilitado() { return true; }
        @Override public OrdenPayPal crearOrdenPayPal(BigDecimal m, String mon, String ref, String desc, String ret, String can) {
            return new OrdenPayPal("ORDER-PP-1", "CREATED", "https://sandbox.paypal.com/checkoutnow?token=ORDER-PP-1");
        }
        @Override public ResultadoCapturaPayPal capturarOrdenPayPal(String idOrdenExterna) {
            return new ResultadoCapturaPayPal(true, "CAP-" + idOrdenExterna, new BigDecimal("78666.67"), "USD", "COMPLETED");
        }
        @Override public ResultadoTarjetaSimulada cobrarTarjetaSimulada(String n, String t, String v, String c, BigDecimal m) {
            return simulada.cobrar(n, t, v, c, m);
        }
    }
}
