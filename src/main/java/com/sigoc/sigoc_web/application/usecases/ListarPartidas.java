package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-04: lista partidas activas (todas o filtradas por obra).
 */
public class ListarPartidas {
    private final PartidaPort partidaPort;

    public ListarPartidas(PartidaPort partidaPort) {
        this.partidaPort = partidaPort;
    }

    public List<Partida> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.ALMACENERO);
        return idObra == null ? partidaPort.findAllActivas() : partidaPort.findActivasByObra(idObra);
    }
}
