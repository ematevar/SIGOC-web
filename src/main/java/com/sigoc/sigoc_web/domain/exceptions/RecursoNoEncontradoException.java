package com.sigoc.sigoc_web.domain.exceptions;

/**
 * Se lanza cuando el registro solicitado no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
