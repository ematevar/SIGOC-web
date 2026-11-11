package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolJpaRepository extends JpaRepository<RolEntity, Integer> {

    List<RolEntity> findByActivoTrueOrderByIdRol();

    Optional<RolEntity> findByIdRolAndActivoTrue(Integer idRol);
}
