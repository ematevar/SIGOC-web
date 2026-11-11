package com.sigoc.sigoc_web.domain.models;

import java.util.List;

/**
 * RF-16: estructura inmutable de resultado de un reporte gerencial.
 */
public record Reporte(String titulo, List<String> columnas, List<List<Object>> filas) {
}
