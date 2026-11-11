package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Ingreso;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-13: lista los ingresos registrados por obra o todos los activos.
 */
public class ListarIngresos {
    private final IngresoPort ingresoPort;

    public ListarIngresos(IngresoPort ingresoPort) {
        this.ingresoPort = ingresoPort;
    }

    public List<Ingreso> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        if (idObra != null && idObra > 0) {
            return ingresoPort.findActivosByObra(idObra);
        }
        return ingresoPort.findAllActivos();
    }
}
