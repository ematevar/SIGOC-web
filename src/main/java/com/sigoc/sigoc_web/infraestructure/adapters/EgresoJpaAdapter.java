package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.ports.out.EgresoPort;
import com.sigoc.sigoc_web.infraestructure.entities.EgresoEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.EgresoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EgresoJpaAdapter implements EgresoPort {

    private final EgresoJpaRepository repository;

    public EgresoJpaAdapter(EgresoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Egreso> findAllActivos() {
        return repository.findByActivoTrueOrderByFechaDescIdEgresoDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Egreso> findActivosByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByFechaDescIdEgresoDesc(idObra)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public Egreso guardar(Egreso egreso) {
        EgresoEntity e = new EgresoEntity();
        e.setIdObra(egreso.getIdObra());
        e.setFecha(egreso.getFecha());
        e.setTipoEgreso(egreso.getTipoEgreso());
        e.setConcepto(egreso.getConcepto());
        e.setMonto(egreso.getMonto());
        e.setIdCompra(egreso.getIdCompra());
        e.setIdCierre(egreso.getIdCierre());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private Egreso toModel(EgresoEntity e) {
        return new Egreso(
                e.getIdEgreso(),
                e.getIdObra(),
                e.getFecha(),
                e.getTipoEgreso(),
                e.getConcepto(),
                e.getMonto(),
                e.getIdCompra(),
                e.getIdCierre(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
