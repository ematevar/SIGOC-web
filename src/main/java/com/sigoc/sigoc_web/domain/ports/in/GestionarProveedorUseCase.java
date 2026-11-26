package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Proveedor;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.List;

/**
 * CU-09: Gestión de proveedores con validación de RUC.
 */
public interface GestionarProveedorUseCase {
    @OperacionSoloLectura
    List<Proveedor> listar(UsuarioSesion actor);

    Proveedor guardar(UsuarioSesion actor, Integer idProveedor, String razonSocial,
                      String ruc, String telefono, String direccion);

    void eliminar(UsuarioSesion actor, Integer idProveedor);
}
