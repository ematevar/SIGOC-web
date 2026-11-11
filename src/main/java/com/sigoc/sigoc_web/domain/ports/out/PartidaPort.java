package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Partida;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PartidaPort {
    List<Partida> findAllActivas();

    List<Partida> findActivasByObra(Integer idObra);

    Optional<Partida> findById(Integer idPartida);

    BigDecimal sumaPesosActivos(Integer idObra, Integer idPartidaExcluir);

    boolean tieneMovimientos(Integer idPartida);

    Partida guardar(Partida partida);

    void desactivar(Integer idPartida);
}
