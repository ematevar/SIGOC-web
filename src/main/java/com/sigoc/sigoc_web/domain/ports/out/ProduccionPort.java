package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;

import java.util.List;

public interface ProduccionPort {
    List<ProduccionEmpleado> findAllActivas();

    List<ProduccionEmpleado> findActivasByPartida(Integer idPartida);

    ProduccionEmpleado guardar(ProduccionEmpleado produccion);
}
