package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Comprobante;

import java.util.List;
import java.util.Optional;

public interface ComprobantePort {
    List<Comprobante> findAllActivos();

    Optional<Comprobante> findById(Integer idComprobante);

    default Optional<Comprobante> findByIdParaActualizar(Integer idComprobante) {
        return findById(idComprobante);
    }

    Optional<Comprobante> findActivoByValorizacion(Integer idValorizacion);

    Optional<Comprobante> findBySerieNumero(String serieNumero);

    default Optional<Comprobante> findByCodigoPago(String codigoPago) {
        return Optional.empty();
    }

    default int maxNumeroBySerie(String serie) {
        return findAllActivos().stream()
                .filter(c -> serie.equalsIgnoreCase(c.getSerie()))
                .mapToInt(c -> c.getNumero() == null ? 0 : c.getNumero())
                .max()
                .orElse(0);
    }

    Comprobante guardar(Comprobante comprobante);
}
