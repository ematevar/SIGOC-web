package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PartidaMaterial {
    private Integer idPartidaMaterial;
    private Integer idPartida;
    private Integer idMaterial;
    private BigDecimal cantidadPresupuestada;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public PartidaMaterial() {
    }
    public PartidaMaterial(Integer idPartidaMaterial, Integer idPartida, Integer idMaterial, BigDecimal cantidadPresupuestada, boolean activo, LocalDateTime fechaBorrado) {
        this.idPartidaMaterial = idPartidaMaterial;
        this.idPartida = idPartida;
        this.idMaterial = idMaterial;
        this.cantidadPresupuestada = cantidadPresupuestada;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdPartidaMaterial() {
        return idPartidaMaterial;
    }

    public void setIdPartidaMaterial(Integer idPartidaMaterial) {
        this.idPartidaMaterial = idPartidaMaterial;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public Integer getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Integer idMaterial) {
        this.idMaterial = idMaterial;
    }

    public BigDecimal getCantidadPresupuestada() {
        return cantidadPresupuestada;
    }

    public void setCantidadPresupuestada(BigDecimal cantidadPresupuestada) {
        this.cantidadPresupuestada = cantidadPresupuestada;
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
