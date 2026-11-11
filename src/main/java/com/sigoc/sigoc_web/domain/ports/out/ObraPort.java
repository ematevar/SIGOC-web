package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Obra;

import java.util.List;
import java.util.Optional;

public interface ObraPort {
    List<Obra> findAllActivas();

    Optional<Obra> findById(Integer idObra);

    default Optional<Obra> findByIdParaActualizar(Integer idObra) {
        return findById(idObra);
    }

    Obra guardar(Obra obra);

    boolean tieneMovimientos(Integer idObra);

    void desactivar(Integer idObra);

    void cerrar(Integer idObra);
}
