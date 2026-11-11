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

    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion residente = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        obraPort = new ObrasEnMemoria();
        partidaPort = new PartidasEnMemoria();
        pmPort = new PartidaMaterialEnMemoria();
    }

    // ---------- RF-03 Obras ----------

    @Test
    void guardarObraSoloAdminYValidaReglas() {
        GuardarObra uc = new GuardarObra(obraPort);
        LocalDate ini = LocalDate.of(2026, 9, 1);
        LocalDate fin = LocalDate.of(2027, 1, 30);

        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(residente, null, "Obra 1", "Cliente", null, new BigDecimal("1000"), ini, fin, null));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(admin, null, "Obra 1", "Cliente", null, BigDecimal.ZERO, ini, fin, null));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(admin, null, "Obra 1", "Cliente", null, new BigDecimal("1000"), fin, ini, null));

        Obra creada = uc.ejecutar(admin, null, " Colegio ", " MINEDU ", "Lima",
                new BigDecimal("150000.555"), ini, fin, null);
        assertEquals("Colegio", creada.getNombre());
        assertEquals(new BigDecimal("150000.56"), creada.getPresupuestoTotal());
        assertEquals("Planificada", creada.getEstado());
    }

    @Test
    void eliminarObraBloqueaSiTieneMovimientosYPuedeCerrarse() {
        Obra obra = new GuardarObra(obraPort).ejecutar(admin, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución");
        obraPort.conMovimientos.add(obra.getIdObra());

        EliminarObra eliminar = new EliminarObra(obraPort);
        var ex = assertThrows(ReglaNegocioException.class, () -> eliminar.ejecutar(admin, obra.getIdObra()));
        assertTrue(ex.getMessage().contains("RF-03"));

        new CerrarObra(obraPort).ejecutar(admin, obra.getIdObra());
        Obra cerrada = obraPort.findById(obra.getIdObra()).orElseThrow();
        assertEquals("Finalizada", cerrada.getEstado());
        assertFalse(cerrada.isOperable());

        // C09: obra finalizada no puede editarse ni reabrirse
        GuardarObra guardar = new GuardarObra(obraPort);
        assertThrows(ReglaNegocioException.class, () -> guardar.ejecutar(admin, obra.getIdObra(),
                "Obra", "Cli", null, new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución"));
    }

    @Test
    void eliminarObraSinMovimientosDesactivaLogicamente() {
        Obra obra = new GuardarObra(obraPort).ejecutar(admin, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        new EliminarObra(obraPort).ejecutar(admin, obra.getIdObra());
        assertFalse(obraPort.findById(obra.getIdObra()).orElseThrow().isActivo());
    }

    // ---------- RF-04 Partidas ----------

    @Test
    void guardarPartidaValidaObraActivaYPesoMaximo100() {
        Obra obra = new GuardarObra(obraPort).ejecutar(admin, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), "En Ejecución");
        GuardarPartida uc = new GuardarPartida(partidaPort, obraPort);

        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(almacenero, null, obra.getIdObra(), "P1", "m3", new BigDecimal("10"), new BigDecimal("50")));

        Partida p1 = uc.ejecutar(residente, null, obra.getIdObra(), "Zapatas", "m3",
                new BigDecimal("100"), new BigDecimal("60"));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(residente, null, obra.getIdObra(), "Muros", "m2",
                        new BigDecimal("50"), new BigDecimal("40.01")));

        // Editar p1 reduciendo a 55 permite luego 45
        uc.ejecutar(admin, p1.getIdPartida(), obra.getIdObra(), "Zapatas", "m3",
                new BigDecimal("100"), new BigDecimal("55"));
        Partida p2 = uc.ejecutar(residente, null, obra.getIdObra(), "Muros", "m2",
                new BigDecimal("50"), new BigDecimal("45"));
        assertEquals(new BigDecimal("45.00"), p2.getPesoPresupuestal());

        // Obra cerrada no acepta partidas
        new CerrarObra(obraPort).ejecutar(admin, obra.getIdObra());
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(residente, null, obra.getIdObra(), "Otra", "und",
                        new BigDecimal("1"), new BigDecimal("1")));
    }

    @Test
    void eliminarPartidaBloqueaSiTieneMovimientos() {
        Obra obra = new GuardarObra(obraPort).ejecutar(admin, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        Partida p = new GuardarPartida(partidaPort, obraPort).ejecutar(residente, null, obra.getIdObra(),
                "Zapatas", "m3", new BigDecimal("10"), new BigDecimal("20"));
        partidaPort.conMovimientos.add(p.getIdPartida());

        EliminarPartida eliminar = new EliminarPartida(partidaPort);
        assertThrows(ReglaNegocioException.class, () -> eliminar.ejecutar(residente, p.getIdPartida()));

        // C15: partida con movimientos no puede cambiar unidad de medida
        GuardarPartida guardar = new GuardarPartida(partidaPort, obraPort);
        assertThrows(ReglaNegocioException.class, () -> guardar.ejecutar(admin, p.getIdPartida(),
                obra.getIdObra(), "Zapatas", "m2", new BigDecimal("10"), new BigDecimal("20")));
    }

    // ---------- RF-16b PartidaMaterial ----------

    @Test
    void asignarYReactivarMaterialPresupuestado() {
        Obra obra = new GuardarObra(obraPort).ejecutar(admin, null, "Obra", "Cli", null,
                new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), null);
        Partida p = new GuardarPartida(partidaPort, obraPort).ejecutar(residente, null, obra.getIdObra(),
                "Zapatas", "m3", new BigDecimal("10"), new BigDecimal("20"));
        pmPort.materialesActivos.add(10);

        AsignarMaterialPartida asignar = new AsignarMaterialPartida(partidaPort, obraPort, pmPort);
        assertThrows(AccesoDenegadoException.class,
                () -> asignar.ejecutar(almacenero, p.getIdPartida(), 10, new BigDecimal("50")));
        assertThrows(ReglaNegocioException.class,
                () -> asignar.ejecutar(residente, p.getIdPartida(), 10, BigDecimal.ZERO));

        PartidaMaterial pm = asignar.ejecutar(residente, p.getIdPartida(), 10, new BigDecimal("180"));
        assertEquals(new BigDecimal("180.00"), pm.getCantidadPresupuestada());

        new EliminarMaterialPartida(pmPort).ejecutar(admin, pm.getIdPartidaMaterial());
        assertTrue(pmPort.findActivosByPartida(p.getIdPartida()).isEmpty());

        PartidaMaterial reactivado = asignar.ejecutar(residente, p.getIdPartida(), 10, new BigDecimal("220"));
        assertEquals(pm.getIdPartidaMaterial(), reactivado.getIdPartidaMaterial());
        assertEquals(new BigDecimal("220.00"), reactivado.getCantidadPresupuestada());
    }

    // ---------- Fakes ----------

    private static final class ObrasEnMemoria implements ObraPort {
        final Map<Integer, Obra> datos = new LinkedHashMap<>();
        final Set<Integer> conMovimientos = new HashSet<>();
        int seq = 1;

        @Override
        public List<Obra> findAllActivas() {
            return datos.values().stream().filter(Obra::isActivo).toList();
        }

        @Override
        public Optional<Obra> findById(Integer idObra) {
            return Optional.ofNullable(datos.get(idObra));
        }

        @Override
        public Obra guardar(Obra obra) {
            if (obra.getIdObra() == null) {
                obra.setIdObra(seq++);
            }
            datos.put(obra.getIdObra(), obra);
            return obra;
        }

        @Override
        public boolean tieneMovimientos(Integer idObra) {
            return conMovimientos.contains(idObra);
        }

        @Override
        public void desactivar(Integer idObra) {
            datos.get(idObra).setActivo(false);
        }

        @Override
        public void cerrar(Integer idObra) {
            datos.get(idObra).setEstado("Finalizada");
        }
    }

    private static final class PartidasEnMemoria implements PartidaPort {
        final Map<Integer, Partida> datos = new LinkedHashMap<>();
        final Set<Integer> conMovimientos = new HashSet<>();
        int seq = 1;

        @Override
        public List<Partida> findAllActivas() {
            return datos.values().stream().filter(Partida::isActivo).toList();
        }

        @Override
        public List<Partida> findActivasByObra(Integer idObra) {
            return datos.values().stream().filter(p -> p.isActivo() && Objects.equals(p.getIdObra(), idObra)).toList();
        }

        @Override
        public Optional<Partida> findById(Integer idPartida) {
            return Optional.ofNullable(datos.get(idPartida));
        }

        @Override
        public BigDecimal sumaPesosActivos(Integer idObra, Integer idPartidaExcluir) {
            return datos.values().stream()
                    .filter(p -> p.isActivo() && Objects.equals(p.getIdObra(), idObra))
                    .filter(p -> idPartidaExcluir == null || !Objects.equals(p.getIdPartida(), idPartidaExcluir))
                    .map(Partida::getPesoPresupuestal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public boolean tieneMovimientos(Integer idPartida) {
            return conMovimientos.contains(idPartida);
        }

        @Override
        public Partida guardar(Partida partida) {
            if (partida.getIdPartida() == null) {
                partida.setIdPartida(seq++);
            }
            datos.put(partida.getIdPartida(), partida);
            return partida;
        }

        @Override
        public void desactivar(Integer idPartida) {
            datos.get(idPartida).setActivo(false);
        }
    }

    private static final class PartidaMaterialEnMemoria implements PartidaMaterialPort {
        final Map<Integer, PartidaMaterial> datos = new LinkedHashMap<>();
        final Set<Integer> materialesActivos = new HashSet<>();
        int seq = 1;

        @Override
        public List<PartidaMaterial> findActivosByPartida(Integer idPartida) {
            return datos.values().stream()
                    .filter(pm -> pm.isActivo() && Objects.equals(pm.getIdPartida(), idPartida))
                    .toList();
        }

        @Override
        public Optional<PartidaMaterial> findById(Integer idPartidaMaterial) {
            return Optional.ofNullable(datos.get(idPartidaMaterial));
        }

        @Override
        public boolean existeMaterialActivo(Integer idMaterial) {
            return materialesActivos.contains(idMaterial);
        }

        @Override
        public PartidaMaterial guardarOActualizar(Integer idPartida, Integer idMaterial, BigDecimal cantidad) {
            PartidaMaterial pm = datos.values().stream()
                    .filter(x -> Objects.equals(x.getIdPartida(), idPartida) && Objects.equals(x.getIdMaterial(), idMaterial))
                    .findFirst()
                    .orElseGet(() -> new PartidaMaterial(seq++, idPartida, idMaterial, cantidad, true, null));
            pm.setCantidadPresupuestada(cantidad);
            pm.setActivo(true);
            datos.put(pm.getIdPartidaMaterial(), pm);
            return pm;
        }

        @Override
        public void desactivar(Integer idPartidaMaterial) {
            datos.get(idPartidaMaterial).setActivo(false);
        }
    }
}
