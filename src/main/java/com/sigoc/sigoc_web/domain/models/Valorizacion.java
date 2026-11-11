package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Valorizacion {
    private Integer idValorizacion;
    private Integer idObra;
    private Integer numero = 1;
    private String periodo;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private LocalDate fechaEmision;
    private BigDecimal avanceAcumulado = BigDecimal.ZERO;
    private BigDecimal porcentajeAvancePeriodo;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal igv = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal montoValorizado;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Valorizacion() {
    }

    public Valorizacion(Integer idValorizacion, Integer idObra, String periodo, LocalDate fechaEmision,
                        BigDecimal porcentajeAvancePeriodo, BigDecimal montoValorizado, String estado,
                        boolean activo, LocalDateTime fechaBorrado) {
        this(idValorizacion, idObra, 1, periodo, null, null, fechaEmision, BigDecimal.ZERO,
                porcentajeAvancePeriodo, BigDecimal.ZERO, BigDecimal.ZERO, montoValorizado,
                montoValorizado, estado, activo, fechaBorrado);
    }

    public Valorizacion(Integer idValorizacion, Integer idObra, Integer numero, String periodo,
                        LocalDate periodoInicio, LocalDate periodoFin, LocalDate fechaEmision,
                        BigDecimal avanceAcumulado, BigDecimal porcentajeAvancePeriodo,
                        BigDecimal subtotal, BigDecimal igv, BigDecimal total,
                        BigDecimal montoValorizado, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idValorizacion = idValorizacion;
        this.idObra = idObra;
        this.numero = numero != null ? numero : 1;
        this.periodo = periodo;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.fechaEmision = fechaEmision;
        this.avanceAcumulado = avanceAcumulado;
        this.porcentajeAvancePeriodo = porcentajeAvancePeriodo;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
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
