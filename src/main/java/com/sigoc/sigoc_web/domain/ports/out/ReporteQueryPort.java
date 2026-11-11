package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Reporte;

import java.time.LocalDate;

public interface ReporteQueryPort {
    Reporte consultarCurvaS(Integer idObra, LocalDate desde, LocalDate hasta);

    Reporte consultarConsumoMateriales(Integer idObra, LocalDate desde, LocalDate hasta);

    Reporte consultarPersonalProductividad(Integer idObra, LocalDate desde, LocalDate hasta);

    Reporte consultarFinanciero(Integer idObra, LocalDate desde, LocalDate hasta);
}
