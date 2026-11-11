package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.infraestructure.entities.RolEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.RolJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RolJpaAdapter implements RolPort {
    private final RolJpaRepository repository;
    public RolJpaAdapter(RolJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Rol> findAll() {
        return repository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public Optional<Rol> findById(Integer idRol) {
        return repository.findById(idRol)
                .map(this::toModel);
    }

    private  Rol toModel(RolEntity entity) {
        return new Rol(entity.getIdRol(), entity.getNombreRol());
    }
}
