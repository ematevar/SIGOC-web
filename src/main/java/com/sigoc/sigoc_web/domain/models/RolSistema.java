package com.sigoc.sigoc_web.domain.models;

import java.util.Optional;

/**
 * Los cuatro perfiles definidos en el SRS Web (RF-01).
 */
public enum RolSistema {
    ADMIN("Administrador"),
    RESIDENTE("Residente de Obra"),
    ALMACENERO("Almacenero"),
    CONTADOR("Contador");

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
        if (r.contains("CONTADOR")) {
            return Optional.of(CONTADOR);
        }
        return Optional.empty();
    }

    /**
     * Indica si un nombre de rol de BD es un perfil válido del sistema.
     */
    public static boolean esValido(String nombreRol) {
        return desde(nombreRol).isPresent();
    }
}
