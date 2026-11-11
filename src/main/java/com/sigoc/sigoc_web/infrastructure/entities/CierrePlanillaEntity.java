package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_planilla")
public class CierrePlanillaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Integer idCierre;

    @Column(name = "id_obra", nullable = false)
    private Integer idObra;

    @Column(name = "fecha_inicio_periodo", nullable = false)
    private LocalDate fechaInicioPeriodo;

    @Column(name = "fecha_fin_periodo", nullable = false)
    private LocalDate fechaFinPeriodo;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public CierrePlanillaEntity() {
    }

    public Integer getIdCierre() { return idCierre; }
    public void setIdCierre(Integer idCierre) { this.idCierre = idCierre; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public LocalDate getFechaInicioPeriodo() { return fechaInicioPeriodo; }
    public void setFechaInicioPeriodo(LocalDate fechaInicioPeriodo) { this.fechaInicioPeriodo = fechaInicioPeriodo; }
    public LocalDate getFechaFinPeriodo() { return fechaFinPeriodo; }
    public void setFechaFinPeriodo(LocalDate fechaFinPeriodo) { this.fechaFinPeriodo = fechaFinPeriodo; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
