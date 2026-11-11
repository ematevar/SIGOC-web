package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

public interface AutenticarUsuarioUseCase {
    UsuarioSesion ejecutar(String usuario, String clave, String captchaEsperado, String captchaIngresado);
}
