package com.sigoc.sigoc_web.infrastructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class PersonalDtos {

    private PersonalDtos() {
    }

    public record GuardarEmpleadoRequest(
            @NotBlank String nombresApellidos,
            @NotBlank String dni,
            @NotBlank String cargo,
            @NotNull BigDecimal jornalDiario,
            @NotNull LocalDate fechaIngreso
    ) {
    }

    public record EmpleadoResponse(
            Integer idEmpleado,
            String nombresApellidos,
            String dni,
            String cargo,
            BigDecimal jornalDiario,
            LocalDate fechaIngreso
    ) {
        public static EmpleadoResponse de(Empleado e) {
            return new EmpleadoResponse(
                    e.getIdEmpleado(),
                    e.getNombresApellidos(),
                    e.getDni(),
                    e.getCargo(),
                    e.getJornalDiario(),
                    e.getFechaIngreso()
            );
        }
    }

    public record RegistrarAsistenciaRequest(
            @NotNull Integer idEmpleado,
            @NotNull Integer idObra,
            @NotNull LocalDate fecha,
            LocalTime horaEntrada,
            LocalTime horaSalida,
            @NotBlank String estado
    ) {
    }

    public record AsistenciaResponse(
            Integer idAsistencia,
            Integer idEmpleado,
            Integer idObra,
            LocalDate fecha,
            LocalTime horaEntrada,
            LocalTime horaSalida,
            String estado
    ) {
        public static AsistenciaResponse de(Asistencia a) {
            return new AsistenciaResponse(
                    a.getIdAsistencia(),
                    a.getIdEmpleado(),
                    a.getIdObra(),
                    a.getFecha(),
                    a.getHoraEntrada(),
                    a.getHoraSalida(),
                    a.getEstado()
            );
        }
    }

    public record RegistrarProduccionRequest(
            @NotNull Integer idEmpleado,
            @NotNull Integer idPartida,
            @NotNull LocalDate fecha,
            @NotNull BigDecimal cantidadProducida
    ) {
    }

    public record ProduccionResponse(
            Integer idProduccion,
            Integer idEmpleado,
            Integer idPartida,
            LocalDate fecha,
            BigDecimal cantidadProducida
    ) {
        public static ProduccionResponse de(ProduccionEmpleado p) {
            return new ProduccionResponse(
                    p.getIdProduccion(),
                    p.getIdEmpleado(),
                    p.getIdPartida(),
                    p.getFecha(),
                    p.getCantidadProducida()
            );
        }
    }
}
