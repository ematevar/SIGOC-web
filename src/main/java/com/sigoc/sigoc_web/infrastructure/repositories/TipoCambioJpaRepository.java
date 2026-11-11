package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.TipoCambioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoCambioJpaRepository extends JpaRepository<TipoCambioEntity, Integer> {
    Optional<TipoCambioEntity> findByMonedaOrigenAndMonedaDestino(String monedaOrigen, String monedaDestino);
}
