package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoTarjetaUseCase;

/**
 * CU-24: Procesamiento de pagos mediante pasarela de tarjeta simulada (entorno educativo).
 */
public class ProcesarPagoTarjeta implements ProcesarPagoTarjetaUseCase {

    private final ProcesarPago procesarPago;

    public ProcesarPagoTarjeta(ProcesarPago procesarPago) {
        this.procesarPago = procesarPago;
    }

    @Override
    public Pago cobrarTarjetaPublica(String codigoPago, String numeroTarjeta, String titular,
                                     String vencimiento, String cvv) {
        return procesarPago.cobrarTarjetaSimuladaPublica(codigoPago, numeroTarjeta, titular, vencimiento, cvv);
    }
}
