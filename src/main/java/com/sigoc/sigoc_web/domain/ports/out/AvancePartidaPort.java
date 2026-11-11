package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.AvancePartida;

import java.util.List;
import java.util.Optional;

public interface AvancePartidaPort {
    List<AvancePartida> findActivosByPartida(Integer idPartida);

    List<AvancePartida> findActivosByObra(Integer idObra);

    List<AvancePartida> findAllActivos();

    Optional<AvancePartida> findUltimoActivoByPartida(Integer idPartida);

    AvancePartida guardar(AvancePartida avance);
}
