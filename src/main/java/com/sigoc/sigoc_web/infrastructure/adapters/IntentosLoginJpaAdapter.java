package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import com.sigoc.sigoc_web.infrastructure.entities.IntentoLoginEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.IntentoLoginJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Persistencia en base de datos de intentos fallidos y bloqueos temporales de login.
 * Sobrevive reinicios del servidor y permite escalamiento horizontal sin estado en memoria.
 */
@Component
@Primary
public class IntentosLoginJpaAdapter implements IntentosLoginPort {

    private final IntentoLoginJpaRepository repo;

    public IntentosLoginJpaAdapter(IntentoLoginJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instant> bloqueadoHasta(String clave) {
        return repo.findById(normalizar(clave))
                .map(IntentoLoginEntity::getBloqueadoHasta)
                .map(ldt -> ldt.toInstant(ZoneOffset.UTC));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int registrarFallo(String clave) {
        String key = normalizar(clave);
        IntentoLoginEntity e = repo.findById(key).orElseGet(() -> {
            IntentoLoginEntity nuevo = new IntentoLoginEntity();
            nuevo.setClaveLogin(key);
            nuevo.setFallos(0);
            return nuevo;
        });
        int total = e.getFallos() + 1;
        e.setFallos(total);
        e.setActualizadoEl(LocalDateTime.now(ZoneOffset.UTC));
        repo.save(e);
        return total;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bloquear(String clave, Instant hasta) {
        String key = normalizar(clave);
        IntentoLoginEntity e = repo.findById(key).orElseGet(() -> {
            IntentoLoginEntity nuevo = new IntentoLoginEntity();
            nuevo.setClaveLogin(key);
            return nuevo;
        });
        e.setFallos(0);
        e.setBloqueadoHasta(LocalDateTime.ofInstant(hasta, ZoneOffset.UTC));
        e.setActualizadoEl(LocalDateTime.now(ZoneOffset.UTC));
        repo.save(e);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reiniciar(String clave) {
        String key = normalizar(clave);
        if (repo.existsById(key)) {
            repo.deleteById(key);
        }
    }

    private String normalizar(String clave) {
        if (clave == null || clave.isBlank()) {
            return "_anon";
        }
        String clean = clave.trim().toLowerCase();
        return clean.length() > 60 ? clean.substring(0, 60) : clean;
    }
}
