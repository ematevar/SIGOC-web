package com.sigoc.sigoc_web.infraestructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partida")
public class PartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida")
    private Integer idPartida;

    @Column(name = "id_obra", nullable = false)
    private Integer idObra;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "metrado_planificado", nullable = false, precision = 10, scale = 2)
    private BigDecimal metradoPlanificado;

    @Column(name = "peso_presupuestal", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoPresupuestal;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public PartidaEntity() {
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getMetradoPlanificado() {
        return metradoPlanificado;
    }

    public void setMetradoPlanificado(BigDecimal metradoPlanificado) {
        this.metradoPlanificado = metradoPlanificado;
    }

    public BigDecimal getPesoPresupuestal() {
        return pesoPresupuestal;
    }

    public void setPesoPresupuestal(BigDecimal pesoPresupuestal) {
        this.pesoPresupuestal = pesoPresupuestal;
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
