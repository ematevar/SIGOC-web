package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Ingreso;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.infrastructure.entities.IngresoEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.IngresoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class IngresoJpaAdapter implements IngresoPort {

    private final IngresoJpaRepository repository;

    public IngresoJpaAdapter(IngresoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingreso> findAllActivos() {
        return repository.findByActivoTrueOrderByFechaDescIdIngresoDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingreso> findActivosByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByFechaDescIdIngresoDesc(idObra)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public Ingreso guardar(Ingreso ingreso) {
        IngresoEntity e = new IngresoEntity();
        e.setIdObra(ingreso.getIdObra());
        e.setFecha(ingreso.getFecha());
        e.setConcepto(ingreso.getConcepto());
        e.setMonto(ingreso.getMonto());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private Ingreso toModel(IngresoEntity e) {
        return new Ingreso(
                e.getIdIngreso(),
                e.getIdObra(),
                e.getFecha(),
                e.getConcepto(),
                e.getMonto(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
