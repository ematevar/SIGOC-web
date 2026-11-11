package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ingreso {
    private Integer idIngreso;
    private Integer idObra;
    private Integer idPago;
    private LocalDate fecha;
    private String concepto;
    private BigDecimal monto;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Ingreso() {
    }

    public Ingreso(Integer idIngreso, Integer idObra, LocalDate fecha, String concepto,
                   BigDecimal monto, boolean activo, LocalDateTime fechaBorrado) {
        this(idIngreso, idObra, null, fecha, concepto, monto, activo, fechaBorrado);
    }

    public Ingreso(Integer idIngreso, Integer idObra, Integer idPago, LocalDate fecha, String concepto,
                   BigDecimal monto, boolean activo, LocalDateTime fechaBorrado) {
        this.idIngreso = idIngreso;
        this.idObra = idObra;
        this.idPago = idPago;
        this.fecha = fecha;
        this.concepto = concepto;
        this.monto = monto;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdIngreso() { return idIngreso; }
    public void setIdIngreso(Integer idIngreso) { this.idIngreso = idIngreso; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
