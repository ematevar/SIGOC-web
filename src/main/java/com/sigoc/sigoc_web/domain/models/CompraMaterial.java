package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompraMaterial {
    private Integer idCompra;
    private Integer idProveedor;
    private String tipoComprobante;
    private LocalDate fechaCompra;
    private String numeroComprobante;
    private BigDecimal total;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public CompraMaterial() {
    }

    public CompraMaterial(Integer idCompra, Integer idProveedor, String tipoComprobante, LocalDate fechaCompra, String numeroComprobante, BigDecimal total, boolean activo, LocalDateTime fechaBorrado) {
        this.idCompra = idCompra;
        this.idProveedor = idProveedor;
        this.tipoComprobante = tipoComprobante;
        this.fechaCompra = fechaCompra;
        this.numeroComprobante = numeroComprobante;
        this.total = total;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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
