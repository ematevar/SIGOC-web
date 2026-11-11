package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.infrastructure.entities.ObraEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.ObraJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ObraJpaAdapter implements ObraPort {

    private final ObraJpaRepository repository;
    private final EntityManager em;

    public ObraJpaAdapter(ObraJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Obra> findAllActivas() {
        return repository.findByActivoTrueOrderByIdObraDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Obra> findById(Integer idObra) {
        return repository.findById(idObra).map(this::toModel);
    }

    @Override
    @Transactional
    public Obra guardar(Obra obra) {
        ObraEntity e = obra.getIdObra() == null
                ? new ObraEntity()
                : repository.findById(obra.getIdObra())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        e.setNombre(obra.getNombre());
        e.setCliente(obra.getCliente());
        e.setUbicacion(obra.getUbicacion());
        e.setPresupuestoTotal(obra.getPresupuestoTotal());
        e.setFechaInicioPlan(obra.getFechaInicioPlan());
        e.setFechaFinPlan(obra.getFechaFinPlan());
        e.setEstado(obra.getEstado());
        e.setActivo(obra.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneMovimientos(Integer idObra) {
        String[] tablas = {"partida", "asistencia", "ingreso", "egreso", "cierre_planilla"};
        for (String tabla : tablas) {
            boolean existe = !em.createNativeQuery(
                            "SELECT 1 FROM " + tabla + " WHERE id_obra = :idObra AND activo = 1")
                    .setParameter("idObra", idObra)
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
    public void desactivar(Integer idObra) {
        ObraEntity e = repository.findById(idObra)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cerrar(Integer idObra) {
        ObraEntity e = repository.findById(idObra)
                .filter(ObraEntity::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        e.setEstado("Finalizada");
    }

    private Obra toModel(ObraEntity e) {
        return new Obra(
                e.getIdObra(),
                e.getNombre(),
                e.getCliente(),
                e.getUbicacion(),
                e.getPresupuestoTotal(),
                e.getFechaInicioPlan(),
                e.getFechaFinPlan(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
