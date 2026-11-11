package com.sigoc.sigoc_web.infraestructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida_material")
public class PartidaMaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida_material")
    private Integer idPartidaMaterial;

    @Column(name = "id_partida", nullable = false)
    private Integer idPartida;

    @Column(name = "id_material", nullable = false)
    private Integer idMaterial;

    @Column(name = "cantidad_presupuestada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadPresupuestada;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public PartidaMaterialEntity() {
    }

    public Integer getIdPartidaMaterial() {
        return idPartidaMaterial;
    }

    public void setIdPartidaMaterial(Integer idPartidaMaterial) {
        this.idPartidaMaterial = idPartidaMaterial;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public Integer getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Integer idMaterial) {
        this.idMaterial = idMaterial;
    }

    public BigDecimal getCantidadPresupuestada() {
        return cantidadPresupuestada;
    }

    public void setCantidadPresupuestada(BigDecimal cantidadPresupuestada) {
        this.cantidadPresupuestada = cantidadPresupuestada;
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
}
