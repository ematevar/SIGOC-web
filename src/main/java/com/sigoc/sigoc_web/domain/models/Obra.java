package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Obra {
    private Integer idObra;
    private String nombre;
    private String cliente;
    private String ubicacion;
    private BigDecimal presupuestoTotal;
    private LocalDate fechaInicioPlan;
    private LocalDate fechaFinPlan;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Obra() {
    }
    public Obra(Integer idObra, String nombre, String cliente, String ubicacion, BigDecimal presupuestoTotal, LocalDate fechaInicioPlan, LocalDate fechaFinPlan, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idObra = idObra;
        this.nombre = nombre;
        this.cliente = cliente;
        this.ubicacion = ubicacion;
        this.presupuestoTotal = presupuestoTotal;
        this.fechaInicioPlan = fechaInicioPlan;
        this.fechaFinPlan = fechaFinPlan;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
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

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(BigDecimal presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }

    public LocalDate getFechaInicioPlan() {
        return fechaInicioPlan;
    }

    public void setFechaInicioPlan(LocalDate fechaInicioPlan) {
        this.fechaInicioPlan = fechaInicioPlan;
    }

    public LocalDate getFechaFinPlan() {
        return fechaFinPlan;
    }

    public void setFechaFinPlan(LocalDate fechaFinPlan) {
        this.fechaFinPlan = fechaFinPlan;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    /**
     * Una obra es operable para partidas y movimientos si está activa y no fue Finalizada/Cerrada/Inactiva.
     */
    public boolean isOperable() {
        if (!activo) {
            return false;
        }
        if (estado == null) {
            return true;
        }
        String e = estado.trim();
        return !e.equalsIgnoreCase("Finalizada")
                && !e.equalsIgnoreCase("Inactiva")
                && !e.equalsIgnoreCase("Cerrada");
    }
}
