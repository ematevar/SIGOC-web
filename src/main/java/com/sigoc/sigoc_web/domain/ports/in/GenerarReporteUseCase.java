package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.time.LocalDate;

public interface GenerarReporteUseCase {
    Reporte ejecutar(UsuarioSesion actor, TipoReporte tipo, Integer idObra, LocalDate desde, LocalDate hasta);
}
