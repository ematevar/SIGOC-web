package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-06: lista los materiales activos del catálogo.
 */
public class ListarMateriales {
    private final MaterialPort materialPort;

    public ListarMateriales(MaterialPort materialPort) {
        this.materialPort = materialPort;
    }

    public List<Material> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.ALMACENERO, RolSistema.RESIDENTE);
        return materialPort.findAllActivos();
    }
}
