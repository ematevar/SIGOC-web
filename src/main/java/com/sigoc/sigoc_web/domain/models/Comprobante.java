package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Comprobante {
    private Integer idComprobante;
    private Integer idValorizacion;
    private String tipoComprobante;
    private String serie = "CV01";
    private Integer numero = 1;
    private String serieNumero;
    private LocalDate fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String codigoPago;
    private LocalDateTime venceEl;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Comprobante() {
    }

    public Comprobante(Integer idComprobante, Integer idValorizacion, String tipoComprobante, String serieNumero,
                       LocalDate fechaEmision, BigDecimal subtotal, BigDecimal igv, BigDecimal total,
                       String estado, boolean activo, LocalDateTime fechaBorrado) {
        this(idComprobante, idValorizacion, tipoComprobante, "CV01", 1, serieNumero,
                fechaEmision, subtotal, igv, total, null, null, estado, activo, fechaBorrado);
    }

    public Comprobante(Integer idComprobante, Integer idValorizacion, String tipoComprobante, String serie,
                       Integer numero, String serieNumero, LocalDate fechaEmision, BigDecimal subtotal,
                       BigDecimal igv, BigDecimal total, String codigoPago, LocalDateTime venceEl,
                       String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idComprobante = idComprobante;
        this.idValorizacion = idValorizacion;
        this.tipoComprobante = tipoComprobante;
        this.serie = serie != null ? serie : "CV01";
        this.numero = numero != null ? numero : 1;
        this.serieNumero = serieNumero;
        this.fechaEmision = fechaEmision;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
        this.codigoPago = codigoPago;
        this.venceEl = venceEl;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public boolean esPagable() {
        return activo && "EMITIDO".equalsIgnoreCase(estado);
    }

    public boolean esEnlaceVigente(LocalDateTime ahora) {
        return codigoPago != null && !codigoPago.isBlank()
                && (venceEl == null || !ahora.isAfter(venceEl));
    }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }
    public Integer getIdValorizacion() { return idValorizacion; }
    public void setIdValorizacion(Integer idValorizacion) { this.idValorizacion = idValorizacion; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getSerieNumero() { return serieNumero; }
    public void setSerieNumero(String serieNumero) { this.serieNumero = serieNumero; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCodigoPago() { return codigoPago; }
    public void setCodigoPago(String codigoPago) { this.codigoPago = codigoPago; }
    public LocalDateTime getVenceEl() { return venceEl; }
    public void setVenceEl(LocalDateTime venceEl) { this.venceEl = venceEl; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
