package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.infrastructure.entities.ComprobanteEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.ComprobanteJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ComprobanteJpaAdapter implements ComprobantePort {

    private final ComprobanteJpaRepository repository;

    public ComprobanteJpaAdapter(ComprobanteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comprobante> findAllActivos() {
        return repository.findByActivoTrueOrderByIdComprobanteDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprobante> findById(Integer idComprobante) {
        return repository.findById(idComprobante).map(this::toModel);
    }

    @Override
    @Transactional
    public Optional<Comprobante> findByIdParaActualizar(Integer idComprobante) {
        return repository.findByIdParaActualizar(idComprobante).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprobante> findActivoByValorizacion(Integer idValorizacion) {
        return repository.findFirstByIdValorizacionAndActivoTrue(idValorizacion).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprobante> findBySerieNumero(String serieNumero) {
        return repository.findBySerieNumero(serieNumero).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprobante> findByCodigoPago(String codigoPago) {
        return repository.findByCodigoPagoAndActivoTrue(codigoPago).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public int maxNumeroBySerie(String serie) {
        return repository.maxNumeroBySerie(serie);
    }

    @Override
    @Transactional
    public Comprobante guardar(Comprobante comprobante) {
        ComprobanteEntity entity = comprobante.getIdComprobante() != null
                ? repository.findById(comprobante.getIdComprobante()).orElseGet(ComprobanteEntity::new)
                : new ComprobanteEntity();
        entity.setIdValorizacion(comprobante.getIdValorizacion());
        entity.setTipoComprobante(comprobante.getTipoComprobante());
        entity.setSerie(comprobante.getSerie() != null ? comprobante.getSerie() : "CV01");
        entity.setNumero(comprobante.getNumero() != null ? comprobante.getNumero() : 1);
        entity.setSerieNumero(comprobante.getSerieNumero());
        entity.setFechaEmision(comprobante.getFechaEmision());
        entity.setSubtotal(comprobante.getSubtotal());
        entity.setIgv(comprobante.getIgv());
        entity.setTotal(comprobante.getTotal());
        entity.setCodigoPago(comprobante.getCodigoPago());
        entity.setVenceEl(comprobante.getVenceEl());
        entity.setEstado(comprobante.getEstado());
        entity.setActivo(comprobante.isActivo());
        entity.setFechaBorrado(comprobante.getFechaBorrado());
        return toModel(repository.saveAndFlush(entity));
    }

    private Comprobante toModel(ComprobanteEntity e) {
        return new Comprobante(
                e.getIdComprobante(),
                e.getIdValorizacion(),
                e.getTipoComprobante(),
                e.getSerie(),
                e.getNumero(),
                e.getSerieNumero(),
                e.getFechaEmision(),
                e.getSubtotal(),
                e.getIgv(),
                e.getTotal(),
                e.getCodigoPago(),
                e.getVenceEl(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
