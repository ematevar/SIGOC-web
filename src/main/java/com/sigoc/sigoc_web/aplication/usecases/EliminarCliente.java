package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * Realiza baja lógica de un cliente activo.
 */
public class EliminarCliente {
    private final ClientePort clientePort;

    public EliminarCliente(ClientePort clientePort) {
        this.clientePort = clientePort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idCliente) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        Cliente cliente = clientePort.findById(idCliente)
                .filter(Cliente::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el cliente."));
        clientePort.desactivar(cliente.getIdCliente());
    }
}
