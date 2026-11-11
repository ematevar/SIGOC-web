package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-02: lista de usuarios (solo Administrador).
 */
public class ListarUsuarios {
    private final UsuarioPort usuarioPort;

    public ListarUsuarios(UsuarioPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    public List<Usuario> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        return usuarioPort.findAll();
    }
}
