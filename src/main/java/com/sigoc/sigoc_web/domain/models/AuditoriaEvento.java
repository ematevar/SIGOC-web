package com.sigoc.sigoc_web.domain.models;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Evento inmutable de la bitácora de auditoría (ISO/IEC 27001 A.8.15, CU-25).
 */
public record AuditoriaEvento(
        Long idEvento,
        LocalDateTime fechaHora,
        Integer idUsuario,
        String usuarioLogin,
        String rol,
        String accion,
        String entidad,
        String idEntidad,
        String resultado,
        String detalle
) {
    public static AuditoriaEvento crear(Clock clock, UsuarioSesion actor, String accion,
                                        String entidad, String idEntidad, String resultado, String detalle) {
        return new AuditoriaEvento(
                null,
                LocalDateTime.now(clock),
                actor != null ? actor.idUsuario() : null,
                actor != null ? actor.username() : "anonimo",
                actor != null && actor.rol() != null ? actor.rol().name() : "PUBLICO",
                accion,
                entidad,
                idEntidad,
                resultado,
                detalle
        );
    }
}
