package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;

import java.util.List;

/**
 * Lista los roles activos válidos según los perfiles del SRS (excluye roles retirados como CONTADOR).
 */
public class ListarRoles {
    private final RolPort port;

    public ListarRoles(RolPort port) {
        this.port = port;
    }

    public List<Rol> ejecutar() {
        return port.findAllActivos().stream()
                .filter(r -> RolSistema.esValido(r.getNombreRol()))
                .toList();
    }
}
