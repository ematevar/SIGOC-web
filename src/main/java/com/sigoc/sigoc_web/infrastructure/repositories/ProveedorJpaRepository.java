package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Integer> {

    List<ProveedorEntity> findByActivoTrueOrderByIdProveedorDesc();

    @Query("select count(p) > 0 from ProveedorEntity p where p.ruc = :ruc and (:idExcluir is null or p.idProveedor <> :idExcluir)")
    boolean existsByRuc(String ruc, Integer idExcluir);
}
