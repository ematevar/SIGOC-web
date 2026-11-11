package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.ports.out.ReporteQueryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC de consultas agregadas para los 4 reportes gerenciales (RF-16).
 */
@Repository
@Transactional(readOnly = true)
public class ReporteJdbcAdapter implements ReporteQueryPort {

    private final JdbcTemplate jdbcTemplate;
    private volatile Boolean cacheTablaPartidaMaterial;

    public ReporteJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reporte consultarCurvaS(Integer idObra, LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT o.nombre AS obra, fechas.fecha_reporte,
                       ROUND(LEAST(100.0, GREATEST(0.0, (DATEDIFF(fechas.fecha_reporte, o.fecha_inicio_plan) * 100.0)
                             / NULLIF(DATEDIFF(o.fecha_fin_plan, o.fecha_inicio_plan), 0))), 2) AS avance_planificado_pct,
                       ROUND(SUM(p.peso_presupuestal * COALESCE((
                           SELECT a.porcentaje_avance FROM avance_partida a
                           WHERE a.id_partida=p.id_partida AND a.activo=1 AND a.fecha_reporte<=fechas.fecha_reporte
                           ORDER BY a.fecha_reporte DESC, a.id_avance DESC LIMIT 1
                       ), 0)) / 100.0, 2) AS avance_ejecutado_pct
                FROM obra o
                JOIN partida p ON p.id_obra=o.id_obra AND p.activo=1
                JOIN (
                    SELECT DISTINCT p2.id_obra, a.fecha_reporte
                    FROM avance_partida a JOIN partida p2 ON p2.id_partida=a.id_partida
                    WHERE a.activo=1 AND p2.activo=1
                    UNION
                    SELECT id_obra, fecha_inicio_plan AS fecha_reporte FROM obra WHERE activo=1
                ) fechas ON fechas.id_obra=o.id_obra
                WHERE o.activo=1
                  AND (? = 0 OR o.id_obra = ?)
                  AND (? IS NULL OR fechas.fecha_reporte >= ?)
                  AND (? IS NULL OR fechas.fecha_reporte <= ?)
                GROUP BY o.id_obra, o.nombre, o.fecha_inicio_plan, o.fecha_fin_plan, fechas.fecha_reporte
                ORDER BY o.nombre, fechas.fecha_reporte
                """;

        List<String> cols = List.of(
                "Obra", "Fecha Corte", "Avance Planificado (%)", "Avance Ejecutado (%)", "Desviación (%)");
        List<List<Object>> rows = jdbcTemplate.query(sql, ps -> {
            int i = 1;
            i = setObraFiltro(ps, i, idObra);
            setRangoFechas(ps, i, desde, hasta);
        }, (rs, rowNum) -> {
            double plan = rs.getDouble(3);
            double ejec = rs.getDouble(4);
            double desv = redondear2(ejec - plan);
            return List.of(rs.getString(1), rs.getDate(2).toString(), plan, ejec, desv);
        });
        return new Reporte("Avance físico planificado vs. ejecutado (Curva S)", cols, rows);
    }

    @Override
    public Reporte consultarConsumoMateriales(Integer idObra, LocalDate desde, LocalDate hasta) {
        boolean tieneTabla = existeTablaPartidaMaterial();
        String subPresupuesto = tieneTabla
                ? "COALESCE((SELECT SUM(pm.cantidad_presupuestada) FROM partida_material pm "
                + "JOIN partida p ON p.id_partida=pm.id_partida "
                + "WHERE pm.id_material=m.id_material AND pm.activo=1 AND p.activo=1 "
                + "AND (? = 0 OR p.id_obra = ?)), 0)"
                : "0";

        String sql = "SELECT m.nombre AS material, m.unidad_medida, "
                + "COALESCE(("
                + "  SELECT SUM(dc.cantidad) FROM detalle_compra dc "
                + "  JOIN compra_material cm ON cm.id_compra=dc.id_compra "
                + "  LEFT JOIN egreso eg ON eg.id_compra=cm.id_compra AND eg.activo=1 "
                + "  WHERE dc.id_material=m.id_material AND dc.activo=1 AND cm.activo=1 "
                + "  AND (? = 0 OR eg.id_obra = ?) "
                + "  AND (? IS NULL OR cm.fecha_compra >= ?) "
                + "  AND (? IS NULL OR cm.fecha_compra <= ?)"
                + "), 0) AS cant_comprada, "
                + subPresupuesto + " AS cant_tabla_presup, "
                + "COALESCE(("
                + "  SELECT SUM(c.cantidad) FROM consumo_material c "
                + "  JOIN partida p ON p.id_partida=c.id_partida "
                + "  WHERE c.id_material=m.id_material AND c.activo=1 AND p.activo=1 "
                + "  AND (? = 0 OR p.id_obra = ?) "
                + "  AND (? IS NULL OR c.fecha_consumo >= ?) "
                + "  AND (? IS NULL OR c.fecha_consumo <= ?)"
                + "), 0) AS consumo_real, "
                + "m.stock_actual, m.stock_minimo "
                + "FROM material m WHERE m.activo=1 ORDER BY m.nombre";

        List<String> cols = List.of(
                "Material", "Unidad", "Presupuestado", "Consumo Real",
                "Desviación Abs.", "Desviación (%)", "Stock Actual", "Stock Mínimo"
        );
        List<List<Object>> rows = new ArrayList<>();
        jdbcTemplate.query(sql, ps -> {
            int i = 1;
            i = setObraFiltro(ps, i, idObra);
            i = setRangoFechas(ps, i, desde, hasta);
            if (tieneTabla) {
                i = setObraFiltro(ps, i, idObra);
            }
            i = setObraFiltro(ps, i, idObra);
            setRangoFechas(ps, i, desde, hasta);
        }, rs -> {
            String mat = rs.getString("material");
            String und = rs.getString("unidad_medida");
            double comprada = rs.getDouble("cant_comprada");
            double tablaPres = rs.getDouble("cant_tabla_presup");
            double real = redondear2(rs.getDouble("consumo_real"));
            double stock = redondear2(rs.getDouble("stock_actual"));
            double minimo = redondear2(rs.getDouble("stock_minimo"));

            // C08: Solo usa presupuesto explícito de partida_material; nunca sustituye con compras históricas.
            Object presVal = tablaPres > 0 ? redondear2(tablaPres) : "Sin presupuesto";
            Object desvAbs = tablaPres > 0 ? redondear2(real - tablaPres) : "N/A";
            Object desvPct = tablaPres > 0 ? redondear2(((real - tablaPres) * 100.0) / tablaPres) : "N/A";

            if (idObra == 0 || tablaPres > 0 || comprada > 0 || real > 0) {
                rows.add(List.of(mat, und, presVal, real, desvAbs, desvPct, stock, minimo));
            }
        });
        return new Reporte("Consumo de materiales real vs. presupuestado", cols, rows);
    }

    @Override
    public Reporte consultarPersonalProductividad(Integer idObra, LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT e.dni, e.nombres_apellidos, e.cargo,
                (SELECT COUNT(*) FROM asistencia a WHERE a.id_empleado=e.id_empleado AND a.activo=1
                 AND (? = 0 OR a.id_obra = ?) AND (? IS NULL OR a.fecha >= ?) AND (? IS NULL OR a.fecha <= ?)) AS total_registros,
                (SELECT COUNT(*) FROM asistencia a WHERE a.id_empleado=e.id_empleado AND a.activo=1
                 AND UPPER(a.estado) IN ('PRESENTE','TARDANZA','ASISTIO','ASISTIÓ')
                 AND (? = 0 OR a.id_obra = ?) AND (? IS NULL OR a.fecha >= ?) AND (? IS NULL OR a.fecha <= ?)) AS asistencias_validas,
                (SELECT COALESCE(SUM(pr.cantidad_producida), 0) FROM produccion_empleado pr
                 JOIN partida p ON p.id_partida=pr.id_partida
                 WHERE pr.id_empleado=e.id_empleado AND pr.activo=1 AND p.activo=1
                 AND (? = 0 OR p.id_obra = ?) AND (? IS NULL OR pr.fecha >= ?) AND (? IS NULL OR pr.fecha <= ?)) AS produccion_total
                FROM empleado e WHERE e.activo=1
                """;

        long diasRango = (desde != null && hasta != null)
                ? Math.max(1L, ChronoUnit.DAYS.between(desde, hasta) + 1)
                : 0L;

        List<String> cols = List.of(
                "DNI", "Trabajador", "Cargo", "Asistencias Válidas",
                "Cumplimiento Asistencia (%)", "Producción Total", "Productividad Promedio"
        );
        List<List<Object>> rows = jdbcTemplate.query(sql, ps -> {
            int i = 1;
            for (int bloque = 0; bloque < 3; bloque++) {
                i = setObraFiltro(ps, i, idObra);
                i = setRangoFechas(ps, i, desde, hasta);
            }
        }, (rs, rowNum) -> {
            int totalReg = rs.getInt("total_registros");
            int validas = rs.getInt("asistencias_validas");
            double prodTotal = rs.getDouble("produccion_total");
            long baseDias = totalReg > 0 ? totalReg : (diasRango > 0 ? diasRango : 1L);
            double pctAsist = (totalReg == 0 && validas == 0) ? 0.0
                    : Math.min(100.0, redondear2((validas * 100.0) / baseDias));
            double prodProm = validas > 0 ? redondear2(prodTotal / validas) : 0.0;
            return List.<Object>of(
                    rs.getString("dni"),
                    rs.getString("nombres_apellidos"),
                    rs.getString("cargo"),
                    validas,
                    pctAsist,
                    prodTotal,
                    prodProm
            );
        });
        rows.sort((a, b) -> Double.compare(((Number) b.get(6)).doubleValue(), ((Number) a.get(6)).doubleValue()));
        return new Reporte("Asistencia, cumplimiento y ranking de productividad del personal", cols, rows);
    }

    @Override
    public Reporte consultarFinanciero(Integer idObra, LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT o.nombre AS obra,
                COALESCE((SELECT SUM(i.monto) FROM ingreso i WHERE i.id_obra=o.id_obra AND i.activo=1
                 AND (? IS NULL OR i.fecha >= ?) AND (? IS NULL OR i.fecha <= ?)), 0) AS ingresos,
                COALESCE((SELECT SUM(e.monto) FROM egreso e WHERE e.id_obra=o.id_obra AND e.activo=1
                 AND UPPER(e.tipo_egreso) IN ('MATERIAL','COMPRA')
                 AND (? IS NULL OR e.fecha >= ?) AND (? IS NULL OR e.fecha <= ?)), 0) AS egr_material,
                COALESCE((SELECT SUM(e.monto) FROM egreso e WHERE e.id_obra=o.id_obra AND e.activo=1
                 AND UPPER(e.tipo_egreso) IN ('PLANILLA')
                 AND (? IS NULL OR e.fecha >= ?) AND (? IS NULL OR e.fecha <= ?)), 0) AS egr_planilla,
                COALESCE((SELECT SUM(e.monto) FROM egreso e WHERE e.id_obra=o.id_obra AND e.activo=1
                 AND UPPER(e.tipo_egreso) NOT IN ('MATERIAL','COMPRA','PLANILLA')
                 AND (? IS NULL OR e.fecha >= ?) AND (? IS NULL OR e.fecha <= ?)), 0) AS egr_otros
                FROM obra o WHERE o.activo=1 AND (? = 0 OR o.id_obra = ?) ORDER BY o.nombre
                """;

        List<String> cols = List.of(
                "Obra", "Ingresos (S/)", "Egresos Material (S/)", "Egresos Planilla (S/)",
                "Otros Egresos (S/)", "Total Egresos (S/)", "Utilidad / Saldo (S/)", "Rentabilidad (%)"
        );
        List<List<Object>> rows = jdbcTemplate.query(sql, ps -> {
            int i = 1;
            for (int b = 0; b < 4; b++) {
                i = setRangoFechas(ps, i, desde, hasta);
            }
            setObraFiltro(ps, i, idObra);
        }, (rs, rowNum) -> {
            double ing = rs.getDouble("ingresos");
            double mat = rs.getDouble("egr_material");
            double pla = rs.getDouble("egr_planilla");
            double otr = rs.getDouble("egr_otros");
            double totEgr = redondear2(mat + pla + otr);
            double utilidad = redondear2(ing - totEgr);
            double rentPct = ing > 0 ? redondear2((utilidad * 100.0) / ing) : 0.0;
            return List.of(rs.getString("obra"), ing, mat, pla, otr, totEgr, utilidad, rentPct);
        });
        return new Reporte("Estado financiero, utilidad y rentabilidad por obra", cols, rows);
    }

    @Override
    public Reporte consultarValorizacionesCobranzas(Integer idObra, LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT o.nombre AS obra, v.numero AS num_val, v.periodo, v.porcentaje_avance_periodo,
                       v.monto_valorizado, v.estado AS estado_val,
                       COALESCE(c.serie_numero, 'Sin comprobante') AS comprobante,
                       COALESCE(c.estado, '-') AS estado_comp,
                       COALESCE(SUM(CASE WHEN p.estado='APROBADO' AND p.activo=1 THEN p.monto ELSE 0 END), 0) AS cobrado,
                       MAX(CASE WHEN p.estado='APROBADO' AND p.activo=1 THEN p.fecha_pago ELSE NULL END) AS fecha_pago
                FROM valorizacion v
                JOIN obra o ON o.id_obra = v.id_obra
                LEFT JOIN comprobante c ON c.id_valorizacion = v.id_valorizacion AND c.activo = 1
                LEFT JOIN pago p ON p.id_valorizacion = v.id_valorizacion AND p.activo = 1
                WHERE o.activo = 1 AND v.activo = 1
                  AND (? = 0 OR v.id_obra = ?)
                  AND (? IS NULL OR v.fecha_emision >= ?)
                  AND (? IS NULL OR v.fecha_emision <= ?)
                GROUP BY o.nombre, v.id_valorizacion, v.numero, v.periodo, v.porcentaje_avance_periodo,
                         v.monto_valorizado, v.estado, c.serie_numero, c.estado
                ORDER BY o.nombre, v.numero
                """;

        List<String> cols = List.of(
                "Obra", "Val. N°", "Periodo", "Avance Periodo (%)", "Valorizado (S/)",
                "Comprobante", "Estado", "Cobrado (S/)", "Fecha Cobro", "Saldo Pendiente (S/)"
        );
        List<List<Object>> rows = jdbcTemplate.query(sql, ps -> {
            int i = setObraFiltro(ps, 1, idObra);
            setRangoFechas(ps, i, desde, hasta);
        }, (rs, rowNum) -> {
            double total = redondear2(rs.getDouble("monto_valorizado"));
            double cobrado = redondear2(rs.getDouble("cobrado"));
            double saldo = redondear2(Math.max(0.0, total - cobrado));
            java.sql.Timestamp tsPago = rs.getTimestamp("fecha_pago");
            String fechaCobro = tsPago != null ? tsPago.toLocalDateTime().toLocalDate().toString() : "-";
            return List.<Object>of(
                    rs.getString("obra"),
                    "N° " + rs.getInt("num_val"),
                    rs.getString("periodo"),
                    redondear2(rs.getDouble("porcentaje_avance_periodo")),
                    total,
                    rs.getString("comprobante"),
                    rs.getString("estado_val"),
                    cobrado,
                    fechaCobro,
                    saldo
            );
        });
        return new Reporte("Estado de valorizaciones, comprobantes emitidos y cobranzas por obra", cols, rows);
    }

    private int setObraFiltro(PreparedStatement ps, int idx, Integer idObra) throws SQLException {
        int val = idObra == null ? 0 : idObra;
        ps.setInt(idx++, val);
        ps.setInt(idx++, val);
        return idx;
    }

    private int setRangoFechas(PreparedStatement ps, int idx, LocalDate desde, LocalDate hasta) throws SQLException {
        setFechaNullable(ps, idx++, desde);
        setFechaNullable(ps, idx++, desde);
        setFechaNullable(ps, idx++, hasta);
        setFechaNullable(ps, idx++, hasta);
        return idx;
    }

    private void setFechaNullable(PreparedStatement ps, int idx, LocalDate fecha) throws SQLException {
        if (fecha == null) {
            ps.setNull(idx, Types.DATE);
        } else {
            ps.setDate(idx, Date.valueOf(fecha));
        }
    }

    private double redondear2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private boolean existeTablaPartidaMaterial() {
        Boolean cached = cacheTablaPartidaMaterial;
        if (cached != null) {
            return cached;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'partida_material'",
                Integer.class);
        boolean exists = count != null && count > 0;
        cacheTablaPartidaMaterial = exists;
        return exists;
    }
}
