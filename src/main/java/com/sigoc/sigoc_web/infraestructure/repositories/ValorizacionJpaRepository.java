package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.ValorizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValorizacionJpaRepository extends JpaRepository<ValorizacionEntity, Integer> {

    List<ValorizacionEntity> findByActivoTrueOrderByIdValorizacionDesc();

    List<ValorizacionEntity> findByIdObraAndActivoTrueOrderByIdValorizacionDesc(Integer idObra);
}
