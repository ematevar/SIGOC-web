package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarProduccionUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ProduccionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-12: Registro y consulta de producción diaria por trabajador y partida.
 */
public class GestionarProduccion implements GestionarProduccionUseCase {

    private final ProduccionPort produccionPort;
    private final EmpleadoPort empleadoPort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final AsistenciaPort asistenciaPort;
    private final Clock clock;

    public GestionarProduccion(ProduccionPort produccionPort, EmpleadoPort empleadoPort,
                               PartidaPort partidaPort, ObraPort obraPort,
                               AsistenciaPort asistenciaPort, Clock clock) {
        this.produccionPort = produccionPort;
        this.empleadoPort = empleadoPort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.asistenciaPort = asistenciaPort;
        this.clock = clock;
    }

    @Override
    public List<ProduccionEmpleado> listar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PRODUCCION);
        return idPartida != null ? produccionPort.findActivasByPartida(idPartida) : produccionPort.findAllActivas();
    }

    @Override
    public ProduccionEmpleado registrar(UsuarioSesion actor, Integer idEmpleado, Integer idPartida,
                                        LocalDate fecha, BigDecimal cantidadProducida) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.REGISTRAR_PRODUCCION);
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
                cantidadBd,
                true,
                null
        );
        return produccionPort.guardar(produccion);
    }
}
