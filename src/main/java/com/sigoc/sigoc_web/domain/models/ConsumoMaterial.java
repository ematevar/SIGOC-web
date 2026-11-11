package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConsumoMaterial {
    private Integer idConsumo;
    private Integer idPartida;
    private Integer idMaterial;
    private LocalDate fechaConsumo;
    private BigDecimal cantidad;
    private Integer idUsuario;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public ConsumoMaterial() {
    }

    public ConsumoMaterial(Integer idConsumo, Integer idPartida, Integer idMaterial, LocalDate fechaConsumo, BigDecimal cantidad, Integer idUsuario, boolean activo, LocalDateTime fechaBorrado) {
        this.idConsumo = idConsumo;
        this.idPartida = idPartida;
        this.idMaterial = idMaterial;
        this.fechaConsumo = fechaConsumo;
        this.cantidad = cantidad;
        this.idUsuario = idUsuario;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdConsumo() {
        return idConsumo;
    }

    public void setIdConsumo(Integer idConsumo) {
        this.idConsumo = idConsumo;
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

    public LocalDate getFechaConsumo() {
        return fechaConsumo;
    }

    public void setFechaConsumo(LocalDate fechaConsumo) {
        this.fechaConsumo = fechaConsumo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
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
