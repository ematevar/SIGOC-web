package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ProduccionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-12: lista los registros activos de producción diaria.
 */
public class ListarProduccion {
    private final ProduccionPort produccionPort;

    public ListarProduccion(ProduccionPort produccionPort) {
        this.produccionPort = produccionPort;
    }

    public List<ProduccionEmpleado> ejecutar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        return idPartida == null ? produccionPort.findAllActivas() : produccionPort.findActivasByPartida(idPartida);
    }
}
