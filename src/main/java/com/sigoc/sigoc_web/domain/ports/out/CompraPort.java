package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.DetalleCompra;

import java.util.List;

public interface CompraPort {
    List<CompraMaterial> findAllActivas();

    boolean existsByProveedorYComprobante(Integer idProveedor, String numeroComprobante);

    CompraMaterial guardarCompraConDetalle(CompraMaterial compra, DetalleCompra detalle);

    default CompraMaterial guardarCompraConDetalles(CompraMaterial compra, List<DetalleCompra> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un detalle de compra.");
        }
        CompraMaterial guardada = null;
        for (DetalleCompra d : detalles) {
            guardada = guardarCompraConDetalle(compra, d);
        }
        return guardada;
    }
}
