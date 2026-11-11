package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Valorizacion {
    private Integer idValorizacion;
    private Integer idObra;
    private String periodo;
    private LocalDate fechaEmision;
    private BigDecimal porcentajeAvancePeriodo;
    private BigDecimal montoValorizado;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Valorizacion() {
    }

    public Valorizacion(Integer idValorizacion, Integer idObra, String periodo, LocalDate fechaEmision,
                        BigDecimal porcentajeAvancePeriodo, BigDecimal montoValorizado, String estado,
                        boolean activo, LocalDateTime fechaBorrado) {
        this.idValorizacion = idValorizacion;
        this.idObra = idObra;
        this.periodo = periodo;
        this.fechaEmision = fechaEmision;
        this.porcentajeAvancePeriodo = porcentajeAvancePeriodo;
        this.montoValorizado = montoValorizado;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public boolean estaPagada() {
        return "PAGADA".equalsIgnoreCase(estado);
    }

    public boolean puedeFacturarse() {
        return activo && "PENDIENTE".equalsIgnoreCase(estado);
    }

    public Integer getIdValorizacion() { return idValorizacion; }
    public void setIdValorizacion(Integer idValorizacion) { this.idValorizacion = idValorizacion; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getPorcentajeAvancePeriodo() { return porcentajeAvancePeriodo; }
    public void setPorcentajeAvancePeriodo(BigDecimal porcentajeAvancePeriodo) { this.porcentajeAvancePeriodo = porcentajeAvancePeriodo; }
    public BigDecimal getMontoValorizado() { return montoValorizado; }
    public void setMontoValorizado(BigDecimal montoValorizado) { this.montoValorizado = montoValorizado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
