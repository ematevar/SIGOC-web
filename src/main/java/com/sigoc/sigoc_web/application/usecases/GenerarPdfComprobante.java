package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-19: genera el PDF interno CV01 del comprobante de valorización.
 */
public class GenerarPdfComprobante {

    private final ComprobantePort comprobantePort;
    private final ValorizacionPort valorizacionPort;
    private final ObraPort obraPort;
    private final ClientePort clientePort;
    private final ExportadorComprobantePdfPort pdfPort;

    public GenerarPdfComprobante(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort,
                                 ObraPort obraPort, ClientePort clientePort,
                                 ExportadorComprobantePdfPort pdfPort) {
        this.comprobantePort = comprobantePort;
        this.valorizacionPort = valorizacionPort;
        this.obraPort = obraPort;
        this.clientePort = clientePort;
        this.pdfPort = pdfPort;
    }

    public ResultadoPdf ejecutar(UsuarioSesion actor, Integer idComprobante) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR, RolSistema.RESIDENTE);
        return generarInterno(idComprobante);
    }

    public ResultadoPdf ejecutarPublico(String codigoPago) {
        Comprobante comp = comprobantePort.findByCodigoPago(codigoPago)
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El enlace de pago no existe."));
        return generarParaComprobante(comp);
    }

    private ResultadoPdf generarInterno(Integer idComprobante) {
        Comprobante comp = comprobantePort.findById(idComprobante)
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante solicitado no existe."));
        return generarParaComprobante(comp);
    }

    private ResultadoPdf generarParaComprobante(Comprobante comp) {
        Valorizacion val = valorizacionPort.findById(comp.getIdValorizacion()).orElse(null);
        Obra obra = val != null ? obraPort.findById(val.getIdObra()).orElse(null) : null;
        Cliente cliente = obra != null && obra.getIdCliente() != null
                ? clientePort.findById(obra.getIdCliente()).orElse(null)
                : null;
        byte[] bytes = pdfPort.generarPdf(comp, val, obra, cliente);
        return new ResultadoPdf(comp.getSerieNumero() + ".pdf", bytes);
    }

    public record ResultadoPdf(String nombreArchivo, byte[] contenido) {
    }
}
