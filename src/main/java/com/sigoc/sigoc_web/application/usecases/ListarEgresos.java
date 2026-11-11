package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.EgresoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-15: lista los egresos activos registrados (materiales, planillas y otros).
 */
public class ListarEgresos {
    private final EgresoPort egresoPort;

    public ListarEgresos(EgresoPort egresoPort) {
        this.egresoPort = egresoPort;
    }

    public List<Egreso> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idObra != null && idObra > 0) {
            return egresoPort.findActivosByObra(idObra);
        }
        return egresoPort.findAllActivos();
    }
}
