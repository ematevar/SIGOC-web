package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.CierrePlanillaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-14: lista los cierres de planilla registrados (inmutables).
 */
public class ListarCierresPlanilla {
    private final CierrePlanillaPort cierrePlanillaPort;

    public ListarCierresPlanilla(CierrePlanillaPort cierrePlanillaPort) {
        this.cierrePlanillaPort = cierrePlanillaPort;
    }

    public List<CierrePlanilla> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        if (idObra != null && idObra > 0) {
            return cierrePlanillaPort.findActivosByObra(idObra);
        }
        return cierrePlanillaPort.findAllActivos();
    }
}
