package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.infrastructure.entities.MaterialEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.MaterialJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MaterialJpaAdapter implements MaterialPort {

    private final MaterialJpaRepository repository;

    public MaterialJpaAdapter(MaterialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> findAllActivos() {
        return repository.findByActivoTrueOrderByIdMaterialDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> findAlertasStockMinimo() {
        return repository.findAlertasStockMinimo().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Material> findById(Integer idMaterial) {
        return repository.findById(idMaterial).map(this::toModel);
    }

    @Override
    @Transactional
    public Optional<Material> findByIdParaActualizar(Integer idMaterial) {
        return repository.findByIdParaActualizar(idMaterial).map(this::toModel);
    }

    @Override
    @Transactional
    public Material guardar(Material material) {
        MaterialEntity e;
        if (material.getIdMaterial() == null) {
            e = new MaterialEntity();
            e.setStockActual(material.getStockActual());
        } else {
            e = repository.findById(material.getIdMaterial())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El material no existe."));
            // stock_actual es de solo lectura en el catálogo (RF-06).
        }
        e.setNombre(material.getNombre());
        e.setUnidadMedida(material.getUnidadMedida());
        e.setStockMinimo(material.getStockMinimo());
        e.setCostoUnitarioRef(material.getCostoUnitarioRef());
        e.setActivo(material.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void actualizarStock(Integer idMaterial, java.math.BigDecimal nuevoStock, java.math.BigDecimal nuevoCostoRef) {
        MaterialEntity e = repository.findById(idMaterial)
                .filter(MaterialEntity::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El material no existe o está inactivo."));
        e.setStockActual(nuevoStock);
        if (nuevoCostoRef != null) {
            e.setCostoUnitarioRef(nuevoCostoRef);
        }
    }

    @Override
    @Transactional
    public void desactivar(Integer idMaterial) {
        MaterialEntity e = repository.findById(idMaterial)
                .orElseThrow(() -> new RecursoNoEncontradoException("El material no existe."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    private Material toModel(MaterialEntity e) {
        return new Material(
                e.getIdMaterial(),
                e.getNombre(),
                e.getUnidadMedida(),
                e.getStockActual(),
                e.getStockMinimo(),
                e.getCostoUnitarioRef(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
