package com.sigoc.sigoc_web.infrastructure.entities;

import java.time.LocalDateTime;

/**
 * Contrato interno para completar automáticamente columnas de auditoría en @PrePersist.
 */
public interface Auditable {
    LocalDateTime getFechaRegistro();

    void setFechaRegistro(LocalDateTime fechaRegistro);

    default Integer getIdUsuarioRegistro() {
        return null;
    }

    default void setIdUsuarioRegistro(Integer idUsuarioRegistro) {
        // Las tablas que ya registran id_usuario pueden omitir id_usuario_registro.
    }
}
