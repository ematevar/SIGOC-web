package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;
import com.sigoc.sigoc_web.domain.ports.out.ProduccionPort;
import com.sigoc.sigoc_web.infraestructure.entities.ProduccionEmpleadoEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.ProduccionEmpleadoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ProduccionJpaAdapter implements ProduccionPort {

    private final ProduccionEmpleadoJpaRepository repository;

    public ProduccionJpaAdapter(ProduccionEmpleadoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduccionEmpleado> findAllActivas() {
        return repository.findByActivoTrueOrderByFechaDescIdProduccionDesc()
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduccionEmpleado> findActivasByPartida(Integer idPartida) {
        return repository.findByIdPartidaAndActivoTrueOrderByFechaDescIdProduccionDesc(idPartida)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public ProduccionEmpleado guardar(ProduccionEmpleado produccion) {
        ProduccionEmpleadoEntity e = new ProduccionEmpleadoEntity();
        e.setIdEmpleado(produccion.getIdEmpleado());
        e.setIdPartida(produccion.getIdPartida());
        e.setFecha(produccion.getFecha());
        e.setCantidadProducida(produccion.getCantidadProducida());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private ProduccionEmpleado toModel(ProduccionEmpleadoEntity e) {
        return new ProduccionEmpleado(
                e.getIdProduccion(),
                e.getIdEmpleado(),
                e.getIdPartida(),
                e.getFecha(),
                e.getCantidadProducida(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
