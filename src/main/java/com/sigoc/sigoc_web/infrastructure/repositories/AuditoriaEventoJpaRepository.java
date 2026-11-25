package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.AuditoriaEventoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaEventoJpaRepository extends JpaRepository<AuditoriaEventoEntity, Long> {
    List<AuditoriaEventoEntity> findAllByOrderByFechaHoraDescIdEventoDesc(Pageable pageable);

    List<AuditoriaEventoEntity> findByAccionOrderByFechaHoraDescIdEventoDesc(String accion, Pageable pageable);
}
