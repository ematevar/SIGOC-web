package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;

/**
 * RF-06: da de baja lógica un material solo si su stock actual es cero.
 */
public class EliminarMaterial {
    private final MaterialPort materialPort;

    public EliminarMaterial(MaterialPort materialPort) {
        this.materialPort = materialPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idMaterial) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.ALMACENERO);
        if (idMaterial == null || idMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona un material válido.");
        }
        Material material = materialPort.findById(idMaterial)
                .filter(Material::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El material no existe."));
        if (material.getStockActual() != null && material.getStockActual().compareTo(BigDecimal.ZERO) > 0) {
            throw new ReglaNegocioException(
                    "No se puede dar de baja un material con stock mayor a cero (Stock actual: "
                            + material.getStockActual() + ").");
        }
        materialPort.desactivar(idMaterial);
    }
}
