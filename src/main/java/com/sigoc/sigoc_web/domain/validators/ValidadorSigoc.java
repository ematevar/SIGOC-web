package com.sigoc.sigoc_web.domain.validators;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones estrictas de documentos, formatos y rangos numéricos de negocio.
 */
public final class ValidadorSigoc {

    private static final Pattern SOLO_LETRAS = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    private static final Pattern SERIE_COMPROBANTE = Pattern.compile("^[FB][0-9]{3}-[0-9]{6,8}$");
    private static final Pattern PERIODO_ANIO_MES = Pattern.compile("^[0-9]{4}-(0[1-9]|1[0-2])$");

    private ValidadorSigoc() {
    }

    public static void validarDni(String dni) {
        if (dni == null || dni.length() != 8 || !dni.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El DNI debe contener exactamente 8 dígitos numéricos.");
        }
        if ("00000000".equals(dni)) {
            throw new ReglaNegocioException("El DNI ingresado no es válido.");
        }
    }

    public static void validarRuc(String ruc) {
        if (ruc == null || ruc.length() != 11 || !ruc.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El RUC debe contener exactamente 11 dígitos numéricos.");
        }
        if (!(ruc.startsWith("10") || ruc.startsWith("15") || ruc.startsWith("17") || ruc.startsWith("20"))) {
            throw new ReglaNegocioException("El RUC debe iniciar con un prefijo válido de SUNAT (10, 15, 17 o 20).");
        }
    }

    public static void validarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaNegocioException("El teléfono es obligatorio.");
        }
        String limpio = telefono.trim();
        if (limpio.length() < 7 || limpio.length() > 9 || !limpio.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El teléfono debe contener únicamente entre 7 y 9 dígitos numéricos.");
        }
    }

    public static void validarNombrePersona(String nombre, String etiquetaCampo) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El campo " + etiquetaCampo + " es obligatorio.");
        }
        if (!SOLO_LETRAS.matcher(nombre.trim()).matches()) {
            throw new ReglaNegocioException("El campo " + etiquetaCampo + " solo debe contener letras y espacios.");
        }
    }

    public static void validarSerieComprobante(String serieNumero) {
        if (serieNumero == null || !SERIE_COMPROBANTE.matcher(serieNumero.trim().toUpperCase()).matches()) {
            throw new ReglaNegocioException(
                    "El comprobante debe tener formato serie-correlativo válido (ej. F001-000001 o B001-000001).");
        }
    }

    public static void validarPeriodo(String periodo) {
        if (periodo == null || !PERIODO_ANIO_MES.matcher(periodo.trim()).matches()) {
            throw new ReglaNegocioException("El periodo debe tener el formato AAAA-MM (ej. 2026-11).");
        }
    }

    public static void validarMontoPositivo(BigDecimal monto, String campo) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto de " + campo + " debe ser mayor que cero.");
        }
    }

    public static void validarPorcentaje(BigDecimal porcentaje, String campo) {
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) <= 0
                || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new ReglaNegocioException("El porcentaje de " + campo + " debe estar entre 0.01 y 100.");
        }
    }
}
