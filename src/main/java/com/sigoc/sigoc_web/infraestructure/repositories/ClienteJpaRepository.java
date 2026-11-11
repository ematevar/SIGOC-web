package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Integer> {

    List<ClienteEntity> findByActivoTrueOrderByIdClienteDesc();

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdClienteNot(String numeroDocumento, Integer idCliente);
}
