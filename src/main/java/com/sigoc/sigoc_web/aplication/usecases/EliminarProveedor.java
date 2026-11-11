package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Proveedor;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ProveedorPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-09: da de baja lógica un proveedor conservando su historial de compras.
 */
public class EliminarProveedor {
    private final ProveedorPort proveedorPort;

    public EliminarProveedor(ProveedorPort proveedorPort) {
        this.proveedorPort = proveedorPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idProveedor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idProveedor == null || idProveedor <= 0) {
            throw new ReglaNegocioException("Selecciona un proveedor válido.");
        }
        proveedorPort.findById(idProveedor)
                .filter(Proveedor::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor."));
        proveedorPort.desactivar(idProveedor);
    }
}
