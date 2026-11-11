package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-11: lista registros activos de asistencia.
 */
public class ListarAsistencias {
    private final AsistenciaPort asistenciaPort;

    public ListarAsistencias(AsistenciaPort asistenciaPort) {
        this.asistenciaPort = asistenciaPort;
    }

    public List<Asistencia> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        return idObra == null ? asistenciaPort.findAllActivas() : asistenciaPort.findActivasByObra(idObra);
    }
}
