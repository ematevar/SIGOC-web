package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre_rol", nullable = false, length = 40)
    private String nombreRol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public RolEntity() {
    }
    public RolEntity(Integer idRol, String nombreRol, boolean activo) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.activo = activo;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
