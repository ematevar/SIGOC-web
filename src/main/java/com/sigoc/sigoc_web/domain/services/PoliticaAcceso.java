package com.sigoc.sigoc_web.domain.services;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

/**
 * Verificación de permisos por perfil dentro de cada caso de uso
 * (segunda capa de control, RF-01 / RNF-07).
 */
public final class PoliticaAcceso {

    private PoliticaAcceso() {
    }

    public static void verificar(UsuarioSesion actor, RolSistema... permitidos) {
        if (actor == null || actor.rol() == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación.");
        }
        for (RolSistema permitido : permitidos) {
            if (actor.rol() == permitido) {
                return;
            }
        }
        throw new AccesoDenegadoException("Acceso denegado: su perfil (" + actor.rol().getEtiqueta()
                + ") no tiene permisos para realizar esta operación.");
    }
}
