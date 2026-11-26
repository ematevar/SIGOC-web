package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarPlanillaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.CierrePlanillaPort;
import com.sigoc.sigoc_web.domain.ports.out.EgresoPort;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CU-14: Previsualización, cierre inmutable y consulta de planillas laborales (CONTADOR).
 */
public class GestionarPlanilla implements GestionarPlanillaUseCase {

    private final CierrePlanillaPort cierrePort;
    private final ObraPort obraPort;
    private final AsistenciaPort asistenciaPort;
    private final EmpleadoPort empleadoPort;
    private final EgresoPort egresoPort;
    private final Clock clock;
    private final AuditoriaEventoPort auditoriaPort;

    public GestionarPlanilla(CierrePlanillaPort cierrePort, ObraPort obraPort,
                             AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                             EgresoPort egresoPort, Clock clock) {
        this(cierrePort, obraPort, asistenciaPort, empleadoPort, egresoPort, clock, null);
    }

    public GestionarPlanilla(CierrePlanillaPort cierrePort, ObraPort obraPort,
                             AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                             EgresoPort egresoPort, Clock clock, AuditoriaEventoPort auditoriaPort) {
        this.cierrePort = cierrePort;
        this.obraPort = obraPort;
        this.asistenciaPort = asistenciaPort;
        this.empleadoPort = empleadoPort;
        this.egresoPort = egresoPort;
        this.clock = clock;
        this.auditoriaPort = auditoriaPort;
    }

    private record LineaPlanilla(Empleado empleado, long dias, BigDecimal subtotal) {
    }

    private record CalculoPlanilla(Obra obra, List<LineaPlanilla> lineas, BigDecimal total) {
    }

    @Override
    public List<CierrePlanilla> listarCierres(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PLANILLAS);
        return idObra != null ? cierrePort.findActivosByObra(idObra) : cierrePort.findAllActivos();
    }

    @Override
    public ResumenPlanilla previsualizar(UsuarioSesion actor, Integer idObra, LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PLANILLAS);
        CalculoPlanilla calculo = calcularPlanilla(idObra, desde, hasta, false);
        List<DetalleTrabajadorPlanilla> detalle = calculo.lineas().stream()
                .map(l -> new DetalleTrabajadorPlanilla(
                        l.empleado().getIdEmpleado(),
                        l.empleado().getNombresApellidos(),
                        l.empleado().getDni(),
                        l.empleado().getCargo(),
                        l.dias(),
                        l.empleado().getJornalDiario(),
                        l.subtotal()))
                .toList();
        return new ResumenPlanilla(
                calculo.obra().getIdObra(),
                calculo.obra().getNombre(),
                desde,
                hasta,
                calculo.total(),
                detalle
        );
    }

    @Override
    public CierrePlanilla cerrar(UsuarioSesion actor, Integer idObra, LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PLANILLAS);
        if (actor == null || actor.idUsuario() == null || actor.idUsuario() <= 0) {
            throw new ReglaNegocioException("Sesión de usuario inválida para cerrar planilla.");
        }
        CalculoPlanilla calculo = calcularPlanilla(idObra, desde, hasta, true);

        CierrePlanilla cierre = new CierrePlanilla();
        cierre.setIdObra(idObra);
        cierre.setFechaInicioPeriodo(desde);
        cierre.setFechaFinPeriodo(hasta);
        cierre.setFechaCierre(LocalDateTime.now(clock));
        cierre.setMontoTotal(calculo.total());
        cierre.setIdUsuario(actor.idUsuario());
        cierre.setActivo(true);
        CierrePlanilla guardado = cierrePort.guardar(cierre);

        for (LineaPlanilla linea : calculo.lineas()) {
            String concepto = "Planilla " + desde + " al " + hasta + ": "
                    + linea.empleado().getNombresApellidos() + " (" + linea.dias() + "d)";
            if (concepto.length() > 150) {
                concepto = concepto.substring(0, 150);
            }
            Egreso egreso = new Egreso();
            egreso.setIdObra(idObra);
            egreso.setFecha(hasta);
            egreso.setTipoEgreso("Planilla");
            egreso.setConcepto(concepto);
            egreso.setMonto(linea.subtotal());
            egreso.setIdCierre(guardado.getIdCierre());
            egreso.setActivo(true);
            egresoPort.guardar(egreso);
        }

        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, "CIERRE_PLANILLA", "cierre_planilla",
                    String.valueOf(guardado.getIdCierre()), "EXITO",
                    "Obra " + idObra + " periodo " + desde + " a " + hasta + " total S/ " + calculo.total()));
        }

        return guardado;
    }

    private CalculoPlanilla calcularPlanilla(Integer idObra, LocalDate desde, LocalDate hasta, boolean bloquearObra) {
        if (idObra == null || idObra <= 0 || desde == null || hasta == null
                || hasta.isBefore(desde) || hasta.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("Verifica la obra y el rango de fechas (sin fechas futuras).");
        }

        Obra obra = (bloquearObra ? obraPort.findByIdParaActualizar(idObra) : obraPort.findById(idObra)).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra no existe o no está operativa (RF-14).");
        }

        if (cierrePort.findSolapado(idObra, desde, hasta).isPresent()) {
            throw new ReglaNegocioException(
                    "El periodo seleccionado se solapa con un cierre de planilla existente (RF-14).");
        }

        List<Asistencia> asistencias = asistenciaPort.findActivasByObraYPeriodo(idObra, desde, hasta);
        Map<Integer, Long> diasPorEmpleado = new LinkedHashMap<>();
        for (Asistencia a : asistencias) {
            if (a.esAsistenciaValida()) {
                diasPorEmpleado.merge(a.getIdEmpleado(), 1L, Long::sum);
            }
        }

        List<LineaPlanilla> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Long> entry : diasPorEmpleado.entrySet()) {
            Empleado emp = empleadoPort.findById(entry.getKey()).orElse(null);
            if (emp == null || emp.getJornalDiario() == null) {
                continue;
            }
            BigDecimal subtotal = emp.getJornalDiario()
                    .multiply(BigDecimal.valueOf(entry.getValue()))
                    .setScale(2, RoundingMode.HALF_UP);
            if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
                lineas.add(new LineaPlanilla(emp, entry.getValue(), subtotal));
                total = total.add(subtotal);
            }
        }

        if (lineas.isEmpty() || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("No hay asistencias válidas para cerrar en ese periodo.");
        }

        return new CalculoPlanilla(obra, lineas, total.setScale(2, RoundingMode.HALF_UP));
    }
}
