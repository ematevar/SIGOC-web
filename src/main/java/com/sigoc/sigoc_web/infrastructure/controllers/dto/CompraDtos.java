package com.sigoc.sigoc_web.infrastructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CompraDtos {

    private CompraDtos() {
    }

    public record ItemCompraRequest(
            @NotNull Integer idMaterial,
            @NotNull BigDecimal cantidad,
            @NotNull BigDecimal precioUnitario
    ) {
    }

    public record RegistrarCompraRequest(
            @NotNull Integer idProveedor,
            @NotNull Integer idObra,
            Integer idMaterial,
            @NotBlank String tipoComprobante,
            @NotBlank String numeroComprobante,
            @NotNull LocalDate fechaCompra,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            @Valid List<ItemCompraRequest> items
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
