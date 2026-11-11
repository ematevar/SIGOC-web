package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * RF-19: Emite comprobante interno de valorización (serie correlativa CV01) y genera/renueva
 * un enlace público de pago válido por 72 horas sin login interno.
 */
public class EmitirComprobante {

    private static final String SERIE_INTERNA = "CV01";
    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ComprobantePort comprobantePort;
    private final ValorizacionPort valorizacionPort;
    private final Clock clock;
    private final int horasVigenciaEnlace;

    public EmitirComprobante(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort, Clock clock) {
        this(comprobantePort, valorizacionPort, clock, 72);
    }

    public EmitirComprobante(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort,
                             Clock clock, int horasVigenciaEnlace) {
        this.comprobantePort = comprobantePort;
        this.valorizacionPort = valorizacionPort;
        this.clock = clock;
        this.horasVigenciaEnlace = Math.max(1, horasVigenciaEnlace);
    }

    public Comprobante ejecutar(UsuarioSesion actor, Integer idValorizacion, String tipoComprobante, String serieNumero) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        if (idValorizacion == null || idValorizacion <= 0) {
            throw new ReglaNegocioException("Selecciona una valorización válida.");
        }
        Valorizacion val = valorizacionPort.findByIdParaActualizar(idValorizacion)
                .filter(Valorizacion::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("La valorización indicada no existe."));

        if (!val.puedeFacturarse()) {
            throw new ReglaNegocioException(
                    "Solo se puede emitir comprobante para una valorización en estado PENDIENTE (actual: " + val.getEstado() + ").");
        }
        if (comprobantePort.findActivoByValorizacion(idValorizacion).isPresent()) {
            throw new ReglaNegocioException("La valorización ya tiene un comprobante activo emitido.");
        }

        String serie;
        int numero;
        String serieNorm;
        if (serieNumero == null || serieNumero.isBlank()) {
            serie = SERIE_INTERNA;
            numero = comprobantePort.maxNumeroBySerie(SERIE_INTERNA) + 1;
            serieNorm = String.format("%s-%06d", SERIE_INTERNA, numero);
        } else {
            ValidadorSigoc.validarSerieComprobante(serieNumero);
            serieNorm = serieNumero.trim().toUpperCase();
            int guion = serieNorm.indexOf('-');
            serie = serieNorm.substring(0, guion);
            numero = Integer.parseInt(serieNorm.substring(guion + 1));
        }

        if (comprobantePort.findBySerieNumero(serieNorm).isPresent()) {
            throw new ReglaNegocioException("El número de comprobante " + serieNorm + " ya fue emitido.");
        }

        String tipoNorm = (tipoComprobante == null || tipoComprobante.isBlank())
                ? "COMPROBANTE_VALORIZACION"
                : tipoComprobante.trim().toUpperCase();

        BigDecimal total;
        BigDecimal subtotal;
        BigDecimal igv;
        if (val.getTotal() != null && val.getTotal().compareTo(BigDecimal.ZERO) > 0
                && val.getSubtotal() != null && val.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            subtotal = val.getSubtotal().setScale(2, RoundingMode.HALF_UP);
            igv = val.getIgv().setScale(2, RoundingMode.HALF_UP);
            total = val.getTotal().setScale(2, RoundingMode.HALF_UP);
        } else {
            total = val.getMontoValorizado().setScale(2, RoundingMode.HALF_UP);
            subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
            igv = total.subtract(subtotal);
        }

        Comprobante comp = new Comprobante();
        comp.setIdValorizacion(idValorizacion);
        comp.setTipoComprobante(tipoNorm);
        comp.setSerie(serie);
        comp.setNumero(numero);
        comp.setSerieNumero(serieNorm);
        comp.setFechaEmision(LocalDate.now(clock));
        comp.setSubtotal(subtotal);
        comp.setIgv(igv);
        comp.setTotal(total);
        comp.setCodigoPago(generarCodigoUnico());
        comp.setVenceEl(LocalDateTime.now(clock).plusHours(horasVigenciaEnlace));
        comp.setEstado("EMITIDO");
        comp.setActivo(true);

        Comprobante guardado = comprobantePort.guardar(comp);
        val.setEstado("FACTURADA");
        valorizacionPort.guardar(val);
        return guardado;
    }

    public Comprobante renovarEnlace(UsuarioSesion actor, Integer idComprobante) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        Comprobante comp = comprobantePort.findByIdParaActualizar(idComprobante)
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante indicado no existe."));
        if (!comp.esPagable()) {
            throw new ReglaNegocioException("Solo se puede renovar el enlace de comprobantes pendientes de pago.");
        }
        comp.setCodigoPago(generarCodigoUnico());
        comp.setVenceEl(LocalDateTime.now(clock).plusHours(horasVigenciaEnlace));
        return comprobantePort.guardar(comp);
    }

    private String generarCodigoUnico() {
        byte[] bytes = new byte[20];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
