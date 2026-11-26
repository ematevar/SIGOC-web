package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarCobranzaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.util.List;

/**
 * CU-22: Gestión de cobranzas, registro interno de pagos y consulta pública del enlace.
 */
public class GestionarCobranza implements GestionarCobranzaUseCase {

    private final PagoPort pagoPort;
    private final ProcesarPago procesarPago;
    private final ConsultarEnlacePagoPublico consultarEnlacePagoPublico;

    public GestionarCobranza(PagoPort pagoPort,
                             ProcesarPago procesarPago,
                             ConsultarEnlacePagoPublico consultarEnlacePagoPublico) {
        this.pagoPort = pagoPort;
        this.procesarPago = procesarPago;
        this.consultarEnlacePagoPublico = consultarEnlacePagoPublico;
    }

    @Override
    public List<Pago> listarPagos(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PAGOS);
        return idObra != null ? pagoPort.findActivosByObra(idObra) : pagoPort.findAllActivos();
    }

    @Override
    public Pago registrarPagoInterno(UsuarioSesion actor, Integer idObra, Integer idValorizacion,
                                     Integer idComprobante, BigDecimal monto,
                                     String metodoPago, String codigoOperacion) {
        return procesarPago.ejecutar(actor, idObra, idValorizacion, idComprobante, monto, metodoPago, codigoOperacion);
    }

    @Override
    public DetalleEnlacePublico consultarEnlacePublico(String codigoPago) {
        ConsultarEnlacePagoPublico.DetalleEnlacePublico d = consultarEnlacePagoPublico.ejecutar(codigoPago);
        return new DetalleEnlacePublico(
                d.serieNumero(), d.fechaEmision(), d.subtotal(), d.igv(), d.totalPen(),
                d.montoUsdEstimado(), d.tipoCambioUsdPen(), d.estado(), d.venceEl(),
                d.expirado(), d.pagado(), d.paypalHabilitado(), d.nombreObra(),
                d.numeroValorizacion(), d.periodoValorizacion(), d.porcentajeAvancePeriodo(),
                d.clienteNombre(), d.bloqueado(), d.bloqueadoHasta(), d.intentosRestantes()
        );
    }
}
