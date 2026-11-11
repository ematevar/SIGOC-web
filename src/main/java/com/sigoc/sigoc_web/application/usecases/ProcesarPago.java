package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.OrdenPayPal;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.ResultadoCapturaPayPal;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.ResultadoTarjetaSimulada;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * RF-20 / C02-C05: Procesa cobros internos y cobros públicos en línea (PayPal Sandbox o tarjeta simulada),
 * validando integridad y vigencia en servidor y generando automáticamente el Ingreso vinculado 1:1.
 */
public class ProcesarPago {

    private static final BigDecimal TOLERANCIA_USD = new BigDecimal("0.02");

    private final PagoPort pagoPort;
    private final ObraPort obraPort;
    private final ValorizacionPort valorizacionPort;
    private final ComprobantePort comprobantePort;
    private final IngresoPort ingresoPort;
    private final TipoCambioPort tipoCambioPort;
    private final PasarelaPagoPort pasarelaPagoPort;
    private final Clock clock;

    public ProcesarPago(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                        ComprobantePort comprobantePort, IngresoPort ingresoPort, Clock clock) {
        this(pagoPort, obraPort, valorizacionPort, comprobantePort, ingresoPort, null, null, clock);
    }

    public ProcesarPago(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                        ComprobantePort comprobantePort, IngresoPort ingresoPort,
                        TipoCambioPort tipoCambioPort, PasarelaPagoPort pasarelaPagoPort, Clock clock) {
        this.pagoPort = pagoPort;
        this.obraPort = obraPort;
        this.valorizacionPort = valorizacionPort;
        this.comprobantePort = comprobantePort;
        this.ingresoPort = ingresoPort;
        this.tipoCambioPort = tipoCambioPort;
        this.pasarelaPagoPort = pasarelaPagoPort;
        this.clock = clock;
    }

    /**
     * Registro interno de pago por Administrador o Contador (restringido al total exacto del comprobante/valorización).
     */
    public Pago ejecutar(UsuarioSesion actor, Integer idObra, Integer idValorizacion,
                         Integer idComprobante, BigDecimal monto, String metodoPago, String codigoOperacion) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida.");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o se encuentra cerrada.");
        }

        ValidadorSigoc.validarMontoPositivo(monto, "pago");
        BigDecimal montoEscala = monto.setScale(2, RoundingMode.HALF_UP);

        String metodo = (metodoPago == null || metodoPago.isBlank())
                ? "PASARELA_SIMULADA" : metodoPago.trim().toUpperCase();
        String codigoOp = resolverCodigoOperacion(codigoOperacion, "SIM-");

        Comprobante comp = null;
        Valorizacion val = null;
        String conceptoIngreso = "Pago aprobado (" + metodo + " - Op: " + codigoOp + ")";

        if (idComprobante != null && idComprobante > 0) {
            comp = comprobantePort.findByIdParaActualizar(idComprobante)
                    .filter(Comprobante::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante asociado no existe."));
            if (!comp.esPagable()) {
                throw new ReglaNegocioException("El comprobante no está en estado pagable (actual: " + comp.getEstado() + ").");
            }
            if (idValorizacion != null && idValorizacion > 0 && !Objects.equals(comp.getIdValorizacion(), idValorizacion)) {
                throw new ReglaNegocioException("El comprobante no corresponde a la valorización indicada.");
            }
            val = valorizacionPort.findByIdParaActualizar(comp.getIdValorizacion())
                    .filter(Valorizacion::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada al comprobante no existe."));
            if (!Objects.equals(val.getIdObra(), idObra)) {
                throw new ReglaNegocioException("El comprobante pertenece a otra obra.");
            }
            if (val.estaPagada()) {
                throw new ReglaNegocioException("La valorización asociada ya se encuentra pagada.");
            }
            if (montoEscala.compareTo(comp.getTotal().setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new ReglaNegocioException(
                        "El monto del pago (S/ " + montoEscala + ") debe coincidir exactamente con el comprobante (S/ "
                                + comp.getTotal() + ").");
            }
            conceptoIngreso = "Pago Comprobante " + comp.getSerieNumero() + " (" + codigoOp + ")";
        } else if (idValorizacion != null && idValorizacion > 0) {
            val = valorizacionPort.findByIdParaActualizar(idValorizacion)
                    .filter(Valorizacion::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
            if (!Objects.equals(val.getIdObra(), idObra)) {
                throw new ReglaNegocioException("La valorización no pertenece a la obra seleccionada.");
            }
            if (val.estaPagada()) {
                throw new ReglaNegocioException("Esta valorización ya se encuentra pagada.");
            }
            if (montoEscala.compareTo(val.getMontoValorizado().setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new ReglaNegocioException(
                        "El monto debe coincidir con la valorización (S/ " + val.getMontoValorizado() + ").");
            }
            conceptoIngreso = "Pago de Valorización N° " + val.getNumero() + " (" + codigoOp + ")";
        }

        return persistirPagoAprobado(obra, val, comp, montoEscala, metodo, codigoOp,
                "PASARELA_SIMULADA", null, null, montoEscala, "PEN", null, null, null, conceptoIngreso);
    }

    /**
     * Inicia una orden de pago en PayPal Sandbox desde el enlace público de 72 horas.
     */
    public OrdenPayPalPublica crearOrdenPayPalPublica(String codigoPago, String returnUrl, String cancelUrl) {
        if (pasarelaPagoPort == null) {
            throw new ReglaNegocioException("Pasarela PayPal no disponible.");
        }
        ContextoEnlace ctx = validarEnlacePagable(codigoPago, false);
        BigDecimal tc = obtenerTipoCambioUsdPen();
        BigDecimal montoUsd = ctx.comprobante().getTotal().divide(tc, 2, RoundingMode.HALF_UP);
        OrdenPayPal orden = pasarelaPagoPort.crearOrdenPayPal(
                montoUsd, "USD", ctx.comprobante().getSerieNumero(),
                "SIGOC " + ctx.comprobante().getSerieNumero() + " - " + ctx.obra().getNombre(),
                returnUrl, cancelUrl);
        return new OrdenPayPalPublica(orden.idOrdenExterna(), orden.approvalUrl(),
                ctx.comprobante().getTotal(), montoUsd, tc);
    }

    /**
     * Captura una orden PayPal Sandbox aprobada por el cliente e ingresa los fondos con verificación de integridad.
     */
    public Pago capturarPayPalPublico(String codigoPago, String idOrdenExterna) {
        if (pasarelaPagoPort == null) {
            throw new ReglaNegocioException("Pasarela PayPal no disponible.");
        }
        ContextoEnlace ctx = validarEnlacePagable(codigoPago, true);
        Comprobante comp = ctx.comprobante();
        Valorizacion val = ctx.valorizacion();
        Obra obra = ctx.obra();

        BigDecimal tc = obtenerTipoCambioUsdPen();
        BigDecimal montoUsdEsperado = comp.getTotal().divide(tc, 2, RoundingMode.HALF_UP);

        ResultadoCapturaPayPal cap = pasarelaPagoPort.capturarOrdenPayPal(idOrdenExterna);
        if (cap.idCapturaExterna() != null && pagoPort.findByIdCapturaExterna(cap.idCapturaExterna()).isPresent()) {
            throw new ReglaNegocioException("Esta operación de PayPal ya fue registrada anteriormente.");
        }

        boolean monedaOk = "USD".equalsIgnoreCase(cap.moneda());
        boolean montoOk = cap.montoCapturado() != null
                && cap.montoCapturado().subtract(montoUsdEsperado).abs().compareTo(TOLERANCIA_USD) <= 0;

        if (!cap.aprobado() || !monedaOk || !montoOk) {
            String motivo = !cap.aprobado() ? cap.motivo()
                    : "El importe capturado en PayPal (" + cap.montoCapturado() + " " + cap.moneda()
                    + ") no coincide con el esperado (" + montoUsdEsperado + " USD).";
            registrarIntentoRechazado(obra.getIdObra(), val.getIdValorizacion(), comp.getIdComprobante(),
                    comp.getTotal(), "PAYPAL_SANDBOX", "PAYPAL_SANDBOX", idOrdenExterna,
                    cap.idCapturaExterna(), montoUsdEsperado, "USD", tc, null, null, motivo);
            throw new PagoRechazadoException(motivo);
        }

        String codigoOp = "PP-" + cap.idCapturaExterna();
        return persistirPagoAprobado(obra, val, comp, comp.getTotal(), "PAYPAL_SANDBOX", codigoOp,
                "PAYPAL_SANDBOX", idOrdenExterna, cap.idCapturaExterna(),
                cap.montoCapturado(), "USD", tc, null, null,
                "Cobro PayPal " + comp.getSerieNumero() + " (" + cap.idCapturaExterna() + ")");
    }

    /**
     * Cobra en línea mediante tarjeta simulada (Visa/Mastercard) desde el enlace público de 72 horas.
     */
    public Pago cobrarTarjetaSimuladaPublica(String codigoPago, String numeroTarjeta,
                                             String titular, String vencimiento, String cvv) {
        if (pasarelaPagoPort == null) {
            throw new ReglaNegocioException("Pasarela simulada no disponible.");
        }
        ContextoEnlace ctx = validarEnlacePagable(codigoPago, true);
        Comprobante comp = ctx.comprobante();
        Valorizacion val = ctx.valorizacion();
        Obra obra = ctx.obra();

        ResultadoTarjetaSimulada res = pasarelaPagoPort.cobrarTarjetaSimulada(
                numeroTarjeta, titular, vencimiento, cvv, comp.getTotal());

        if (!res.aprobado()) {
            registrarIntentoRechazado(obra.getIdObra(), val.getIdValorizacion(), comp.getIdComprobante(),
                    comp.getTotal(), "TARJETA_SIMULADA", "PASARELA_SIMULADA", null, null,
                    comp.getTotal(), "PEN", null, res.marcaTarjeta(), res.ultimos4(), res.motivo());
            throw new PagoRechazadoException(res.motivo());
        }

        return persistirPagoAprobado(obra, val, comp, comp.getTotal(), "TARJETA_SIMULADA", res.codigoOperacion(),
                "PASARELA_SIMULADA", null, null, comp.getTotal(), "PEN", null,
                res.marcaTarjeta(), res.ultimos4(),
                "Cobro tarjeta " + res.marcaTarjeta() + " *" + res.ultimos4() + " - " + comp.getSerieNumero());
    }

    private ContextoEnlace validarEnlacePagable(String codigoPago, boolean bloquear) {
        if (codigoPago == null || codigoPago.isBlank()) {
            throw new RecursoNoEncontradoException("Enlace de pago inválido.");
        }
        Comprobante comp = comprobantePort.findByCodigoPago(codigoPago.trim())
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El enlace de pago no existe o fue anulado."));
        if (bloquear) {
            comp = comprobantePort.findByIdParaActualizar(comp.getIdComprobante()).orElse(comp);
        }
        if (!comp.esPagable()) {
            throw new ReglaNegocioException("Este comprobante ya fue pagado o no está disponible para cobro.");
        }
        if (!comp.esEnlaceVigente(LocalDateTime.now(clock))) {
            throw new ReglaNegocioException("El enlace de pago ha expirado (vigencia 72h). Solicita su renovación al contador.");
        }
        Valorizacion val = (bloquear
                ? valorizacionPort.findByIdParaActualizar(comp.getIdValorizacion())
                : valorizacionPort.findById(comp.getIdValorizacion()))
                .filter(Valorizacion::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
        if (val.estaPagada()) {
            throw new ReglaNegocioException("La valorización ya fue pagada.");
        }
        Obra obra = obraPort.findById(val.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra asociada se encuentra cerrada.");
        }
        return new ContextoEnlace(comp, val, obra);
    }

    private BigDecimal obtenerTipoCambioUsdPen() {
        if (tipoCambioPort == null) {
            return GestionarTipoCambio.TIPO_CAMBIO_DEFECTO;
        }
        return tipoCambioPort.findByPar("USD", "PEN")
                .map(TipoCambio::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .orElse(GestionarTipoCambio.TIPO_CAMBIO_DEFECTO);
    }

    private void registrarIntentoRechazado(Integer idObra, Integer idValorizacion, Integer idComprobante,
                                           BigDecimal monto, String metodoPago, String pasarela,
                                           String idOrdenExterna, String idCapturaExterna,
                                           BigDecimal montoCobro, String monedaCobro, BigDecimal tc,
                                           String marca, String ultimos4, String motivo) {
        Pago intento = new Pago();
        intento.setIdObra(idObra);
        intento.setIdValorizacion(idValorizacion);
        intento.setIdComprobante(idComprobante);
        intento.setMonto(monto);
        intento.setMetodoPago(metodoPago);
        intento.setCodigoOperacion("REJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        intento.setPasarela(pasarela);
        intento.setIdOrdenExterna(idOrdenExterna);
        intento.setIdCapturaExterna(idCapturaExterna);
        intento.setMontoCobro(montoCobro);
        intento.setMonedaCobro(monedaCobro);
        intento.setTipoCambio(tc);
        intento.setMarcaTarjeta(marca);
        intento.setUltimos4(ultimos4);
        intento.setMotivo(motivo != null && motivo.length() > 250 ? motivo.substring(0, 250) : motivo);
        intento.setFechaPago(LocalDateTime.now(clock));
        intento.setEstado("RECHAZADO");
        intento.setActivo(true);
        pagoPort.guardar(intento);
    }

    private Pago persistirPagoAprobado(Obra obra, Valorizacion val, Comprobante comp,
                                       BigDecimal montoPen, String metodo, String codigoOp,
                                       String pasarela, String idOrdenExterna, String idCapturaExterna,
                                       BigDecimal montoCobro, String monedaCobro, BigDecimal tc,
                                       String marca, String ultimos4, String conceptoIngreso) {
        Pago pago = new Pago();
        pago.setIdObra(obra.getIdObra());
        pago.setIdValorizacion(val != null ? val.getIdValorizacion() : null);
        pago.setIdComprobante(comp != null ? comp.getIdComprobante() : null);
        pago.setMonto(montoPen);
        pago.setMetodoPago(metodo);
        pago.setCodigoOperacion(codigoOp);
        pago.setPasarela(pasarela);
        pago.setIdOrdenExterna(idOrdenExterna);
        pago.setIdCapturaExterna(idCapturaExterna);
        pago.setMontoCobro(montoCobro);
        pago.setMonedaCobro(monedaCobro);
        pago.setTipoCambio(tc);
        pago.setMarcaTarjeta(marca);
        pago.setUltimos4(ultimos4);
        pago.setMotivo("Aprobado");
        pago.setFechaPago(LocalDateTime.now(clock));
        pago.setEstado("APROBADO");
        pago.setActivo(true);
        Pago pagoGuardado = pagoPort.guardar(pago);

        Ingreso ing = new Ingreso();
        ing.setIdObra(obra.getIdObra());
        ing.setIdPago(pagoGuardado.getIdPago());
        ing.setFecha(LocalDate.now(clock));
        ing.setConcepto(conceptoIngreso.length() > 150 ? conceptoIngreso.substring(0, 150) : conceptoIngreso);
        ing.setMonto(montoPen);
        ing.setActivo(true);
        Ingreso ingresoGuardado = ingresoPort.guardar(ing);

        pagoGuardado.setIdIngresoGenerado(ingresoGuardado.getIdIngreso());
        pagoGuardado = pagoPort.guardar(pagoGuardado);

        if (comp != null) {
            comp.setEstado("PAGADO");
            comprobantePort.guardar(comp);
        }
        if (val != null) {
            val.setEstado("PAGADA");
            valorizacionPort.guardar(val);
        }
        return pagoGuardado;
    }

    private String resolverCodigoOperacion(String codigoOperacion, String prefijo) {
        if (codigoOperacion == null || codigoOperacion.isBlank()) {
            return prefijo + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        String norm = codigoOperacion.trim().toUpperCase();
        if (pagoPort.findByCodigoOperacion(norm).isPresent()) {
            throw new ReglaNegocioException("El código de operación " + norm + " ya fue registrado.");
        }
        return norm;
    }

    private record ContextoEnlace(Comprobante comprobante, Valorizacion valorizacion, Obra obra) {
    }

    public record OrdenPayPalPublica(
            String idOrdenExterna,
            String approvalUrl,
            BigDecimal montoPen,
            BigDecimal montoUsd,
            BigDecimal tipoCambio
    ) {
    }
}
