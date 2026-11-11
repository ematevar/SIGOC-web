package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.infraestructure.entities.PartidaMaterialEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.PartidaMaterialJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PartidaMaterialJpaAdapter implements PartidaMaterialPort {

    private final PartidaMaterialJpaRepository repository;
    private final EntityManager em;

    public PartidaMaterialJpaAdapter(PartidaMaterialJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidaMaterial> findActivosByPartida(Integer idPartida) {
        return repository.findByIdPartidaAndActivoTrueOrderByIdPartidaMaterialDesc(idPartida)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartidaMaterial> findById(Integer idPartidaMaterial) {
        return repository.findById(idPartidaMaterial).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMaterialActivo(Integer idMaterial) {
        return !em.createNativeQuery(
                        "SELECT 1 FROM material WHERE id_material = :idMaterial AND activo = 1")
                .setParameter("idMaterial", idMaterial)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    @Override
    @Transactional
    public PartidaMaterial guardarOActualizar(Integer idPartida, Integer idMaterial, BigDecimal cantidadPresupuestada) {
        PartidaMaterialEntity e = repository.findByIdPartidaAndIdMaterial(idPartida, idMaterial)
                .orElseGet(PartidaMaterialEntity::new);
        e.setIdPartida(idPartida);
        e.setIdMaterial(idMaterial);
        e.setCantidadPresupuestada(cantidadPresupuestada);
        e.setActivo(true);
        e.setFechaBorrado(null);
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void desactivar(Integer idPartidaMaterial) {
        PartidaMaterialEntity e = repository.findById(idPartidaMaterial)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el material presupuestado."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    private PartidaMaterial toModel(PartidaMaterialEntity e) {
        return new PartidaMaterial(
                e.getIdPartidaMaterial(),
                e.getIdPartida(),
                e.getIdMaterial(),
                e.getCantidadPresupuestada(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
