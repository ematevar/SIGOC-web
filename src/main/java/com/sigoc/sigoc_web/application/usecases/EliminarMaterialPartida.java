package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-16b: da de baja lógica un material presupuestado en una partida.
 */
public class EliminarMaterialPartida {
    private final PartidaMaterialPort partidaMaterialPort;

    public EliminarMaterialPartida(PartidaMaterialPort partidaMaterialPort) {
        this.partidaMaterialPort = partidaMaterialPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idPartidaMaterial) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idPartidaMaterial == null || idPartidaMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona un registro válido.");
        }
        partidaMaterialPort.findById(idPartidaMaterial)
                .filter(PartidaMaterial::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el material presupuestado."));
        partidaMaterialPort.desactivar(idPartidaMaterial);
    }
}
