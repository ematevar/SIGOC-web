package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.AvancePartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvancePartidaJpaRepository extends JpaRepository<AvancePartidaEntity, Integer> {

    List<AvancePartidaEntity> findByActivoTrueOrderByFechaReporteDescIdAvanceDesc();

    List<AvancePartidaEntity> findByIdPartidaAndActivoTrueOrderByFechaReporteDescIdAvanceDesc(Integer idPartida);

    Optional<AvancePartidaEntity> findTopByIdPartidaAndActivoTrueOrderByFechaReporteDescIdAvanceDesc(Integer idPartida);

    @Query("select a from AvancePartidaEntity a, PartidaEntity p "
            + "where a.idPartida = p.idPartida and p.idObra = :idObra and a.activo = true and p.activo = true "
            + "order by a.fechaReporte desc, a.idAvance desc")
    List<AvancePartidaEntity> findActivosByObra(Integer idObra);
}
