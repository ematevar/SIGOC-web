package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.ports.out.TipoCambioPort;
import com.sigoc.sigoc_web.infrastructure.entities.TipoCambioEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.TipoCambioJpaRepository;
import com.sigoc.sigoc_web.infrastructure.security.ContextoAuditoria;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class TipoCambioManualAdapter implements TipoCambioPort {

    private final TipoCambioJpaRepository repository;

    public TipoCambioManualAdapter(TipoCambioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoCambio> findByPar(String monedaOrigen, String monedaDestino) {
        return repository.findByMonedaOrigenAndMonedaDestino(monedaOrigen, monedaDestino).map(this::toModel);
    }

    @Override
    @Transactional
    public TipoCambio guardar(TipoCambio tipoCambio) {
        TipoCambioEntity entity = repository.findByMonedaOrigenAndMonedaDestino(
                        tipoCambio.getMonedaOrigen(), tipoCambio.getMonedaDestino())
                .orElseGet(TipoCambioEntity::new);
        entity.setMonedaOrigen(tipoCambio.getMonedaOrigen());
        entity.setMonedaDestino(tipoCambio.getMonedaDestino());
        entity.setValor(tipoCambio.getValor());
        entity.setFechaActualizacion(tipoCambio.getFechaActualizacion());
        Integer actorId = ContextoAuditoria.usuarioActual();
        entity.setIdUsuarioRegistro(actorId != null ? actorId : tipoCambio.getIdUsuarioRegistro());
        return toModel(repository.saveAndFlush(entity));
    }

    private TipoCambio toModel(TipoCambioEntity e) {
        return new TipoCambio(
                e.getIdTipoCambio(),
                e.getMonedaOrigen(),
                e.getMonedaDestino(),
                e.getValor(),
                e.getFechaActualizacion(),
                e.getIdUsuarioRegistro()
        );
    }
}
