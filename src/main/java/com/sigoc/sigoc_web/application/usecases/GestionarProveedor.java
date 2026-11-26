package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Proveedor;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarProveedorUseCase;
import com.sigoc.sigoc_web.domain.ports.out.ProveedorPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.util.List;

/**
 * CU-09: Gestión de proveedores con validación de RUC único.
 */
public class GestionarProveedor implements GestionarProveedorUseCase {

    private final ProveedorPort proveedorPort;

    public GestionarProveedor(ProveedorPort proveedorPort) {
        this.proveedorPort = proveedorPort;
    }

    @Override
    public List<Proveedor> listar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PROVEEDORES);
        return proveedorPort.findAllActivos();
    }

    @Override
    public Proveedor guardar(UsuarioSesion actor, Integer idProveedor, String razonSocial,
                             String ruc, String telefono, String direccion) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PROVEEDORES);
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: razon_social.");
        }
        String rucLimpio = ruc == null ? "" : ruc.trim();
        ValidadorSigoc.validarRuc(rucLimpio);
        if (proveedorPort.existsByRuc(rucLimpio, idProveedor)) {
            throw new ReglaNegocioException("Ya existe un proveedor registrado con el RUC " + rucLimpio + " (RF-09).");
        }

        Proveedor proveedor;
        if (idProveedor != null) {
            proveedor = proveedorPort.findById(idProveedor)
                    .filter(Proveedor::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor."));
        } else {
            proveedor = new Proveedor();
            proveedor.setActivo(true);
        }

        proveedor.setRazonSocial(razonSocial.trim());
        proveedor.setRuc(rucLimpio);
        proveedor.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        proveedor.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());
        return proveedorPort.guardar(proveedor);
    }

    @Override
    public void eliminar(UsuarioSesion actor, Integer idProveedor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PROVEEDORES);
        if (idProveedor == null || idProveedor <= 0) {
            throw new ReglaNegocioException("Selecciona un proveedor válido.");
        }
        proveedorPort.findById(idProveedor)
                .filter(Proveedor::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor."));
        proveedorPort.desactivar(idProveedor);
    }
}
