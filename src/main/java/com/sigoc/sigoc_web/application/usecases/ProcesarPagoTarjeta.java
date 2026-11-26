package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoTarjetaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaTarjetaPort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;

import java.time.Clock;

/**
 * CU-24: Procesamiento de pagos en línea mediante tarjeta simulada (entorno educativo).
 */
public class ProcesarPagoTarjeta implements ProcesarPagoTarjetaUseCase {

    private final PagoPort pagoPort;
    private final ObraPort obraPort;
    private final ValorizacionPort valorizacionPort;
    private final ComprobantePort comprobantePort;
    private final IngresoPort ingresoPort;
    private final PasarelaTarjetaPort pasarelaTarjetaPort;
    private final Clock clock;
    private final int horasVigenciaEnlace;
    private final AuditoriaEventoPort auditoriaPort;

    public ProcesarPagoTarjeta(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                               ComprobantePort comprobantePort, IngresoPort ingresoPort,
                               PasarelaTarjetaPort pasarelaTarjetaPort, Clock clock,
                               int horasVigenciaEnlace, AuditoriaEventoPort auditoriaPort) {
        this.pagoPort = pagoPort;
        this.obraPort = obraPort;
        this.valorizacionPort = valorizacionPort;
        this.comprobantePort = comprobantePort;
        this.ingresoPort = ingresoPort;
        this.pasarelaTarjetaPort = pasarelaTarjetaPort;
        this.clock = clock;
        this.horasVigenciaEnlace = Math.max(1, horasVigenciaEnlace);
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    public Pago cobrarTarjetaPublica(String codigoPago, String numeroTarjeta, String titular,
                                     String vencimiento, String cvv) {
        if (pasarelaTarjetaPort == null) {
            throw new ReglaNegocioException("Pasarela de tarjeta simulada no disponible.");
        }
        GestionarCobranza.ContextoEnlace ctx = GestionarCobranza.validarEnlacePagable(
                comprobantePort, valorizacionPort, obraPort, pagoPort, clock,
                horasVigenciaEnlace, codigoPago, true);
        Comprobante comp = ctx.comprobante();
        Valorizacion val = ctx.valorizacion();
        Obra obra = ctx.obra();

        PasarelaTarjetaPort.ResultadoTarjeta res = pasarelaTarjetaPort.cobrarTarjeta(
                numeroTarjeta, titular, vencimiento, cvv, comp.getTotal());

        if (!res.aprobado()) {
            GestionarCobranza.registrarIntentoRechazado(pagoPort, clock, obra.getIdObra(), val.getIdValorizacion(),
                    comp.getIdComprobante(), comp.getTotal(), "TARJETA_SIMULADA", "PASARELA_SIMULADA",
                    null, null, comp.getTotal(), "PEN", null, res.marcaTarjeta(), res.ultimos4(), res.motivo());
            auditar("PAGO_TARJETA_RECHAZADO", String.valueOf(comp.getIdComprobante()), "RECHAZADO", res.motivo());
            throw new PagoRechazadoException(
                    GestionarCobranza.mensajeTrasRechazo(pagoPort, clock, comp, horasVigenciaEnlace, res.motivo()));
        }

        Pago aprobado = GestionarCobranza.persistirPagoAprobado(
                pagoPort, ingresoPort, comprobantePort, valorizacionPort, clock,
                obra, val, comp, comp.getTotal(), "TARJETA_SIMULADA", res.codigoOperacion(),
                "PASARELA_SIMULADA", null, null, comp.getTotal(), "PEN", null,
                res.marcaTarjeta(), res.ultimos4(),
                "Cobro tarjeta " + res.marcaTarjeta() + " *" + res.ultimos4() + " - " + comp.getSerieNumero());
        auditar("PAGO_TARJETA_APROBADO", String.valueOf(aprobado.getIdPago()), "EXITO",
                "Comprobante " + comp.getSerieNumero() + " tarjeta *" + res.ultimos4());
        return aprobado;
    }

    private void auditar(String accion, String idEntidad, String resultado, String detalle) {
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, null, accion, "pago", idEntidad, resultado, detalle));
        }
    }
}
