package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record CrearUsuarioRequest(@NotBlank String nombreCompleto, @NotBlank String usuario,
                                      @NotBlank String clave, @NotNull Integer idRol) {
    }

    public record ActualizarUsuarioRequest(@NotBlank String nombreCompleto, @NotNull Integer idRol) {
    }

    public record CambiarEstadoRequest(@NotNull Boolean activo) {
    }

    /** Nunca expone el hash de la contraseña. */
    public record UsuarioResponse(Integer idUsuario, String nombreCompleto, String usuario,
                                  Integer idRol, String rol, boolean activo) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getIdUsuario(), u.getNombreCompleto(), u.getUsername(),
                    u.getIdRol(), u.getRol(), u.isActivo());
        }
    }
}
