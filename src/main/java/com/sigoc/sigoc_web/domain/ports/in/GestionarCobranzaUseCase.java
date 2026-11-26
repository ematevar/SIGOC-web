package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CU-22: Gestión de cobranzas, registro interno de pagos y consulta pública del enlace de pago.
 */
public interface GestionarCobranzaUseCase {

    record DetalleEnlacePublico(
            String serieNumero,
            String fechaEmision,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal totalPen,
            BigDecimal montoUsdEstimado,
            BigDecimal tipoCambioUsdPen,
            String estado,
            LocalDateTime venceEl,
            boolean expirado,
            boolean pagado,
            boolean pasarelaExternaHabilitada,
            String nombreObra,
            Integer numeroValorizacion,
            String periodoValorizacion,
            BigDecimal porcentajeAvancePeriodo,
            String clienteNombre,
            boolean bloqueado,
            LocalDateTime bloqueadoHasta,
            int intentosRestantes
    ) {
        public boolean isPaypalHabilitado() {
            return pasarelaExternaHabilitada;
        }
    }

    List<Pago> listarPagos(UsuarioSesion actor, Integer idObra);

    Pago registrarPagoInterno(UsuarioSesion actor, Integer idObra, Integer idValorizacion,
                              Integer idComprobante, BigDecimal monto,
                              String metodoPago, String codigoOperacion);

    DetalleEnlacePublico consultarEnlacePublico(String codigoPago);
}
