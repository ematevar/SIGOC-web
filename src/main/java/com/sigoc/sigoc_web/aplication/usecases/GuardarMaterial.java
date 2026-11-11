package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RF-06: crea o actualiza un material de construcción manteniendo {@code stock_actual}
 * como campo de solo lectura (solo varía mediante compras y consumos).
 */
public class GuardarMaterial {
    private final MaterialPort materialPort;

    public GuardarMaterial(MaterialPort materialPort) {
        this.materialPort = materialPort;
    }

    public Material ejecutar(UsuarioSesion actor, Integer idMaterial, String nombre,
                            String unidadMedida, BigDecimal stockMinimo, BigDecimal costoUnitarioRef) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.ALMACENERO);
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: nombre.");
        }
        if (unidadMedida == null || unidadMedida.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: unidad_medida.");
        }
        BigDecimal min = stockMinimo == null ? BigDecimal.ZERO : stockMinimo.setScale(2, RoundingMode.HALF_UP);
        if (min.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El campo stock_minimo no puede ser negativo.");
        }
        BigDecimal costo = costoUnitarioRef == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : costoUnitarioRef.setScale(2, RoundingMode.HALF_UP);
        if (costo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El campo costo_unitario_ref no puede ser negativo.");
        }

        Material material;
        if (idMaterial != null) {
            material = materialPort.findById(idMaterial)
                    .filter(Material::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El material no existe."));
        } else {
            material = new Material();
            material.setStockActual(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            material.setActivo(true);
        }

        material.setNombre(nombre.trim());
        material.setUnidadMedida(unidadMedida.trim());
        material.setStockMinimo(min);
        material.setCostoUnitarioRef(costo);
        return materialPort.guardar(material);
    }
}
