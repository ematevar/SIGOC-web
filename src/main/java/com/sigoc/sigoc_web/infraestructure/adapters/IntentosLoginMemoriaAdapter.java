package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro en memoria de intentos fallidos de inicio de sesión (por login).
 */
@Component
public class IntentosLoginMemoriaAdapter implements IntentosLoginPort {

    private final Map<String, Integer> fallos = new ConcurrentHashMap<>();
    private final Map<String, Instant> bloqueos = new ConcurrentHashMap<>();

    @Override
    public Optional<Instant> bloqueadoHasta(String clave) {
        return Optional.ofNullable(bloqueos.get(clave));
    }

    @Override
    public int registrarFallo(String clave) {
        return fallos.merge(clave, 1, Integer::sum);
    }

    @Override
    public void bloquear(String clave, Instant hasta) {
        bloqueos.put(clave, hasta);
        fallos.remove(clave);
    }

    @Override
    public void reiniciar(String clave) {
        fallos.remove(clave);
        bloqueos.remove(clave);
    }
}
