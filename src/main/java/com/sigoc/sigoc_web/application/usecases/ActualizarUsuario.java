package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-02: actualiza nombre y rol cuidando que siempre exista al menos un Administrador activo.
 */
public class ActualizarUsuario {

    private final UsuarioPort usuarioPort;
    private final RolPort rolPort;

    public ActualizarUsuario(UsuarioPort usuarioPort, RolPort rolPort) {
        this.usuarioPort = usuarioPort;
        this.rolPort = rolPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idUsuario, String nombre, Integer idRol) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idUsuario == null || idUsuario <= 0 || nombre == null || nombre.isBlank() || idRol == null || idRol <= 0) {
            throw new ReglaNegocioException("Nombre o rol inválido.");
        }
        Rol nuevoRol = rolPort.findActivoById(idRol)
                .filter(r -> RolSistema.esValido(r.getNombreRol()))
                .orElseThrow(() -> new ReglaNegocioException("El rol seleccionado no es válido."));
        Usuario actual = usuarioPort.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        boolean eraAdmin = RolSistema.desde(actual.getRol()).orElse(null) == RolSistema.ADMIN;
        boolean seraAdmin = RolSistema.desde(nuevoRol.getNombreRol()).orElse(null) == RolSistema.ADMIN;
        if (actual.isActivo() && eraAdmin && !seraAdmin && usuarioPort.contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede cambiar el rol del único Administrador activo.");
        }
        usuarioPort.actualizarDatos(idUsuario, nombre.trim(), nuevoRol.getIdRol());
    }
}
