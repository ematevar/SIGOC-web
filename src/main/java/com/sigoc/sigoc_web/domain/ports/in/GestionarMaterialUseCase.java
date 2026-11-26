package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.util.List;

/**
 * CU-06: Catálogo de materiales de construcción y alertas de stock mínimo.
 */
public interface GestionarMaterialUseCase {
    @OperacionSoloLectura
    List<Material> listar(UsuarioSesion actor);

    @OperacionSoloLectura
    List<Material> listarAlertasStock(UsuarioSesion actor);

    Material guardar(UsuarioSesion actor, Integer idMaterial, String nombre,
                     String unidadMedida, BigDecimal stockMinimo, BigDecimal costoUnitarioRef);

    void eliminar(UsuarioSesion actor, Integer idMaterial);
}
