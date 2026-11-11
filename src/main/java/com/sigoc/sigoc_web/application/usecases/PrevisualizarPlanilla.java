package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.domain.ports.out.CierrePlanillaPort;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-14: calcula el desglose previo de planilla por trabajador sin confirmar el cierre.
 */
public class PrevisualizarPlanilla {

    public record DetalleTrabajadorPlanilla(
            Integer idEmpleado,
            String nombresApellidos,
            String dni,
            String cargo,
            long diasValidos,
            BigDecimal jornalDiario,
            BigDecimal subtotal
    ) {
    }

    public record ResumenPlanilla(
            Integer idObra,
            String nombreObra,
            LocalDate desde,
            LocalDate hasta,
            BigDecimal totalPlanilla,
            List<DetalleTrabajadorPlanilla> trabajadores
    ) {
    }

    private final CierrePlanillaPort cierrePort;
    private final ObraPort obraPort;
    private final AsistenciaPort asistenciaPort;
    private final EmpleadoPort empleadoPort;
    private final Clock clock;

    public PrevisualizarPlanilla(CierrePlanillaPort cierrePort, ObraPort obraPort,
                                 AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort, Clock clock) {
        this.cierrePort = cierrePort;
        this.obraPort = obraPort;
        this.asistenciaPort = asistenciaPort;
        this.empleadoPort = empleadoPort;
        this.clock = clock;
    }

    public ResumenPlanilla ejecutar(UsuarioSesion actor, Integer idObra, LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        var calc = CerrarPlanilla.calcularPlanilla(
                cierrePort, obraPort, asistenciaPort, empleadoPort, clock,
                idObra, desde, hasta, false);
        List<DetalleTrabajadorPlanilla> detalles = calc.lineas().stream()
                .map(l -> new DetalleTrabajadorPlanilla(
                        l.empleado().getIdEmpleado(),
                        l.empleado().getNombresApellidos(),
                        l.empleado().getDni(),
                        l.empleado().getCargo(),
                        l.dias(),
                        l.empleado().getJornalDiario(),
                        l.subtotal()))
                .toList();
        return new ResumenPlanilla(idObra, calc.obra().getNombre(), desde, hasta, calc.total(), detalles);
    }
}
