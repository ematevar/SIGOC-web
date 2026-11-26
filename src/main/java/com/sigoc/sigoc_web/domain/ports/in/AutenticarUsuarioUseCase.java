package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

/**
 * CU-01: Autenticación con captcha y bloqueo temporal por intentos fallidos.
 */
public interface AutenticarUsuarioUseCase {
    @OperacionSoloLectura
    String generarCaptcha();

    UsuarioSesion iniciarSesion(String usuario, String clave, String captchaEsperado, String captchaIngresado);
}
