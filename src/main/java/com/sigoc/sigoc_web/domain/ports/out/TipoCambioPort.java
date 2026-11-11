package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.TipoCambio;

import java.util.Optional;

public interface TipoCambioPort {
    Optional<TipoCambio> findByPar(String monedaOrigen, String monedaDestino);

    TipoCambio guardar(TipoCambio tipoCambio);
}
