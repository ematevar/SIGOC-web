package com.sigoc.sigoc_web.domain.exceptions;

/**
 * Se lanza cuando los datos de entrada violan una regla de negocio del SRS.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
