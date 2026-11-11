package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.infraestructure.entities.ValorizacionEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.ValorizacionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ValorizacionJpaAdapter implements ValorizacionPort {

    private final ValorizacionJpaRepository repository;

    public ValorizacionJpaAdapter(ValorizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Valorizacion> findAllActivas() {
        return repository.findByActivoTrueOrderByIdValorizacionDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Valorizacion> findActivasByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByIdValorizacionDesc(idObra)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Valorizacion> findById(Integer idValorizacion) {
        return repository.findById(idValorizacion).map(this::toModel);
    }

    @Override
    @Transactional
    public Valorizacion guardar(Valorizacion valorizacion) {
        ValorizacionEntity entity = valorizacion.getIdValorizacion() != null
                ? repository.findById(valorizacion.getIdValorizacion()).orElseGet(ValorizacionEntity::new)
                : new ValorizacionEntity();
        entity.setIdObra(valorizacion.getIdObra());
        entity.setPeriodo(valorizacion.getPeriodo());
        entity.setFechaEmision(valorizacion.getFechaEmision());
        entity.setPorcentajeAvancePeriodo(valorizacion.getPorcentajeAvancePeriodo());
        entity.setMontoValorizado(valorizacion.getMontoValorizado());
        entity.setEstado(valorizacion.getEstado());
        entity.setActivo(valorizacion.isActivo());
        entity.setFechaBorrado(valorizacion.getFechaBorrado());
        return toModel(repository.saveAndFlush(entity));
    }

    private Valorizacion toModel(ValorizacionEntity e) {
        return new Valorizacion(
                e.getIdValorizacion(),
                e.getIdObra(),
                e.getPeriodo(),
                e.getFechaEmision(),
                e.getPorcentajeAvancePeriodo(),
                e.getMontoValorizado(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
