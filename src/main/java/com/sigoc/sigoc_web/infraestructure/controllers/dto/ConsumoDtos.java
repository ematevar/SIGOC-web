package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ConsumoDtos {

    private ConsumoDtos() {
    }

    public record RegistrarConsumoRequest(
            @NotNull Integer idMaterial,
            @NotNull Integer idPartida,
            @NotNull LocalDate fechaConsumo,
            @NotNull BigDecimal cantidad
    ) {
    }

    public record ConsumoResponse(
            Integer idConsumo,
            Integer idPartida,
            Integer idMaterial,
            LocalDate fechaConsumo,
            BigDecimal cantidad,
            Integer idUsuario
    ) {
        public static ConsumoResponse de(ConsumoMaterial c) {
            return new ConsumoResponse(
                    c.getIdConsumo(),
                    c.getIdPartida(),
                    c.getIdMaterial(),
                    c.getFechaConsumo(),
                    c.getCantidad(),
                    c.getIdUsuario()
            );
        }
    }

    public record RegistroConsumoResponse(
            ConsumoResponse consumo,
            BigDecimal stockResultante,
            boolean alertaStockMinimo
    ) {
    }

    public record ConsumoAcumuladoResponse(Integer idPartida, Integer idMaterial, BigDecimal totalConsumido) {
    }
}
