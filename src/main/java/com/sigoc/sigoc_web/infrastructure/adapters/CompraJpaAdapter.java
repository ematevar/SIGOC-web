package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.DetalleCompra;
import com.sigoc.sigoc_web.domain.ports.out.CompraPort;
import com.sigoc.sigoc_web.infrastructure.entities.CompraMaterialEntity;
import com.sigoc.sigoc_web.infrastructure.entities.DetalleCompraEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.CompraMaterialJpaRepository;
import com.sigoc.sigoc_web.infrastructure.repositories.DetalleCompraJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CompraJpaAdapter implements CompraPort {

    private final CompraMaterialJpaRepository compraRepo;
    private final DetalleCompraJpaRepository detalleRepo;

    public CompraJpaAdapter(CompraMaterialJpaRepository compraRepo, DetalleCompraJpaRepository detalleRepo) {
        this.compraRepo = compraRepo;
        this.detalleRepo = detalleRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraMaterial> findAllActivas() {
        return compraRepo.findByActivoTrueOrderByFechaCompraDescIdCompraDesc().stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public boolean existsByProveedorYComprobante(Integer idProveedor, String numeroComprobante) {
        return compraRepo.existsByProveedorYComprobante(idProveedor, numeroComprobante);
    }

    @Override
    @Transactional
    public CompraMaterial guardarCompraConDetalle(CompraMaterial compra, DetalleCompra detalle) {
        return guardarCompraConDetalles(compra, List.of(detalle));
    }

    @Override
    @Transactional
    public CompraMaterial guardarCompraConDetalles(CompraMaterial compra, List<DetalleCompra> detalles) {
        CompraMaterialEntity ce = new CompraMaterialEntity();
        ce.setIdProveedor(compra.getIdProveedor());
        ce.setTipoComprobante(compra.getTipoComprobante());
        ce.setFechaCompra(compra.getFechaCompra());
        ce.setNumeroComprobante(compra.getNumeroComprobante());
        ce.setTotal(compra.getTotal());
        ce.setActivo(true);
        CompraMaterialEntity guardada = compraRepo.saveAndFlush(ce);

        List<DetalleCompraEntity> entidades = detalles.stream().map(detalle -> {
            DetalleCompraEntity de = new DetalleCompraEntity();
            de.setIdCompra(guardada.getIdCompra());
            de.setIdMaterial(detalle.getIdMaterial());
            de.setCantidad(detalle.getCantidad());
            de.setPrecioUnitario(detalle.getPrecioUnitario());
            de.setSubtotal(detalle.getSubtotal());
            de.setActivo(true);
            return de;
        }).toList();
        detalleRepo.saveAll(entidades);

        return toModel(guardada);
    }

    private CompraMaterial toModel(CompraMaterialEntity e) {
        return new CompraMaterial(
                e.getIdCompra(),
                e.getIdProveedor(),
                e.getTipoComprobante(),
                e.getFechaCompra(),
                e.getNumeroComprobante(),
                e.getTotal(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
