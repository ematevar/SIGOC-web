package com.sigoc.sigoc_web.domain.ports.out;

import java.math.BigDecimal;

/**
 * Puerto de salida para cobro mediante pasarela de tarjeta simulada (CU-24, ISP).
 */
public interface PasarelaTarjetaPort {

    ResultadoTarjeta cobrarTarjeta(String numeroTarjeta, String titular,
                                   String vencimiento, String cvv, BigDecimal montoPen);

    record ResultadoTarjeta(boolean aprobado, String codigoOperacion,
                            String marcaTarjeta, String ultimos4, String motivo) {
    }
}
