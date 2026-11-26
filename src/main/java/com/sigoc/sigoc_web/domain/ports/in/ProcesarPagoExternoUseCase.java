package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Pago;

import java.math.BigDecimal;

/**
 * CU-20: Procesamiento de pagos en línea mediante pasarela externa desacoplada.
 */
public interface ProcesarPagoExternoUseCase {

    record OrdenPagoExterno(
            String idOrdenExterna,
            String approvalUrl,
            BigDecimal montoPen,
            BigDecimal montoUsd,
            BigDecimal tipoCambio
    ) {
    }

    OrdenPagoExterno crearOrdenPublica(String codigoPago, String returnUrl, String cancelUrl);

    Pago capturarPublico(String codigoPago, String idOrdenExterna);
}
