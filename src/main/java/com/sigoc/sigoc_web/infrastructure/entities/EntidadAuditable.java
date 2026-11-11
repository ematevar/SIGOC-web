package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

/**
 * Superclase JPA para entidades con columnas de auditoría id_usuario_registro y fecha_registro.
 */
@MappedSuperclass
@EntityListeners(AuditoriaEntityListener.class)
public abstract class EntidadAuditable implements Auditable {

    @Column(name = "id_usuario_registro")
    private Integer idUsuarioRegistro;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

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
