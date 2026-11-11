package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, Integer> {

    List<PartidaEntity> findByActivoTrueOrderByIdPartidaDesc();

    List<PartidaEntity> findByIdObraAndActivoTrueOrderByIdPartidaDesc(Integer idObra);

    @Query("select coalesce(sum(p.pesoPresupuestal), 0) from PartidaEntity p "
            + "where p.idObra = :idObra and p.activo = true "
            + "and (:idExcluir is null or p.idPartida <> :idExcluir)")
    BigDecimal sumaPesosActivos(Integer idObra, Integer idExcluir);
}
