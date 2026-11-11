package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialPort {
    List<Material> findAllActivos();

    List<Material> findAlertasStockMinimo();

    Optional<Material> findById(Integer idMaterial);

    Material guardar(Material material);

    void desactivar(Integer idMaterial);
}
