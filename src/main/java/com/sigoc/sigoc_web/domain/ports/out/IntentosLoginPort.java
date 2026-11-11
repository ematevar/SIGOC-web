package com.sigoc.sigoc_web.domain.ports.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Almacén de intentos fallidos de inicio de sesión por clave (login).
 */
public interface IntentosLoginPort {
    Optional<Instant> bloqueadoHasta(String clave);

    /** Registra un fallo y devuelve el número acumulado de fallos. */
    int registrarFallo(String clave);

    void bloquear(String clave, Instant hasta);

    void reiniciar(String clave);
}
