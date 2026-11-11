package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.EgresoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EgresoJpaRepository extends JpaRepository<EgresoEntity, Integer> {

    List<EgresoEntity> findByActivoTrueOrderByFechaDescIdEgresoDesc();

    List<EgresoEntity> findByIdObraAndActivoTrueOrderByFechaDescIdEgresoDesc(Integer idObra);
}
