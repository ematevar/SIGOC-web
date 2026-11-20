package com.sigoc.sigoc_web.domain.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Límite de intentos de pago rechazados en el enlace público.
 * Con 5 rechazos el enlace queda bloqueado 30 minutos. Al terminar el bloqueo el conteo vuelve a cero.
 * Renovar el enlace también reinicia el conteo, porque solo se cuentan los rechazos desde la última emisión.
 */
public final class ControlIntentosPago {

    public static final int MAX_INTENTOS_RECHAZADOS = 5;
    public static final Duration BLOQUEO = Duration.ofMinutes(30);

    private ControlIntentosPago() {
    }

    /**
     * @param rechazos fechas de los intentos rechazados desde que se emitió o renovó el enlace, en orden ascendente
     * @param ahora    momento actual
     */
    public static Estado evaluar(List<LocalDateTime> rechazos, LocalDateTime ahora) {
        int conteo = 0;
        LocalDateTime bloqueadoHasta = null;
        for (LocalDateTime fecha : rechazos) {
            if (fecha == null) {
                continue;
            }
            if (bloqueadoHasta != null && !fecha.isBefore(bloqueadoHasta)) {
                conteo = 0;
                bloqueadoHasta = null;
            }
            conteo++;
            if (conteo >= MAX_INTENTOS_RECHAZADOS && bloqueadoHasta == null) {
                bloqueadoHasta = fecha.plus(BLOQUEO);
            }
        }
        if (bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta)) {
            return new Estado(true, bloqueadoHasta, 0);
        }
        if (bloqueadoHasta != null) {
            conteo = 0;
        }
        return new Estado(false, null, MAX_INTENTOS_RECHAZADOS - conteo);
    }

    public record Estado(boolean bloqueado, LocalDateTime bloqueadoHasta, int intentosRestantes) {

        public long minutosRestantes(LocalDateTime ahora) {
            if (bloqueadoHasta == null) {
                return 0;
            }
            long segundos = Duration.between(ahora, bloqueadoHasta).getSeconds();
            return Math.max(1L, (segundos + 59) / 60);
        }
    }
}
