package com.sigoc.sigoc_web.infrastructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class CompraDtos {

    private CompraDtos() {
    }

    public record RegistrarCompraRequest(
            @NotNull Integer idProveedor,
            @NotNull Integer idObra,
            @NotNull Integer idMaterial,
            @NotBlank String tipoComprobante,
            @NotBlank String numeroComprobante,
            @NotNull LocalDate fechaCompra,
            @NotNull BigDecimal cantidad,
            @NotNull BigDecimal precioUnitario
    ) {
    }

    public record CompraResponse(
            Integer idCompra,
            Integer idProveedor,
            String tipoComprobante,
            LocalDate fechaCompra,
            String numeroComprobante,
            BigDecimal total
    ) {
        public static CompraResponse de(CompraMaterial c) {
            return new CompraResponse(
                    c.getIdCompra(),
                    c.getIdProveedor(),
                    c.getTipoComprobante(),
                    c.getFechaCompra(),
                    c.getNumeroComprobante(),
                    c.getTotal()
            );
        }
    }
}
