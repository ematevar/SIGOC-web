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
import java.time.Clock;
import java.time.LocalDate;

/**
 * Emite un comprobante de prueba con desglose IGV (18%) y marca la valorización como FACTURADA.
 */
public class EmitirComprobante {
    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

    private final ComprobantePort comprobantePort;
    private final ValorizacionPort valorizacionPort;
    private final Clock clock;

    public EmitirComprobante(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort, Clock clock) {
        this.comprobantePort = comprobantePort;
        this.valorizacionPort = valorizacionPort;
        this.clock = clock;
    }

    public Comprobante ejecutar(UsuarioSesion actor, Integer idValorizacion,
                               String tipoComprobante, String serieNumero) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        Valorizacion val = valorizacionPort.findById(idValorizacion)
                .filter(Valorizacion::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("La valorización indicada no existe."));

        if (!val.puedeFacturarse()) {
            throw new ReglaNegocioException(
                    "Solo se puede facturar una valorización en estado PENDIENTE (actual: " + val.getEstado() + ").");
        }
        if (comprobantePort.findActivoByValorizacion(idValorizacion).isPresent()) {
            throw new ReglaNegocioException("La valorización ya tiene un comprobante activo emitido.");
        }

        ValidadorSigoc.validarSerieComprobante(serieNumero);
        String serieNorm = serieNumero.trim().toUpperCase();
        if (comprobantePort.findBySerieNumero(serieNorm).isPresent()) {
            throw new ReglaNegocioException("El número de comprobante " + serieNorm + " ya fue emitido.");
        }

        String tipoNorm = (tipoComprobante == null || tipoComprobante.isBlank())
                ? (serieNorm.startsWith("B") ? "BOLETA_PRUEBA" : "FACTURA_PRUEBA")
                : tipoComprobante.trim().toUpperCase();

        BigDecimal total = val.getMontoValorizado().setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);

        Comprobante comp = new Comprobante();
        comp.setIdValorizacion(idValorizacion);
        comp.setTipoComprobante(tipoNorm);
        comp.setSerieNumero(serieNorm);
        comp.setFechaEmision(LocalDate.now(clock));
        comp.setSubtotal(subtotal);
        comp.setIgv(igv);
        comp.setTotal(total);
        comp.setEstado("EMITIDO");
        comp.setActivo(true);

        Comprobante guardado = comprobantePort.guardar(comp);

        val.setEstado("FACTURADA");
        valorizacionPort.guardar(val);

        return guardado;
    }
}
