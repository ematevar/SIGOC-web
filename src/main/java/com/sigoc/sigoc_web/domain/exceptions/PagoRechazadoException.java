package com.sigoc.sigoc_web.domain.exceptions;

/**
 * Se lanza cuando la pasarela externa o simulada rechaza un intento de cobro,
 * permitiendo conservar el registro de auditoría del intento en estado RECHAZADO.
 */
public class PagoRechazadoException extends RuntimeException {

    private final String motivo;

    public PagoRechazadoException(String motivo) {
        super(motivo);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
