package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.IngresoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngresoJpaRepository extends JpaRepository<IngresoEntity, Integer> {

    List<IngresoEntity> findByActivoTrueOrderByFechaDescIdIngresoDesc();

    List<IngresoEntity> findByIdObraAndActivoTrueOrderByFechaDescIdIngresoDesc(Integer idObra);
}
