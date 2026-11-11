package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobante")
public class ComprobanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @Column(name = "id_valorizacion", nullable = false)
    private Integer idValorizacion;

    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private String tipoComprobante;

    @Column(name = "serie_numero", nullable = false, unique = true, length = 25)
    private String serieNumero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado = "EMITIDO";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public ComprobanteEntity() {
    }

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }
    public Integer getIdValorizacion() { return idValorizacion; }
    public void setIdValorizacion(Integer idValorizacion) { this.idValorizacion = idValorizacion; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
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
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
