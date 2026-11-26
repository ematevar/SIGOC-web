package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.CambiarClaveUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;

import java.time.Clock;

/**
 * CU-02: Cambio de contraseña del usuario autenticado.
 */
public class CambiarClave implements CambiarClaveUseCase {

    private final UsuarioPort usuarioPort;
    private final PasswordHasherPort hasher;
    private final AuditoriaEventoPort auditoriaPort;
    private final Clock clock;

    public CambiarClave(UsuarioPort usuarioPort, PasswordHasherPort hasher) {
        this(usuarioPort, hasher, null, Clock.systemDefaultZone());
    }

    public CambiarClave(UsuarioPort usuarioPort, PasswordHasherPort hasher,
                        AuditoriaEventoPort auditoriaPort, Clock clock) {
        this.usuarioPort = usuarioPort;
        this.hasher = hasher;
        this.auditoriaPort = auditoriaPort;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    @Override
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
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, "CAMBIO_CLAVE", "usuario", String.valueOf(actor.idUsuario()),
                    "EXITO", "Cambio de contraseña realizado"));
        }
    }
}
