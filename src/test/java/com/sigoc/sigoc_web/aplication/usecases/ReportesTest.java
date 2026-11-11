package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import com.sigoc.sigoc_web.infraestructure.adapters.ExportadorReporteAdapter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportesTest {

    private static final UsuarioSesion ADMIN = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private static final UsuarioSesion ALMACENERO = new UsuarioSesion(3, "alm", "Alm", RolSistema.ALMACENERO, false);

    @Test
    void soloAdminPuedeGenerarYValidaRangoFechas() {
        GenerarReporte uc = new GenerarReporte(fakePort());

        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(ALMACENERO, TipoReporte.CURVA_S, null, null, null));

        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(ADMIN, TipoReporte.FINANCIERO, 1,
                        LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 10)));

        Reporte curva = uc.ejecutar(ADMIN, TipoReporte.CURVA_S, 1,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        assertEquals("Curva S Fake", curva.titulo());
        assertEquals(1, curva.filas().size());
    }

    @Test
    void exportaCsvXlsxYPdfConFirmasValidas() {
        GenerarReporte generar = new GenerarReporte(fakePort());
        ExportarReporte exportar = new ExportarReporte(generar, new ExportadorReporteAdapter());

        byte[] csv = exportar.ejecutar(ADMIN, TipoReporte.FINANCIERO, FormatoExportacion.CSV, null, null, null);
        String csvTexto = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvTexto.startsWith("\uFEFF\"Obra\",\"Saldo\""));
        assertTrue(csvTexto.contains("\"Obra Norte\",\"1500.5\""));

        byte[] xlsx = exportar.ejecutar(ADMIN, TipoReporte.FINANCIERO, FormatoExportacion.XLSX, null, null, null);
        assertTrue(xlsx.length > 100);
        assertEquals('P', (char) xlsx[0]);
        assertEquals('K', (char) xlsx[1]);

        byte[] pdf = exportar.ejecutar(ADMIN, TipoReporte.FINANCIERO, FormatoExportacion.PDF, null, null, null);
        String pdfCabecera = new String(pdf, 0, Math.min(pdf.length, 16), StandardCharsets.ISO_8859_1);
        assertTrue(pdfCabecera.startsWith("%PDF-1.4"));
    }

    @Test
    void convierteParametrosDeTipoYFormato() {
        assertEquals(TipoReporte.CURVA_S, TipoReporte.desdeParametro("0"));
        assertEquals(TipoReporte.CONSUMO_MATERIALES, TipoReporte.desdeParametro("CONSUMO_MATERIALES"));
        assertEquals(FormatoExportacion.XLSX, FormatoExportacion.desdeParametro("excel"));
        assertThrows(ReglaNegocioException.class, () -> TipoReporte.desdeParametro("invalido"));
        assertThrows(ReglaNegocioException.class, () -> FormatoExportacion.desdeParametro("docx"));
    }

    private static ReporteQueryPort fakePort() {
        return new ReporteQueryPort() {
            @Override
            public Reporte consultarCurvaS(Integer idObra, LocalDate desde, LocalDate hasta) {
                return new Reporte("Curva S Fake", List.of("Obra", "Plan", "Ejec"),
                        List.of(List.of("Obra Norte", 50.0, 55.0)));
            }

            @Override
            public Reporte consultarConsumoMateriales(Integer idObra, LocalDate desde, LocalDate hasta) {
                return new Reporte("Consumo Fake", List.of("Material"), List.of(List.of("Cemento")));
            }

            @Override
            public Reporte consultarPersonalProductividad(Integer idObra, LocalDate desde, LocalDate hasta) {
                return new Reporte("Personal Fake", List.of("Trabajador"), List.of(List.of("Juan")));
            }

            @Override
            public Reporte consultarFinanciero(Integer idObra, LocalDate desde, LocalDate hasta) {
                return new Reporte("Financiero Fake", List.of("Obra", "Saldo"),
                        List.of(List.of("Obra Norte", 1500.5)));
            }
        };
    }
}
