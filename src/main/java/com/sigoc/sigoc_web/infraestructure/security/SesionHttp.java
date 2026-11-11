package com.sigoc.sigoc_web.infraestructure.security;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Acceso a los atributos de sesión HTTP usados por la capa web.
 */
public final class SesionHttp {

    public static final String ATTR_USUARIO = "SIGOC_USUARIO";
    public static final String ATTR_CAPTCHA = "SIGOC_CAPTCHA";

    private SesionHttp() {
    }

    public static UsuarioSesion usuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UsuarioSesion) session.getAttribute(ATTR_USUARIO);
    }

    public static void guardarUsuario(HttpServletRequest request, UsuarioSesion usuario) {
        request.getSession(true).setAttribute(ATTR_USUARIO, usuario);
    }
}
