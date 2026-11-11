package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.infrastructure.entities.PartidaEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.PartidaJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PartidaJpaAdapter implements PartidaPort {

    private final PartidaJpaRepository repository;
    private final EntityManager em;

    public PartidaJpaAdapter(PartidaJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findAllActivas() {
        return repository.findByActivoTrueOrderByIdPartidaDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findActivasByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByIdPartidaDesc(idObra).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Partida> findById(Integer idPartida) {
        return repository.findById(idPartida).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumaPesosActivos(Integer idObra, Integer idPartidaExcluir) {
        BigDecimal sum = repository.sumaPesosActivos(idObra, idPartidaExcluir);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneMovimientos(Integer idPartida) {
        String[] tablas = {"avance_partida", "consumo_material", "produccion_empleado"};
        for (String tabla : tablas) {
            boolean existe = !em.createNativeQuery(
                            "SELECT 1 FROM " + tabla + " WHERE id_partida = :idPartida AND activo = 1")
                    .setParameter("idPartida", idPartida)
                    .setMaxResults(1)
                    .getResultList()
                    .isEmpty();
            if (existe) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Partida guardar(Partida partida) {
        PartidaEntity e = partida.getIdPartida() == null
                ? new PartidaEntity()
                : repository.findById(partida.getIdPartida())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        e.setIdObra(partida.getIdObra());
        e.setNombre(partida.getNombre());
        e.setUnidadMedida(partida.getUnidadMedida());
        e.setMetradoPlanificado(partida.getMetradoPlanificado());
        e.setPesoPresupuestal(partida.getPesoPresupuestal());
        e.setActivo(partida.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void desactivar(Integer idPartida) {
        PartidaEntity e = repository.findById(idPartida)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    private Partida toModel(PartidaEntity e) {
        return new Partida(
                e.getIdPartida(),
                e.getIdObra(),
                e.getNombre(),
                e.getUnidadMedida(),
                e.getMetradoPlanificado(),
                e.getPesoPresupuestal(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
