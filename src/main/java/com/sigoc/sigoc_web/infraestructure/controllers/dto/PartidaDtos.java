package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class PartidaDtos {

    private PartidaDtos() {
    }

    public record GuardarPartidaRequest(
            @NotNull Integer idObra,
            @NotBlank String nombre,
            @NotBlank String unidadMedida,
            @NotNull BigDecimal metradoPlanificado,
            @NotNull BigDecimal pesoPresupuestal
    ) {
    }

    public record PartidaResponse(
            Integer idPartida,
            Integer idObra,
            String nombre,
            String unidadMedida,
            BigDecimal metradoPlanificado,
            BigDecimal pesoPresupuestal
    ) {
        public static PartidaResponse de(Partida p) {
            return new PartidaResponse(
                    p.getIdPartida(),
                    p.getIdObra(),
                    p.getNombre(),
                    p.getUnidadMedida(),
                    p.getMetradoPlanificado(),
                    p.getPesoPresupuestal()
            );
        }
    }

    public record AsignarMaterialRequest(
            @NotNull Integer idMaterial,
            @NotNull BigDecimal cantidadPresupuestada
    ) {
    }

    public record PartidaMaterialResponse(
            Integer idPartidaMaterial,
            Integer idPartida,
            Integer idMaterial,
            BigDecimal cantidadPresupuestada
    ) {
        public static PartidaMaterialResponse de(PartidaMaterial pm) {
            return new PartidaMaterialResponse(
                    pm.getIdPartidaMaterial(),
                    pm.getIdPartida(),
                    pm.getIdMaterial(),
                    pm.getCantidadPresupuestada()
            );
        }
    }
}
