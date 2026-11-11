package com.sigoc.sigoc_web.infrastructure.security;

/**
 * Almacena en ThreadLocal el identificador del usuario autenticado en el request HTTP actual
 * para completar la auditoría en la capa de persistencia (RNF-13 / RN-28).
 */
public final class ContextoAuditoria {

    private static final ThreadLocal<Integer> USUARIO_ACTUAL = new ThreadLocal<>();

    private ContextoAuditoria() {
    }

    public static void fijarUsuario(Integer idUsuario) {
        if (idUsuario != null && idUsuario > 0) {
            USUARIO_ACTUAL.set(idUsuario);
        } else {
            USUARIO_ACTUAL.remove();
        }
    }

    public static Integer usuarioActual() {
        return USUARIO_ACTUAL.get();
    }

    public static void limpiar() {
        USUARIO_ACTUAL.remove();
    }
}
