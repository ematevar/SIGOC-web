package com.sigoc.sigoc_web.domain.ports.out;

import java.math.BigDecimal;

/**
 * Puerto de salida agnóstico al proveedor para pasarelas de pago externas (CU-20, QA-08).
 */
public interface PasarelaPagoExternaPort {

    boolean estaHabilitada();

    String nombreProveedor();

    OrdenExterna crearOrden(BigDecimal monto, String moneda, String referenciaComprobante,
                            String descripcion, String returnUrl, String cancelUrl);

    ResultadoCaptura capturarOrden(String idOrdenExterna);

    record OrdenExterna(String idOrdenExterna, String estado, String approvalUrl) {
    }

    record ResultadoCaptura(boolean aprobado, String idCapturaExterna,
                            BigDecimal montoCapturado, String moneda, String motivo) {
    }
}
