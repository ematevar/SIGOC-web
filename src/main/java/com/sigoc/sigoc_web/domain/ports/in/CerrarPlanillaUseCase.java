package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.time.LocalDate;

public interface CerrarPlanillaUseCase {
    CierrePlanilla ejecutar(UsuarioSesion actor, Integer idObra, LocalDate periodoInicio, LocalDate periodoFin);
}
