package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Rol;

import java.util.List;
import java.util.Optional;

public interface RolPort {
    List<Rol> findAll();
    Optional<Rol> findById(Integer idRol);

    /** Roles con activo=1. */
    List<Rol> findAllActivos();

    Optional<Rol> findActivoById(Integer idRol);
}
