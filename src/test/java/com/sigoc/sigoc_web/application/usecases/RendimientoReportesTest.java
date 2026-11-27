package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import com.sigoc.sigoc_web.infrastructure.adapters.ExportadorReporteAdapter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de carga y latencia para RNF-04:
 * - Consulta/generación de reporte bajo 20 peticiones concurrentes: p95 < 2000 ms.
 * - Exportación PDF apaisado (OpenPDF) y Excel OpenXML (.xlsx) bajo 10 peticiones concurrentes: p95 < 3000 ms.
 */
class RendimientoReportesTest {

    private static final UsuarioSesion GERENTE = new UsuarioSesion(2, "gerente", "Gerente Obras", RolSistema.GERENTE, false);

    private Reporte reporteSintetico(String titulo, int filas) {
        List<List<Object>> rows = new ArrayList<>(filas);
        for (int i = 1; i <= filas; i++) {
            rows.add(List.of("Obra #" + i, "2026-11-01", "85.50%", "S/ 450,000.00", "S/ 380,000.00"));
        }
        return new Reporte(
                titulo,
                List.of("Obra", "Fecha", "Avance", "Presupuesto", "Ejecutado"),
                rows
        );
    }

    private ReporteQueryPort queryStub(int filas) {
        Reporte r = reporteSintetico("Reporte Financiero de Carga", filas);
        return new ReporteQueryPort() {
            @Override public Reporte consultarCurvaS(Integer idObra, LocalDate desde, LocalDate hasta) { return r; }
            @Override public Reporte consultarConsumoMateriales(Integer idObra, LocalDate desde, LocalDate hasta) { return r; }
            @Override public Reporte consultarPersonalProductividad(Integer idObra, LocalDate desde, LocalDate hasta) { return r; }
            @Override public Reporte consultarFinanciero(Integer idObra, LocalDate desde, LocalDate hasta) { return r; }
            @Override public Reporte consultarValorizacionesCobranzas(Integer idObra, LocalDate desde, LocalDate hasta) { return r; }
        };
    }

    @Test
    void p95GeneracionYExportacionPdfXlsxCumpleRnf04BajoCargaConcurrente() throws Exception {
        ExportadorReporteAdapter exportador = new ExportadorReporteAdapter();
        GestionarReporte casoUso = new GestionarReporte(queryStub(200), exportador);

        // 20 peticiones concurrentes de generación de reporte (meta RNF-04: p95 < 2000 ms)
        List<Long> latenciasGen = ejecutarCargaConcurrente(20, () -> {
            Reporte r = casoUso.generar(GERENTE, TipoReporte.FINANCIERO, null, null, null);
            assertTrue(r.filas().size() == 200);
        });
        long p95Gen = percentil95(latenciasGen);

        // 10 peticiones concurrentes de exportación PDF + XLSX (meta RNF-04: p95 < 3000 ms)
        List<Long> latenciasExp = ejecutarCargaConcurrente(10, () -> {
            byte[] pdf = casoUso.exportar(GERENTE, TipoReporte.FINANCIERO, FormatoExportacion.PDF, null, null, null);
            byte[] xlsx = casoUso.exportar(GERENTE, TipoReporte.FINANCIERO, FormatoExportacion.XLSX, null, null, null);
            assertTrue(pdf.length > 500 && xlsx.length > 500);
        });
        long p95Exp = percentil95(latenciasExp);

        System.out.printf("[RNF-04] Generacion (20 hilos, 200 filas) p95=%d ms | Exportacion PDF+XLSX (10 hilos) p95=%d ms%n",
                p95Gen, p95Exp);
        assertTrue(p95Gen < 2000, "p95 de generación debe ser < 2000 ms, fue " + p95Gen + " ms");
        assertTrue(p95Exp < 3000, "p95 de exportación PDF/XLSX debe ser < 3000 ms, fue " + p95Exp + " ms");
    }

    private List<Long> ejecutarCargaConcurrente(int hilos, Runnable tarea) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Long> tiemposMs = Collections.synchronizedList(new ArrayList<>(hilos));
        List<Future<?>> futures = new ArrayList<>(hilos);
        for (int i = 0; i < hilos; i++) {
            futures.add(pool.submit(() -> {
                try {
                    inicio.await();
                    long t0 = System.nanoTime();
                    tarea.run();
                    tiemposMs.add((System.nanoTime() - t0) / 1_000_000L);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }));
        }
        inicio.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();
        return tiemposMs;
    }

    private long percentil95(List<Long> valores) {
        List<Long> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);
        int idx = (int) Math.ceil(0.95 * ordenados.size()) - 1;
        return ordenados.get(Math.max(0, Math.min(idx, ordenados.size() - 1)));
    }
}
