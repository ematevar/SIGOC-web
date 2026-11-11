package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialJpaRepository extends JpaRepository<MaterialEntity, Integer> {

    List<MaterialEntity> findByActivoTrueOrderByIdMaterialDesc();

    @Query("select m from MaterialEntity m where m.activo = true and m.stockActual <= m.stockMinimo order by m.stockActual asc, m.nombre asc")
    List<MaterialEntity> findAlertasStockMinimo();
}
