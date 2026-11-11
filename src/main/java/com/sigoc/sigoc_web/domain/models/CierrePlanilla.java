package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CierrePlanilla {
    private Integer idCierre;
    private Integer idObra;
    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;
    private LocalDateTime fechaCierre;
    private BigDecimal montoTotal;
    private Integer idUsuario;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public CierrePlanilla() {
    }

    public CierrePlanilla(Integer idCierre, Integer idObra, LocalDate fechaInicioPeriodo, LocalDate fechaFinPeriodo, LocalDateTime fechaCierre, BigDecimal montoTotal, Integer idUsuario, boolean activo, LocalDateTime fechaBorrado) {
        this.idCierre = idCierre;
        this.idObra = idObra;
        this.fechaInicioPeriodo = fechaInicioPeriodo;
        this.fechaFinPeriodo = fechaFinPeriodo;
        this.fechaCierre = fechaCierre;
        this.montoTotal = montoTotal;
        this.idUsuario = idUsuario;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdCierre() {
        return idCierre;
    }

    public void setIdCierre(Integer idCierre) {
        this.idCierre = idCierre;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public LocalDate getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(LocalDate fechaInicioPeriodo) {
        this.fechaInicioPeriodo = fechaInicioPeriodo;
    }

    public LocalDate getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(LocalDate fechaFinPeriodo) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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
