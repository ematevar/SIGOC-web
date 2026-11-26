package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.List;

/**
 * CU-25: Consultar bitácora de auditoría inmutable (solo lectura para GERENTE).
 */
public interface ConsultarAuditoriaUseCase {
    @OperacionSoloLectura
    List<AuditoriaEvento> ejecutar(UsuarioSesion actor, String accion, Integer limite);
}
