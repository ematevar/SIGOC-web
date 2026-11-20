package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.ControlIntentosPago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Consulta pública de comprobante de valorización mediante su token de 72 horas (sin login).
 */
public class ConsultarEnlacePagoPublico {

    private final ComprobantePort comprobantePort;
    private final ValorizacionPort valorizacionPort;
    private final ObraPort obraPort;
    private final ClientePort clientePort;
    private final TipoCambioPort tipoCambioPort;
    private final PasarelaPagoPort pasarelaPagoPort;
    private final Clock clock;
    private final PagoPort pagoPort;
    private final int horasVigenciaEnlace;

    public ConsultarEnlacePagoPublico(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort,
                                      ObraPort obraPort, ClientePort clientePort,
                                      TipoCambioPort tipoCambioPort, PasarelaPagoPort pasarelaPagoPort, Clock clock) {
        this(comprobantePort, valorizacionPort, obraPort, clientePort, tipoCambioPort, pasarelaPagoPort, clock,
                null, 72);
    }

    public ConsultarEnlacePagoPublico(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort,
                                      ObraPort obraPort, ClientePort clientePort,
                                      TipoCambioPort tipoCambioPort, PasarelaPagoPort pasarelaPagoPort, Clock clock,
                                      PagoPort pagoPort, int horasVigenciaEnlace) {
        this.comprobantePort = comprobantePort;
        this.valorizacionPort = valorizacionPort;
        this.obraPort = obraPort;
        this.clientePort = clientePort;
        this.tipoCambioPort = tipoCambioPort;
        this.pasarelaPagoPort = pasarelaPagoPort;
        this.clock = clock;
        this.pagoPort = pagoPort;
        this.horasVigenciaEnlace = horasVigenciaEnlace;
    }

    public DetalleEnlacePublico ejecutar(String codigoPago) {
        Comprobante comp = comprobantePort.findByCodigoPago(codigoPago == null ? "" : codigoPago.trim())
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El enlace de pago no existe o ya no está disponible."));

        Valorizacion val = valorizacionPort.findById(comp.getIdValorizacion()).orElse(null);
        Obra obra = val != null ? obraPort.findById(val.getIdObra()).orElse(null) : null;
        Cliente cliente = obra != null && obra.getIdCliente() != null
                ? clientePort.findById(obra.getIdCliente()).orElse(null) : null;

        BigDecimal tc = tipoCambioPort.findByPar("USD", "PEN")
                .map(TipoCambio::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .orElse(GestionarTipoCambio.TIPO_CAMBIO_DEFECTO);

        BigDecimal montoUsd = comp.getTotal().divide(tc, 2, RoundingMode.HALF_UP);
        LocalDateTime ahora = LocalDateTime.now(clock);
        boolean pagado = "PAGADO".equalsIgnoreCase(comp.getEstado());
        boolean expirado = !pagado && !comp.esEnlaceVigente(ahora);
        boolean paypalHabilitado = pasarelaPagoPort != null && pasarelaPagoPort.paypalHabilitado();

        ControlIntentosPago.Estado intentos = new ControlIntentosPago.Estado(
                false, null, ControlIntentosPago.MAX_INTENTOS_RECHAZADOS);
        if (pagoPort != null && !pagado && !expirado) {
            LocalDateTime desde = comp.getVenceEl() != null ? comp.getVenceEl().minusHours(horasVigenciaEnlace) : null;
            intentos = ControlIntentosPago.evaluar(
                    pagoPort.findFechasRechazosDesde(comp.getIdComprobante(), desde), ahora);
        }

        return new DetalleEnlacePublico(
                comp.getSerieNumero(),
                comp.getFechaEmision() != null ? comp.getFechaEmision().toString() : "-",
                comp.getSubtotal(),
                comp.getIgv(),
                comp.getTotal(),
                montoUsd,
                tc,
                comp.getEstado(),
                comp.getVenceEl(),
                expirado,
                pagado,
                paypalHabilitado,
                obra != null ? obra.getNombre() : "-",
                val != null ? val.getNumero() : 1,
                val != null ? val.getPeriodo() : "-",
                val != null ? val.getPorcentajeAvancePeriodo() : BigDecimal.ZERO,
                cliente != null ? cliente.getNombreRazonSocial()
                        : (obra != null && obra.getCliente() != null ? obra.getCliente() : "-"),
                intentos.bloqueado(),
                intentos.bloqueadoHasta(),
                intentos.intentosRestantes()
        );
    }

    public record DetalleEnlacePublico(
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
            boolean paypalHabilitado,
            String nombreObra,
            Integer numeroValorizacion,
            String periodoValorizacion,
            BigDecimal porcentajeAvancePeriodo,
            String clienteNombre,
            boolean bloqueado,
            LocalDateTime bloqueadoHasta,
            int intentosRestantes
    ) {
    }
}
