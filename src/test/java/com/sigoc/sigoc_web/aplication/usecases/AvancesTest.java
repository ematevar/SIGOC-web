package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AvancesTest {

    private ObrasFake obraPort;
    private PartidasFake partidaPort;
    private AvancesFake avancePort;
    private ObtenerAvanceObra obtenerAvanceObra;
    private RegistrarAvance registrarAvance;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion residente = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        obraPort = new ObrasFake();
        partidaPort = new PartidasFake();
        avancePort = new AvancesFake();
        obtenerAvanceObra = new ObtenerAvanceObra(obraPort, partidaPort, avancePort);
        registrarAvance = new RegistrarAvance(avancePort, partidaPort, obraPort, obtenerAvanceObra, clock);

        obraPort.datos.put(1, new Obra(1, "Colegio", "MINEDU", "Lima",
                new BigDecimal("100000"), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 5, 31),
                "En Ejecución", true, null));
        partidaPort.datos.put(1, new Partida(1, 1, "Cimientos", "m3", new BigDecimal("100"), new BigDecimal("40.00"), true, null));
        partidaPort.datos.put(2, new Partida(2, 1, "Muros", "m2", new BigDecimal("200"), new BigDecimal("60.00"), true, null));
    }

    @Test
    void soloResidentePuedeRegistrarAvance() {
        LocalDate hoy = LocalDate.of(2026, 10, 15);
        assertThrows(AccesoDenegadoException.class,
                () -> registrarAvance.ejecutar(admin, 1, hoy, new BigDecimal("10"), "ok"));
        assertThrows(AccesoDenegadoException.class,
                () -> registrarAvance.ejecutar(almacenero, 1, hoy, new BigDecimal("10"), "ok"));
    }

    @Test
    void validaFechaYPorcentajeYObraOperable() {
        LocalDate hoy = LocalDate.of(2026, 10, 15);
        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, hoy.plusDays(1), new BigDecimal("10"), null));
        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, hoy, new BigDecimal("-1"), null));
        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, hoy, new BigDecimal("100.01"), null));

        obraPort.datos.get(1).setEstado("Finalizada");
        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, hoy, new BigDecimal("10"), null));
    }

    @Test
    void rechazaFechaOPorcentajeRegresivoYCalculaAvancePonderado() {
        var r1 = registrarAvance.ejecutar(residente, 1, LocalDate.of(2026, 10, 10), new BigDecimal("50"), "mitad");
        // 40% * 50% / 100 = 20.00%
        assertEquals(new BigDecimal("20.00"), r1.avanceObra());

        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, LocalDate.of(2026, 10, 9), new BigDecimal("60"), null));
        assertThrows(ReglaNegocioException.class,
                () -> registrarAvance.ejecutar(residente, 1, LocalDate.of(2026, 10, 12), new BigDecimal("49.99"), null));

        var r2 = registrarAvance.ejecutar(residente, 2, LocalDate.of(2026, 10, 12), new BigDecimal("25"), "inicio muros");
        // 40%*50% + 60%*25% = 20 + 15 = 35.00%
        assertEquals(new BigDecimal("35.00"), r2.avanceObra());
        assertEquals(new BigDecimal("35.00"), obtenerAvanceObra.ejecutar(admin, 1));
    }

    private static final class ObrasFake implements ObraPort {
        final Map<Integer, Obra> datos = new LinkedHashMap<>();

        @Override public List<Obra> findAllActivas() { return List.of(); }
        @Override public Optional<Obra> findById(Integer idObra) { return Optional.ofNullable(datos.get(idObra)); }
        @Override public Obra guardar(Obra obra) { return obra; }
        @Override public boolean tieneMovimientos(Integer idObra) { return false; }
        @Override public void desactivar(Integer idObra) {}
        @Override public void cerrar(Integer idObra) {}
    }

    private static final class PartidasFake implements PartidaPort {
        final Map<Integer, Partida> datos = new LinkedHashMap<>();

        @Override public List<Partida> findAllActivas() { return List.of(); }
        @Override public List<Partida> findActivasByObra(Integer idObra) {
            return datos.values().stream().filter(p -> p.isActivo() && Objects.equals(p.getIdObra(), idObra)).toList();
        }
        @Override public Optional<Partida> findById(Integer idPartida) { return Optional.ofNullable(datos.get(idPartida)); }
        @Override public BigDecimal sumaPesosActivos(Integer idObra, Integer idPartidaExcluir) { return BigDecimal.ZERO; }
        @Override public boolean tieneMovimientos(Integer idPartida) { return false; }
        @Override public Partida guardar(Partida partida) { return partida; }
        @Override public void desactivar(Integer idPartida) {}
    }

    private static final class AvancesFake implements AvancePartidaPort {
        final List<AvancePartida> datos = new ArrayList<>();
        int seq = 1;

        @Override
        public List<AvancePartida> findActivosByPartida(Integer idPartida) {
            return datos.stream().filter(a -> a.isActivo() && Objects.equals(a.getIdPartida(), idPartida)).toList();
        }

        @Override
        public List<AvancePartida> findActivosByObra(Integer idObra) {
            return datos;
        }

        @Override
        public List<AvancePartida> findAllActivos() {
            return datos;
        }

        @Override
        public Optional<AvancePartida> findUltimoActivoByPartida(Integer idPartida) {
            return datos.stream()
                    .filter(a -> a.isActivo() && Objects.equals(a.getIdPartida(), idPartida))
                    .max(Comparator.comparing(AvancePartida::getFechaReporte).thenComparing(AvancePartida::getIdAvance));
        }

        @Override
        public AvancePartida guardar(AvancePartida avance) {
            avance.setIdAvance(seq++);
            datos.add(avance);
            return avance;
        }
    }
}
