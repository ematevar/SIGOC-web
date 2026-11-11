package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Comprobante;

import java.util.List;
import java.util.Optional;

public interface ComprobantePort {
    List<Comprobante> findAllActivos();

    Optional<Comprobante> findById(Integer idComprobante);

    Optional<Comprobante> findActivoByValorizacion(Integer idValorizacion);

    Optional<Comprobante> findBySerieNumero(String serieNumero);

    Comprobante guardar(Comprobante comprobante);
}
