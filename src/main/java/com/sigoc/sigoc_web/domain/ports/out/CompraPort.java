package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.DetalleCompra;

import java.util.List;

public interface CompraPort {
    List<CompraMaterial> findAllActivas();

    boolean existsByProveedorYComprobante(Integer idProveedor, String numeroComprobante);

    CompraMaterial guardarCompraConDetalle(CompraMaterial compra, DetalleCompra detalle);
}
