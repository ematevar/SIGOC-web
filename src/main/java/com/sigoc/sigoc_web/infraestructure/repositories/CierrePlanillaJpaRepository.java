package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.CierrePlanillaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CierrePlanillaJpaRepository extends JpaRepository<CierrePlanillaEntity, Integer> {

    List<CierrePlanillaEntity> findByActivoTrueOrderByFechaCierreDescIdCierreDesc();

    List<CierrePlanillaEntity> findByIdObraAndActivoTrueOrderByFechaCierreDescIdCierreDesc(Integer idObra);

    @Query("""
            SELECT c FROM CierrePlanillaEntity c
            WHERE c.idObra = :idObra
              AND c.activo = true
              AND NOT (c.fechaFinPeriodo < :desde OR c.fechaInicioPeriodo > :hasta)
            ORDER BY c.idCierre DESC
            """)
    List<CierrePlanillaEntity> findSolapados(@Param("idObra") Integer idObra,
                                             @Param("desde") LocalDate desde,
                                             @Param("hasta") LocalDate hasta);

    default Optional<CierrePlanillaEntity> findPrimeroSolapado(Integer idObra, LocalDate desde, LocalDate hasta) {
        return findSolapados(idObra, desde, hasta).stream().findFirst();
    }
}
