package com.sigoc.sigoc_web.domain.models;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;

/**
 * RF-16: formatos de exportación admitidos.
 */
public enum FormatoExportacion {
    CSV("csv", "text/csv; charset=UTF-8"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String contentType;

    FormatoExportacion(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    public static FormatoExportacion desdeParametro(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("Especifica el formato de exportación (CSV, XLSX o PDF).");
        }
        return switch (valor.trim().toUpperCase()) {
            case "CSV" -> CSV;
            case "XLSX", "EXCEL" -> XLSX;
            case "PDF" -> PDF;
            default -> throw new ReglaNegocioException("Formato de exportación no soportado: " + valor);
        };
    }
}
