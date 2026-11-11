package com.sigoc.sigoc_web.domain.ports.out;

public interface PasswordHasherPort {
    String hash(String clave);

    boolean verificar(String clave, String hashAlmacenado);

    /** Indica si el hash corresponde a la contraseña inicial sembrada (obliga a cambiarla, RF-01). */
    boolean esClaveInicial(String hashAlmacenado);
}
