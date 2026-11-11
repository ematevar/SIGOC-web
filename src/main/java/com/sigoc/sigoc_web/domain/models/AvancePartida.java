package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvancePartida {
    private Integer idAvance;
    private Integer idPartida;
    private LocalDate fechaReporte;
    private BigDecimal porcentajeAvance;
    private String observaciones;
    private Integer idUsuario;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public AvancePartida() {
    }

    public AvancePartida(Integer idAvance, Integer idPartida, LocalDate fechaReporte, BigDecimal porcentajeAvance, String observaciones, Integer idUsuario, boolean activo, LocalDateTime fechaBorrado) {
        this.idAvance = idAvance;
        this.idPartida = idPartida;
        this.fechaReporte = fechaReporte;
        this.porcentajeAvance = porcentajeAvance;
        this.observaciones = observaciones;
        this.idUsuario = idUsuario;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public LocalDate getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDate fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(BigDecimal porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
