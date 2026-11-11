package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.ProduccionEmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduccionEmpleadoJpaRepository extends JpaRepository<ProduccionEmpleadoEntity, Integer> {

    List<ProduccionEmpleadoEntity> findByActivoTrueOrderByFechaDescIdProduccionDesc();

    List<ProduccionEmpleadoEntity> findByIdPartidaAndActivoTrueOrderByFechaDescIdProduccionDesc(Integer idPartida);
}
