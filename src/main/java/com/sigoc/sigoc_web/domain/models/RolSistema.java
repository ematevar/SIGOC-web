package com.sigoc.sigoc_web.domain.models;

import java.util.Optional;

/**
 * Los tres perfiles definidos en el SRS V4 (RF-01).
 */
public enum RolSistema {
    ADMIN("Administrador"),
    RESIDENTE("Residente de Obra"),
    ALMACENERO("Almacenero");

    private final String etiqueta;

    RolSistema(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Normaliza el nombre de rol almacenado en BD (compatibilidad con datos previos:
     * INGENIERO se considera RESIDENTE).
     */
    public static Optional<RolSistema> desde(String nombreRol) {
        if (nombreRol == null) {
            return Optional.empty();
        }
        String r = nombreRol.trim().toUpperCase();
        if (r.contains("ADMIN")) {
            return Optional.of(ADMIN);
        }
        if (r.contains("RESIDENTE") || r.contains("INGENIERO")) {
            return Optional.of(RESIDENTE);
        }
        if (r.contains("ALMACEN")) {
            return Optional.of(ALMACENERO);
        }
        return Optional.empty();
    }

    /**
     * Indica si un nombre de rol de BD es un perfil válido del sistema (excluye CONTADOR, etc.).
     */
    public static boolean esValido(String nombreRol) {
        return desde(nombreRol).isPresent();
    }
}
