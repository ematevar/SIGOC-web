package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;

import java.util.List;

/**
 * Puerto de salida para persistencia inmutable (solo inserción y lectura)
 * de eventos de auditoría.
 */
public interface AuditoriaEventoPort {
    AuditoriaEvento registrar(AuditoriaEvento evento);

    List<AuditoriaEvento> listarRecientes(String accion, int limite);
}
