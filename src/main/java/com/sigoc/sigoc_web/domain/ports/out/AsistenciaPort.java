package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Asistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaPort {
    List<Asistencia> findAllActivas();

    List<Asistencia> findActivasByObra(Integer idObra);

    List<Asistencia> findActivasByObraYPeriodo(Integer idObra, LocalDate desde, LocalDate hasta);

    Optional<Asistencia> findActivaByEmpleadoObraYFecha(Integer idEmpleado, Integer idObra, LocalDate fecha);

    boolean estaEnPeriodoCerrado(Integer idObra, LocalDate fecha);

    Asistencia guardar(Asistencia asistencia);
}
