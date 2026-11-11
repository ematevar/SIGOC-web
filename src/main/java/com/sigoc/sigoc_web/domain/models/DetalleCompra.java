package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetalleCompra {
    private Integer idDetalleCompra;
    private Integer idCompra;
    private Integer idMaterial;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public DetalleCompra() {
    }

    public DetalleCompra(Integer idDetalleCompra, Integer idCompra, Integer idMaterial, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, boolean activo, LocalDateTime fechaBorrado) {
        this.idDetalleCompra = idDetalleCompra;
        this.idCompra = idCompra;
        this.idMaterial = idMaterial;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(Integer idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
    }

    public Integer getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Integer idMaterial) {
        this.idMaterial = idMaterial;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaBorrado() {
        return fechaBorrado;
    }

    public void setFechaBorrado(LocalDateTime fechaBorrado) {
        this.fechaBorrado = fechaBorrado;
    }
}
