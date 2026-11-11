package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.infraestructure.entities.ClienteEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.ClienteJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteJpaAdapter implements ClientePort {

    private final ClienteJpaRepository repository;

    public ClienteJpaAdapter(ClienteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAllActivos() {
        return repository.findByActivoTrueOrderByIdClienteDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Integer idCliente) {
        return repository.findById(idCliente).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNumeroDocumento(String numeroDocumento, Integer idExcluir) {
        if (idExcluir == null) {
            return repository.existsByNumeroDocumento(numeroDocumento);
        }
        return repository.existsByNumeroDocumentoAndIdClienteNot(numeroDocumento, idExcluir);
    }

    @Override
    @Transactional
    public Cliente guardar(Cliente cliente) {
        ClienteEntity entity = cliente.getIdCliente() != null
                ? repository.findById(cliente.getIdCliente()).orElseGet(ClienteEntity::new)
                : new ClienteEntity();
        entity.setTipoDocumento(cliente.getTipoDocumento());
        entity.setNumeroDocumento(cliente.getNumeroDocumento());
        entity.setNombreRazonSocial(cliente.getNombreRazonSocial());
        entity.setTelefono(cliente.getTelefono());
        entity.setCorreo(cliente.getCorreo());
        entity.setDireccion(cliente.getDireccion());
        entity.setActivo(cliente.isActivo());
        entity.setFechaBorrado(cliente.getFechaBorrado());
        return toModel(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public void desactivar(Integer idCliente) {
        repository.findById(idCliente).ifPresent(e -> {
            e.setActivo(false);
            e.setFechaBorrado(LocalDateTime.now());
            repository.saveAndFlush(e);
        });
    }

    private Cliente toModel(ClienteEntity e) {
        return new Cliente(
                e.getIdCliente(),
                e.getTipoDocumento(),
                e.getNumeroDocumento(),
                e.getNombreRazonSocial(),
                e.getTelefono(),
                e.getCorreo(),
                e.getDireccion(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
