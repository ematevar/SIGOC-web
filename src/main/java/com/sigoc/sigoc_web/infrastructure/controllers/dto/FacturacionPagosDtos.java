package com.sigoc.sigoc_web.infrastructure.controllers.dto;

import com.sigoc.sigoc_web.application.usecases.RegistrarValorizacion.ResumenCalculoValorizacion;
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
            @NotNull LocalDate periodoInicio,
            @NotNull LocalDate periodoFin,
            LocalDate fechaEmision
    ) {
    }

    public record CalculoValorizacionResponse(
            Integer idObra,
            String nombreObra,
            int numeroSiguiente,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            BigDecimal avanceAcumulado,
            BigDecimal avanceYaValorizado,
            BigDecimal porcentajeAvancePeriodo,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total
    ) {
        public static CalculoValorizacionResponse de(ResumenCalculoValorizacion r) {
            return new CalculoValorizacionResponse(r.idObra(), r.nombreObra(), r.numeroSiguiente(),
                    r.periodoInicio(), r.periodoFin(), r.avanceAcumulado(), r.avanceYaValorizado(),
                    r.porcentajeAvancePeriodo(), r.subtotal(), r.igv(), r.total());
        }
    }

    public record ValorizacionResponse(
            Integer idValorizacion,
            Integer idObra,
            Integer numero,
            String periodo,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            LocalDate fechaEmision,
            BigDecimal avanceAcumulado,
            BigDecimal porcentajeAvancePeriodo,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            BigDecimal montoValorizado,
            String estado
    ) {
        public static ValorizacionResponse de(Valorizacion v) {
            return new ValorizacionResponse(
                    v.getIdValorizacion(), v.getIdObra(), v.getNumero(), v.getPeriodo(),
                    v.getPeriodoInicio(), v.getPeriodoFin(), v.getFechaEmision(),
                    v.getAvanceAcumulado(), v.getPorcentajeAvancePeriodo(),
                    v.getSubtotal(), v.getIgv(), v.getTotal(), v.getMontoValorizado(), v.getEstado());
        }
    }

    public record EmitirComprobanteRequest(
            String tipoComprobante,
            String serieNumero
    ) {
    }

    public record ComprobanteResponse(
            Integer idComprobante,
            Integer idValorizacion,
            String tipoComprobante,
            String serie,
            Integer numero,
            String serieNumero,
            LocalDate fechaEmision,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            String codigoPago,
            LocalDateTime venceEl,
            String estado
    ) {
        public static ComprobanteResponse de(Comprobante c) {
            return new ComprobanteResponse(
                    c.getIdComprobante(), c.getIdValorizacion(), c.getTipoComprobante(),
                    c.getSerie(), c.getNumero(), c.getSerieNumero(), c.getFechaEmision(),
                    c.getSubtotal(), c.getIgv(), c.getTotal(), c.getCodigoPago(),
                    c.getVenceEl(), c.getEstado());
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
            String pasarela,
            String idOrdenExterna,
            String idCapturaExterna,
            BigDecimal montoCobro,
            String monedaCobro,
            BigDecimal tipoCambio,
            String marcaTarjeta,
            String ultimos4,
            String motivo,
            LocalDateTime fechaPago,
            String estado
    ) {
        public static PagoResponse de(Pago p) {
            return new PagoResponse(
                    p.getIdPago(), p.getIdObra(), p.getIdValorizacion(), p.getIdComprobante(),
                    p.getIdIngresoGenerado(), p.getMonto(), p.getMetodoPago(), p.getCodigoOperacion(),
                    p.getPasarela(), p.getIdOrdenExterna(), p.getIdCapturaExterna(),
                    p.getMontoCobro(), p.getMonedaCobro(), p.getTipoCambio(),
                    p.getMarcaTarjeta(), p.getUltimos4(), p.getMotivo(),
                    p.getFechaPago(), p.getEstado());
        }
    }
}
