package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.infrastructure.entities.AuditoriaEventoEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.AuditoriaEventoJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditoriaEventoJpaAdapter implements AuditoriaEventoPort {

    private final AuditoriaEventoJpaRepository repository;

    public AuditoriaEventoJpaAdapter(AuditoriaEventoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditoriaEvento registrar(AuditoriaEvento evento) {
        AuditoriaEventoEntity saved = repository.save(new AuditoriaEventoEntity(
                evento.fechaHora(),
                evento.idUsuario(),
                evento.usuarioLogin(),
                evento.rol(),
                evento.accion(),
                evento.entidad(),
                evento.idEntidad(),
                evento.resultado(),
                evento.detalle()
        ));
        return toDomain(saved);
    }

    @Override
    public List<AuditoriaEvento> listarRecientes(String accion, int limite) {
        PageRequest page = PageRequest.of(0, limite);
        List<AuditoriaEventoEntity> rows = (accion == null || accion.isBlank())
                ? repository.findAllByOrderByFechaHoraDescIdEventoDesc(page)
                : repository.findByAccionOrderByFechaHoraDescIdEventoDesc(accion, page);
        return rows.stream().map(this::toDomain).toList();
    }

    private AuditoriaEvento toDomain(AuditoriaEventoEntity e) {
        return new AuditoriaEvento(
                e.getIdEvento(),
                e.getFechaHora(),
                e.getIdUsuario(),
                e.getUsuarioLogin(),
                e.getRol(),
                e.getAccion(),
                e.getEntidad(),
                e.getIdEntidad(),
                e.getResultado(),
                e.getDetalle()
        );
    }
}
