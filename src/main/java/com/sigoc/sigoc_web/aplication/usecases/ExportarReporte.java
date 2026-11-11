package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorReportePort;

import java.time.LocalDate;

/**
 * RF-16: genera un reporte gerencial y lo exporta a CSV, XLSX o PDF.
 */
public class ExportarReporte {

    private final GenerarReporte generarReporte;
    private final ExportadorReportePort exportadorPort;

    public ExportarReporte(GenerarReporte generarReporte, ExportadorReportePort exportadorPort) {
        this.generarReporte = generarReporte;
        this.exportadorPort = exportadorPort;
    }

    public byte[] ejecutar(UsuarioSesion actor, TipoReporte tipo, FormatoExportacion formato,
                           Integer idObra, LocalDate desde, LocalDate hasta) {
        if (formato == null) {
            throw new ReglaNegocioException("Especifica el formato de exportación.");
        }
        Reporte reporte = generarReporte.ejecutar(actor, tipo, idObra, desde, hasta);
        return exportadorPort.exportar(reporte, formato);
    }
}
