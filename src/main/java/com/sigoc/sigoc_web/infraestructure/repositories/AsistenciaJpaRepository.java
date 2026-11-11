package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.AsistenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaJpaRepository extends JpaRepository<AsistenciaEntity, Integer> {

    List<AsistenciaEntity> findByActivoTrueOrderByFechaDescIdAsistenciaDesc();

    List<AsistenciaEntity> findByIdObraAndActivoTrueOrderByFechaDescIdAsistenciaDesc(Integer idObra);

    Optional<AsistenciaEntity> findFirstByIdEmpleadoAndIdObraAndFechaAndActivoTrue(
            Integer idEmpleado, Integer idObra, LocalDate fecha);

    List<AsistenciaEntity> findByIdObraAndFechaBetweenAndActivoTrue(
            Integer idObra, LocalDate desde, LocalDate hasta);
}
