package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FinanzasPlanillaTest {

    private static final Clock FIJO = Clock.fixed(Instant.parse("2026-04-15T12:00:00Z"), ZoneOffset.UTC);
    private static final UsuarioSesion ADMIN = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private static final UsuarioSesion RESIDENTE = new UsuarioSesion(2, "res", "Res", RolSistema.RESIDENTE, false);

    @Test
    void ingresoExigeRangoPlanificadoDeObraYSoloAdmin() {
        ObraPort obraPort = obraActiva(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        IngresosFake ingresoPort = new IngresosFake();
        RegistrarIngreso uc = new RegistrarIngreso(ingresoPort, obraPort, FIJO);

        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(RESIDENTE, 1, LocalDate.of(2026, 4, 10), "Adelanto", new BigDecimal("5000")));

        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 3, 25), "Fuera de rango", new BigDecimal("5000")));

        Ingreso guardado = uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 10), "Valorización #1", new BigDecimal("12500.5"));
        assertEquals(new BigDecimal("12500.50"), guardado.getMonto());
        assertEquals(1, ingresoPort.datos.size());
    }

    @Test
    void egresoOtroSoloObraOperableYFechaNoFutura() {
        ObraPort obraPort = obraActiva(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        EgresosFake egresoPort = new EgresosFake();
        RegistrarEgreso uc = new RegistrarEgreso(egresoPort, obraPort, FIJO);

        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 20), "Futuro", new BigDecimal("100")));

        Egreso egreso = uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 12), "Alquiler mezcladora", new BigDecimal("850"));
        assertEquals("Otro", egreso.getTipoEgreso());
        assertEquals(new BigDecimal("850.00"), egreso.getMonto());
    }

    @Test
    void cerrarPlanillaCalculaSubtotalPorEmpleadoGeneraEgresosYBloqueaSolape() {
        ObraPort obraPort = obraActiva(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        CierresFake cierrePort = new CierresFake();
        EgresosFake egresoPort = new EgresosFake();

        EmpleadosFake empPort = new EmpleadosFake();
        Empleado e1 = new Empleado(10, "Juan Perez", "12345678", "Operario", new BigDecimal("120.00"), LocalDate.of(2026, 1, 1), true, null);
        Empleado e2 = new Empleado(11, "Maria Lopez", "87654321", "Peon", new BigDecimal("80.00"), LocalDate.of(2026, 1, 1), true, null);
        empPort.datos.put(10, e1);
        empPort.datos.put(11, e2);

        AsistenciasFake asistPort = new AsistenciasFake();
        // Juan: PRESENTE + TARDANZA = 2 dias -> 240.00; FALTA ignorada
        asistPort.datos.add(asistencia(1, 10, 1, LocalDate.of(2026, 4, 6), "PRESENTE"));
        asistPort.datos.add(asistencia(2, 10, 1, LocalDate.of(2026, 4, 7), "TARDANZA"));
        asistPort.datos.add(asistencia(3, 10, 1, LocalDate.of(2026, 4, 8), "FALTA"));
        // Maria: 1 dia -> 80.00
        asistPort.datos.add(asistencia(4, 11, 1, LocalDate.of(2026, 4, 6), "PRESENTE"));

        CerrarPlanilla uc = new CerrarPlanilla(cierrePort, obraPort, asistPort, empPort, egresoPort, FIJO);

        CierrePlanilla cierre = uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 12));
        assertEquals(new BigDecimal("320.00"), cierre.getMontoTotal());
        assertEquals(2, egresoPort.datos.size());
        assertEquals("Planilla", egresoPort.datos.get(0).getTipoEgreso());
        assertEquals(cierre.getIdCierre(), egresoPort.datos.get(0).getIdCierre());
        assertEquals(new BigDecimal("240.00"), egresoPort.datos.get(0).getMonto());
        assertEquals(new BigDecimal("80.00"), egresoPort.datos.get(1).getMonto());

        // Solapamiento prohibido
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 14)));
    }

    @Test
    void cerrarPlanillaSinAsistenciasValidasFalla() {
        ObraPort obraPort = obraActiva(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        CierresFake cierrePort = new CierresFake();
        EgresosFake egresoPort = new EgresosFake();
        EmpleadosFake empPort = new EmpleadosFake();
        AsistenciasFake asistPort = new AsistenciasFake();
        asistPort.datos.add(asistencia(1, 10, 1, LocalDate.of(2026, 4, 6), "FALTA"));

        CerrarPlanilla uc = new CerrarPlanilla(cierrePort, obraPort, asistPort, empPort, egresoPort, FIJO);
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(ADMIN, 1, LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 12)));
    }

    private static Asistencia asistencia(int id, int idEmp, int idObra, LocalDate fecha, String estado) {
        return new Asistencia(id, idEmp, idObra, fecha, LocalTime.of(8, 0), LocalTime.of(17, 0), estado, true, null);
    }

    private static ObraPort obraActiva(LocalDate inicio, LocalDate fin) {
        Obra obra = new Obra(1, "Obra Norte", "Lima", "Edificacion", new BigDecimal("50000"), inicio, fin, "En Ejecución", true, null);
        return new ObraPort() {
            @Override public List<Obra> findAllActivas() { return List.of(obra); }
            @Override public Optional<Obra> findById(Integer id) { return Objects.equals(id, 1) ? Optional.of(obra) : Optional.empty(); }
            @Override public Obra guardar(Obra o) { return o; }
            @Override public boolean tieneMovimientos(Integer idObra) { return false; }
            @Override public void cerrar(Integer idObra) {}
            @Override public void desactivar(Integer idObra) {}
        };
    }

    private static final class IngresosFake implements IngresoPort {
        final List<Ingreso> datos = new ArrayList<>();
        int seq = 1;
        @Override public List<Ingreso> findAllActivos() { return datos; }
        @Override public List<Ingreso> findActivosByObra(Integer idObra) { return datos; }
        @Override public Ingreso guardar(Ingreso i) { i.setIdIngreso(seq++); datos.add(i); return i; }
    }

    private static final class EgresosFake implements EgresoPort {
        final List<Egreso> datos = new ArrayList<>();
        int seq = 1;
        @Override public List<Egreso> findAllActivos() { return datos; }
        @Override public List<Egreso> findActivosByObra(Integer idObra) { return datos; }
        @Override public Egreso guardar(Egreso e) { e.setIdEgreso(seq++); datos.add(e); return e; }
    }

    private static final class CierresFake implements CierrePlanillaPort {
        final List<CierrePlanilla> datos = new ArrayList<>();
        int seq = 1;
        @Override public List<CierrePlanilla> findAllActivos() { return datos; }
        @Override public List<CierrePlanilla> findActivosByObra(Integer idObra) { return datos; }
        @Override public Optional<CierrePlanilla> findSolapado(Integer idObra, LocalDate desde, LocalDate hasta) {
            return datos.stream().filter(c -> Objects.equals(c.getIdObra(), idObra)
                    && !(c.getFechaFinPeriodo().isBefore(desde) || c.getFechaInicioPeriodo().isAfter(hasta)))
                    .findFirst();
        }
        @Override public CierrePlanilla guardar(CierrePlanilla c) { c.setIdCierre(seq++); datos.add(c); return c; }
    }

    private static final class EmpleadosFake implements EmpleadoPort {
        final Map<Integer, Empleado> datos = new LinkedHashMap<>();
        @Override public List<Empleado> findAllActivos() { return List.copyOf(datos.values()); }
        @Override public Optional<Empleado> findById(Integer id) { return Optional.ofNullable(datos.get(id)); }
        @Override public boolean existsByDni(String dni, Integer idExcluir) { return false; }
        @Override public Empleado guardar(Empleado e) { return e; }
        @Override public void desactivar(Integer id) {}
    }

    private static final class AsistenciasFake implements AsistenciaPort {
        final List<Asistencia> datos = new ArrayList<>();
        @Override public List<Asistencia> findAllActivas() { return datos; }
        @Override public List<Asistencia> findActivasByObra(Integer idObra) { return datos; }
        @Override public List<Asistencia> findActivasByObraYPeriodo(Integer idObra, LocalDate desde, LocalDate hasta) {
            return datos.stream().filter(a -> a.isActivo()
                    && Objects.equals(a.getIdObra(), idObra)
                    && !a.getFecha().isBefore(desde)
                    && !a.getFecha().isAfter(hasta)).toList();
        }
        @Override public Optional<Asistencia> findActivaByEmpleadoObraYFecha(Integer idE, Integer idO, LocalDate f) { return Optional.empty(); }
        @Override public boolean estaEnPeriodoCerrado(Integer idObra, LocalDate fecha) { return false; }
        @Override public Asistencia guardar(Asistencia a) { return a; }
    }
}
