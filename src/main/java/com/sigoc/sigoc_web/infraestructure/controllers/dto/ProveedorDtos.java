package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Proveedor;
import jakarta.validation.constraints.NotBlank;

public final class ProveedorDtos {

    private ProveedorDtos() {
    }

    public record GuardarProveedorRequest(
            @NotBlank String razonSocial,
            @NotBlank String ruc,
            String telefono,
            String direccion
    ) {
    }

    public record ProveedorResponse(
            Integer idProveedor,
            String razonSocial,
            String ruc,
            String telefono,
            String direccion
    ) {
        public static ProveedorResponse de(Proveedor p) {
            return new ProveedorResponse(
                    p.getIdProveedor(),
                    p.getRazonSocial(),
                    p.getRuc(),
                    p.getTelefono(),
                    p.getDireccion()
            );
        }
    }
}
