package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ObrasPartidasTest {

    private ObrasEnMemoria obraPort;
    private PartidasEnMemoria partidaPort;
    private PartidaMaterialEnMemoria pmPort;
    private GestionarObra gestionarObra;
    private GestionarPartida gestionarPartida;

    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion gerente = new UsuarioSesion(2, "gerente", "Gerente", RolSistema.GERENTE, false);
    private final UsuarioSesion residente = new UsuarioSesion(3, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(4, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        obraPort = new ObrasEnMemoria();
        partidaPort = new PartidasEnMemoria();
        pmPort = new PartidaMaterialEnMemoria();
        gestionarObra = new GestionarObra(obraPort, null);
        gestionarPartida = new GestionarPartida(partidaPort, obraPort, pmPort);
    }

    // ---------- CU-03 Obras ----------

    @Test
    void guardarObraSoloGerenteYValidaReglas() {
        LocalDate ini = LocalDate.of(2026, 9, 1);
        LocalDate fin = LocalDate.of(2027, 1, 30);

        assertThrows(AccesoDenegadoException.class,
                () -> gestionarObra.guardar(admin, null, null, "Obra 1", "Cliente", null, new BigDecimal("1000"), ini, fin, null));
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarObra.guardar(residente, null, null, "Obra 1", "Cliente", null, new BigDecimal("1000"), ini, fin, null));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarObra.guardar(gerente, null, null, "Obra 1", "Cliente", null, BigDecimal.ZERO, ini, fin, null));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarObra.guardar(gerente, null, null, "Obra 1", "Cliente", null, new BigDecimal("1000"), fin, ini, null));

        Obra creada = gestionarObra.guardar(gerente, null, null, " Colegio ", " MINEDU ", "Lima",
                new BigDecimal("150000.555"), ini, fin, null);
        assertEquals("Colegio", creada.getNombre());
        assertEquals(new BigDecimal("150000.56"), creada.getPresupuestoTotal());
        assertEquals("Planificada", creada.getEstado());
    }

    @Test
    void eliminarObraBloqueaSiTieneMovimientosYPuedeCerrarse() {
        Obra obra = gestionarObra.guardar(gerente, null, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución");
        obraPort.conMovimientos.add(obra.getIdObra());

        var ex = assertThrows(ReglaNegocioException.class, () -> gestionarObra.eliminar(gerente, obra.getIdObra()));
        assertTrue(ex.getMessage().contains("RF-03"));

        gestionarObra.cerrar(gerente, obra.getIdObra());
        Obra cerrada = obraPort.findById(obra.getIdObra()).orElseThrow();
        assertEquals("Finalizada", cerrada.getEstado());
        assertFalse(cerrada.isOperable());

        assertThrows(ReglaNegocioException.class, () -> gestionarObra.guardar(gerente, obra.getIdObra(), null,
                "Obra", "Cli", null, new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución"));
    }

    @Test
    void eliminarObraSinMovimientosDesactivaLogicamente() {
        Obra obra = gestionarObra.guardar(gerente, null, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        gestionarObra.eliminar(gerente, obra.getIdObra());
        assertFalse(obraPort.findById(obra.getIdObra()).orElseThrow().isActivo());
    }

    // ---------- CU-04 Partidas ----------

    @Test
    void guardarPartidaValidaObraActivaYPesoMaximo100() {
        Obra obra = gestionarObra.guardar(gerente, null, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución");

        assertThrows(AccesoDenegadoException.class,
                () -> gestionarPartida.guardar(almacenero, null, obra.getIdObra(), "P1", "m3", new BigDecimal("10"), new BigDecimal("50")));
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarPartida.guardar(admin, null, obra.getIdObra(), "P1", "m3", new BigDecimal("10"), new BigDecimal("50")));

        Partida p1 = gestionarPartida.guardar(residente, null, obra.getIdObra(), "Zapatas", "m3",
                new BigDecimal("100"), new BigDecimal("60"));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarPartida.guardar(residente, null, obra.getIdObra(), "Muros", "m2",
                        new BigDecimal("50"), new BigDecimal("40.01")));

        gestionarPartida.guardar(gerente, p1.getIdPartida(), obra.getIdObra(), "Zapatas", "m3",
                new BigDecimal("100"), new BigDecimal("55"));
        Partida p2 = gestionarPartida.guardar(residente, null, obra.getIdObra(), "Muros", "m2",
                new BigDecimal("50"), new BigDecimal("45"));
        assertEquals(new BigDecimal("45.00"), p2.getPesoPresupuestal());

        gestionarObra.cerrar(gerente, obra.getIdObra());
        assertThrows(ReglaNegocioException.class,
                () -> gestionarPartida.guardar(residente, null, obra.getIdObra(), "Otra", "und",
                        new BigDecimal("1"), new BigDecimal("1")));
    }

    @Test
    void eliminarPartidaBloqueaSiTieneMovimientos() {
        Obra obra = gestionarObra.guardar(gerente, null, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        Partida p = gestionarPartida.guardar(residente, null, obra.getIdObra(),
                "Zapatas", "m3", new BigDecimal("10"), new BigDecimal("20"));
        partidaPort.conMovimientos.add(p.getIdPartida());

        assertThrows(ReglaNegocioException.class, () -> gestionarPartida.eliminar(residente, p.getIdPartida()));
        assertThrows(ReglaNegocioException.class, () -> gestionarPartida.guardar(gerente, p.getIdPartida(),
                obra.getIdObra(), "Zapatas", "m2", new BigDecimal("10"), new BigDecimal("20")));
    }

    @Test
    void asignarYReactivarMaterialPresupuestado() {
        Obra obra = gestionarObra.guardar(gerente, null, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        Partida p = gestionarPartida.guardar(residente, null, obra.getIdObra(),
                "Zapatas", "m3", new BigDecimal("10"), new BigDecimal("20"));
        pmPort.materialesActivos.add(10);

        assertThrows(AccesoDenegadoException.class,
                () -> gestionarPartida.asignarMaterial(almacenero, p.getIdPartida(), 10, new BigDecimal("50")));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarPartida.asignarMaterial(residente, p.getIdPartida(), 10, BigDecimal.ZERO));

        PartidaMaterial pm = gestionarPartida.asignarMaterial(residente, p.getIdPartida(), 10, new BigDecimal("180"));
        assertEquals(new BigDecimal("180.00"), pm.getCantidadPresupuestada());

        gestionarPartida.eliminarMaterial(gerente, pm.getIdPartidaMaterial());
        assertTrue(pmPort.findActivosByPartida(p.getIdPartida()).isEmpty());

        PartidaMaterial reactivado = gestionarPartida.asignarMaterial(residente, p.getIdPartida(), 10, new BigDecimal("220"));
        assertEquals(pm.getIdPartidaMaterial(), reactivado.getIdPartidaMaterial());
        assertEquals(new BigDecimal("220.00"), reactivado.getCantidadPresupuestada());
    }

    // ---------- Fakes ----------

    private static final class ObrasEnMemoria implements ObraPort {
        final Map<Integer, Obra> datos = new LinkedHashMap<>();
        final Set<Integer> conMovimientos = new HashSet<>();
        int seq = 1;

        @Override public List<Obra> findAllActivas() { return datos.values().stream().filter(Obra::isActivo).toList(); }
        @Override public Optional<Obra> findById(Integer idObra) { return Optional.ofNullable(datos.get(idObra)); }
        @Override public Obra guardar(Obra obra) {
            if (obra.getIdObra() == null) obra.setIdObra(seq++);
            datos.put(obra.getIdObra(), obra);
            return obra;
        }
        @Override public boolean tieneMovimientos(Integer idObra) { return conMovimientos.contains(idObra); }
        @Override public void desactivar(Integer idObra) { datos.get(idObra).setActivo(false); }
        @Override public void cerrar(Integer idObra) { datos.get(idObra).setEstado("Finalizada"); }
    }

    private static final class PartidasEnMemoria implements PartidaPort {
        final Map<Integer, Partida> datos = new LinkedHashMap<>();
        final Set<Integer> conMovimientos = new HashSet<>();
        int seq = 1;

        @Override public List<Partida> findAllActivas() { return datos.values().stream().filter(Partida::isActivo).toList(); }
        @Override public List<Partida> findActivasByObra(Integer idObra) {
            return datos.values().stream().filter(p -> p.isActivo() && Objects.equals(p.getIdObra(), idObra)).toList();
        }
        @Override public Optional<Partida> findById(Integer idPartida) { return Optional.ofNullable(datos.get(idPartida)); }
        @Override public BigDecimal sumaPesosActivos(Integer idObra, Integer idPartidaExcluir) {
            return datos.values().stream()
                    .filter(p -> p.isActivo() && Objects.equals(p.getIdObra(), idObra))
                    .filter(p -> idPartidaExcluir == null || !Objects.equals(p.getIdPartida(), idPartidaExcluir))
                    .map(Partida::getPesoPresupuestal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        @Override public boolean tieneMovimientos(Integer idPartida) { return conMovimientos.contains(idPartida); }
        @Override public Partida guardar(Partida partida) {
            if (partida.getIdPartida() == null) partida.setIdPartida(seq++);
            datos.put(partida.getIdPartida(), partida);
            return partida;
        }
        @Override public void desactivar(Integer idPartida) { datos.get(idPartida).setActivo(false); }
    }

    private static final class PartidaMaterialEnMemoria implements PartidaMaterialPort {
        final Map<Integer, PartidaMaterial> datos = new LinkedHashMap<>();
        final Set<Integer> materialesActivos = new HashSet<>();
        int seq = 1;

        @Override public List<PartidaMaterial> findActivosByPartida(Integer idPartida) {
            return datos.values().stream()
                    .filter(pm -> pm.isActivo() && Objects.equals(pm.getIdPartida(), idPartida))
                    .toList();
        }
        @Override public Optional<PartidaMaterial> findById(Integer idPartidaMaterial) {
            return Optional.ofNullable(datos.get(idPartidaMaterial));
        }
        @Override public boolean existeMaterialActivo(Integer idMaterial) { return materialesActivos.contains(idMaterial); }
        @Override public PartidaMaterial guardarOActualizar(Integer idPartida, Integer idMaterial, BigDecimal cantidad) {
            PartidaMaterial pm = datos.values().stream()
                    .filter(x -> Objects.equals(x.getIdPartida(), idPartida) && Objects.equals(x.getIdMaterial(), idMaterial))
                    .findFirst()
                    .orElseGet(() -> new PartidaMaterial(seq++, idPartida, idMaterial, cantidad, true, null));
            pm.setCantidadPresupuestada(cantidad);
            pm.setActivo(true);
            datos.put(pm.getIdPartidaMaterial(), pm);
            return pm;
        }
        @Override public void desactivar(Integer idPartidaMaterial) { datos.get(idPartidaMaterial).setActivo(false); }
    }
}
