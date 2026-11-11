package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialPort {
    List<Material> findAllActivos();

    List<Material> findAlertasStockMinimo();

    Optional<Material> findById(Integer idMaterial);

    Material guardar(Material material);

    /** Actualiza el stock (y opcionalmente el costo de referencia) tras una compra o consumo. */
    void actualizarStock(Integer idMaterial, java.math.BigDecimal nuevoStock, java.math.BigDecimal nuevoCostoRef);

    void desactivar(Integer idMaterial);
}
