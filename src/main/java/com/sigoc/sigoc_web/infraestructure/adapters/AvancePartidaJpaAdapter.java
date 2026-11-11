package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.AvancePartida;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.infraestructure.entities.AvancePartidaEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.AvancePartidaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AvancePartidaJpaAdapter implements AvancePartidaPort {

    private final AvancePartidaJpaRepository repository;

    public AvancePartidaJpaAdapter(AvancePartidaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePartida> findActivosByPartida(Integer idPartida) {
        return repository.findByIdPartidaAndActivoTrueOrderByFechaReporteDescIdAvanceDesc(idPartida)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePartida> findActivosByObra(Integer idObra) {
        return repository.findActivosByObra(idObra).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePartida> findAllActivos() {
        return repository.findByActivoTrueOrderByFechaReporteDescIdAvanceDesc()
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AvancePartida> findUltimoActivoByPartida(Integer idPartida) {
        return repository.findTopByIdPartidaAndActivoTrueOrderByFechaReporteDescIdAvanceDesc(idPartida)
                .map(this::toModel);
    }

    @Override
    @Transactional
    public AvancePartida guardar(AvancePartida avance) {
        AvancePartidaEntity e = new AvancePartidaEntity();
        e.setIdPartida(avance.getIdPartida());
        e.setFechaReporte(avance.getFechaReporte());
        e.setPorcentajeAvance(avance.getPorcentajeAvance());
        e.setObservaciones(avance.getObservaciones());
        e.setIdUsuario(avance.getIdUsuario());
        e.setActivo(avance.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    private AvancePartida toModel(AvancePartidaEntity e) {
        return new AvancePartida(
                e.getIdAvance(),
                e.getIdPartida(),
                e.getFechaReporte(),
                e.getPorcentajeAvance(),
                e.getObservaciones(),
                e.getIdUsuario(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
