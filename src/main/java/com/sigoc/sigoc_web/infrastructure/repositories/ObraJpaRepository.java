package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraJpaRepository extends JpaRepository<ObraEntity, Integer> {
    List<ObraEntity> findByActivoTrueOrderByIdObraDesc();
}
