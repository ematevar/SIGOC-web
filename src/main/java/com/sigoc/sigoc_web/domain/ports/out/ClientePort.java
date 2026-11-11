package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClientePort {
    List<Cliente> findAllActivos();

    Optional<Cliente> findById(Integer idCliente);

    boolean existsByNumeroDocumento(String numeroDocumento, Integer idExcluir);

    Cliente guardar(Cliente cliente);

    void desactivar(Integer idCliente);
}
