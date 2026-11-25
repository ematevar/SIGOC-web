package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.ConsultarAuditoriaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * CU-25: Consulta en solo lectura de la bitácora de auditoría (exclusivo GERENTE).
 */
public class ConsultarAuditoria implements ConsultarAuditoriaUseCase {

    private final AuditoriaEventoPort auditoriaEventoPort;

    public ConsultarAuditoria(AuditoriaEventoPort auditoriaEventoPort) {
        this.auditoriaEventoPort = auditoriaEventoPort;
    }

    @Override
    public List<AuditoriaEvento> ejecutar(UsuarioSesion actor, String accion, Integer limite) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.CONSULTAR_AUDITORIA);
        int max = (limite == null || limite <= 0) ? 100 : Math.min(limite, 500);
        String filtro = (accion == null || accion.isBlank()) ? null : accion.trim().toUpperCase();
        return auditoriaEventoPort.listarRecientes(filtro, max);
    }
}
