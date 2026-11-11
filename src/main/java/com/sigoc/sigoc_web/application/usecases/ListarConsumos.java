package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ConsumoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-08: lista consumos activos de materiales.
 */
public class ListarConsumos {
    private final ConsumoPort consumoPort;

    public ListarConsumos(ConsumoPort consumoPort) {
        this.consumoPort = consumoPort;
    }

    public List<ConsumoMaterial> ejecutar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.ALMACENERO);
        return idPartida == null ? consumoPort.findAllActivos() : consumoPort.findActivosByPartida(idPartida);
    }
}
