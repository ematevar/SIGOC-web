package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import com.sigoc.sigoc_web.infrastructure.adapters.ExportadorReporteAdapter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportesTest {

    private static final UsuarioSesion ADMIN = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private static final UsuarioSesion GERENTE = new UsuarioSesion(2, "gerente", "Gerente", RolSistema.GERENTE, false);
    private static final UsuarioSesion ALMACENERO = new UsuarioSesion(3, "alm", "Alm", RolSistema.ALMACENERO, false);
    private static final UsuarioSesion CONTADOR = new UsuarioSesion(4, "cont", "Contador", RolSistema.CONTADOR, false);

    @Test
    void soloGerenteYContadorPuedenGenerarYValidaRangoFechas() {
        GestionarReporte uc = new GestionarReporte(fakePort(), new ExportadorReporteAdapter());

        assertThrows(AccesoDenegadoException.class,
                () -> uc.generar(ALMACENERO, TipoReporte.CURVA_S, null, null, null));
        assertThrows(AccesoDenegadoException.class,
                () -> uc.generar(ADMIN, TipoReporte.CURVA_S, null, null, null));

        assertThrows(ReglaNegocioException.class,
                () -> uc.generar(GERENTE, TipoReporte.FINANCIERO, 1,
                        LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 10)));

        Reporte curva = uc.generar(GERENTE, TipoReporte.CURVA_S, 1,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        assertEquals("Curva S Fake", curva.titulo());
        assertEquals(1, curva.filas().size());

        Reporte valCob = uc.generar(CONTADOR, TipoReporte.VALORIZACIONES_COBRANZAS, 1, null, null);
        assertEquals("Valorizaciones Fake", valCob.titulo());
        assertEquals(1, valCob.filas().size());
    }

    @Test
    void exportaCsvXlsxYPdfSinTruncamientoYConFirmasValidas() {
        GestionarReporte uc = new GestionarReporte(fakePort(), new ExportadorReporteAdapter());

        byte[] csv = uc.exportar(GERENTE, TipoReporte.FINANCIERO, FormatoExportacion.CSV, null, null, null);
        String csvTexto = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvTexto.startsWith("\uFEFF\"Obra\",\"Saldo\""));
        assertTrue(csvTexto.contains("\"Construcción Integral Hospital Regional Norte\""));

        byte[] xlsx = uc.exportar(GERENTE, TipoReporte.FINANCIERO, FormatoExportacion.XLSX, null, null, null);
        assertTrue(xlsx.length > 100);
        assertEquals('P', (char) xlsx[0]);
        assertEquals('K', (char) xlsx[1]);

        byte[] pdf = uc.exportar(GERENTE, TipoReporte.FINANCIERO, FormatoExportacion.PDF, null, null, null);
        String pdfCabecera = new String(pdf, 0, Math.min(pdf.length, 16), StandardCharsets.ISO_8859_1);
        assertTrue(pdfCabecera.startsWith("%PDF-1.4"));
        assertTrue(pdf.length > 800);
    }

    @Test
    void convierteParametrosDeTipoYFormato() {
        assertEquals(TipoReporte.CURVA_S, TipoReporte.desdeParametro("0"));
        assertEquals(TipoReporte.CONSUMO_MATERIALES, TipoReporte.desdeParametro("CONSUMO_MATERIALES"));
        assertEquals(TipoReporte.VALORIZACIONES_COBRANZAS, TipoReporte.desdeParametro("4"));
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
                        List.of(List.of("Construcción Integral Hospital Regional Norte", 1500.5)));
            }

            @Override
            public Reporte consultarValorizacionesCobranzas(Integer idObra, LocalDate desde, LocalDate hasta) {
                return new Reporte("Valorizaciones Fake", List.of("Obra", "Val. N°", "Total"),
                        List.of(List.of("Obra Norte", "N° 1", 295000.0)));
            }
        };
    }
}
