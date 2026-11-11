package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-06 / RF-08: lista los materiales activos cuyo stock actual es menor o igual al stock mínimo.
 */
public class ListarAlertasStock {
    private final MaterialPort materialPort;

    public ListarAlertasStock(MaterialPort materialPort) {
        this.materialPort = materialPort;
    }

    public List<Material> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.ALMACENERO, RolSistema.RESIDENTE);
        return materialPort.findAlertasStockMinimo();
    }
}
