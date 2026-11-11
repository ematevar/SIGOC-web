package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;

/**
 * RF-01: cambio de contraseña del usuario en sesión (obligatorio tras el primer inicio con la clave inicial).
 */
public class CambiarClave {

    private final UsuarioPort usuarioPort;
    private final PasswordHasherPort hasher;

    public CambiarClave(UsuarioPort usuarioPort, PasswordHasherPort hasher) {
        this.usuarioPort = usuarioPort;
        this.hasher = hasher;
    }

    public void ejecutar(UsuarioSesion actor, String actual, String nueva) {
        if (actor == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación.");
        }
        if (nueva == null || nueva.length() < 8) {
            throw new ReglaNegocioException("La contraseña nueva debe tener al menos 8 caracteres.");
        }
        if (actual != null && actual.equals(nueva)) {
            throw new ReglaNegocioException("La nueva contraseña debe ser diferente a la actual.");
        }
        Usuario usuario = usuarioPort.findById(actor.idUsuario())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ReglaNegocioException("La contraseña actual no es correcta."));
        if (actual == null || !hasher.verificar(actual, usuario.getPasswordHash())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }
        usuarioPort.actualizarClave(actor.idUsuario(), hasher.hash(nueva));
    }
}
