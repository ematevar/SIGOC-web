package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.PartidaMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidaMaterialJpaRepository extends JpaRepository<PartidaMaterialEntity, Integer> {

    List<PartidaMaterialEntity> findByIdPartidaAndActivoTrueOrderByIdPartidaMaterialDesc(Integer idPartida);

    Optional<PartidaMaterialEntity> findByIdPartidaAndIdMaterial(Integer idPartida, Integer idMaterial);
}
