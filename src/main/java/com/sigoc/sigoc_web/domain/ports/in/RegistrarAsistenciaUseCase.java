package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.time.LocalDate;
import java.time.LocalTime;

public interface RegistrarAsistenciaUseCase {
    Asistencia ejecutar(UsuarioSesion actor, Integer idEmpleado, Integer idObra,
                        LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, String estado);
}
