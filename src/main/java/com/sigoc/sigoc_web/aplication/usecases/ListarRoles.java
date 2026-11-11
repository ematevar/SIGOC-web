package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;

import java.util.List;

public class ListarRoles {
    private final RolPort port;

    public ListarRoles(RolPort port) {
        this.port = port;
    }
    public List<Rol> ejecutar(){
        return port.findAll();
    }
}
