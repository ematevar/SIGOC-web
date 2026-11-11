package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GenerarReporteUseCase;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.time.LocalDate;

/**
 * RF-16: genera uno de los 4 reportes gerenciales (Curva S, Consumo, Personal, Financiero).
 */
public class GenerarReporte implements GenerarReporteUseCase {

    private final ReporteQueryPort reporteQueryPort;

    public GenerarReporte(ReporteQueryPort reporteQueryPort) {
        this.reporteQueryPort = reporteQueryPort;
    }

    public Reporte ejecutar(UsuarioSesion actor, TipoReporte tipo, Integer idObra,
                            LocalDate desde, LocalDate hasta) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
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
}
