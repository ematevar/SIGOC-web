package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "intento_login")
public class IntentoLoginEntity {

    @Id
    @Column(name = "clave_login", length = 60, nullable = false)
    private String claveLogin;

    @Column(name = "fallos", nullable = false)
    private int fallos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "actualizado_el", nullable = false)
    private LocalDateTime actualizadoEl;

    public String getClaveLogin() { return claveLogin; }
    public void setClaveLogin(String claveLogin) { this.claveLogin = claveLogin; }
    public int getFallos() { return fallos; }
    public void setFallos(int fallos) { this.fallos = fallos; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
    public LocalDateTime getActualizadoEl() { return actualizadoEl; }
    public void setActualizadoEl(LocalDateTime actualizadoEl) { this.actualizadoEl = actualizadoEl; }
}
