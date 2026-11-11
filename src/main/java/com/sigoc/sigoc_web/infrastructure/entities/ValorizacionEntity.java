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

    @Column(name = "numero", nullable = false)
    private Integer numero = 1;

    @Column(name = "periodo", nullable = false, length = 30)
    private String periodo;

    @Column(name = "periodo_inicio")
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin")
    private LocalDate periodoFin;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "avance_acumulado", nullable = false, precision = 5, scale = 2)
    private BigDecimal avanceAcumulado = BigDecimal.ZERO;

    @Column(name = "porcentaje_avance_periodo", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvancePeriodo;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

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
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }
    public LocalDate getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(LocalDate periodoFin) { this.periodoFin = periodoFin; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getAvanceAcumulado() { return avanceAcumulado; }
    public void setAvanceAcumulado(BigDecimal avanceAcumulado) { this.avanceAcumulado = avanceAcumulado; }
    public BigDecimal getPorcentajeAvancePeriodo() { return porcentajeAvancePeriodo; }
    public void setPorcentajeAvancePeriodo(BigDecimal porcentajeAvancePeriodo) { this.porcentajeAvancePeriodo = porcentajeAvancePeriodo; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getMontoValorizado() { return montoValorizado; }
    public void setMontoValorizado(BigDecimal montoValorizado) { this.montoValorizado = montoValorizado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
