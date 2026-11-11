package com.sigoc.sigoc_web.domain.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Asistencia {
    private Integer idAsistencia;
    private Integer idEmpleado;
    private Integer idObra;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Asistencia() {
    }

    public Asistencia(Integer idAsistencia, Integer idEmpleado, Integer idObra, LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idAsistencia = idAsistencia;
        this.idEmpleado = idEmpleado;
        this.idObra = idObra;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(Integer idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
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

    /**
     * Indica si la asistencia habilita producción (RF-12) y es remunerada en planilla (RF-14).
     */
    public boolean esAsistenciaValida() {
        if (estado == null) {
            return false;
        }
        String e = estado.trim().toUpperCase();
        return e.equals("PRESENTE") || e.equals("TARDANZA") || e.equals("ASISTIO") || e.equals("ASISTIÓ");
    }
}
