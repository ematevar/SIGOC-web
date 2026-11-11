package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FacturacionPagosDtos {
    private FacturacionPagosDtos() {
    }

    public record GuardarClienteRequest(
            @NotBlank String tipoDocumento,
            @NotBlank String numeroDocumento,
            @NotBlank String nombreRazonSocial,
            @NotBlank String telefono,
            String correo,
            String direccion
    ) {
    }

    public record ClienteResponse(
            Integer idCliente,
            String tipoDocumento,
            String numeroDocumento,
            String nombreRazonSocial,
            String telefono,
            String correo,
            String direccion
    ) {
        public static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getIdCliente(), c.getTipoDocumento(), c.getNumeroDocumento(),
                    c.getNombreRazonSocial(), c.getTelefono(), c.getCorreo(), c.getDireccion());
        }
    }

    public record RegistrarValorizacionRequest(
            @NotNull Integer idObra,
            @NotBlank String periodo,
            LocalDate fechaEmision,
            @NotNull @DecimalMin("0.01") BigDecimal porcentajeAvancePeriodo,
            @NotNull @DecimalMin("0.01") BigDecimal montoValorizado
    ) {
    }

    public record ValorizacionResponse(
            Integer idValorizacion,
            Integer idObra,
            String periodo,
            LocalDate fechaEmision,
            BigDecimal porcentajeAvancePeriodo,
            BigDecimal montoValorizado,
            String estado
    ) {
        public static ValorizacionResponse de(Valorizacion v) {
            return new ValorizacionResponse(v.getIdValorizacion(), v.getIdObra(), v.getPeriodo(),
                    v.getFechaEmision(), v.getPorcentajeAvancePeriodo(), v.getMontoValorizado(), v.getEstado());
        }
    }

    public record EmitirComprobanteRequest(
            String tipoComprobante,
            @NotBlank String serieNumero
    ) {
    }

    public record ComprobanteResponse(
            Integer idComprobante,
            Integer idValorizacion,
            String tipoComprobante,
            String serieNumero,
            LocalDate fechaEmision,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            String estado
    ) {
        public static ComprobanteResponse de(Comprobante c) {
            return new ComprobanteResponse(c.getIdComprobante(), c.getIdValorizacion(), c.getTipoComprobante(),
                    c.getSerieNumero(), c.getFechaEmision(), c.getSubtotal(), c.getIgv(), c.getTotal(), c.getEstado());
        }
    }

    public record ProcesarPagoRequest(
            @NotNull Integer idObra,
            Integer idValorizacion,
            Integer idComprobante,
            @NotNull @DecimalMin("0.01") BigDecimal monto,
            String metodoPago,
            String codigoOperacion
    ) {
    }

    public record PagoResponse(
            Integer idPago,
            Integer idObra,
            Integer idValorizacion,
            Integer idComprobante,
            Integer idIngresoGenerado,
            BigDecimal monto,
            String metodoPago,
            String codigoOperacion,
            LocalDateTime fechaPago,
            String estado
    ) {
        public static PagoResponse de(Pago p) {
            return new PagoResponse(p.getIdPago(), p.getIdObra(), p.getIdValorizacion(), p.getIdComprobante(),
                    p.getIdIngresoGenerado(), p.getMonto(), p.getMetodoPago(), p.getCodigoOperacion(),
                    p.getFechaPago(), p.getEstado());
        }
    }
}
