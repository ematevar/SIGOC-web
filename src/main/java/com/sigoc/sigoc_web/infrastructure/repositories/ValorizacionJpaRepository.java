package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ValorizacionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValorizacionJpaRepository extends JpaRepository<ValorizacionEntity, Integer> {

    List<ValorizacionEntity> findByActivoTrueOrderByIdValorizacionDesc();

    List<ValorizacionEntity> findByIdObraAndActivoTrueOrderByIdValorizacionDesc(Integer idObra);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ValorizacionEntity v WHERE v.idValorizacion = :id")
    Optional<ValorizacionEntity> findByIdParaActualizar(@Param("id") Integer idValorizacion);
}
