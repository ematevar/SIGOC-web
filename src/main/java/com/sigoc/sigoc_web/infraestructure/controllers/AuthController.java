package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.CambiarClave;
import com.sigoc.sigoc_web.aplication.usecases.GenerarCaptcha;
import com.sigoc.sigoc_web.aplication.usecases.IniciarSesion;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.AuthDtos.*;
import com.sigoc.sigoc_web.infraestructure.security.SesionHttp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RF-01: captcha, inicio/cierre de sesión y cambio de contraseña.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GenerarCaptcha generarCaptcha;
    private final IniciarSesion iniciarSesion;
    private final CambiarClave cambiarClave;

    public AuthController(GenerarCaptcha generarCaptcha, IniciarSesion iniciarSesion, CambiarClave cambiarClave) {
        this.generarCaptcha = generarCaptcha;
        this.iniciarSesion = iniciarSesion;
        this.cambiarClave = cambiarClave;
    }

    @GetMapping("/captcha")
    public CaptchaResponse captcha(HttpServletRequest request) {
        String codigo = generarCaptcha.ejecutar();
        request.getSession(true).setAttribute(SesionHttp.ATTR_CAPTCHA, codigo);
        return new CaptchaResponse(codigo);
    }

    @PostMapping("/login")
    public SesionResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String esperado = (String) session.getAttribute(SesionHttp.ATTR_CAPTCHA);
        // El captcha es de un solo uso: se descarta tras cada intento.
        session.removeAttribute(SesionHttp.ATTR_CAPTCHA);
        UsuarioSesion usuario = iniciarSesion.ejecutar(body.usuario(), body.clave(), esperado, body.captcha());
        request.changeSessionId(); // previene fijación de sesión
        SesionHttp.guardarUsuario(request, usuario);
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
