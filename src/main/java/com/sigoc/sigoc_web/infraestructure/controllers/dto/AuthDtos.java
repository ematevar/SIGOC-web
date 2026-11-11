package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record CaptchaResponse(String captcha) {
    }

    public record LoginRequest(@NotBlank String usuario, @NotBlank String clave, @NotBlank String captcha) {
    }

    public record CambiarClaveRequest(@NotBlank String actual, @NotBlank String nueva) {
    }

    public record SesionResponse(Integer idUsuario, String usuario, String nombreCompleto,
                                 String rol, String rolEtiqueta, boolean requiereCambioClave) {
    }
}
