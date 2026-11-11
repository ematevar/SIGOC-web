package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorPort {
    List<Proveedor> findAllActivos();

    Optional<Proveedor> findById(Integer idProveedor);

    boolean existsByRuc(String ruc, Integer idExcluir);

    Proveedor guardar(Proveedor proveedor);

    void desactivar(Integer idProveedor);
}
