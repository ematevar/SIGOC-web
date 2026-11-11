package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avance_partida")
public class AvancePartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avance")
    private Integer idAvance;

    @Column(name = "id_partida", nullable = false)
    private Integer idPartida;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDate fechaReporte;

    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvance;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public AvancePartidaEntity() {
    }

    public Integer getIdAvance() {
        return idAvance;
    }

    public void setIdAvance(Integer idAvance) {
        this.idAvance = idAvance;
    }

    public Integer getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    public LocalDate getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDate fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(BigDecimal porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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
