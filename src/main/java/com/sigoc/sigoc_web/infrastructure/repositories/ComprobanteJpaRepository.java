package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ComprobanteEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteJpaRepository extends JpaRepository<ComprobanteEntity, Integer> {

    List<ComprobanteEntity> findByActivoTrueOrderByIdComprobanteDesc();

    Optional<ComprobanteEntity> findFirstByIdValorizacionAndActivoTrue(Integer idValorizacion);

    Optional<ComprobanteEntity> findBySerieNumero(String serieNumero);

    Optional<ComprobanteEntity> findByCodigoPagoAndActivoTrue(String codigoPago);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ComprobanteEntity c WHERE c.idComprobante = :id")
    Optional<ComprobanteEntity> findByIdParaActualizar(@Param("id") Integer idComprobante);

    @Query("SELECT COALESCE(MAX(c.numero), 0) FROM ComprobanteEntity c WHERE c.serie = :serie")
    int maxNumeroBySerie(@Param("serie") String serie);
}
