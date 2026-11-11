package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private Integer idPago;
    private Integer idObra;
    private Integer idValorizacion;
    private Integer idComprobante;
    private Integer idIngresoGenerado;
    private BigDecimal monto;
    private String metodoPago;
    private String codigoOperacion;
    private LocalDateTime fechaPago;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Pago() {
    }

    public Pago(Integer idPago, Integer idObra, Integer idValorizacion, Integer idComprobante,
                Integer idIngresoGenerado, BigDecimal monto, String metodoPago, String codigoOperacion,
                LocalDateTime fechaPago, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idPago = idPago;
        this.idObra = idObra;
        this.idValorizacion = idValorizacion;
        this.idComprobante = idComprobante;
        this.idIngresoGenerado = idIngresoGenerado;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.codigoOperacion = codigoOperacion;
        this.fechaPago = fechaPago;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public Integer getIdValorizacion() { return idValorizacion; }
    public void setIdValorizacion(Integer idValorizacion) { this.idValorizacion = idValorizacion; }
    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }
    public Integer getIdIngresoGenerado() { return idIngresoGenerado; }
    public void setIdIngresoGenerado(Integer idIngresoGenerado) { this.idIngresoGenerado = idIngresoGenerado; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
