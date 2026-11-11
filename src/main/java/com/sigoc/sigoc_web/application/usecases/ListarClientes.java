package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * Lista los clientes activos registrados.
 */
public class ListarClientes {
    private final ClientePort clientePort;

    public ListarClientes(ClientePort clientePort) {
        this.clientePort = clientePort;
    }

    public List<Cliente> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        return clientePort.findAllActivos();
    }
}
