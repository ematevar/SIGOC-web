package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.ResultadoTarjetaSimulada;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Pasarela simulada local de tarjeta (Visa / Mastercard) con algoritmo de Luhn
 * y rechazo configurable de prueba. No persiste PAN ni CVV.
 */
@Component
public class PasarelaSimuladaAdapter {

    private final Clock clock;

    public PasarelaSimuladaAdapter(Clock clock) {
        this.clock = clock;
    }

    public ResultadoTarjetaSimulada cobrar(String numeroTarjeta, String titular,
                                           String vencimiento, String cvv, BigDecimal montoPen) {
        if (titular == null || titular.trim().length() < 3) {
            throw new ReglaNegocioException("Ingresa el nombre del titular de la tarjeta.");
        }
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("[\\s-]", "");
        if (digitos.length() < 13 || digitos.length() > 19 || !digitos.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El número de tarjeta debe contener entre 13 y 19 dígitos.");
        }
        if (!luhnValido(digitos)) {
            throw new ReglaNegocioException("El número de tarjeta no supera la verificación Luhn.");
        }
        YearMonth expira = parsearVencimiento(vencimiento);
        if (expira.isBefore(YearMonth.now(clock))) {
            throw new ReglaNegocioException("La tarjeta ingresada se encuentra vencida.");
        }
        String cvvNorm = cvv == null ? "" : cvv.trim();
        if (cvvNorm.length() < 3 || cvvNorm.length() > 4 || !cvvNorm.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El CVV debe contener 3 o 4 dígitos.");
        }
        if (montoPen == null || montoPen.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto del cobro debe ser positivo.");
        }

        String marca = detectarMarca(digitos);
        String ultimos4 = digitos.substring(digitos.length() - 4);
        String codigo = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("0002".equals(ultimos4) || "999".equals(cvvNorm)) {
            return new ResultadoTarjetaSimulada(false, codigo, marca, ultimos4,
                    "Operación denegada por el banco emisor (tarjeta/CVV de rechazo de prueba).");
        }
        return new ResultadoTarjetaSimulada(true, codigo, marca, ultimos4, "Aprobado");
    }

    static boolean luhnValido(String digits) {
        int suma = 0;
        boolean alternar = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternar) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            suma += n;
            alternar = !alternar;
        }
        return suma % 10 == 0;
    }

    private YearMonth parsearVencimiento(String vencimiento) {
        if (vencimiento == null || !vencimiento.trim().matches("^(0[1-9]|1[0-2])/(\\d{2}|\\d{4})$")) {
            throw new ReglaNegocioException("La fecha de vencimiento debe tener el formato MM/AA.");
        }
        String[] partes = vencimiento.trim().split("/");
        int mes = Integer.parseInt(partes[0]);
        int anio = partes[1].length() == 2 ? 2000 + Integer.parseInt(partes[1]) : Integer.parseInt(partes[1]);
        return YearMonth.of(anio, mes);
    }

    private String detectarMarca(String digits) {
        if (digits.startsWith("4")) {
            return "VISA";
        }
        int prefix2 = Integer.parseInt(digits.substring(0, 2));
        if (prefix2 >= 51 && prefix2 <= 55) {
            return "MASTERCARD";
        }
        int prefix4 = Integer.parseInt(digits.substring(0, 4));
        if (prefix4 >= 2221 && prefix4 <= 2720) {
            return "MASTERCARD";
        }
        if (prefix2 == 34 || prefix2 == 37) {
            return "AMEX";
        }
        return "TARJETA";
    }
}
