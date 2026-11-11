package com.sigoc.sigoc_web.infrastructure.security;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Primera capa de control (RF-01): exige sesión iniciada en /api/** y bloquea todo
 * salvo el cambio de contraseña mientras el usuario conserve la clave inicial.
 */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    static final Set<String> RUTAS_PUBLICAS = Set.of("/api/auth/captcha", "/api/auth/login");
    static final Set<String> RUTAS_CON_CLAVE_INICIAL = Set.of(
            "/api/auth/cambiar-clave", "/api/auth/logout", "/api/auth/sesion");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (RUTAS_PUBLICAS.contains(ruta) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        UsuarioSesion usuario = SesionHttp.usuario(request);
        if (usuario == null) {
            escribirError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para continuar.");
            return false;
        }
        if (usuario.requiereCambioClave() && !RUTAS_CON_CLAVE_INICIAL.contains(ruta)) {
            escribirError(response, HttpStatus.FORBIDDEN,
                    "Debe cambiar la contraseña inicial antes de usar el sistema.");
            return false;
        }
        return true;
    }

    private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":" + status.value()
                + ",\"mensaje\":\"" + mensaje.replace("\"", "\\\"")
                + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
