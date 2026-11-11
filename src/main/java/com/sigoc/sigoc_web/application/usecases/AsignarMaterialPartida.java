package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RF-16b: asigna o actualiza la cantidad presupuestada de un material en una partida activa.
 */
public class AsignarMaterialPartida {

    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final PartidaMaterialPort partidaMaterialPort;

    public AsignarMaterialPartida(PartidaPort partidaPort, ObraPort obraPort, PartidaMaterialPort partidaMaterialPort) {
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.partidaMaterialPort = partidaMaterialPort;
    }

    public PartidaMaterial ejecutar(UsuarioSesion actor, Integer idPartida, Integer idMaterial,
                                    BigDecimal cantidadPresupuestada) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idPartida == null || idPartida <= 0 || idMaterial == null || idMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona una partida y un material válidos.");
        }
        BigDecimal cantBd = cantidadPresupuestada == null
                ? null
                : cantidadPresupuestada.setScale(2, RoundingMode.HALF_UP);
        if (cantBd == null || cantBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad presupuestada debe ser mayor que cero.");
        }
        Partida partida = partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        Obra obra = obraPort.findById(partida.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("Solo se puede presupuestar materiales en partidas de una obra activa.");
        }
        if (!partidaMaterialPort.existeMaterialActivo(idMaterial)) {
            throw new RecursoNoEncontradoException("El material indicado no existe o está inactivo.");
        }
        return partidaMaterialPort.guardarOActualizar(idPartida, idMaterial,
                cantidadPresupuestada.setScale(2, RoundingMode.HALF_UP));
    }
}
