package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Egreso;

import java.util.List;

public interface EgresoPort {
    List<Egreso> findAllActivos();

    List<Egreso> findActivosByObra(Integer idObra);

    Egreso guardar(Egreso egreso);
}
