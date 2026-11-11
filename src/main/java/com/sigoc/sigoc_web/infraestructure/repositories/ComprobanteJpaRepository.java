package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.ComprobanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteJpaRepository extends JpaRepository<ComprobanteEntity, Integer> {

    List<ComprobanteEntity> findByActivoTrueOrderByIdComprobanteDesc();

    Optional<ComprobanteEntity> findFirstByIdValorizacionAndActivoTrue(Integer idValorizacion);

    Optional<ComprobanteEntity> findBySerieNumero(String serieNumero);
}
