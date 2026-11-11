package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "egreso")
public class EgresoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_egreso")
    private Integer idEgreso;

    @Column(name = "id_obra", nullable = false)
    private Integer idObra;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "tipo_egreso", nullable = false, length = 20)
    private String tipoEgreso;

    @Column(name = "concepto", nullable = false, length = 150)
    private String concepto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "id_compra")
    private Integer idCompra;

    @Column(name = "id_cierre")
    private Integer idCierre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public EgresoEntity() {
    }

    public Integer getIdEgreso() { return idEgreso; }
    public void setIdEgreso(Integer idEgreso) { this.idEgreso = idEgreso; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getTipoEgreso() { return tipoEgreso; }
    public void setTipoEgreso(String tipoEgreso) { this.tipoEgreso = tipoEgreso; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }
    public Integer getIdCierre() { return idCierre; }
    public void setIdCierre(Integer idCierre) { this.idCierre = idCierre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
