package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.ConsumoMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ConsumoMaterialJpaRepository extends JpaRepository<ConsumoMaterialEntity, Integer> {

    List<ConsumoMaterialEntity> findByActivoTrueOrderByFechaConsumoDescIdConsumoDesc();

    List<ConsumoMaterialEntity> findByIdPartidaAndActivoTrueOrderByFechaConsumoDescIdConsumoDesc(Integer idPartida);

    @Query("select coalesce(sum(c.cantidad), 0) from ConsumoMaterialEntity c "
            + "where c.idPartida = :idPartida and c.idMaterial = :idMaterial and c.activo = true")
    BigDecimal sumaCantidadPorPartidaYMaterial(Integer idPartida, Integer idMaterial);
}
