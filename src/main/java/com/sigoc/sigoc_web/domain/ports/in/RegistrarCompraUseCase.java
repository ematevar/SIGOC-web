package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.CompraMaterial;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegistrarCompraUseCase {
    CompraMaterial ejecutar(UsuarioSesion actor, Integer idMaterial, Integer idProveedor, Integer idObra,
                            String tipoComprobante, String numeroComprobante, LocalDate fechaCompra,
                            BigDecimal cantidad, BigDecimal precioUnitario);
}
