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

class PersonalAsistenciaProduccionTest {

    private EmpleadosFake empleadoPort;
    private AsistenciasFake asistenciaPort;
    private ProduccionFake produccionPort;
    private ObrasFake obraPort;
    private PartidasFake partidaPort;

    private GuardarEmpleado guardarEmpleado;
    private RegistrarAsistencia registrarAsistencia;
    private RegistrarProduccion registrarProduccion;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion residente = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        empleadoPort = new EmpleadosFake();
        asistenciaPort = new AsistenciasFake();
        produccionPort = new ProduccionFake();
        obraPort = new ObrasFake();
        partidaPort = new PartidasFake();

        guardarEmpleado = new GuardarEmpleado(empleadoPort);
        registrarAsistencia = new RegistrarAsistencia(asistenciaPort, empleadoPort, obraPort, clock);
        registrarProduccion = new RegistrarProduccion(produccionPort, empleadoPort, partidaPort, obraPort, asistenciaPort, clock);

        obraPort.datos.put(1, new Obra(1, "Colegio", "MINEDU", "Lima",
                new BigDecimal("100000"), LocalDate.of(2026, 9, 1), LocalDate.of(2027, 5, 31),
                "En Ejecución", true, null));
        partidaPort.datos.put(1, new Partida(1, 1, "Zapatas", "m3", new BigDecimal("100"), new BigDecimal("50"), true, null));
    }

    // ---------- RF-10 Empleados ----------

    @Test
    void empleadoValidaDniYJornalYPermisos() {
        LocalDate ing = LocalDate.of(2026, 9, 1);
        assertThrows(AccesoDenegadoException.class,
                () -> guardarEmpleado.ejecutar(almacenero, null, "Carlos", "12345678", "Operario", new BigDecimal("100"), ing));
        assertThrows(ReglaNegocioException.class,
                () -> guardarEmpleado.ejecutar(admin, null, "Carlos", "1234", "Operario", new BigDecimal("100"), ing));
        assertThrows(ReglaNegocioException.class,
                () -> guardarEmpleado.ejecutar(residente, null, "Carlos", "12345678", "Operario", BigDecimal.ZERO, ing));

        Empleado emp = guardarEmpleado.ejecutar(residente, null, " Carlos Ruiz ", "12345678",
                "Operario", new BigDecimal("110.5"), ing);
        assertEquals("Carlos Ruiz", emp.getNombresApellidos());
        assertEquals(new BigDecimal("110.50"), emp.getJornalDiario());
        assertThrows(ReglaNegocioException.class,
                () -> guardarEmpleado.ejecutar(admin, null, "Otro", "12345678", "Peon", new BigDecimal("80"), ing));
    }

    // ---------- RF-11 Asistencia ----------

    @Test
    void asistenciaValidaHorasFaltaDuplicadoYPeriodoCerrado() {
        Empleado emp = guardarEmpleado.ejecutar(residente, null, "Carlos", "12345678",
                "Operario", new BigDecimal("100"), LocalDate.of(2026, 9, 1));
        LocalDate hoy = LocalDate.of(2026, 10, 15);

        assertThrows(AccesoDenegadoException.class,
                () -> registrarAsistencia.ejecutar(admin, emp.getIdEmpleado(), 1, hoy,
                        LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE"));

        // Salida <= entrada rechazada
        assertThrows(ReglaNegocioException.class,
                () -> registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1, hoy,
                        LocalTime.of(8, 0), LocalTime.of(8, 0), "PRESENTE"));

        // FALTA limpia horas aunque se envíen
        Asistencia falta = registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1,
                hoy.minusDays(1), LocalTime.of(8, 0), LocalTime.of(17, 0), "falta");
        assertEquals("FALTA", falta.getEstado());
        assertNull(falta.getHoraEntrada());
        assertNull(falta.getHoraSalida());

        // Duplicado en misma obra y fecha
        assertThrows(ReglaNegocioException.class,
                () -> registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1,
                        hoy.minusDays(1), null, null, "PERMISO"));

        // Periodo cerrado inmutable
        asistenciaPort.diasCerrados.add(hoy);
        assertThrows(ReglaNegocioException.class,
                () -> registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1,
                        hoy, LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE"));
    }

    // ---------- RF-12 Producción ----------

    @Test
    void produccionRequiereAsistenciaValidaEseDiaEnEsaObra() {
        Empleado emp = guardarEmpleado.ejecutar(residente, null, "Carlos", "12345678",
                "Operario", new BigDecimal("100"), LocalDate.of(2026, 9, 1));
        LocalDate diaFalta = LocalDate.of(2026, 10, 13);
        LocalDate diaPresente = LocalDate.of(2026, 10, 14);
        LocalDate diaSinRegistro = LocalDate.of(2026, 10, 15);

        registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1, diaFalta, null, null, "FALTA");
        registrarAsistencia.ejecutar(residente, emp.getIdEmpleado(), 1, diaPresente,
                LocalTime.of(8, 0), LocalTime.of(17, 0), "PRESENTE");

        assertThrows(AccesoDenegadoException.class,
                () -> registrarProduccion.ejecutar(admin, emp.getIdEmpleado(), 1, diaPresente, new BigDecimal("10")));
        assertThrows(ReglaNegocioException.class,
                () -> registrarProduccion.ejecutar(residente, emp.getIdEmpleado(), 1, diaSinRegistro, new BigDecimal("10")));
        assertThrows(ReglaNegocioException.class,
                () -> registrarProduccion.ejecutar(residente, emp.getIdEmpleado(), 1, diaFalta, new BigDecimal("10")));

        ProduccionEmpleado prod = registrarProduccion.ejecutar(
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
