package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.time.LocalDate;

/**
 * CU-16: Generación y exportación de reportes gerenciales (Curva S, Consumo, Personal, Financiero).
 */
public interface GenerarReporteUseCase {
    @OperacionSoloLectura
    Reporte generar(UsuarioSesion actor, TipoReporte tipo, Integer idObra, LocalDate desde, LocalDate hasta);

    @OperacionSoloLectura
    byte[] exportar(UsuarioSesion actor, TipoReporte tipo, FormatoExportacion formato,
                    Integer idObra, LocalDate desde, LocalDate hasta);
}
