package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;

public interface ExportadorReportePort {
    byte[] exportar(Reporte reporte, FormatoExportacion formato);
}
