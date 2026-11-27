package com.sigoc.sigoc_web.infrastructure;

import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorReportePort;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de rendimiento real para RNF-04 contra MySQL 8 InnoDB (Testcontainers):
 * siembra 5 obras, 25 partidas, 100 avances, 10 materiales, compras, consumos,
 * 20 trabajadores, 100 asistencias, planillas, ingresos, egresos y valorizaciones,
 * realiza calentamiento de JVM/InnoDB (5 rondas) y mide 30 iteraciones reales
 * de las 5 consultas SQL de {@link ReporteQueryPort} ({@code ReporteJdbcAdapter})
 * y la exportación PDF/XLSX de {@link ExportadorReportePort}.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RendimientoReportesMySqlIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("sigoc_perf_it")
            .withUsername("sigoc")
            .withPassword("sigoc_test");

    @DynamicPropertySource
    static void configurarMySql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ReporteQueryPort reporteQueryPort;

    @Autowired
    private ExportadorReportePort exportadorReportePort;

    @Test
    void benchmarkRealCincoReportesJdbcYExportacionPdfXlsxCumpleRnf04() {
        sembrarDatosRealistas();

        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);

        // Calentamiento JVM + Buffer Pool InnoDB (5 iteraciones descartadas)
        for (int w = 0; w < 5; w++) {
            ejecutarCincoConsultas(desde, hasta);
        }

        // 30 iteraciones cronometradas de las 5 consultas SQL reales en MySQL 8
        int iteraciones = 30;
        List<Long> latenciasConsultasMs = new ArrayList<>(iteraciones * 5);
        List<Long> latenciasExportMs = new ArrayList<>(iteraciones);

        for (int i = 0; i < iteraciones; i++) {
            latenciasConsultasMs.add(medirMs(() -> assertFalse(reporteQueryPort.consultarCurvaS(0, desde, hasta).filas().isEmpty())));
            latenciasConsultasMs.add(medirMs(() -> assertFalse(reporteQueryPort.consultarConsumoMateriales(0, desde, hasta).filas().isEmpty())));
            latenciasConsultasMs.add(medirMs(() -> assertFalse(reporteQueryPort.consultarPersonalProductividad(0, desde, hasta).filas().isEmpty())));
            latenciasConsultasMs.add(medirMs(() -> assertFalse(reporteQueryPort.consultarFinanciero(0, desde, hasta).filas().isEmpty())));
            latenciasConsultasMs.add(medirMs(() -> assertFalse(reporteQueryPort.consultarValorizacionesCobranzas(0, desde, hasta).filas().isEmpty())));

            Reporte fin = reporteQueryPort.consultarFinanciero(0, desde, hasta);
            latenciasExportMs.add(medirMs(() -> {
                byte[] pdf = exportadorReportePort.exportar(fin, FormatoExportacion.PDF);
                byte[] xlsx = exportadorReportePort.exportar(fin, FormatoExportacion.XLSX);
                assertTrue(pdf.length > 500 && xlsx.length > 500);
            }));
        }

        long p50Sql = percentil(latenciasConsultasMs, 0.50);
        long p95Sql = percentil(latenciasConsultasMs, 0.95);
        long maxSql = Collections.max(latenciasConsultasMs);
        long p50Exp = percentil(latenciasExportMs, 0.50);
        long p95Exp = percentil(latenciasExportMs, 0.95);
        long maxExp = Collections.max(latenciasExportMs);

        System.out.printf(
                "[RNF-04 MySQL 8 REAL] 150 consultas ReporteJdbcAdapter (5 reportes x 30 iter, 5 warmup): p50=%d ms, p95=%d ms, max=%d ms%n",
                p50Sql, p95Sql, maxSql);
        System.out.printf(
                "[RNF-04 MySQL 8 REAL] 30 exportaciones PDF+XLSX (OpenPDF+OpenXML): p50=%d ms, p95=%d ms, max=%d ms%n",
                p50Exp, p95Exp, maxExp);

        assertTrue(p95Sql < 2000, "p95 de consultas SQL reales debe ser < 2000 ms (fue " + p95Sql + " ms)");
        assertTrue(p95Exp < 3000, "p95 de exportación PDF+XLSX debe ser < 3000 ms (fue " + p95Exp + " ms)");
    }

    private void ejecutarCincoConsultas(LocalDate desde, LocalDate hasta) {
        reporteQueryPort.consultarCurvaS(0, desde, hasta);
        reporteQueryPort.consultarConsumoMateriales(0, desde, hasta);
        reporteQueryPort.consultarPersonalProductividad(0, desde, hasta);
        reporteQueryPort.consultarFinanciero(0, desde, hasta);
        reporteQueryPort.consultarValorizacionesCobranzas(0, desde, hasta);
    }

    private void sembrarDatosRealistas() {
        jdbc.update("INSERT INTO rol (id_rol, nombre_rol, activo) VALUES (1, 'GERENTE', 1)");
        jdbc.update("""
                INSERT INTO usuario (id_usuario, id_rol, nombre_completo, usuario_login, contrasena_hash, estado, activo)
                VALUES (1, 1, 'Gerente Perf', 'gerente_perf', 'PBKDF2$600000$x$y', 1, 1)
                """);

        for (int o = 1; o <= 5; o++) {
            jdbc.update("""
                    INSERT INTO cliente (id_cliente, tipo_documento, numero_documento, nombre_razon_social, telefono, activo)
                    VALUES (?, 'RUC', ?, ?, '952000001', 1)
                    """, o, "2060000000" + o, "Cliente Obra " + o);
            jdbc.update("""
                    INSERT INTO obra (id_obra, id_cliente, nombre, cliente, ubicacion, presupuesto_total, fecha_inicio_plan, fecha_fin_plan, estado, activo)
                    VALUES (?, ?, ?, ?, 'Tacna', 500000.00, '2026-01-10', '2026-12-20', 'EN_EJECUCION', 1)
                    """, o, o, "Obra Civil " + o, "Cliente Obra " + o);
        }

        int idPartida = 1;
        int idAvance = 1;
        for (int o = 1; o <= 5; o++) {
            for (int p = 1; p <= 5; p++) {
                jdbc.update("""
                        INSERT INTO partida (id_partida, id_obra, nombre, unidad_medida, metrado_planificado, peso_presupuestal, activo)
                        VALUES (?, ?, ?, 'm2', 200.00, 20.00, 1)
                        """, idPartida, o, "Partida " + o + "-" + p);
                for (int a = 1; a <= 4; a++) {
                    jdbc.update("""
                            INSERT INTO avance_partida (id_avance, id_partida, fecha_reporte, porcentaje_avance, id_usuario, activo)
                            VALUES (?, ?, ?, ?, 1, 1)
                            """, idAvance++, idPartida, LocalDate.of(2026, a * 2, 15), a * 20.0);
                }
                idPartida++;
            }
        }

        for (int m = 1; m <= 10; m++) {
            jdbc.update("""
                    INSERT INTO material (id_material, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario_ref, activo)
                    VALUES (?, ?, 'und', 500.00, 50.00, 35.00, 1)
                    """, m, "Material " + m);
        }
        for (int prov = 1; prov <= 5; prov++) {
            jdbc.update("""
                    INSERT INTO proveedor (id_proveedor, razon_social, ruc, activo)
                    VALUES (?, ?, ?, 1)
                    """, prov, "Proveedor " + prov, "2050000000" + prov);
        }

        int idDet = 1;
        int idEgr = 1;
        for (int c = 1; c <= 25; c++) {
            int obra = ((c - 1) % 5) + 1;
            int prov = ((c - 1) % 5) + 1;
            int mat = ((c - 1) % 10) + 1;
            jdbc.update("""
                    INSERT INTO compra_material (id_compra, id_proveedor, tipo_comprobante, fecha_compra, numero_comprobante, total, activo)
                    VALUES (?, ?, 'Factura', '2026-04-10', ?, 3500.00, 1)
                    """, c, prov, "F001-000" + c);
            jdbc.update("""
                    INSERT INTO detalle_compra (id_detalle, id_compra, id_material, cantidad, precio_unitario, subtotal, activo)
                    VALUES (?, ?, ?, 100.00, 35.00, 3500.00, 1)
                    """, idDet++, c, mat);
            jdbc.update("""
                    INSERT INTO egreso (id_egreso, id_obra, id_compra, fecha, tipo_egreso, concepto, monto, activo)
                    VALUES (?, ?, ?, '2026-04-10', 'Material', 'Compra materiales', 3500.00, 1)
                    """, idEgr++, obra, c);
        }

        for (int cs = 1; cs <= 50; cs++) {
            int part = ((cs - 1) % 25) + 1;
            int mat = ((cs - 1) % 10) + 1;
            jdbc.update("""
                    INSERT INTO consumo_material (id_consumo, id_partida, id_material, fecha_consumo, cantidad, id_usuario, activo)
                    VALUES (?, ?, ?, '2026-05-12', 12.00, 1, 1)
                    """, cs, part, mat);
        }

        for (int emp = 1; emp <= 20; emp++) {
            jdbc.update("""
                    INSERT INTO empleado (id_empleado, nombres_apellidos, dni, cargo, jornal_diario, fecha_ingreso, activo)
                    VALUES (?, ?, ?, 'Operario', 110.00, '2026-01-02', 1)
                    """, emp, "Trabajador " + emp, String.format("4000%04d", emp));
        }

        int idAsist = 1;
        for (int emp = 1; emp <= 20; emp++) {
            int obra = ((emp - 1) % 5) + 1;
            for (int d = 1; d <= 5; d++) {
                jdbc.update("""
                        INSERT INTO asistencia (id_asistencia, id_empleado, id_obra, fecha, estado, activo)
                        VALUES (?, ?, ?, ?, 'Presente', 1)
                        """, idAsist++, emp, obra, LocalDate.of(2026, 6, d));
            }
        }

        for (int o = 1; o <= 5; o++) {
            jdbc.update("""
                    INSERT INTO ingreso (id_ingreso, id_obra, fecha, concepto, monto, activo)
                    VALUES (?, ?, '2026-06-20', 'Adelanto obra', 120000.00, 1)
                    """, o, o);
            jdbc.update("""
                    INSERT INTO valorizacion (id_valorizacion, id_obra, numero, periodo, periodo_inicio, periodo_fin, fecha_emision, avance_acumulado, porcentaje_avance_periodo, subtotal, igv, total, monto_valorizado, estado, activo)
                    VALUES (?, ?, 1, '2026-S1', '2026-01-01', '2026-06-30', '2026-07-01', 60.00, 60.00, 250000.00, 45000.00, 295000.00, 295000.00, 'FACTURADA', 1)
                    """, o, o);
            jdbc.update("""
                    INSERT INTO comprobante (id_comprobante, id_valorizacion, tipo_comprobante, serie, numero, serie_numero, fecha_emision, subtotal, igv, total, codigo_pago, vence_el, estado, activo)
                    VALUES (?, ?, 'COMPROBANTE_INTERNO', 'CV01', ?, ?, '2026-07-01', 250000.00, 45000.00, 295000.00, ?, '2026-07-04 12:00:00', 'EMITIDO', 1)
                    """, o, o, o, "CV01-00000" + o, "TOK-PERF-" + o);
        }
    }

    private long medirMs(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000_000L;
    }

    private long percentil(List<Long> valores, double p) {
        List<Long> ord = new ArrayList<>(valores);
        Collections.sort(ord);
        int idx = (int) Math.ceil(p * ord.size()) - 1;
        return ord.get(Math.max(0, Math.min(idx, ord.size() - 1)));
    }
}
