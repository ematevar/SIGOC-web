package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Egreso {
    private Integer idEgreso;
    private Integer idObra;
    private LocalDate fecha;
    private String tipoEgreso;
    private String concepto;
    private BigDecimal monto;
    private Integer idCompra;
    private Integer idCierre;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Egreso() {
    }
    public Egreso(Integer idEgreso, Integer idObra, LocalDate fecha, String tipoEgreso, String concepto, BigDecimal monto, Integer idCompra, Integer idCierre, boolean activo, LocalDateTime fechaBorrado) {
        this.idEgreso = idEgreso;
        this.idObra = idObra;
        this.fecha = fecha;
        this.tipoEgreso = tipoEgreso;
        this.concepto = concepto;
        this.monto = monto;
        this.idCompra = idCompra;
        this.idCierre = idCierre;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdEgreso() {
        return idEgreso;
    }

    public void setIdEgreso(Integer idEgreso) {
        this.idEgreso = idEgreso;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipoEgreso() {
        return tipoEgreso;
    }

    public void setTipoEgreso(String tipoEgreso) {
        this.tipoEgreso = tipoEgreso;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
    }

    public Integer getIdCierre() {
        return idCierre;
    }

    public void setIdCierre(Integer idCierre) {
        this.idCierre = idCierre;
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
