package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoExternoUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoExternaPort;
import com.sigoc.sigoc_web.domain.ports.out.TipoCambioPort;
import com.sigoc.sigoc_web.domain.ports.out.TransaccionPort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;

/**
 * CU-20: Procesamiento de pagos en línea mediante pasarela externa agnóstica al proveedor (QA-08).
 * Utiliza dos transacciones cortas con estado intermedio PENDIENTE_CAPTURA para ejecutar
 * la llamada HTTP externa fuera de cualquier bloqueo pesimista de base de datos.
 */
public class ProcesarPagoExterno implements ProcesarPagoExternoUseCase {

    private static final BigDecimal TOLERANCIA_USD = new BigDecimal("0.02");
    public static final String ESTADO_PENDIENTE_CAPTURA = "PENDIENTE_CAPTURA";

    private final PagoPort pagoPort;
    private final ObraPort obraPort;
    private final ValorizacionPort valorizacionPort;
    private final ComprobantePort comprobantePort;
    private final IngresoPort ingresoPort;
    private final TipoCambioPort tipoCambioPort;
    private final PasarelaPagoExternaPort pasarelaExternaPort;
    private final TransaccionPort transaccionPort;
    private final Clock clock;
    private final int horasVigenciaEnlace;
    private final AuditoriaEventoPort auditoriaPort;

    public ProcesarPagoExterno(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                               ComprobantePort comprobantePort, IngresoPort ingresoPort,
                               TipoCambioPort tipoCambioPort, PasarelaPagoExternaPort pasarelaExternaPort,
                               TransaccionPort transaccionPort, Clock clock, int horasVigenciaEnlace,
                               AuditoriaEventoPort auditoriaPort) {
        this.pagoPort = pagoPort;
        this.obraPort = obraPort;
        this.valorizacionPort = valorizacionPort;
        this.comprobantePort = comprobantePort;
        this.ingresoPort = ingresoPort;
        this.tipoCambioPort = tipoCambioPort;
        this.pasarelaExternaPort = pasarelaExternaPort;
        this.transaccionPort = transaccionPort != null ? transaccionPort : TransaccionPort.directo();
        this.clock = clock;
        this.horasVigenciaEnlace = Math.max(1, horasVigenciaEnlace);
        this.auditoriaPort = auditoriaPort;
    }

    private record ReservaCaptura(
            Integer idComprobante,
            Integer idValorizacion,
            Integer idObra,
            String serieNumero,
            BigDecimal totalPen,
            BigDecimal montoUsdEsperado,
            BigDecimal tipoCambio
    ) {
    }

    @Override
    public OrdenPagoExterno crearOrdenPublica(String codigoPago, String returnUrl, String cancelUrl) {
        if (pasarelaExternaPort == null) {
            throw new ReglaNegocioException("Pasarela externa de pagos no disponible.");
        }
        GestionarCobranza.ContextoEnlace ctx = GestionarCobranza.validarEnlacePagable(
                comprobantePort, valorizacionPort, obraPort, pagoPort, clock,
                horasVigenciaEnlace, codigoPago, false);
        BigDecimal tc = GestionarCobranza.obtenerTipoCambioUsdPen(tipoCambioPort);
        BigDecimal montoUsd = ctx.comprobante().getTotal().divide(tc, 2, RoundingMode.HALF_UP);
        PasarelaPagoExternaPort.OrdenExterna orden = pasarelaExternaPort.crearOrden(
                montoUsd, "USD", ctx.comprobante().getSerieNumero(),
                "SIGOC " + ctx.comprobante().getSerieNumero() + " - " + ctx.obra().getNombre(),
                returnUrl, cancelUrl);
        return new OrdenPagoExterno(orden.idOrdenExterna(), orden.approvalUrl(),
                ctx.comprobante().getTotal(), montoUsd, tc);
    }

    @Override
    public Pago capturarPublico(String codigoPago, String idOrdenExterna) {
        if (pasarelaExternaPort == null) {
            throw new ReglaNegocioException("Pasarela externa de pagos no disponible.");
        }
        String proveedor = pasarelaExternaPort.nombreProveedor();

        // Fase 1 (TX corta): validar, reservar comprobante en PENDIENTE_CAPTURA y liberar bloqueo pesimista
        ReservaCaptura reserva = transaccionPort.ejecutarNueva(() -> {
            GestionarCobranza.ContextoEnlace ctx = GestionarCobranza.validarEnlacePagable(
                    comprobantePort, valorizacionPort, obraPort, pagoPort, clock,
                    horasVigenciaEnlace, codigoPago, true);
            Comprobante comp = ctx.comprobante();
            comp.setEstado(ESTADO_PENDIENTE_CAPTURA);
            comprobantePort.guardar(comp);
            BigDecimal tc = GestionarCobranza.obtenerTipoCambioUsdPen(tipoCambioPort);
            BigDecimal montoUsdEsperado = comp.getTotal().divide(tc, 2, RoundingMode.HALF_UP);
            return new ReservaCaptura(
                    comp.getIdComprobante(),
                    ctx.valorizacion().getIdValorizacion(),
                    ctx.obra().getIdObra(),
                    comp.getSerieNumero(),
                    comp.getTotal(),
                    montoUsdEsperado,
                    tc
            );
        });

        // Fase 2 (sin TX ni lock en BD): llamada HTTP hacia la pasarela externa
        PasarelaPagoExternaPort.ResultadoCaptura cap;
        try {
            cap = pasarelaExternaPort.capturarOrden(idOrdenExterna);
        } catch (RuntimeException ex) {
            revertirReservaPorFalloTecnico(reserva);
            throw ex;
        }

        // Fase 3 (TX corta): consolidar pago aprobado o revertir estado a EMITIDO registrando rechazo
        return transaccionPort.ejecutarNuevaSinRollbackPagoRechazado(() -> {
            Comprobante comp = comprobantePort.findByIdParaActualizar(reserva.idComprobante())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante no existe."));
            Valorizacion val = valorizacionPort.findByIdParaActualizar(reserva.idValorizacion())
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
            Obra obra = obraPort.findById(reserva.idObra())
                    .orElseThrow(() -> new RecursoNoEncontradoException("La obra asociada no existe."));

            if (cap.idCapturaExterna() != null && pagoPort.findByIdCapturaExterna(cap.idCapturaExterna()).isPresent()) {
                comp.setEstado("EMITIDO");
                comprobantePort.guardar(comp);
                throw new ReglaNegocioException("Esta operación de pago externo ya fue registrada anteriormente.");
            }

            boolean monedaOk = "USD".equalsIgnoreCase(cap.moneda());
            boolean montoOk = cap.montoCapturado() != null
                    && cap.montoCapturado().subtract(reserva.montoUsdEsperado()).abs().compareTo(TOLERANCIA_USD) <= 0;

            if (!cap.aprobado() || !monedaOk || !montoOk) {
                comp.setEstado("EMITIDO");
                comprobantePort.guardar(comp);
                String motivo = !cap.aprobado() ? cap.motivo()
                        : "El importe capturado (" + cap.montoCapturado() + " " + cap.moneda()
                        + ") no coincide con el esperado (" + reserva.montoUsdEsperado() + " USD).";
                GestionarCobranza.registrarIntentoRechazado(pagoPort, clock, obra.getIdObra(), val.getIdValorizacion(),
                        comp.getIdComprobante(), reserva.totalPen(), proveedor, proveedor, idOrdenExterna,
                        cap.idCapturaExterna(), reserva.montoUsdEsperado(), "USD", reserva.tipoCambio(),
                        null, null, motivo);
                auditar("PAGO_EXTERNO_RECHAZADO", String.valueOf(comp.getIdComprobante()), "RECHAZADO", motivo);
                throw new PagoRechazadoException(
                        GestionarCobranza.mensajeTrasRechazo(pagoPort, clock, comp, horasVigenciaEnlace, motivo));
            }

            String codigoOp = "EXT-" + cap.idCapturaExterna();
            Pago aprobado = GestionarCobranza.persistirPagoAprobado(
                    pagoPort, ingresoPort, comprobantePort, valorizacionPort, clock,
                    obra, val, comp, reserva.totalPen(), proveedor, codigoOp,
                    proveedor, idOrdenExterna, cap.idCapturaExterna(),
                    cap.montoCapturado(), "USD", reserva.tipoCambio(), null, null,
                    "Cobro externo " + reserva.serieNumero() + " (" + cap.idCapturaExterna() + ")");
            auditar("PAGO_EXTERNO_APROBADO", String.valueOf(aprobado.getIdPago()), "EXITO",
                    "Comprobante " + reserva.serieNumero() + " captura " + cap.idCapturaExterna());
            return aprobado;
        });
    }

    private void revertirReservaPorFalloTecnico(ReservaCaptura reserva) {
        transaccionPort.ejecutarNueva(() -> {
            comprobantePort.findByIdParaActualizar(reserva.idComprobante()).ifPresent(c -> {
                if (ESTADO_PENDIENTE_CAPTURA.equalsIgnoreCase(c.getEstado())) {
                    c.setEstado("EMITIDO");
                    comprobantePort.guardar(c);
                }
            });
            return null;
        });
    }

    private void auditar(String accion, String idEntidad, String resultado, String detalle) {
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, null, accion, "pago", idEntidad, resultado, detalle));
        }
    }
}
