package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Valorizacion;

import java.util.List;
import java.util.Optional;

public interface ValorizacionPort {
    List<Valorizacion> findAllActivas();

    List<Valorizacion> findActivasByObra(Integer idObra);

    Optional<Valorizacion> findById(Integer idValorizacion);

    Valorizacion guardar(Valorizacion valorizacion);
}
