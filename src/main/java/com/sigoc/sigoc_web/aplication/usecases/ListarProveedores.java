package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Proveedor;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ProveedorPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-09: lista los proveedores activos.
 */
public class ListarProveedores {
    private final ProveedorPort proveedorPort;

    public ListarProveedores(ProveedorPort proveedorPort) {
        this.proveedorPort = proveedorPort;
    }

    public List<Proveedor> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.ALMACENERO);
        return proveedorPort.findAllActivos();
    }
}
