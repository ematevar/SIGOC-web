package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.EmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoJpaRepository extends JpaRepository<EmpleadoEntity, Integer> {

    List<EmpleadoEntity> findByActivoTrueOrderByIdEmpleadoDesc();

    @Query("select count(e) > 0 from EmpleadoEntity e where e.dni = :dni and (:idExcluir is null or e.idEmpleado <> :idExcluir)")
    boolean existsByDni(String dni, Integer idExcluir);
}
