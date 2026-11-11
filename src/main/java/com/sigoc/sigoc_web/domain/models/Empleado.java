package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Empleado {
    private Integer idEmpleado;
    private String nombresApellidos;
    private String dni;
    private String cargo;
    private BigDecimal jornalDiario;
    private LocalDate fechaIngreso;

    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Empleado() {
    }

    public Empleado(Integer idEmpleado, String nombresApellidos, String dni, String cargo, BigDecimal jornalDiario, LocalDate fechaIngreso, boolean activo, LocalDateTime fechaBorrado) {
        this.idEmpleado = idEmpleado;
        this.nombresApellidos = nombresApellidos;
        this.dni = dni;
        this.cargo = cargo;
        this.jornalDiario = jornalDiario;
        this.fechaIngreso = fechaIngreso;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombresApellidos() {
        return nombresApellidos;
    }

    public void setNombresApellidos(String nombresApellidos) {
        this.nombresApellidos = nombresApellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getJornalDiario() {
        return jornalDiario;
    }

    public void setJornalDiario(BigDecimal jornalDiario) {
        this.jornalDiario = jornalDiario;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
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
