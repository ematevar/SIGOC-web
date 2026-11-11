package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ObraEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObraJpaRepository extends JpaRepository<ObraEntity, Integer> {
    List<ObraEntity> findByActivoTrueOrderByIdObraDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM ObraEntity o WHERE o.idObra = :id")
    Optional<ObraEntity> findByIdParaActualizar(@Param("id") Integer id);
}
