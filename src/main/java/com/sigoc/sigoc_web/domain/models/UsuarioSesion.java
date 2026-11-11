package com.sigoc.sigoc_web.domain.models;

/**
 * Actor autenticado que ejecuta los casos de uso (equivale a la sesión del desktop).
 */
public record UsuarioSesion(
        Integer idUsuario,
        String username,
        String nombreCompleto,
        RolSistema rol,
        boolean requiereCambioClave
) implements java.io.Serializable {
    public UsuarioSesion conClaveCambiada() {
        return new UsuarioSesion(idUsuario, username, nombreCompleto, rol, false);
    }
}
