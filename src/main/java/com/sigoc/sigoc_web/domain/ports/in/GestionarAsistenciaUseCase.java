package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * CU-11: Registro individual y en lote de asistencia diaria en obra.
 */
public interface GestionarAsistenciaUseCase {

    record FilaAsistencia(Integer idEmpleado, String estado, LocalTime horaEntrada, LocalTime horaSalida) {
    }

    @OperacionSoloLectura
    List<Asistencia> listar(UsuarioSesion actor, Integer idObra);

    Asistencia registrar(UsuarioSesion actor, Integer idEmpleado, Integer idObra,
                         LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, String estado);

    List<Asistencia> registrarLote(UsuarioSesion actor, Integer idObra, LocalDate fecha,
                                   List<FilaAsistencia> filas);
}
