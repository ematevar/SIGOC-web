package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Pago;

/**
 * CU-24: Procesamiento de pagos mediante pasarela de tarjeta simulada (entorno educativo).
 */
public interface ProcesarPagoTarjetaUseCase {
    Pago cobrarTarjetaPublica(String codigoPago, String numeroTarjeta, String titular,
                              String vencimiento, String cvv);
}
