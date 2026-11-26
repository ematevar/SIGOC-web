package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-07: Registro y consulta de compras de materiales con actualización de stock y egreso.
 */
public interface GestionarCompraUseCase {

    record ItemCompra(Integer idMaterial, BigDecimal cantidad, BigDecimal precioUnitario) {
    }

    List<CompraMaterial> listar(UsuarioSesion actor);

    CompraMaterial registrar(UsuarioSesion actor, Integer idProveedor, Integer idObra,
                             Integer idMaterial, String tipoComprobante, String numeroComprobante,
                             LocalDate fechaCompra, BigDecimal cantidad, BigDecimal precioUnitario);

    CompraMaterial registrarConItems(UsuarioSesion actor, Integer idProveedor, Integer idObra,
                                     String tipoComprobante, String numeroComprobante,
                                     LocalDate fechaCompra, List<ItemCompra> items);
}
