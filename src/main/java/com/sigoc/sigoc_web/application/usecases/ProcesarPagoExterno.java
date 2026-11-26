package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoExternoUseCase;

/**
 * CU-20: Procesamiento de pagos en línea mediante pasarela externa desacoplada.
 */
public class ProcesarPagoExterno implements ProcesarPagoExternoUseCase {

    private final ProcesarPago procesarPago;

    public ProcesarPagoExterno(ProcesarPago procesarPago) {
        this.procesarPago = procesarPago;
    }

    @Override
    public OrdenPagoExterno crearOrdenPublica(String codigoPago, String returnUrl, String cancelUrl) {
        ProcesarPago.OrdenPayPalPublica orden = procesarPago.crearOrdenPayPalPublica(codigoPago, returnUrl, cancelUrl);
        return new OrdenPagoExterno(
                orden.idOrdenExterna(),
                orden.approvalUrl(),
                orden.montoPen(),
                orden.montoUsd(),
                orden.tipoCambio()
        );
    }

    @Override
    public Pago capturarPublico(String codigoPago, String idOrdenExterna) {
        return procesarPago.capturarPayPalPublico(codigoPago, idOrdenExterna);
    }
}
