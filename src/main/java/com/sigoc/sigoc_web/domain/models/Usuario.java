package com.sigoc.sigoc_web.domain.models;

public class Usuario {
    private Integer idUsuario;
    private String nombreCompleto;
    private String username;
    private String rol;
    private String passwordHash;
    private Integer idRol;
    private boolean activo;
    private boolean requiereCambioClave;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String nombreCompleto, String username, String rol, String passwordHash, Integer idRol, boolean activo, boolean requiereCambioClave) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.rol = rol;
        this.passwordHash = passwordHash;
        this.idRol = idRol;
        this.activo = activo;
        this.requiereCambioClave = requiereCambioClave;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isRequiereCambioClave() {
        return requiereCambioClave;
    }

    public void setRequiereCambioClave(boolean requiereCambioClave) {
        this.requiereCambioClave = requiereCambioClave;
    }
}
