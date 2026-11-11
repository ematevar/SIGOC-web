package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;

import java.math.BigDecimal;
import java.util.List;

public interface ConsumoPort {
    List<ConsumoMaterial> findAllActivos();

    List<ConsumoMaterial> findActivosByPartida(Integer idPartida);

    BigDecimal sumaCantidadPorPartidaYMaterial(Integer idPartida, Integer idMaterial);

    ConsumoMaterial guardar(ConsumoMaterial consumo);
}
