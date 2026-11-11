package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.MaterialEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialJpaRepository extends JpaRepository<MaterialEntity, Integer> {

    List<MaterialEntity> findByActivoTrueOrderByIdMaterialDesc();

    @Query("select m from MaterialEntity m where m.activo = true and m.stockActual <= m.stockMinimo order by m.stockActual asc, m.nombre asc")
    List<MaterialEntity> findAlertasStockMinimo();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MaterialEntity m WHERE m.idMaterial = :id")
    Optional<MaterialEntity> findByIdParaActualizar(@Param("id") Integer id);
}
