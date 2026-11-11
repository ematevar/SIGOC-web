package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Proveedor;
import com.sigoc.sigoc_web.domain.ports.out.ProveedorPort;
import com.sigoc.sigoc_web.infraestructure.entities.ProveedorEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.ProveedorJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProveedorJpaAdapter implements ProveedorPort {

    private final ProveedorJpaRepository repository;

    public ProveedorJpaAdapter(ProveedorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> findAllActivos() {
        return repository.findByActivoTrueOrderByIdProveedorDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> findById(Integer idProveedor) {
        return repository.findById(idProveedor).map(this::toModel);
    }

    @Override
    public boolean existsByRuc(String ruc, Integer idExcluir) {
        return repository.existsByRuc(ruc, idExcluir);
    }

    @Override
    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        ProveedorEntity e = proveedor.getIdProveedor() == null
                ? new ProveedorEntity()
                : repository.findById(proveedor.getIdProveedor())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor."));
        e.setRazonSocial(proveedor.getRazonSocial());
        e.setRuc(proveedor.getRuc());
        e.setTelefono(proveedor.getTelefono());
        e.setDireccion(proveedor.getDireccion());
        e.setActivo(proveedor.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void desactivar(Integer idProveedor) {
        ProveedorEntity e = repository.findById(idProveedor)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    private Proveedor toModel(ProveedorEntity e) {
        return new Proveedor(
                e.getIdProveedor(),
                e.getRazonSocial(),
                e.getRuc(),
                e.getTelefono(),
                e.getDireccion(),
                e.isActivo()
        );
    }
}
