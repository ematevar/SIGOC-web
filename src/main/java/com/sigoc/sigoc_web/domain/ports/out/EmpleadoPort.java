package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoPort {
    List<Empleado> findAllActivos();

    Optional<Empleado> findById(Integer idEmpleado);

    boolean existsByDni(String dni, Integer idExcluir);

    Empleado guardar(Empleado empleado);

    void desactivar(Integer idEmpleado);
}
