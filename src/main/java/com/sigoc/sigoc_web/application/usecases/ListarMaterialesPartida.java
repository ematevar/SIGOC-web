package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-16b: lista los materiales presupuestados activos de una partida.
 */
public class ListarMaterialesPartida {
    private final PartidaPort partidaPort;
    private final PartidaMaterialPort partidaMaterialPort;

    public ListarMaterialesPartida(PartidaPort partidaPort, PartidaMaterialPort partidaMaterialPort) {
        this.partidaPort = partidaPort;
        this.partidaMaterialPort = partidaMaterialPort;
    }

    public List<PartidaMaterial> ejecutar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR);
        partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        return partidaMaterialPort.findActivosByPartida(idPartida);
    }
}
