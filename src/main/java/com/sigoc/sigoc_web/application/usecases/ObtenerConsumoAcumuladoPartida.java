package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ConsumoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;

/**
 * RF-08: obtiene el consumo acumulado de un material dentro de una partida.
 */
public class ObtenerConsumoAcumuladoPartida {
    private final ConsumoPort consumoPort;

    public ObtenerConsumoAcumuladoPartida(ConsumoPort consumoPort) {
        this.consumoPort = consumoPort;
    }

    public BigDecimal ejecutar(UsuarioSesion actor, Integer idPartida, Integer idMaterial) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR);
        return consumoPort.sumaCantidadPorPartidaYMaterial(idPartida, idMaterial);
    }
}
