package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "obra")
@EntityListeners(AuditoriaEntityListener.class)
public class ObraEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obra")
    private Integer idObra;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "presupuesto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal presupuestoTotal;

    @Column(name = "fecha_inicio_plan", nullable = false)
    private LocalDate fechaInicioPlan;

    @Column(name = "fecha_fin_plan", nullable = false)
    private LocalDate fechaFinPlan;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Planificada";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    @Column(name = "id_usuario_registro")
    private Integer idUsuarioRegistro;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public ObraEntity() {
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

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(BigDecimal presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }

    public LocalDate getFechaInicioPlan() {
        return fechaInicioPlan;
    }

    public void setFechaInicioPlan(LocalDate fechaInicioPlan) {
        this.fechaInicioPlan = fechaInicioPlan;
    }

    public LocalDate getFechaFinPlan() {
        return fechaFinPlan;
    }

    public void setFechaFinPlan(LocalDate fechaFinPlan) {
        this.fechaFinPlan = fechaFinPlan;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    @Override
    public Integer getIdUsuarioRegistro() {
        return idUsuarioRegistro;
    }

    @Override
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) {
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    @Override
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    @Override
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
