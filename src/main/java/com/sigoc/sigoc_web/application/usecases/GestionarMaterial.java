package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarMaterialUseCase;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * CU-06: Catálogo de materiales de construcción y alertas de stock mínimo.
 */
public class GestionarMaterial implements GestionarMaterialUseCase {

    private final MaterialPort materialPort;

    public GestionarMaterial(MaterialPort materialPort) {
        this.materialPort = materialPort;
    }

    @Override
    public List<Material> listar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_MATERIALES);
        return materialPort.findAllActivos();
    }

    @Override
    public List<Material> listarAlertasStock(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_ALERTAS_STOCK);
        return materialPort.findAlertasStockMinimo();
    }

    @Override
    public Material guardar(UsuarioSesion actor, Integer idMaterial, String nombre,
                            String unidadMedida, BigDecimal stockMinimo, BigDecimal costoUnitarioRef) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_MATERIALES);
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

    @Override
    public void eliminar(UsuarioSesion actor, Integer idMaterial) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_MATERIALES);
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
