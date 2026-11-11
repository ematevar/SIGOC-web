package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Unifica el acceso de dominio a PayPal Sandbox y la pasarela simulada de tarjeta.
 */
@Component
@Primary
public class PasarelaPagoGatewayAdapter implements PasarelaPagoPort {

    private final PayPalSandboxAdapter payPalSandboxAdapter;
    private final PasarelaSimuladaAdapter pasarelaSimuladaAdapter;

    public PasarelaPagoGatewayAdapter(PayPalSandboxAdapter payPalSandboxAdapter,
                                      PasarelaSimuladaAdapter pasarelaSimuladaAdapter) {
        this.payPalSandboxAdapter = payPalSandboxAdapter;
        this.pasarelaSimuladaAdapter = pasarelaSimuladaAdapter;
    }

    @Override
    public boolean paypalHabilitado() {
        return payPalSandboxAdapter.habilitado();
    }

    @Override
    public OrdenPayPal crearOrdenPayPal(BigDecimal montoUsd, String moneda, String referenciaComprobante,
                                        String descripcion, String returnUrl, String cancelUrl) {
        return payPalSandboxAdapter.crearOrden(montoUsd, moneda, referenciaComprobante, descripcion, returnUrl, cancelUrl);
    }

    @Override
    public ResultadoCapturaPayPal capturarOrdenPayPal(String idOrdenExterna) {
        return payPalSandboxAdapter.capturarOrden(idOrdenExterna);
    }

    @Override
    public ResultadoTarjetaSimulada cobrarTarjetaSimulada(String numeroTarjeta, String titular,
                                                          String vencimiento, String cvv, BigDecimal montoPen) {
        return pasarelaSimuladaAdapter.cobrar(numeroTarjeta, titular, vencimiento, cvv, montoPen);
    }
}
