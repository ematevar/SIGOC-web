package com.sigoc.sigoc_web.domain.ports.out;

import java.math.BigDecimal;

/**
 * Puerto de salida hacia pasarelas de pago externas o simuladas (RF-20).
 */
public interface PasarelaPagoPort {

    boolean paypalHabilitado();

    OrdenPayPal crearOrdenPayPal(BigDecimal montoUsd, String moneda, String referenciaComprobante,
                                 String descripcion, String returnUrl, String cancelUrl);

    ResultadoCapturaPayPal capturarOrdenPayPal(String idOrdenExterna);

    ResultadoTarjetaSimulada cobrarTarjetaSimulada(String numeroTarjeta, String titular,
                                                   String vencimiento, String cvv, BigDecimal montoPen);

    record OrdenPayPal(String idOrdenExterna, String estado, String approvalUrl) {
    }

    record ResultadoCapturaPayPal(boolean aprobado, String idCapturaExterna,
                                  BigDecimal montoCapturado, String moneda, String motivo) {
    }

    record ResultadoTarjetaSimulada(boolean aprobado, String codigoOperacion,
                                    String marcaTarjeta, String ultimos4, String motivo) {
    }
}
