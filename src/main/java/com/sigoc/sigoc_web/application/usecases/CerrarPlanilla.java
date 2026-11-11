package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * RF-14: cierra e inmoviliza la planilla de una obra en un rango [desde, hasta]
 * sin solapamientos y genera egresos tipo Planilla por cada trabajador con asistencias válidas.
 */
public class CerrarPlanilla {

    private final CierrePlanillaPort cierrePort;
    private final ObraPort obraPort;
    private final AsistenciaPort asistenciaPort;
    private final EmpleadoPort empleadoPort;
    private final EgresoPort egresoPort;
    private final Clock clock;

    public CerrarPlanilla(CierrePlanillaPort cierrePort, ObraPort obraPort,
                          AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                          EgresoPort egresoPort, Clock clock) {
        this.cierrePort = cierrePort;
        this.obraPort = obraPort;
        this.asistenciaPort = asistenciaPort;
        this.empleadoPort = empleadoPort;
        this.egresoPort = egresoPort;
        this.clock = clock;
    }

    public CierrePlanilla ejecutar(UsuarioSesion actor, Integer idObra, LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        if (actor == null || actor.idUsuario() == null || actor.idUsuario() <= 0
                || idObra == null || idObra <= 0 || desde == null || hasta == null
                || hasta.isBefore(desde) || hasta.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("Verifica la obra y el rango de fechas (sin fechas futuras).");
        }

        Obra obra = obraPort.findByIdParaActualizar(idObra).orElse(null);
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

        record LineaPago(Empleado empleado, long dias, BigDecimal subtotal) {}
        List<LineaPago> lineas = new ArrayList<>();
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
                lineas.add(new LineaPago(emp, entry.getValue(), subtotal));
                total = total.add(subtotal);
            }
        }

        if (lineas.isEmpty() || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("No hay asistencias válidas para cerrar en ese periodo.");
        }

        CierrePlanilla cierre = new CierrePlanilla();
        cierre.setIdObra(idObra);
        cierre.setFechaInicioPeriodo(desde);
        cierre.setFechaFinPeriodo(hasta);
        cierre.setFechaCierre(LocalDateTime.now(clock));
        cierre.setMontoTotal(total.setScale(2, RoundingMode.HALF_UP));
        cierre.setIdUsuario(actor.idUsuario());
        cierre.setActivo(true);
        CierrePlanilla guardado = cierrePort.guardar(cierre);

        for (LineaPago linea : lineas) {
            String concepto = "Planilla " + desde + " al " + hasta + " — "
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

        return guardado;
    }
}
