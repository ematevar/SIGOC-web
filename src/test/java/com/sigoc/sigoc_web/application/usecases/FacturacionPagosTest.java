package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FacturacionPagosTest {

    private static final Clock FIJO = Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"), ZoneOffset.UTC);
    private static final UsuarioSesion ADMIN = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private static final UsuarioSesion RESIDENTE = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private static final UsuarioSesion ALMACENERO = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

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

        // Nombre con números falla en DNI
        assertThrows(ReglaNegocioException.class, () ->
                uc.ejecutar(ADMIN, null, "DNI", "74859612", "Ana 123", "952123456", null, null));

        Cliente c = uc.ejecutar(ADMIN, null, "ruc", "20601234567", "Constructora Andes SAC", "952123456", "a@b.pe", "Lima");
        assertEquals("RUC", c.getTipoDocumento());
        assertThrows(ReglaNegocioException.class, () ->
                uc.ejecutar(ADMIN, null, "RUC", "20601234567", "Otra SAC", "952123456", null, null));
    }

    @Test
    void flujoCompletoValorizacionComprobanteYPagoConIngresoAutomatico() {
        ObraPort obraPort = obraOperable(1);
        ValorizacionesFake valPort = new ValorizacionesFake();
        ComprobantesFake compPort = new ComprobantesFake();
        PagosFake pagoPort = new PagosFake();
        IngresosFake ingresoPort = new IngresosFake();

        RegistrarValorizacion regVal = new RegistrarValorizacion(valPort, obraPort, FIJO);
        EmitirComprobante emComp = new EmitirComprobante(compPort, valPort, FIJO);
        ProcesarPago procPago = new ProcesarPago(pagoPort, obraPort, valPort, compPort, ingresoPort, FIJO);

        // Residente puede registrar valorización de obra operativa, Almacenero no
        assertThrows(AccesoDenegadoException.class, () ->
                regVal.ejecutar(ALMACENERO, 1, "2026-11", null, new BigDecimal("25"), new BigDecimal("11800")));
        Valorizacion val = regVal.ejecutar(RESIDENTE, 1, "2026-11", null, new BigDecimal("25"), new BigDecimal("11800"));
        assertEquals("PENDIENTE", val.getEstado());

        // Emitir comprobante calcula IGV 18% exacto y pasa valorización a FACTURADA
        Comprobante comp = emComp.ejecutar(ADMIN, val.getIdValorizacion(), null, "f001-000001");
        assertEquals("F001-000001", comp.getSerieNumero());
        assertEquals("FACTURA_PRUEBA", comp.getTipoComprobante());
        assertEquals(new BigDecimal("11800.00"), comp.getTotal());
        assertEquals(new BigDecimal("10000.00"), comp.getSubtotal());
        assertEquals(new BigDecimal("1800.00"), comp.getIgv());
        assertEquals("FACTURADA", valPort.findById(val.getIdValorizacion()).orElseThrow().getEstado());

        // No permite doble facturación
        assertThrows(ReglaNegocioException.class, () ->
                emComp.ejecutar(ADMIN, val.getIdValorizacion(), null, "F001-000002"));

        // Procesar pago sandbox genera ingreso automático y marca valorización/comprobante como PAGADA/PAGADO
        Pago pago = procPago.ejecutar(ADMIN, 1, val.getIdValorizacion(), comp.getIdComprobante(),
                new BigDecimal("11800"), "yape_simulado", "OP-100");
        assertEquals("APROBADO", pago.getEstado());
        assertEquals(1, ingresoPort.datos.size());
        assertEquals(ingresoPort.datos.get(0).getIdIngreso(), pago.getIdIngresoGenerado());
        assertEquals("PAGADA", valPort.findById(val.getIdValorizacion()).orElseThrow().getEstado());
        assertEquals("PAGADO", compPort.findById(comp.getIdComprobante()).orElseThrow().getEstado());

        // Bloquea código de operación repetido o valorización ya pagada
        assertThrows(ReglaNegocioException.class, () ->
                procPago.ejecutar(ADMIN, 1, null, null, new BigDecimal("100"), null, "OP-100"));
        assertThrows(ReglaNegocioException.class, () ->
                procPago.ejecutar(ADMIN, 1, val.getIdValorizacion(), null, new BigDecimal("100"), null, "OP-101"));
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
        @Override public Pago guardar(Pago p) { if (p.getIdPago() == null) p.setIdPago(seq++); datos.put(p.getIdPago(), p); return p; }
    }

    private static final class IngresosFake implements IngresoPort {
        final List<Ingreso> datos = new ArrayList<>();
        int seq = 1;
        @Override public List<Ingreso> findAllActivos() { return datos; }
        @Override public List<Ingreso> findActivosByObra(Integer idObra) { return datos; }
        @Override public Ingreso guardar(Ingreso i) { i.setIdIngreso(seq++); datos.add(i); return i; }
    }
}
