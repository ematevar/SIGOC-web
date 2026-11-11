package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.CompraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-07: lista las compras de materiales activas.
 */
public class ListarCompras {
    private final CompraPort compraPort;

    public ListarCompras(CompraPort compraPort) {
        this.compraPort = compraPort;
    }

    public List<CompraMaterial> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ALMACENERO, RolSistema.ADMIN, RolSistema.CONTADOR);
        return compraPort.findAllActivas();
    }
}
