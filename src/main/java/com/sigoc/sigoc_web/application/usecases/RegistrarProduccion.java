package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * RF-12: registra producción diaria de un trabajador en una partida activa verificando
 * que tenga asistencia válida (PRESENTE o TARDANZA) ese día en esa obra.
 */
public class RegistrarProduccion {

    private final ProduccionPort produccionPort;
    private final EmpleadoPort empleadoPort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final AsistenciaPort asistenciaPort;
    private final Clock clock;

    public RegistrarProduccion(ProduccionPort produccionPort, EmpleadoPort empleadoPort,
                               PartidaPort partidaPort, ObraPort obraPort,
                               AsistenciaPort asistenciaPort, Clock clock) {
        this.produccionPort = produccionPort;
        this.empleadoPort = empleadoPort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.asistenciaPort = asistenciaPort;
        this.clock = clock;
    }

    public ProduccionEmpleado ejecutar(UsuarioSesion actor, Integer idEmpleado, Integer idPartida,
                                       LocalDate fecha, BigDecimal cantidadProducida) {
        PoliticaAcceso.verificar(actor, RolSistema.RESIDENTE);
        BigDecimal cantidadBd = cantidadProducida == null ? null : cantidadProducida.setScale(2, RoundingMode.HALF_UP);
        if (idEmpleado == null || idEmpleado <= 0 || idPartida == null || idPartida <= 0
                || fecha == null || fecha.isAfter(LocalDate.now(clock))
                || cantidadBd == null || cantidadBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Datos de producción inválidos.");
        }
        Empleado empleado = empleadoPort.findById(idEmpleado).filter(Empleado::isActivo).orElse(null);
        if (empleado == null) {
            throw new ReglaNegocioException("El trabajador seleccionado no existe o está inactivo (RF-10).");
        }
        Partida partida = partidaPort.findById(idPartida).filter(Partida::isActivo).orElse(null);
        if (partida == null) {
            throw new ReglaNegocioException("La partida seleccionada no existe o está inactiva (RF-12).");
        }
        Obra obra = obraPort.findById(partida.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra a la que pertenece la partida no se encuentra activa (RF-12).");
        }

        Asistencia asistencia = asistenciaPort
                .findActivaByEmpleadoObraYFecha(idEmpleado, obra.getIdObra(), fecha)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El trabajador no tiene asistencia registrada en la obra de esta partida para la fecha "
                                + fecha + " (RF-12)."));
        if (!asistencia.esAsistenciaValida()) {
            throw new ReglaNegocioException(
                    "No se puede registrar producción porque el trabajador figura con estado '"
                            + asistencia.getEstado() + "' en esa fecha (RF-12).");
        }

        ProduccionEmpleado produccion = new ProduccionEmpleado(
                null,
                idEmpleado,
                idPartida,
                fecha,
                cantidadProducida.setScale(2, RoundingMode.HALF_UP),
                true,
                null
        );
        return produccionPort.guardar(produccion);
    }
}
