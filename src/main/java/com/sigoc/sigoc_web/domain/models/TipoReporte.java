package com.sigoc.sigoc_web.domain.models;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;

/**
 * RF-16 / RF-22: los 5 tipos de reporte gerencial soportados.
 */
public enum TipoReporte {
    CURVA_S(0),
    CONSUMO_MATERIALES(1),
    PERSONAL_PRODUCTIVIDAD(2),
    FINANCIERO(3),
    VALORIZACIONES_COBRANZAS(4);

    private final int indice;

    TipoReporte(int indice) {
        this.indice = indice;
    }

    public int getIndice() {
        return indice;
    }

    public static TipoReporte desdeParametro(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("Especifica el tipo de reporte.");
        }
        String norm = valor.trim().toUpperCase();
        return switch (norm) {
            case "0", "CURVA_S", "AVANCE" -> CURVA_S;
            case "1", "CONSUMO_MATERIALES", "MATERIALES", "CONSUMO" -> CONSUMO_MATERIALES;
            case "2", "PERSONAL_PRODUCTIVIDAD", "PERSONAL", "PRODUCTIVIDAD" -> PERSONAL_PRODUCTIVIDAD;
            case "3", "FINANCIERO", "FINANZAS" -> FINANCIERO;
            case "4", "VALORIZACIONES_COBRANZAS", "VALORIZACIONES", "COBRANZAS" -> VALORIZACIONES_COBRANZAS;
            default -> throw new ReglaNegocioException("Tipo de reporte desconocido: " + valor);
        };
    }
}
