package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.List;

/**
 * CU-19: Emisión de comprobantes CV01, renovación de enlaces y generación de PDF.
 */
public interface GestionarComprobanteUseCase {

    record ResultadoPdf(String nombreArchivo, byte[] contenido) {
    }

    @OperacionSoloLectura
    List<Comprobante> listar(UsuarioSesion actor);

    Comprobante emitir(UsuarioSesion actor, Integer idValorizacion, String tipoComprobante, String serieNumero);

    Comprobante renovarEnlace(UsuarioSesion actor, Integer idComprobante);

    @OperacionSoloLectura
    ResultadoPdf generarPdf(UsuarioSesion actor, Integer idComprobante);

    @OperacionSoloLectura
    ResultadoPdf generarPdfPublico(String codigoPago);
}
