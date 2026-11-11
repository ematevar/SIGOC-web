package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Partida {
    private Integer idPartida;
    private Integer idObra;
    private String nombre;
    private String unidadMedida;
    private BigDecimal metradoPlanificado;
    private BigDecimal pesoPresupuestal;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Partida() {
    }

    public Partida(Integer idPartida, Integer idObra, String nombre, String unidadMedida, BigDecimal metradoPlanificado, BigDecimal pesoPresupuestal, boolean activo, LocalDateTime fechaBorrado) {
        this.idPartida = idPartida;
        this.idObra = idObra;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.metradoPlanificado = metradoPlanificado;
        this.pesoPresupuestal = pesoPresupuestal;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getMetradoPlanificado() {
        return metradoPlanificado;
    }

    public void setMetradoPlanificado(BigDecimal metradoPlanificado) {
        this.metradoPlanificado = metradoPlanificado;
    }

    public BigDecimal getPesoPresupuestal() {
        return pesoPresupuestal;
    }

    public void setPesoPresupuestal(BigDecimal pesoPresupuestal) {
        this.pesoPresupuestal = pesoPresupuestal;
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
