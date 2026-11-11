package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.services.CaptchaService;

/**
 * RF-01: genera el código captcha mostrado en el formulario de inicio de sesión.
 */
public class GenerarCaptcha {
    private final CaptchaService captcha;

    public GenerarCaptcha(CaptchaService captcha) {
        this.captcha = captcha;
    }

    public String ejecutar() {
        return captcha.generar();
    }
}
