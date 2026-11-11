package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Ingreso;

import java.util.List;

public interface IngresoPort {
    List<Ingreso> findAllActivos();

    List<Ingreso> findActivosByObra(Integer idObra);

    Ingreso guardar(Ingreso ingreso);
}
