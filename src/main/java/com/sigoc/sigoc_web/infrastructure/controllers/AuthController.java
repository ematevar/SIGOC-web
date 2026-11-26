package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.AutenticarUsuarioUseCase;
import com.sigoc.sigoc_web.domain.ports.in.CambiarClaveUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.AuthDtos.*;
import com.sigoc.sigoc_web.infrastructure.security.CaptchaImagenRenderer;
import com.sigoc.sigoc_web.infrastructure.security.SesionHttp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CU-01 y CU-02: captcha, inicio/cierre de sesión y cambio de contraseña.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final CambiarClaveUseCase cambiarClave;
    private final CaptchaImagenRenderer captchaImagenRenderer;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuario, CambiarClaveUseCase cambiarClave,
                          CaptchaImagenRenderer captchaImagenRenderer) {
        this.autenticarUsuario = autenticarUsuario;
        this.cambiarClave = cambiarClave;
        this.captchaImagenRenderer = captchaImagenRenderer;
    }

    @GetMapping("/captcha")
    public CaptchaResponse captcha(HttpServletRequest request) {
        String codigo = autenticarUsuario.generarCaptcha();
        request.getSession(true).setAttribute(SesionHttp.ATTR_CAPTCHA, codigo);
        return new CaptchaResponse(captchaImagenRenderer.renderizarPngDataUri(codigo));
    }

    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String esperado = (String) session.getAttribute(SesionHttp.ATTR_CAPTCHA);
        session.removeAttribute(SesionHttp.ATTR_CAPTCHA);
        UsuarioSesion usuario = autenticarUsuario.iniciarSesion(body.usuario(), body.clave(), esperado, body.captcha());
        request.changeSessionId();
        SesionHttp.guardarUsuario(request, usuario);
        SesionHttp.regenerarCsrfToken(request);
        return aRespuesta(usuario);
    }

    @GetMapping("/sesion")
    public SesionResponse sesion(UsuarioSesion usuario) {
        return aRespuesta(usuario);
    }

    @PostMapping("/cambiar-clave")
    public ResponseEntity<Void> cambiarClave(UsuarioSesion usuario, @Valid @RequestBody CambiarClaveRequest body,
                                             HttpServletRequest request) {
        cambiarClave.ejecutar(usuario, body.actual(), body.nueva());
        SesionHttp.guardarUsuario(request, usuario.conClaveCambiada());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private SesionResponse aRespuesta(UsuarioSesion u) {
        return new SesionResponse(u.idUsuario(), u.username(), u.nombreCompleto(),
                u.rol().name(), u.rol().getEtiqueta(), u.requiereCambioClave());
    }
}
