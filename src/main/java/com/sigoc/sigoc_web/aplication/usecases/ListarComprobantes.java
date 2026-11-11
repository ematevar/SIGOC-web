package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * Lista comprobantes activos emitidos por valorizaciones.
 */
public class ListarComprobantes {
    private final ComprobantePort comprobantePort;

    public ListarComprobantes(ComprobantePort comprobantePort) {
        this.comprobantePort = comprobantePort;
    }

    public List<Comprobante> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        return comprobantePort.findAllActivos();
    }
}
