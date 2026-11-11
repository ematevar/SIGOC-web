package com.sigoc.sigoc_web.infrastructure.security;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Acceso a los atributos de sesión HTTP usados por la capa web y protección CSRF.
 */
public final class SesionHttp {

    public static final String ATTR_USUARIO = "SIGOC_USUARIO";
    public static final String ATTR_CAPTCHA = "SIGOC_CAPTCHA";
    public static final String ATTR_CSRF = "SIGOC_CSRF";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SesionHttp() {
    }

    public static UsuarioSesion usuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UsuarioSesion) session.getAttribute(ATTR_USUARIO);
    }

    public static void guardarUsuario(HttpServletRequest request, UsuarioSesion usuario) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR_USUARIO, usuario);
        if (session.getAttribute(ATTR_CSRF) == null) {
            session.setAttribute(ATTR_CSRF, nuevoToken());
        }
    }

    public static String obtenerOCrearCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object actual = session.getAttribute(ATTR_CSRF);
        if (actual instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = nuevoToken();
        session.setAttribute(ATTR_CSRF, token);
        return token;
    }

    public static String regenerarCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = nuevoToken();
        session.setAttribute(ATTR_CSRF, token);
        return token;
    }

    public static void invalidar(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
