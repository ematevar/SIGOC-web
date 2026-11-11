package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
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
 * Procesa un pago en pasarela sandbox, actualiza valorización/comprobante a PAGADA/PAGADO
 * y registra automáticamente el ingreso financiero en la obra operativa.
 */
public class ProcesarPago {
    private final PagoPort pagoPort;
    private final ObraPort obraPort;
    private final ValorizacionPort valorizacionPort;
    private final ComprobantePort comprobantePort;
    private final IngresoPort ingresoPort;
    private final Clock clock;

    public ProcesarPago(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                        ComprobantePort comprobantePort, IngresoPort ingresoPort, Clock clock) {
        this.pagoPort = pagoPort;
        this.obraPort = obraPort;
        this.valorizacionPort = valorizacionPort;
        this.comprobantePort = comprobantePort;
        this.ingresoPort = ingresoPort;
        this.clock = clock;
    }

    public Pago ejecutar(UsuarioSesion actor, Integer idObra, Integer idValorizacion,
                         Integer idComprobante, BigDecimal monto, String metodoPago, String codigoOperacion) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida para el pago.");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o se encuentra cerrada.");
        }

        ValidadorSigoc.validarMontoPositivo(monto, "pago");
        BigDecimal montoEscala = monto.setScale(2, RoundingMode.HALF_UP);

        String metodo = (metodoPago == null || metodoPago.isBlank())
                ? "PASARELA_SANDBOX"
                : metodoPago.trim().toUpperCase();

        String codigoOp;
        if (codigoOperacion == null || codigoOperacion.isBlank()) {
            codigoOp = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            codigoOp = codigoOperacion.trim().toUpperCase();
            if (pagoPort.findByCodigoOperacion(codigoOp).isPresent()) {
                throw new ReglaNegocioException("El código de operación " + codigoOp + " ya fue registrado.");
            }
        }

        String conceptoIngreso = "Pago aprobado (" + metodo + " - Op: " + codigoOp + ")";

        if (idValorizacion != null && idValorizacion > 0) {
            Valorizacion val = valorizacionPort.findById(idValorizacion)
                    .filter(Valorizacion::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
            if (!Objects.equals(val.getIdObra(), idObra)) {
                throw new ReglaNegocioException("La valorización no pertenece a la obra seleccionada.");
            }
            if (val.estaPagada()) {
                throw new ReglaNegocioException("Esta valorización ya se encuentra pagada.");
            }
            val.setEstado("PAGADA");
            valorizacionPort.guardar(val);
            conceptoIngreso = "Pago de Valorización " + val.getPeriodo() + " (" + codigoOp + ")";
        }

        if (idComprobante != null && idComprobante > 0) {
            Comprobante comp = comprobantePort.findById(idComprobante)
                    .filter(Comprobante::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante asociado no existe."));
            if (!comp.esPagable()) {
                throw new ReglaNegocioException(
                        "El comprobante no está en estado pagable (actual: " + comp.getEstado() + ").");
            }
            if (idValorizacion != null && idValorizacion > 0
                    && !Objects.equals(comp.getIdValorizacion(), idValorizacion)) {
                throw new ReglaNegocioException("El comprobante no corresponde a la valorización indicada.");
            }
            comp.setEstado("PAGADO");
            comprobantePort.guardar(comp);
            conceptoIngreso = "Pago Comprobante " + comp.getSerieNumero() + " (" + codigoOp + ")";
        }

        if (conceptoIngreso.length() > 150) {
            conceptoIngreso = conceptoIngreso.substring(0, 150);
        }

        Ingreso nuevoIngreso = new Ingreso();
        nuevoIngreso.setIdObra(idObra);
        nuevoIngreso.setFecha(LocalDate.now(clock));
        nuevoIngreso.setConcepto(conceptoIngreso);
        nuevoIngreso.setMonto(montoEscala);
        nuevoIngreso.setActivo(true);
        Ingreso ingresoGuardado = ingresoPort.guardar(nuevoIngreso);

        Pago pago = new Pago();
        pago.setIdObra(idObra);
        pago.setIdValorizacion(idValorizacion);
        pago.setIdComprobante(idComprobante);
        pago.setIdIngresoGenerado(ingresoGuardado.getIdIngreso());
        pago.setMonto(montoEscala);
        pago.setMetodoPago(metodo);
        pago.setCodigoOperacion(codigoOp);
        pago.setFechaPago(LocalDateTime.now(clock));
        pago.setEstado("APROBADO");
        pago.setActivo(true);
        return pagoPort.guardar(pago);
    }
}
