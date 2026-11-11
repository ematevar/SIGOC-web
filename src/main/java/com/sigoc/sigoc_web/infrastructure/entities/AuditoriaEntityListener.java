package com.sigoc.sigoc_web.infrastructure.entities;

import com.sigoc.sigoc_web.infrastructure.security.ContextoAuditoria;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Listener JPA que registra fecha_registro (America/Lima) e id_usuario_registro al insertar.
 */
public class AuditoriaEntityListener {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @PrePersist
    public void alInsertar(Object entidad) {
        if (entidad instanceof Auditable auditable) {
            if (auditable.getFechaRegistro() == null) {
                auditable.setFechaRegistro(LocalDateTime.now(ZONA_LIMA));
            }
            if (auditable.getIdUsuarioRegistro() == null) {
                auditable.setIdUsuarioRegistro(ContextoAuditoria.usuarioActual());
            }
        }
    }
}
