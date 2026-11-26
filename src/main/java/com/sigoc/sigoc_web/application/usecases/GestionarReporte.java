package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GenerarReporteUseCase;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorReportePort;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.time.LocalDate;

/**
 * CU-16: Generación y exportación de reportes gerenciales (GERENTE y CONTADOR).
 */
public class GestionarReporte implements GenerarReporteUseCase {

    private final ReporteQueryPort reporteQueryPort;
    private final ExportadorReportePort exportadorPort;

    public GestionarReporte(ReporteQueryPort reporteQueryPort, ExportadorReportePort exportadorPort) {
        this.reporteQueryPort = reporteQueryPort;
        this.exportadorPort = exportadorPort;
    }

    @Override
    public Reporte generar(UsuarioSesion actor, TipoReporte tipo, Integer idObra,
                           LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GENERAR_REPORTES);
        if (tipo == null) {
            throw new ReglaNegocioException("Especifica el tipo de reporte.");
        }
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final del filtro no puede ser anterior a la fecha inicial.");
        }
        int obraFiltro = (idObra == null || idObra <= 0) ? 0 : idObra;
        return switch (tipo) {
            case CURVA_S -> reporteQueryPort.consultarCurvaS(obraFiltro, desde, hasta);
            case CONSUMO_MATERIALES -> reporteQueryPort.consultarConsumoMateriales(obraFiltro, desde, hasta);
            case PERSONAL_PRODUCTIVIDAD -> reporteQueryPort.consultarPersonalProductividad(obraFiltro, desde, hasta);
            case FINANCIERO -> reporteQueryPort.consultarFinanciero(obraFiltro, desde, hasta);
            case VALORIZACIONES_COBRANZAS -> reporteQueryPort.consultarValorizacionesCobranzas(obraFiltro, desde, hasta);
        };
    }

    @Override
    public byte[] exportar(UsuarioSesion actor, TipoReporte tipo, FormatoExportacion formato,
                           Integer idObra, LocalDate desde, LocalDate hasta) {
        if (formato == null) {
            throw new ReglaNegocioException("Especifica el formato de exportación.");
        }
        Reporte reporte = generar(actor, tipo, idObra, desde, hasta);
        return exportadorPort.exportar(reporte, formato);
    }
}
