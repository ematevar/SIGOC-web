package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.AvancePartida;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class AvanceDtos {

    private AvanceDtos() {
    }

    public record RegistrarAvanceRequest(
            @NotNull Integer idPartida,
            @NotNull LocalDate fechaReporte,
            @NotNull BigDecimal porcentajeAvance,
            String observaciones
    ) {
    }

    public record AvanceResponse(
            Integer idAvance,
            Integer idPartida,
            LocalDate fechaReporte,
            BigDecimal porcentajeAvance,
            String observaciones,
            Integer idUsuario
    ) {
        public static AvanceResponse de(AvancePartida a) {
            return new AvanceResponse(
                    a.getIdAvance(),
                    a.getIdPartida(),
                    a.getFechaReporte(),
                    a.getPorcentajeAvance(),
                    a.getObservaciones(),
                    a.getIdUsuario()
            );
        }
    }

    public record RegistroAvanceResponse(AvanceResponse avance, BigDecimal avanceObra) {
    }

    public record AvanceObraResponse(Integer idObra, BigDecimal avanceAcumulado) {
    }
}
