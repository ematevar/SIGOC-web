package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Valorizacion;

public interface ExportadorComprobantePdfPort {
    byte[] generarPdf(Comprobante comprobante, Valorizacion valorizacion, Obra obra, Cliente cliente);
}
