package com.sigoc.sigoc_web.infrastructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Obra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ObraDtos {

    private ObraDtos() {
    }

    public record GuardarObraRequest(
            @NotBlank String nombre,
            Integer idCliente,
            String cliente,
            String ubicacion,
            @NotNull BigDecimal presupuestoTotal,
            @NotNull LocalDate fechaInicioPlan,
            @NotNull LocalDate fechaFinPlan,
            String estado
    ) {
    }

    public record ObraResponse(
            Integer idObra,
            Integer idCliente,
            String nombre,
            String cliente,
            String ubicacion,
            BigDecimal presupuestoTotal,
            LocalDate fechaInicioPlan,
            LocalDate fechaFinPlan,
            String estado,
            boolean operable
    ) {
        public static ObraResponse de(Obra o) {
            return new ObraResponse(
                    o.getIdObra(),
                    o.getIdCliente(),
                    o.getNombre(),
                    o.getCliente(),
                    o.getUbicacion(),
                    o.getPresupuestoTotal(),
                    o.getFechaInicioPlan(),
                    o.getFechaFinPlan(),
                    o.getEstado(),
                    o.isOperable()
            );
        }
    }
}
