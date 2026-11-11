package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-04: da de baja lógica una partida si no tiene avances, consumos o producción asociados.
 */
public class EliminarPartida {
    private final PartidaPort partidaPort;

    public EliminarPartida(PartidaPort partidaPort) {
        this.partidaPort = partidaPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idPartida == null || idPartida <= 0) {
            throw new ReglaNegocioException("Selecciona una partida válida.");
        }
        partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        if (partidaPort.tieneMovimientos(idPartida)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar una partida que ya registra avances, consumos o producción.");
        }
        partidaPort.desactivar(idPartida);
    }
}
