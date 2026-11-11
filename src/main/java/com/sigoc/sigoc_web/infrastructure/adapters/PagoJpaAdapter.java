package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.infrastructure.entities.PagoEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.PagoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PagoJpaAdapter implements PagoPort {

    private final PagoJpaRepository repository;

    public PagoJpaAdapter(PagoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findAllActivos() {
        return repository.findByActivoTrueOrderByIdPagoDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findActivosByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByIdPagoDesc(idObra).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Integer idPago) {
        return repository.findById(idPago).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> findByCodigoOperacion(String codigoOperacion) {
        return repository.findByCodigoOperacion(codigoOperacion).map(this::toModel);
    }

    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        PagoEntity entity = pago.getIdPago() != null
                ? repository.findById(pago.getIdPago()).orElseGet(PagoEntity::new)
                : new PagoEntity();
        entity.setIdObra(pago.getIdObra());
        entity.setIdValorizacion(pago.getIdValorizacion());
        entity.setIdComprobante(pago.getIdComprobante());
        entity.setIdIngresoGenerado(pago.getIdIngresoGenerado());
        entity.setMonto(pago.getMonto());
        entity.setMetodoPago(pago.getMetodoPago());
        entity.setCodigoOperacion(pago.getCodigoOperacion());
        entity.setFechaPago(pago.getFechaPago());
        entity.setEstado(pago.getEstado());
        entity.setActivo(pago.isActivo());
        entity.setFechaBorrado(pago.getFechaBorrado());
        return toModel(repository.saveAndFlush(entity));
    }

    private Pago toModel(PagoEntity e) {
        return new Pago(
                e.getIdPago(),
                e.getIdObra(),
                e.getIdValorizacion(),
                e.getIdComprobante(),
                e.getIdIngresoGenerado(),
                e.getMonto(),
                e.getMetodoPago(),
                e.getCodigoOperacion(),
                e.getFechaPago(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
