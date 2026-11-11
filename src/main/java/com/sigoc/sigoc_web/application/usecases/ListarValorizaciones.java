package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * Lista valorizaciones activas de forma general o filtradas por obra.
 */
public class ListarValorizaciones {
    private final ValorizacionPort valorizacionPort;

    public ListarValorizaciones(ValorizacionPort valorizacionPort) {
        this.valorizacionPort = valorizacionPort;
    }

    public List<Valorizacion> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        if (idObra != null && idObra > 0) {
            return valorizacionPort.findActivasByObra(idObra);
        }
        return valorizacionPort.findAllActivas();
    }
}
