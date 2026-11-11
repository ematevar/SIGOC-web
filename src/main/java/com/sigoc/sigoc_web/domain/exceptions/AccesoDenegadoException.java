package com.sigoc.sigoc_web.domain.exceptions;

/**
 * Se lanza cuando el perfil del usuario en sesión no tiene permiso para la operación (RF-01, RNF-07).
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
