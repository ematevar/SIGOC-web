package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComprasConsumosTest {

    private ComprasFake compraPort;
    private ConsumosFake consumoPort;
    private EgresosFake egresoPort;
    private ProveedoresFake proveedorPort;
    private ObrasFake obraPort;
    private PartidasFake partidaPort;
    private MaterialesFake materialPort;

    private RegistrarCompra registrarCompra;
    private RegistrarConsumo registrarConsumo;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion residente = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        compraPort = new ComprasFake();
        consumoPort = new ConsumosFake();
        egresoPort = new EgresosFake();
        proveedorPort = new ProveedoresFake();
        obraPort = new ObrasFake();
        partidaPort = new PartidasFake();
        materialPort = new MaterialesFake();

        registrarCompra = new RegistrarCompra(compraPort, proveedorPort, obraPort, materialPort, egresoPort, clock);
        registrarConsumo = new RegistrarConsumo(consumoPort, materialPort, partidaPort, obraPort, clock);

        proveedorPort.datos.put(1, new Proveedor(1, "Proveedora SAC", "20123456789", null, null, true));
        obraPort.datos.put(1, new Obra(1, "Colegio", "MINEDU", "Lima",
                new BigDecimal("100000"), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 5, 31),
                "En Ejecución", true, null));
        partidaPort.datos.put(1, new Partida(1, 1, "Zapatas", "m3", new BigDecimal("100"), new BigDecimal("50"), true, null));
        materialPort.datos.put(1, new Material(1, "Cemento", "bolsa",
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("28.00"), true, null));
    }

    // ---------- RF-07 Compras ----------

    @Test
    void compraSoloAlmaceneroOAdminYGeneraStockYEgreso() {
        LocalDate hoy = LocalDate.of(2026, 10, 15);
        assertThrows(AccesoDenegadoException.class,
                () -> registrarCompra.ejecutar(residente, 1, 1, 1, "Factura", "F001-1",
                        hoy, new BigDecimal("50"), new BigDecimal("30")));
        assertThrows(ReglaNegocioException.class,
                () -> registrarCompra.ejecutar(almacenero, 1, 1, 1, "Guia", "F001-1",
                        hoy, new BigDecimal("50"), new BigDecimal("30")));

        CompraMaterial compra = registrarCompra.ejecutar(almacenero, 1, 1, 1, "Factura", "F001-1",
                hoy, new BigDecimal("50"), new BigDecimal("32.50"));
        assertEquals(new BigDecimal("1625.00"), compra.getTotal());
        assertEquals(new BigDecimal("60.00"), materialPort.datos.get(1).getStockActual());
        assertEquals(new BigDecimal("32.50"), materialPort.datos.get(1).getCostoUnitarioRef());
        assertEquals(1, egresoPort.datos.size());
        assertEquals("Material", egresoPort.datos.get(0).getTipoEgreso());
        assertEquals(compra.getIdCompra(), egresoPort.datos.get(0).getIdCompra());

        // Comprobante duplicado para el mismo proveedor
        assertThrows(ReglaNegocioException.class,
                () -> registrarCompra.ejecutar(admin, 1, 1, 1, "Factura", "f001-1",
                        hoy, new BigDecimal("5"), new BigDecimal("30")));
    }

    // ---------- RF-08 Consumos ----------

    @Test
    void consumoSoloResidenteValidaStockYDisparaAlerta() {
        LocalDate hoy = LocalDate.of(2026, 10, 15);
        materialPort.datos.get(1).setStockActual(new BigDecimal("30.00"));

        assertThrows(AccesoDenegadoException.class,
                () -> registrarConsumo.ejecutar(almacenero, 1, 1, hoy, new BigDecimal("5")));
        assertThrows(ReglaNegocioException.class,
                () -> registrarConsumo.ejecutar(residente, 1, 1, hoy, new BigDecimal("35")));

        // Consume 5 -> queda 25 (> 20 mínimo, sin alerta)
        var r1 = registrarConsumo.ejecutar(residente, 1, 1, hoy, new BigDecimal("5"));
        assertEquals(new BigDecimal("25.00"), r1.stockResultante());
        assertFalse(r1.alertaStockMinimo());

        // Consume 5 -> queda 20 (<= 20 mínimo, dispara alerta!)
        var r2 = registrarConsumo.ejecutar(residente, 1, 1, hoy, new BigDecimal("5"));
        assertEquals(new BigDecimal("20.00"), r2.stockResultante());
        assertTrue(r2.alertaStockMinimo());

        assertEquals(new BigDecimal("10.00"),
                new ObtenerConsumoAcumuladoPartida(consumoPort).ejecutar(admin, 1, 1));
    }

    @Test
    void consumoBloqueadoEnObraCerrada() {
        obraPort.datos.get(1).setEstado("Finalizada");
        assertThrows(ReglaNegocioException.class,
                () -> registrarConsumo.ejecutar(residente, 1, 1, LocalDate.of(2026, 10, 15), new BigDecimal("2")));
    }

    // ---------- Fakes ----------

    private static final class ComprasFake implements CompraPort {
        final List<CompraMaterial> compras = new ArrayList<>();
        int seq = 1;

        @Override public List<CompraMaterial> findAllActivas() { return compras; }
        @Override public boolean existsByProveedorYComprobante(Integer idProveedor, String numeroComprobante) {
            return compras.stream().anyMatch(c -> Objects.equals(c.getIdProveedor(), idProveedor)
                    && c.getNumeroComprobante().equalsIgnoreCase(numeroComprobante));
        }
        @Override public CompraMaterial guardarCompraConDetalle(CompraMaterial compra, DetalleCompra detalle) {
            compra.setIdCompra(seq++);
            compras.add(compra);
            return compra;
        }
    }

    private static final class ConsumosFake implements ConsumoPort {
        final List<ConsumoMaterial> consumos = new ArrayList<>();
        int seq = 1;

        @Override public List<ConsumoMaterial> findAllActivos() { return consumos; }
        @Override public List<ConsumoMaterial> findActivosByPartida(Integer idPartida) { return consumos; }
        @Override public BigDecimal sumaCantidadPorPartidaYMaterial(Integer idPartida, Integer idMaterial) {
            return consumos.stream()
                    .filter(c -> Objects.equals(c.getIdPartida(), idPartida) && Objects.equals(c.getIdMaterial(), idMaterial))
                    .map(ConsumoMaterial::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        @Override public ConsumoMaterial guardar(ConsumoMaterial consumo) {
            consumo.setIdConsumo(seq++);
            consumos.add(consumo);
            return consumo;
        }
    }

    private static final class EgresosFake implements EgresoPort {
        final List<Egreso> datos = new ArrayList<>();
        int seq = 1;

        @Override public List<Egreso> findAllActivos() { return datos; }
        @Override public List<Egreso> findActivosByObra(Integer idObra) { return datos; }
        @Override public Egreso guardar(Egreso egreso) {
            egreso.setIdEgreso(seq++);
            datos.add(egreso);
            return egreso;
        }
    }

    private static final class ProveedoresFake implements ProveedorPort {
        final Map<Integer, Proveedor> datos = new LinkedHashMap<>();
        @Override public List<Proveedor> findAllActivos() { return List.of(); }
        @Override public Optional<Proveedor> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public boolean existsByRuc(String ruc, Integer idExcluir) { return false; }
        @Override public Proveedor guardar(Proveedor p) { return p; }
        @Override public void desactivar(Integer id) {}
    }

    private static final class ObrasFake implements ObraPort {
        final Map<Integer, Obra> datos = new LinkedHashMap<>();
        @Override public List<Obra> findAllActivas() { return List.of(); }
        @Override public Optional<Obra> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Obra guardar(Obra o) { return o; }
        @Override public boolean tieneMovimientos(Integer id) { return false; }
        @Override public void desactivar(Integer id) {}
        @Override public void cerrar(Integer id) {}
    }

    private static final class PartidasFake implements PartidaPort {
        final Map<Integer, Partida> datos = new LinkedHashMap<>();
        @Override public List<Partida> findAllActivas() { return List.of(); }
        @Override public List<Partida> findActivasByObra(Integer id) { return List.of(); }
        @Override public Optional<Partida> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public BigDecimal sumaPesosActivos(Integer idObra, Integer idEx) { return BigDecimal.ZERO; }
        @Override public boolean tieneMovimientos(Integer id) { return false; }
        @Override public Partida guardar(Partida p) { return p; }
        @Override public void desactivar(Integer id) {}
    }

    private static final class MaterialesFake implements MaterialPort {
        final Map<Integer, Material> datos = new LinkedHashMap<>();
        @Override public List<Material> findAllActivos() { return List.of(); }
        @Override public List<Material> findAlertasStockMinimo() { return List.of(); }
        @Override public Optional<Material> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Material guardar(Material m) { return m; }
        @Override public void actualizarStock(Integer id, BigDecimal nuevoStock, BigDecimal nuevoCostoRef) {
            Material m = datos.get(id);
            m.setStockActual(nuevoStock);
            if (nuevoCostoRef != null) {
                m.setCostoUnitarioRef(nuevoCostoRef);
            }
        }
        @Override public void desactivar(Integer id) {}
    }
}
