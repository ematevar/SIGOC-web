package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "valorizacion")
public class ValorizacionEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valorizacion")
    private Integer idValorizacion;

    @Column(name = "id_obra", nullable = false)
    private Integer idObra;

    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "porcentaje_avance_periodo", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvancePeriodo;

    @Column(name = "monto_valorizado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoValorizado;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado = "PENDIENTE";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public ValorizacionEntity() {
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
