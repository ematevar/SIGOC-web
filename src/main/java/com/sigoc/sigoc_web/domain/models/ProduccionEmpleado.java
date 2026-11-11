package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProduccionEmpleado {
    private Integer idProduccion;
    private Integer idEmpleado;
    private Integer idPartida;
    private LocalDate fecha;
    private BigDecimal cantidadProducida;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public ProduccionEmpleado() {
    }
    public ProduccionEmpleado(Integer idProduccion, Integer idEmpleado, Integer idPartida, LocalDate fecha, BigDecimal cantidadProducida, boolean activo, LocalDateTime fechaBorrado) {
        this.idProduccion = idProduccion;
        this.idEmpleado = idEmpleado;
        this.idPartida = idPartida;
        this.fecha = fecha;
        this.cantidadProducida = cantidadProducida;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdProduccion() {
        return idProduccion;
    }

    public void setIdProduccion(Integer idProduccion) {
        this.idProduccion = idProduccion;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidadProducida() {
        return cantidadProducida;
    }

    public void setCantidadProducida(BigDecimal cantidadProducida) {
        this.cantidadProducida = cantidadProducida;
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
