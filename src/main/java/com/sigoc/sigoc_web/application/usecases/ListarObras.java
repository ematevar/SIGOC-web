package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-03: lista las obras activas del sistema.
 */
public class ListarObras {
    private final ObraPort obraPort;

    public ListarObras(ObraPort obraPort) {
        this.obraPort = obraPort;
    }

    public List<Obra> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.ALMACENERO);
        return obraPort.findAllActivas();
    }
}
