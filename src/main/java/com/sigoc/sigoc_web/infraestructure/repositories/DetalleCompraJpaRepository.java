package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.DetalleCompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleCompraJpaRepository extends JpaRepository<DetalleCompraEntity, Integer> {
}
