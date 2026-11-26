package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

/**
 * CU-02: Cambio de contraseña del usuario autenticado.
 */
public interface CambiarClaveUseCase {
    void ejecutar(UsuarioSesion actor, String claveActual, String claveNueva);
}
