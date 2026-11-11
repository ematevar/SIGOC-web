package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.AvancePartida;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-05: lista avances físicos activos filtrados por partida, obra o todos.
 */
public class ListarAvances {

    private final AvancePartidaPort avancePort;

    public ListarAvances(AvancePartidaPort avancePort) {
        this.avancePort = avancePort;
    }

    public List<AvancePartida> ejecutar(UsuarioSesion actor, Integer idPartida, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        if (idPartida != null) {
            return avancePort.findActivosByPartida(idPartida);
        }
        if (idObra != null) {
            return avancePort.findActivosByObra(idObra);
        }
        return avancePort.findAllActivos();
    }
}
