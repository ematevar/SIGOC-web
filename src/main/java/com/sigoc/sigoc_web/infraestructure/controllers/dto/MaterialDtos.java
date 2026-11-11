package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Material;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public final class MaterialDtos {

    private MaterialDtos() {
    }

    public record GuardarMaterialRequest(
            @NotBlank String nombre,
            @NotBlank String unidadMedida,
            BigDecimal stockMinimo,
            BigDecimal costoUnitarioRef
    ) {
    }

    public record MaterialResponse(
            Integer idMaterial,
            String nombre,
            String unidadMedida,
            BigDecimal stockActual,
            BigDecimal stockMinimo,
            BigDecimal costoUnitarioRef,
            boolean alertaStock
    ) {
        public static MaterialResponse de(Material m) {
            return new MaterialResponse(
                    m.getIdMaterial(),
                    m.getNombre(),
                    m.getUnidadMedida(),
                    m.getStockActual(),
                    m.getStockMinimo(),
                    m.getCostoUnitarioRef(),
                    m.isStockEnAlerta()
            );
        }
    }
}
