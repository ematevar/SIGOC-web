package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarAsistenciaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CU-11: Registro individual y en lote de asistencia diaria en obra.
 */
public class GestionarAsistencia implements GestionarAsistenciaUseCase {

    private final AsistenciaPort asistenciaPort;
    private final EmpleadoPort empleadoPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public GestionarAsistencia(AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                               ObraPort obraPort, Clock clock) {
        this.asistenciaPort = asistenciaPort;
        this.empleadoPort = empleadoPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    @Override
    public List<Asistencia> listar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_ASISTENCIA);
        return idObra != null ? asistenciaPort.findActivasByObra(idObra) : asistenciaPort.findAllActivas();
    }

    @Override
    public Asistencia registrar(UsuarioSesion actor, Integer idEmpleado, Integer idObra,
                                LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, String estado) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.REGISTRAR_ASISTENCIA);
        if (idEmpleado == null || idEmpleado <= 0 || idObra == null || idObra <= 0
                || fecha == null || fecha.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("Empleado, obra o fecha inválidos.");
        }
        if (estado == null || estado.isBlank()) {
            throw new ReglaNegocioException("Selecciona un estado de asistencia.");
        }
        String estNorm = estado.trim().toUpperCase();
        LocalTime entradaFinal;
        LocalTime salidaFinal;

        if (estNorm.equals("FALTA") || estNorm.equals("PERMISO")) {
            entradaFinal = null;
            salidaFinal = null;
        } else if (estNorm.equals("PRESENTE") || estNorm.equals("TARDANZA")) {
            if (horaEntrada == null) {
                throw new ReglaNegocioException("Registra la hora de entrada para el estado " + estNorm + ".");
            }
            if (horaSalida != null && !horaSalida.isAfter(horaEntrada)) {
                throw new ReglaNegocioException("La hora de salida debe ser posterior a la hora de entrada (RF-11).");
            }
            entradaFinal = horaEntrada;
            salidaFinal = horaSalida;
        } else {
            throw new ReglaNegocioException("Estado de asistencia no reconocido: " + estado);
        }

        Empleado empleado = empleadoPort.findById(idEmpleado).filter(Empleado::isActivo).orElse(null);
        if (empleado == null) {
            throw new ReglaNegocioException("El trabajador seleccionado no existe o está inactivo (RF-10).");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o no se encuentra activa.");
        }
        if (asistenciaPort.estaEnPeriodoCerrado(idObra, fecha)) {
            throw new ReglaNegocioException(
                    "No se puede registrar ni modificar asistencia en un periodo de planilla ya cerrado (RF-14).");
        }
        if (asistenciaPort.findActivaByEmpleadoObraYFecha(idEmpleado, idObra, fecha).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe asistencia registrada para este trabajador, obra y fecha (RF-11).");
        }

        Asistencia nueva = new Asistencia(null, idEmpleado, idObra, fecha, entradaFinal, salidaFinal,
                estNorm, true, null);
        return asistenciaPort.guardar(nueva);
    }

    @Override
    public List<Asistencia> registrarLote(UsuarioSesion actor, Integer idObra, LocalDate fecha,
                                          List<FilaAsistencia> filas) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.REGISTRAR_ASISTENCIA);
        if (filas == null || filas.isEmpty()) {
            throw new ReglaNegocioException("Incluye al menos un trabajador para registrar asistencia.");
        }
        List<Asistencia> registradas = new ArrayList<>(filas.size());
        for (FilaAsistencia fila : filas) {
            registradas.add(registrar(
                    actor,
                    fila.idEmpleado(),
                    idObra,
                    fecha,
                    fila.horaEntrada(),
                    fila.horaSalida(),
                    fila.estado()
            ));
        }
        return registradas;
    }
}
