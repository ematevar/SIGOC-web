package com.sigoc.sigoc_web.infrastructure.security;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Primera capa de control (RF-01, RNF-07, C12, C13):
 * - Exige sesión iniciada en /api/** (salvo rutas públicas).
 * - Revalida contra BD que la cuenta siga activa y conserve su rol actual.
 * - Verifica token CSRF en operaciones que modifican estado.
 * - Bloquea operaciones mientras el usuario conserve la contraseña inicial.
 */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    static final Set<String> RUTAS_PUBLICAS = Set.of("/api/auth/captcha", "/api/auth/login");
    static final String PREFIJO_PUBLICO = "/api/public/";
    static final Set<String> RUTAS_CON_CLAVE_INICIAL = Set.of(
            "/api/auth/cambiar-clave", "/api/auth/logout", "/api/auth/sesion");
    static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");
    public static final String HEADER_CSRF = "X-CSRF-TOKEN";

    private final UsuarioPort usuarioPort;

    public AutenticacionInterceptor(UsuarioPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (RUTAS_PUBLICAS.contains(ruta)
                || ruta.startsWith(PREFIJO_PUBLICO)
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        UsuarioSesion usuario = SesionHttp.usuario(request);
        if (usuario == null) {
            escribirError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para continuar.");
            return false;
        }
        if (!sigueHabilitado(usuario)) {
            SesionHttp.invalidar(request);
            escribirError(response, HttpStatus.UNAUTHORIZED,
                    "Su cuenta fue desactivada o su rol cambió. Inicie sesión nuevamente.");
            return false;
        }
        if (!METODOS_SEGUROS.contains(request.getMethod().toUpperCase())
                && !csrfValido(request)) {
            escribirError(response, HttpStatus.FORBIDDEN,
                    "Token de seguridad CSRF inválido o ausente. Recargue la página.");
            return false;
        }
        if (usuario.requiereCambioClave() && !RUTAS_CON_CLAVE_INICIAL.contains(ruta)) {
            escribirError(response, HttpStatus.FORBIDDEN,
                    "Debe cambiar la contraseña inicial antes de usar el sistema.");
            return false;
        }
        return true;
    }

    private boolean sigueHabilitado(UsuarioSesion sesion) {
        Optional<Usuario> actual = usuarioPort.findHabilitadoByLogin(sesion.username());
        if (actual.isEmpty()) {
            return false;
        }
        Usuario db = actual.get();
        if (!db.getIdUsuario().equals(sesion.idUsuario())) {
            return false;
        }
        return RolSistema.desde(db.getRol()).map(r -> r == sesion.rol()).orElse(false);
    }

    private boolean csrfValido(HttpServletRequest request) {
        String esperado = SesionHttp.obtenerOCrearCsrfToken(request);
        String recibido = request.getHeader(HEADER_CSRF);
        if (esperado == null || recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
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
