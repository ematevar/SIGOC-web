package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.CompraMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraMaterialJpaRepository extends JpaRepository<CompraMaterialEntity, Integer> {

    List<CompraMaterialEntity> findByActivoTrueOrderByFechaCompraDescIdCompraDesc();

    @Query("select count(c) > 0 from CompraMaterialEntity c "
            + "where c.idProveedor = :idProveedor and lower(c.numeroComprobante) = lower(:comprobante) and c.activo = true")
    boolean existsByProveedorYComprobante(Integer idProveedor, String comprobante);
}
