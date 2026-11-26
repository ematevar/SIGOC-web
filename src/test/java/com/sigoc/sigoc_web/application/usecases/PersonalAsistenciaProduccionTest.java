package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.in.GestionarAsistenciaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PersonalAsistenciaProduccionTest {

    private EmpleadosFake empleadoPort;
    private AsistenciasFake asistenciaPort;
    private ProduccionFake produccionPort;
    private ObrasFake obraPort;
    private PartidasFake partidaPort;

    private GestionarEmpleado gestionarEmpleado;
    private GestionarAsistencia gestionarAsistencia;
    private GestionarProduccion gestionarProduccion;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion gerente = new UsuarioSesion(2, "gerente", "Gerente", RolSistema.GERENTE, false);
    private final UsuarioSesion residente = new UsuarioSesion(3, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(4, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        empleadoPort = new EmpleadosFake();
        asistenciaPort = new AsistenciasFake();
        produccionPort = new ProduccionFake();
        obraPort = new ObrasFake();
        partidaPort = new PartidasFake();

        gestionarEmpleado = new GestionarEmpleado(empleadoPort);
        gestionarAsistencia = new GestionarAsistencia(asistenciaPort, empleadoPort, obraPort, clock);
        gestionarProduccion = new GestionarProduccion(produccionPort, empleadoPort, partidaPort, obraPort, asistenciaPort, clock);

        obraPort.datos.put(1, new Obra(1, "Colegio", "MINEDU", "Lima",
                new BigDecimal("100000"), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 5, 31),
                "En Ejecución", true, null));
        partidaPort.datos.put(1, new Partida(1, 1, "Zapatas", "m3", new BigDecimal("100"), new BigDecimal("50"), true, null));
    }

    @Test
    void empleadoValidaDniYJornalYPermisos() {
        LocalDate ing = LocalDate.of(2026, 9, 1);
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarEmpleado.guardar(almacenero, null, "Carlos", "12345678", "Operario", new BigDecimal("100"), ing));
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarEmpleado.guardar(admin, null, "Carlos", "12345678", "Operario", new BigDecimal("100"), ing));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarEmpleado.guardar(gerente, null, "Carlos", "1234", "Operario", new BigDecimal("100"), ing));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarEmpleado.guardar(residente, null, "Carlos", "12345678", "Operario", BigDecimal.ZERO, ing));

        Empleado emp = gestionarEmpleado.guardar(residente, null, " Carlos Ruiz ", "12345678",
                "Operario", new BigDecimal("110.5"), ing);
        assertEquals("Carlos Ruiz", emp.getNombresApellidos());
        assertEquals(new BigDecimal("110.50"), emp.getJornalDiario());
        assertThrows(ReglaNegocioException.class,
                () -> gestionarEmpleado.guardar(gerente, null, "Otro", "12345678", "Peon", new BigDecimal("80"), ing));
    }

    @Test
    void asistenciaValidaHorasFaltaDuplicadoYPeriodoCerrado() {
        Empleado emp = gestionarEmpleado.guardar(residente, null, "Carlos", "12345678",
                "Operario", new BigDecimal("100"), LocalDate.of(2026, 9, 1));
        LocalDate hoy = LocalDate.of(2026, 10, 15);

        assertThrows(AccesoDenegadoException.class,
                () -> gestionarAsistencia.registrar(admin, emp.getIdEmpleado(), 1, hoy,
                        LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE"));

        assertThrows(ReglaNegocioException.class,
                () -> gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1, hoy,
                        LocalTime.of(8, 0), LocalTime.of(8, 0), "PRESENTE"));

        Asistencia falta = gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1,
                hoy.minusDays(1), LocalTime.of(8, 0), LocalTime.of(17, 0), "falta");
        assertEquals("FALTA", falta.getEstado());
        assertNull(falta.getHoraEntrada());
        assertNull(falta.getHoraSalida());

        assertThrows(ReglaNegocioException.class,
                () -> gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1,
                        hoy.minusDays(1), null, null, "PERMISO"));

        asistenciaPort.diasCerrados.add(hoy);
        assertThrows(ReglaNegocioException.class,
                () -> gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1,
                        hoy, LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE"));
    }

    @Test
    void asistenciaEnLoteRegistraMultiplesEmpleadosYAceptaFaltaSinHoras() {
        Empleado e1 = gestionarEmpleado.guardar(residente, null, "Carlos", "12345678",
                "Operario", new BigDecimal("100"), LocalDate.of(2026, 9, 1));
        Empleado e2 = gestionarEmpleado.guardar(residente, null, "Lucia", "87654321",
                "Oficial", new BigDecimal("90"), LocalDate.of(2026, 9, 1));
        LocalDate hoy = LocalDate.of(2026, 10, 15);

        List<Asistencia> guardadas = gestionarAsistencia.registrarLote(residente, 1, hoy, List.of(
                new GestionarAsistenciaUseCase.FilaAsistencia(e1.getIdEmpleado(), "PRESENTE",
                        LocalTime.of(8, 0), LocalTime.of(17, 0)),
                new GestionarAsistenciaUseCase.FilaAsistencia(e2.getIdEmpleado(), "FALTA", null, null)
        ));

        assertEquals(2, guardadas.size());
        assertEquals("PRESENTE", guardadas.get(0).getEstado());
        assertEquals("FALTA", guardadas.get(1).getEstado());
        assertNull(guardadas.get(1).getHoraEntrada());
    }

    @Test
    void produccionRequiereAsistenciaValidaEseDiaEnEsaObra() {
        Empleado emp = gestionarEmpleado.guardar(residente, null, "Carlos", "12345678",
                "Operario", new BigDecimal("100"), LocalDate.of(2026, 9, 1));
        LocalDate diaFalta = LocalDate.of(2026, 10, 13);
        LocalDate diaPresente = LocalDate.of(2026, 10, 14);
        LocalDate diaSinRegistro = LocalDate.of(2026, 10, 15);

        gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1, diaFalta, null, null, "FALTA");
        gestionarAsistencia.registrar(residente, emp.getIdEmpleado(), 1, diaPresente,
                LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE");

        assertThrows(AccesoDenegadoException.class,
                () -> gestionarProduccion.registrar(admin, emp.getIdEmpleado(), 1, diaPresente, new BigDecimal("10")));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarProduccion.registrar(residente, emp.getIdEmpleado(), 1, diaSinRegistro, new BigDecimal("10")));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarProduccion.registrar(residente, emp.getIdEmpleado(), 1, diaFalta, new BigDecimal("10")));

        ProduccionEmpleado prod = gestionarProduccion.registrar(
                residente, emp.getIdEmpleado(), 1, diaPresente, new BigDecimal("12.5"));
        assertEquals(new BigDecimal("12.50"), prod.getCantidadProducida());
    }

    // ---------- Fakes ----------

    private static final class EmpleadosFake implements EmpleadoPort {
        final Map<Integer, Empleado> datos = new LinkedHashMap<>();
        int seq = 1;

        @Override public List<Empleado> findAllActivos() { return datos.values().stream().filter(Empleado::isActivo).toList(); }
        @Override public Optional<Empleado> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public boolean existsByDni(String dni, Integer idEx) {
            return datos.values().stream().anyMatch(e -> e.getDni().equals(dni) && (idEx == null || !Objects.equals(e.getIdEmpleado(), idEx)));
        }
        @Override public Empleado guardar(Empleado e) {
            if (e.getIdEmpleado() == null) e.setIdEmpleado(seq++);
            datos.put(e.getIdEmpleado(), e);
            return e;
        }
        @Override public void desactivar(Integer id) { datos.get(id).setActivo(false); }
    }

    private static final class AsistenciasFake implements AsistenciaPort {
        final List<Asistencia> datos = new ArrayList<>();
        final Set<LocalDate> diasCerrados = new HashSet<>();
        int seq = 1;

        @Override public List<Asistencia> findAllActivas() { return datos; }
        @Override public List<Asistencia> findActivasByObra(Integer idObra) { return datos; }
        @Override public List<Asistencia> findActivasByObraYPeriodo(Integer idObra, LocalDate desde, LocalDate hasta) {
            return datos.stream().filter(a -> a.isActivo()
                    && Objects.equals(a.getIdObra(), idObra)
                    && !a.getFecha().isBefore(desde)
                    && !a.getFecha().isAfter(hasta)).toList();
        }
        @Override public Optional<Asistencia> findActivaByEmpleadoObraYFecha(Integer idEmp, Integer idObra, LocalDate f) {
            return datos.stream().filter(a -> a.isActivo()
                    && Objects.equals(a.getIdEmpleado(), idEmp)
                    && Objects.equals(a.getIdObra(), idObra)
                    && Objects.equals(a.getFecha(), f)).findFirst();
        }
        @Override public boolean estaEnPeriodoCerrado(Integer idObra, LocalDate fecha) {
            return diasCerrados.contains(fecha);
        }
        @Override public Asistencia guardar(Asistencia a) {
            a.setIdAsistencia(seq++);
            datos.add(a);
            return a;
        }
    }

    private static final class ProduccionFake implements ProduccionPort {
        final List<ProduccionEmpleado> datos = new ArrayList<>();
        int seq = 1;

        @Override public List<ProduccionEmpleado> findAllActivas() { return datos; }
        @Override public List<ProduccionEmpleado> findActivasByPartida(Integer idPartida) { return datos; }
        @Override public ProduccionEmpleado guardar(ProduccionEmpleado p) {
            p.setIdProduccion(seq++);
            datos.add(p);
            return p;
        }
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
}
