package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoJpaRepository extends JpaRepository<PagoEntity, Integer> {

    List<PagoEntity> findByActivoTrueOrderByIdPagoDesc();

    List<PagoEntity> findByIdObraAndActivoTrueOrderByIdPagoDesc(Integer idObra);

    Optional<PagoEntity> findByCodigoOperacion(String codigoOperacion);
}
