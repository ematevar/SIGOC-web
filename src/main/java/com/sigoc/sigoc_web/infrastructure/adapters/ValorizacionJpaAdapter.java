package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.infrastructure.entities.ValorizacionEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.ValorizacionJpaRepository;
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
    public Optional<Valorizacion> findByIdParaActualizar(Integer idValorizacion) {
        return repository.findByIdParaActualizar(idValorizacion).map(this::toModel);
    }

    @Override
    @Transactional
    public Valorizacion guardar(Valorizacion valorizacion) {
        ValorizacionEntity entity = valorizacion.getIdValorizacion() != null
                ? repository.findById(valorizacion.getIdValorizacion()).orElseGet(ValorizacionEntity::new)
                : new ValorizacionEntity();
        entity.setIdObra(valorizacion.getIdObra());
        entity.setNumero(valorizacion.getNumero() != null ? valorizacion.getNumero() : 1);
        entity.setPeriodo(valorizacion.getPeriodo());
        entity.setPeriodoInicio(valorizacion.getPeriodoInicio());
        entity.setPeriodoFin(valorizacion.getPeriodoFin());
        entity.setFechaEmision(valorizacion.getFechaEmision());
        entity.setAvanceAcumulado(valorizacion.getAvanceAcumulado());
        entity.setPorcentajeAvancePeriodo(valorizacion.getPorcentajeAvancePeriodo());
        entity.setSubtotal(valorizacion.getSubtotal());
        entity.setIgv(valorizacion.getIgv());
        entity.setTotal(valorizacion.getTotal());
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
                e.getNumero(),
                e.getPeriodo(),
                e.getPeriodoInicio(),
                e.getPeriodoFin(),
                e.getFechaEmision(),
                e.getAvanceAcumulado(),
                e.getPorcentajeAvancePeriodo(),
                e.getSubtotal(),
                e.getIgv(),
                e.getTotal(),
                e.getMontoValorizado(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
