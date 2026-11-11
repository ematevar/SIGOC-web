package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-02: activa o desactiva (baja lógica) un usuario sin dejar el sistema sin Administradores activos.
 */
public class CambiarEstadoUsuario {

    private final UsuarioPort usuarioPort;

    public CambiarEstadoUsuario(UsuarioPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idUsuario, boolean activo) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idUsuario == null || idUsuario <= 0) {
            throw new ReglaNegocioException("Usuario inválido.");
        }
        Usuario usuario = usuarioPort.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        if (!activo && usuario.isActivo()
                && RolSistema.desde(usuario.getRol()).orElse(null) == RolSistema.ADMIN
                && usuarioPort.contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede desactivar al único Administrador activo del sistema.");
        }
        usuarioPort.cambiarEstado(idUsuario, activo);
    }
}
