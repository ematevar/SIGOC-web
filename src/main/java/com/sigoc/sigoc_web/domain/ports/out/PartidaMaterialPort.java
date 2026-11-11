package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.PartidaMaterial;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PartidaMaterialPort {
    List<PartidaMaterial> findActivosByPartida(Integer idPartida);

    Optional<PartidaMaterial> findById(Integer idPartidaMaterial);

    boolean existeMaterialActivo(Integer idMaterial);

    PartidaMaterial guardarOActualizar(Integer idPartida, Integer idMaterial, BigDecimal cantidadPresupuestada);

    void desactivar(Integer idPartidaMaterial);
}
