// SIGOC Web - Finanzas, Reportes, Valorizaciones y Pagos

async function renderFinanzas() {
    const [obras, ingresos, egresos, cierres] = await Promise.all([
        api('/api/obras'), api('/api/finanzas/ingresos'), api('/api/finanzas/egresos'), api('/api/finanzas/planillas')
    ]);
    const obrasOpt = obras.filter(o => o.operable).map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('');

    moduleContainer.innerHTML = `
        <div class="tabs">
            <button class="btn btn-primary btn-sm" data-ftab="ing">Ingresos (RF-13)</button>
            <button class="btn btn-secondary btn-sm" data-ftab="pla">Cierre de Planilla (RF-14)</button>
            <button class="btn btn-secondary btn-sm" data-ftab="egr">Egresos (RF-15)</button>
        </div>

        <div id="ftab-ing">
            <div class="card">
                <h3>Registrar Ingreso de Obra</h3>
                <form id="formIngreso" class="form-grid">
                    <div class="form-group"><label>Obra</label><select id="inObra">${obrasOpt}</select></div>
                    <div class="form-group"><label>Fecha *</label><input id="inFecha" type="date" value="${hoyIso()}" required></div>
                    <div class="form-group"><label>Concepto *</label><input id="inConcepto" maxlength="150" required></div>
                    <div class="form-group"><label>Monto (S/) *</label><input id="inMonto" type="number" step="0.01" min="0.01" required></div>
                    <div class="form-group"><button type="submit" class="btn btn-primary">Registrar Ingreso</button></div>
                </form>
            </div>
            <div class="card">
                <h3>Ingresos Activos</h3>
                <div class="table-wrap"><table>
                    <thead><tr><th>ID</th><th>Obra</th><th>Fecha</th><th>Concepto</th><th>Monto (S/)</th></tr></thead>
                    <tbody>${ingresos.map(i => `<tr><td>${i.idIngreso}</td><td>#${i.idObra}</td><td>${esc(i.fecha)}</td><td>${esc(i.concepto)}</td><td>S/ ${Number(i.monto).toFixed(2)}</td></tr>`).join('')}</tbody>
                </table></div>
            </div>
        </div>

        <div id="ftab-pla" class="hidden">
            <div class="card">
                <h3>Cerrar e Inmovilizar Planilla de Asistencias (RF-14)</h3>
                <form id="formCierre" class="form-grid">
                    <div class="form-group"><label>Obra</label><select id="clObra">${obrasOpt}</select></div>
                    <div class="form-group"><label>Desde *</label><input id="clDesde" type="date" required></div>
                    <div class="form-group"><label>Hasta *</label><input id="clHasta" type="date" value="${hoyIso()}" required></div>
                    <div class="form-group" style="display:flex;gap:8px;align-items:flex-end;">
                        <button type="button" id="btnPreviewPlanilla" class="btn btn-secondary">Previsualizar</button>
                        <button type="submit" class="btn btn-warning">Cerrar Planilla</button>
                    </div>
                </form>
                <div id="planillaPreviewBox" class="hidden" style="margin-top:14px;"></div>
            </div>
            <div class="card">
                <h3>Cierres de Planilla Registrados</h3>
                <div class="table-wrap"><table>
                    <thead><tr><th>ID</th><th>Obra</th><th>Periodo</th><th>Fecha Cierre</th><th>Monto Total (S/)</th></tr></thead>
                    <tbody>${cierres.map(c => `<tr><td>${c.idCierre}</td><td>#${c.idObra}</td><td>${esc(c.fechaInicioPeriodo)} → ${esc(c.fechaFinPeriodo)}</td><td>${esc(c.fechaCierre)}</td><td><strong>S/ ${Number(c.montoTotal).toFixed(2)}</strong></td></tr>`).join('')}</tbody>
                </table></div>
            </div>
        </div>

        <div id="ftab-egr" class="hidden">
            <div class="card">
                <h3>Registrar Egreso Directo ("Otro")</h3>
                <form id="formEgreso" class="form-grid">
                    <div class="form-group"><label>Obra</label><select id="egObra">${obrasOpt}</select></div>
                    <div class="form-group"><label>Fecha *</label><input id="egFecha" type="date" value="${hoyIso()}" required></div>
                    <div class="form-group"><label>Concepto *</label><input id="egConcepto" maxlength="150" required></div>
                    <div class="form-group"><label>Monto (S/) *</label><input id="egMonto" type="number" step="0.01" min="0.01" required></div>
                    <div class="form-group"><button type="submit" class="btn btn-primary">Registrar Egreso</button></div>
                </form>
            </div>
            <div class="card">
                <h3>Historial de Egresos</h3>
                <div class="table-wrap"><table>
                    <thead><tr><th>ID</th><th>Obra</th><th>Fecha</th><th>Tipo</th><th>Concepto</th><th>Monto (S/)</th></tr></thead>
                    <tbody>${egresos.map(e => `<tr><td>${e.idEgreso}</td><td>#${e.idObra}</td><td>${esc(e.fecha)}</td><td><span class="badge badge-info">${esc(e.tipoEgreso)}</span></td><td>${esc(e.concepto)}</td><td>S/ ${Number(e.monto).toFixed(2)}</td></tr>`).join('')}</tbody>
                </table></div>
            </div>
        </div>`;

    document.querySelectorAll('[data-ftab]').forEach(btn => btn.onclick = () => {
        ['ing', 'pla', 'egr'].forEach(k => document.getElementById(`ftab-${k}`).classList.toggle('hidden', k !== btn.dataset.ftab));
        document.querySelectorAll('[data-ftab]').forEach(b => b.className = b === btn ? 'btn btn-primary btn-sm' : 'btn btn-secondary btn-sm');
    });

    document.getElementById('formIngreso').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/finanzas/ingresos', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('inObra').value),
                    fecha: document.getElementById('inFecha').value,
                    concepto: document.getElementById('inConcepto').value.trim(),
                    monto: Number(document.getElementById('inMonto').value)
                }
            });
            mostrarAlerta('Ingreso registrado.');
            await renderFinanzas();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };

    document.getElementById('btnPreviewPlanilla').onclick = async () => {
        const idObra = document.getElementById('clObra').value;
        const desde = document.getElementById('clDesde').value;
        const hasta = document.getElementById('clHasta').value;
        if (!idObra || !desde || !hasta) {
            mostrarAlerta('Selecciona obra, fecha inicio y fecha fin para previsualizar.', 'warning');
            return;
        }
        try {
            const prev = await api(`/api/finanzas/planillas/preview?idObra=${idObra}&desde=${desde}&hasta=${hasta}`);
            const box = document.getElementById('planillaPreviewBox');
            box.classList.remove('hidden');
            box.innerHTML = `
                <div class="alert alert-info">Previsualización de planilla — Total estimado: <strong>S/ ${Number(prev.totalPlanilla).toFixed(2)}</strong></div>
                <div class="table-wrap"><table>
                    <thead><tr><th>Trabajador</th><th>DNI</th><th>Cargo</th><th>Días Válidos</th><th>Jornal (S/)</th><th>Subtotal (S/)</th></tr></thead>
                    <tbody>${prev.trabajadores.map(t => `<tr><td>${esc(t.nombresApellidos)}</td><td>${esc(t.dni)}</td><td>${esc(t.cargo)}</td><td>${t.diasValidos}</td><td>S/ ${Number(t.jornalDiario).toFixed(2)}</td><td><strong>S/ ${Number(t.subtotal).toFixed(2)}</strong></td></tr>`).join('')}</tbody>
                </table></div>`;
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };

    document.getElementById('formCierre').onsubmit = async (e) => {
        e.preventDefault();
        if (!confirm('El cierre de planilla es inmutable y genera los egresos automáticamente. ¿Continuar?')) return;
        try {
            const c = await api('/api/finanzas/planillas/cerrar', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('clObra').value),
                    desde: document.getElementById('clDesde').value,
                    hasta: document.getElementById('clHasta').value
                }
            });
            mostrarAlerta(`Planilla cerrada (#${c.idCierre}) por S/ ${Number(c.montoTotal).toFixed(2)}`);
            await renderFinanzas();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };

    document.getElementById('formEgreso').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/finanzas/egresos', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('egObra').value),
                    fecha: document.getElementById('egFecha').value,
                    concepto: document.getElementById('egConcepto').value.trim(),
                    monto: Number(document.getElementById('egMonto').value)
                }
            });
            mostrarAlerta('Egreso registrado.');
            await renderFinanzas();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

function renderBarrasComparativasSvg(tituloIzq, tituloDer, colorIzq, colorDer, pares, unidad = '') {
    if (!pares || pares.length === 0) {
        return '<p style="color:var(--muted);">Sin registros suficientes para graficar.</p>';
    }
    const items = pares.slice(0, 10);
    const w = 740, padL = 170, padR = 24, padT = 18, rowH = 30;
    const h = padT + items.length * rowH + 20;
    const maxVal = Math.max(1, ...items.flatMap(it => [Math.abs(it.v1), Math.abs(it.v2)]));
    const barMaxW = w - padL - padR;

    const barras = items.map((it, idx) => {
        const y = padT + idx * rowH;
        const w1 = (Math.max(0, it.v1) * barMaxW) / maxVal;
        const w2 = (Math.max(0, it.v2) * barMaxW) / maxVal;
        const label = it.label.length > 22 ? it.label.slice(0, 21) + '…' : it.label;
        return `
            <text x="${padL - 8}" y="${y + 15}" text-anchor="end" font-size="11" fill="#334155">${esc(label)}</text>
            <rect x="${padL}" y="${y + 2}" width="${w1.toFixed(1)}" height="10" rx="2" fill="${colorIzq}">
                <title>${esc(it.label)} — ${esc(tituloIzq)}: ${it.v1}${esc(unidad)}</title>
            </rect>
            <rect x="${padL}" y="${y + 14}" width="${w2.toFixed(1)}" height="10" rx="2" fill="${colorDer}">
                <title>${esc(it.label)} — ${esc(tituloDer)}: ${it.v2}${esc(unidad)}</title>
            </rect>`;
    }).join('');

    return `<div class="chart-box">
        <svg viewBox="0 0 ${w} ${h}" width="100%" style="max-width:760px;display:block;">${barras}</svg>
        <div style="font-size:12px;color:var(--muted);display:flex;gap:16px;margin-top:6px;">
            <span><strong style="color:${colorIzq};">■</strong> ${esc(tituloIzq)}</span>
            <span><strong style="color:${colorDer};">■</strong> ${esc(tituloDer)}</span>
        </div>
    </div>`;
}

function renderReporteSvg(tipo, reporte) {
    const filas = reporte.filas || [];
    if (filas.length === 0) return '<p style="color:var(--muted);">Sin datos para graficar.</p>';
    switch (tipo) {
        case 'CURVA_S':
            return renderCurvaSSvg(reporte);
        case 'CONSUMO_MATERIALES':
            return renderBarrasComparativasSvg(
                'Presupuestado', 'Consumo Real', '#2563eb', '#f59e0b',
                filas.map(r => ({ label: String(r[0]), v1: Number(r[2]) || 0, v2: Number(r[3]) || 0 }))
            );
        case 'PERSONAL_PRODUCTIVIDAD':
            return renderBarrasComparativasSvg(
                'Asistencia (%)', 'Producción Total', '#059669', '#2563eb',
                filas.map(r => ({ label: String(r[1]), v1: Number(r[4]) || 0, v2: Number(r[5]) || 0 }))
            );
        case 'FINANCIERO':
            return renderBarrasComparativasSvg(
                'Ingresos (S/)', 'Total Egresos (S/)', '#059669', '#dc2626',
                filas.map(r => ({ label: String(r[0]), v1: Number(r[1]) || 0, v2: Number(r[5]) || 0 })), ' S/'
            );
        case 'VALORIZACIONES_COBRANZAS':
            return renderBarrasComparativasSvg(
                'Valorizado (S/)', 'Cobrado (S/)', '#2563eb', '#059669',
                filas.map(r => ({ label: `${r[0]} · ${r[1]}`, v1: Number(r[4]) || 0, v2: Number(r[7]) || 0 })), ' S/'
            );
        default:
            return '';
    }
}

function renderCurvaSSvg(reporte) {
    const filas = reporte.filas || [];
    if (filas.length === 0) return '<p style="color:var(--muted);">Sin datos para graficar Curva S.</p>';
    const w = 740, h = 270, padL = 46, padR = 24, padT = 20, padB = 48;
    const n = filas.length;
    const xAt = i => n === 1 ? (padL + w - padR) / 2 : padL + (i * (w - padL - padR)) / (n - 1);
    const yAt = v => h - padB - (Math.max(0, Math.min(100, Number(v) || 0)) * (h - padT - padB)) / 100;

    const ptsPlan = filas.map((r, i) => `${xAt(i)},${yAt(r[2])}`).join(' ');
    const ptsEjec = filas.map((r, i) => `${xAt(i)},${yAt(r[3])}`).join(' ');
    const grid = [0, 25, 50, 75, 100].map(v => {
        const y = yAt(v);
        return `<line x1="${padL}" y1="${y}" x2="${w - padR}" y2="${y}" stroke="#e2e8f0" stroke-width="1"/>
                <text x="${padL - 8}" y="${y + 4}" text-anchor="end" font-size="10" fill="#64748b">${v}%</text>`;
    }).join('');
    const pasoEtiqueta = Math.max(1, Math.ceil(n / 7));
    const xLabels = filas.map((r, i) => (i % pasoEtiqueta === 0 || i === n - 1)
        ? `<text x="${xAt(i)}" y="${h - 14}" text-anchor="middle" font-size="10" fill="#64748b">${esc(String(r[1] || r[0]).slice(0, 12))}</text>`
        : '').join('');
    const dots = filas.map((r, i) => `
        <circle cx="${xAt(i)}" cy="${yAt(r[2])}" r="3.5" fill="#0066cc"><title>${esc(r[0])} (${esc(r[1])}) · Plan: ${r[2]}%</title></circle>
        <circle cx="${xAt(i)}" cy="${yAt(r[3])}" r="3.5" fill="#059669"><title>${esc(r[0])} (${esc(r[1])}) · Ejec: ${r[3]}%</title></circle>`).join('');

    return `<div class="chart-box">
        <svg viewBox="0 0 ${w} ${h}" width="100%" style="max-width:760px;display:block;">
            ${grid}
            ${xLabels}
            <polyline fill="none" stroke="#0066cc" stroke-width="2.5" points="${ptsPlan}"/>
            <polyline fill="none" stroke="#059669" stroke-width="2.5" points="${ptsEjec}"/>
            ${dots}
        </svg>
        <div style="font-size:12px;color:var(--muted);display:flex;gap:16px;">
            <span><strong style="color:#0066cc;">—</strong> Planificado (%)</span>
            <span><strong style="color:#059669;">—</strong> Ejecutado (%)</span>
        </div>
    </div>`;
}

async function renderReportes() {
    const obras = await api('/api/obras');
    moduleContainer.innerHTML = `
        <div class="card">
            <h3>Reportes Gerenciales (RF-16 / RF-22)</h3>
            <form id="formRep" class="form-grid">
                <div class="form-group"><label>Tipo de Reporte</label>
                    <select id="rpTipo">
                        <option value="CURVA_S">Curva S — Avance Planificado vs Ejecutado</option>
                        <option value="CONSUMO_MATERIALES">Consumo de Materiales Real vs Presupuestado</option>
                        <option value="PERSONAL_PRODUCTIVIDAD">Personal — Asistencia y Productividad</option>
                        <option value="FINANCIERO">Estado Financiero y Rentabilidad</option>
                        <option value="VALORIZACIONES_COBRANZAS">Valorizaciones, Comprobantes y Cobranzas (RF-22)</option>
                    </select>
                </div>
                <div class="form-group"><label>Obra (opcional)</label>
                    <select id="rpObra"><option value="">Todas las obras</option>${obras.map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Desde</label><input id="rpDesde" type="date"></div>
                <div class="form-group"><label>Hasta</label><input id="rpHasta" type="date"></div>
                <div class="form-group" style="display:flex;gap:6px;flex-wrap:wrap;">
                    <button type="submit" class="btn btn-primary">Generar</button>
                    <button type="button" class="btn btn-secondary" data-exp="PDF">PDF</button>
                    <button type="button" class="btn btn-success" data-exp="XLSX">Excel</button>
                    <button type="button" class="btn btn-secondary" data-exp="CSV">CSV</button>
                </div>
            </form>
        </div>
        <div id="rpChartCard" class="card">
            <h3 id="rpChartTitulo">Gráfico del Reporte</h3>
            <div id="rpChart"></div>
        </div>
        <div class="card">
            <h3 id="rpTitulo">Resultado del Reporte</h3>
            <div class="table-wrap"><table id="rpTabla"></table></div>
        </div>`;

    function queryParams() {
        const p = new URLSearchParams({ tipo: document.getElementById('rpTipo').value });
        const ob = document.getElementById('rpObra').value;
        const d = document.getElementById('rpDesde').value;
        const h = document.getElementById('rpHasta').value;
        if (ob) p.set('idObra', ob);
        if (d) p.set('desde', d);
        if (h) p.set('hasta', h);
        return p;
    }

    async function cargarReporte() {
        const p = queryParams();
        const tipo = document.getElementById('rpTipo').value;
        const rep = await api(`/api/reportes?${p.toString()}`);
        document.getElementById('rpTitulo').textContent = rep.titulo;
        document.getElementById('rpChartTitulo').textContent = `Visualización — ${rep.titulo}`;
        document.getElementById('rpChart').innerHTML = renderReporteSvg(tipo, rep);
        const thead = `<thead><tr>${rep.columnas.map(c => `<th>${esc(c)}</th>`).join('')}</tr></thead>`;
        const tbody = `<tbody>${rep.filas.map(f => `<tr>${f.map(c => `<td>${esc(c)}</td>`).join('')}</tr>`).join('')}</tbody>`;
        document.getElementById('rpTabla').innerHTML = thead + tbody;
    }

    document.getElementById('formRep').onsubmit = async (e) => {
        e.preventDefault();
        try { await cargarReporte(); } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
    document.querySelectorAll('[data-exp]').forEach(b => b.onclick = () => {
        const p = queryParams();
        p.set('formato', b.dataset.exp);
        window.location.href = `/api/reportes/exportar?${p.toString()}`;
    });
    await cargarReporte();
}


async function renderValorizaciones() {
    const [obras, valorizaciones, comprobantes] = await Promise.all([
        api('/api/obras'), api('/api/valorizaciones'), api('/api/valorizaciones/comprobantes')
    ]);
    const esAdmin = sesion.rol === 'CONTADOR';
    const puedeValorizar = esAdmin || sesion.rol === 'RESIDENTE';
    const pendientes = valorizaciones.filter(v => v.estado === 'PENDIENTE');
    const inicioMes = hoyIso().slice(0, 7) + '-01';

    moduleContainer.innerHTML = `
        ${puedeValorizar ? `
        <div class="card">
            <h3>Registrar Valorización Automática de Obra (RN-20..RN-22)</h3>
            <form id="formVal" class="form-grid">
                <div class="form-group"><label>Obra *</label>
                    <select id="valObra">${obras.filter(o => o.operable).map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Periodo Inicio *</label><input id="valIni" type="date" value="${inicioMes}" required></div>
                <div class="form-group"><label>Periodo Fin *</label><input id="valFin" type="date" value="${hoyIso()}" required></div>
                <div class="form-group"><label>Fecha Emisión</label><input id="valFecha" type="date" value="${hoyIso()}" required></div>
                <div class="form-group" style="display:flex; gap:8px; align-items:flex-end;">
                    <button type="button" id="btnValPreview" class="btn btn-secondary">Previsualizar Cálculo</button>
                    <button type="submit" class="btn btn-primary">Registrar Valorización</button>
                </div>
            </form>
            <div id="valPreviewBox" class="hidden" style="margin-top:12px; padding:12px; background:#f8fafc; border:1px solid #cbd5e1; border-radius:8px;"></div>
        </div>` : ''}
        ${esAdmin ? `
        <div class="card">
            <h3>Emitir Comprobante Interno de Valorización (CV01 · IGV 18% · Enlace 72h)</h3>
            <form id="formComp" class="form-grid">
                <div class="form-group"><label>Valorización Pendiente</label>
                    <select id="cmpVal">${pendientes.map(v => `<option value="${v.idValorizacion}">N° ${v.numero || 1} · Obra #${v.idObra} · ${esc(v.periodo)} (S/ ${Number(v.montoValorizado).toFixed(2)})</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Serie - Correlativo (Opcional)</label><input id="cmpSerie" placeholder="Autogenerado CV01-XXXXXX si vacío"></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Emitir Comprobante CV01</button></div>
            </form>
        </div>` : ''}
        <div class="card">
            <h3>Valorizaciones Registradas</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>N°</th><th>Obra</th><th>Periodo</th><th>Emisión</th><th>% Periodo (Acum.)</th><th>Subtotal</th><th>IGV</th><th>Total (S/)</th><th>Estado</th></tr></thead>
                <tbody>${valorizaciones.map(v => `<tr>
                    <td>#${v.numero || v.idValorizacion}</td><td>#${v.idObra}</td><td>${esc(v.periodo)}</td><td>${esc(v.fechaEmision)}</td>
                    <td>${Number(v.porcentajeAvancePeriodo).toFixed(2)}% (${Number(v.avanceAcumulado || 0).toFixed(2)}%)</td>
                    <td>S/ ${Number(v.subtotal || 0).toFixed(2)}</td><td>S/ ${Number(v.igv || 0).toFixed(2)}</td>
                    <td><strong>S/ ${Number(v.montoValorizado).toFixed(2)}</strong></td>
                    <td><span class="badge ${v.estado === 'PAGADA' ? 'badge-ok' : v.estado === 'FACTURADA' ? 'badge-info' : 'badge-warn'}">${esc(v.estado)}</span></td>
                </tr>`).join('')}</tbody>
            </table></div>
        </div>
        <div class="card">
            <h3>Comprobantes Internos CV01 y Enlaces de Pago (72h)</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>Serie-Número</th><th>Valorización</th><th>Emisión</th><th>Subtotal</th><th>IGV</th><th>Total</th><th>Vigencia Enlace</th><th>Estado</th><th>Acciones</th></tr></thead>
                <tbody>${comprobantes.map(c => `<tr>
                    <td><strong>${esc(c.serieNumero)}</strong></td><td>#${c.idValorizacion}</td><td>${esc(c.fechaEmision)}</td>
                    <td>S/ ${Number(c.subtotal).toFixed(2)}</td><td>S/ ${Number(c.igv).toFixed(2)}</td><td>S/ ${Number(c.total).toFixed(2)}</td>
                    <td>${c.venceEl ? esc(String(c.venceEl).replace('T', ' ').slice(0, 16)) : '-'}</td>
                    <td><span class="badge ${c.estado === 'PAGADO' ? 'badge-ok' : 'badge-info'}">${esc(c.estado)}</span></td>
                    <td style="white-space:nowrap;">
                        <a class="btn btn-secondary btn-sm" href="/api/valorizaciones/comprobantes/${c.idComprobante}/pdf" target="_blank">PDF</a>
                        ${c.codigoPago ? `<button type="button" class="btn btn-secondary btn-sm" data-copylink="${esc(c.codigoPago)}">Copiar Link</button>` : ''}
                        ${esAdmin && c.estado === 'EMITIDO' ? `<button type="button" class="btn btn-primary btn-sm" data-renlink="${c.idComprobante}">Renovar 72h</button>` : ''}
                    </td>
                </tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    document.querySelectorAll('[data-copylink]').forEach(b => b.onclick = async () => {
        const url = `${window.location.origin}/pago/${b.dataset.copylink}`;
        try {
            await navigator.clipboard.writeText(url);
            mostrarAlerta(`Enlace público copiado: ${url}`);
        } catch {
            window.prompt('Copia el enlace público de pago (72h):', url);
        }
    });

    if (esAdmin) {
        document.querySelectorAll('[data-renlink]').forEach(b => b.onclick = async () => {
            try {
                await api(`/api/valorizaciones/comprobantes/${b.dataset.renlink}/renovar-enlace`, { method: 'POST' });
                mostrarAlerta('Enlace de pago renovado por 72 horas adicionales.');
                await renderValorizaciones();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        });

        document.getElementById('formComp').onsubmit = async (e) => {
            e.preventDefault();
            const idVal = document.getElementById('cmpVal').value;
            if (!idVal) { mostrarAlerta('No hay valorizaciones pendientes por facturar.', 'warning'); return; }
            try {
                const c = await api(`/api/valorizaciones/${idVal}/comprobante`, {
                    method: 'POST',
                    body: {
                        tipoComprobante: 'COMPROBANTE_VALORIZACION',
                        serieNumero: document.getElementById('cmpSerie').value.trim() || null
                    }
                });
                mostrarAlerta(`Comprobante ${c.serieNumero} emitido (Total S/ ${Number(c.total).toFixed(2)}).`);
                await renderValorizaciones();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }

    if (!puedeValorizar) return;

    document.getElementById('btnValPreview').onclick = async () => {
        const box = document.getElementById('valPreviewBox');
        try {
            const p = await api(`/api/valorizaciones/preview?idObra=${document.getElementById('valObra').value}&periodoInicio=${document.getElementById('valIni').value}&periodoFin=${document.getElementById('valFin').value}`);
            box.innerHTML = `
                <strong>Vista previa — Valorización N° ${p.numeroSiguiente} (${esc(p.nombreObra)})</strong><br>
                Avance físico acumulado a la fecha fin: <strong>${Number(p.avanceAcumulado).toFixed(2)}%</strong> ·
                Ya valorizado: <strong>${Number(p.avanceYaValorizado).toFixed(2)}%</strong> ·
                <strong>Avance del periodo: ${Number(p.porcentajeAvancePeriodo).toFixed(2)}%</strong><br>
                Subtotal: S/ ${Number(p.subtotal).toFixed(2)} · IGV (18%): S/ ${Number(p.igv).toFixed(2)} ·
                <strong>Total: S/ ${Number(p.total).toFixed(2)}</strong>`;
            box.classList.remove('hidden');
        } catch (err) {
            box.classList.add('hidden');
            mostrarAlerta(err.message, 'error');
        }
    };

    document.getElementById('formVal').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/valorizaciones', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('valObra').value),
                    periodoInicio: document.getElementById('valIni').value,
                    periodoFin: document.getElementById('valFin').value,
                    fechaEmision: document.getElementById('valFecha').value
                }
            });
            mostrarAlerta('Valorización automática registrada.');
            await renderValorizaciones();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderPagos() {
    const [obras, valorizaciones, comprobantes, pagos, tipoCambio] = await Promise.all([
        api('/api/obras'), api('/api/valorizaciones'), api('/api/valorizaciones/comprobantes'),
        api('/api/pagos'), api('/api/tipo-cambio')
    ]);
    const valsNoPagadas = valorizaciones.filter(v => v.estado !== 'PAGADA');
    const compsEmitidos = comprobantes.filter(c => c.estado === 'EMITIDO');
    const esFinanzas = sesion.rol === 'CONTADOR';

    moduleContainer.innerHTML = `
        <div class="card">
            <h3>Tipo de Cambio Referencial USD → PEN (PayPal Sandbox)</h3>
            <form id="formTc" class="form-grid">
                <div class="form-group"><label for="tcPar">Par de Moneda</label><input id="tcPar" value="USD → PEN" disabled></div>
                <div class="form-group"><label for="tcValor">Valor (S/ por 1 USD) *</label>
                    <input id="tcValor" type="number" step="0.0001" min="0.1" required>
                </div>
                <div class="form-group"><label for="tcFecha">Última actualización</label>
                    <input id="tcFecha" disabled>
                </div>
                <div id="tcBotonContenedor" class="form-group"></div>
            </form>
        </div>
        <div class="card">
            <h3>Registro / Conciliación Interna de Pagos (Genera Ingreso Automático)</h3>
            <form id="formPago" class="form-grid">
                <div class="form-group"><label for="pgObra">Obra Destino *</label>
                    <select id="pgObra"></select>
                </div>
                <div class="form-group"><label for="pgVal">Valorización (Opcional)</label>
                    <select id="pgVal"><option value="">-- Ninguna --</option></select>
                </div>
                <div class="form-group"><label for="pgComp">Comprobante CV01 (Opcional)</label>
                    <select id="pgComp"><option value="">-- Ninguno --</option></select>
                </div>
                <div class="form-group"><label for="pgMonto">Monto (S/) *</label><input id="pgMonto" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><label for="pgMetodo">Método</label>
                    <select id="pgMetodo">
                        <option value="TARJETA_SIMULADA">Tarjeta Simulada</option>
                        <option value="TRANSFERENCIA_SIMULADA">Transferencia Simulada</option>
                        <option value="PAYPAL_SANDBOX">PayPal Sandbox</option>
                    </select>
                </div>
                <div class="form-group"><label for="pgCodigo">Cód. Operación (Opcional)</label><input id="pgCodigo" placeholder="Autogenerado si vacío"></div>
                <div class="form-group"><button type="submit" class="btn btn-success">Aprobar y Registrar Pago</button></div>
            </form>
        </div>
        <div class="card">
            <h3>Historial de Pagos y Auditoría de Pasarela</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Obra</th><th>Comprob.</th><th>Ingreso</th><th>Pasarela / Método</th><th>Cód. Operación</th><th>Monto (S/)</th><th>Cobro</th><th>Tarjeta</th><th>Estado</th></tr></thead>
                <tbody id="tablaPagosBody"></tbody>
            </table></div>
        </div>`;

    const tcValorInput = document.getElementById('tcValor');
    tcValorInput.value = Number(tipoCambio.valor || 3.75).toFixed(4);
    if (!esFinanzas) tcValorInput.disabled = true;

    const tcFechaInput = document.getElementById('tcFecha');
    tcFechaInput.value = tipoCambio.fechaActualizacion
        ? String(tipoCambio.fechaActualizacion).replace('T', ' ').slice(0, 16)
        : '-';

    if (esFinanzas) {
        const btnTc = document.createElement('button');
        btnTc.type = 'submit';
        btnTc.className = 'btn btn-secondary';
        btnTc.textContent = 'Actualizar Tipo de Cambio';
        document.getElementById('tcBotonContenedor').appendChild(btnTc);
    }

    const selObra = document.getElementById('pgObra');
    obras.filter(o => o.operable).forEach(o => {
        const opt = document.createElement('option');
        opt.value = o.idObra;
        opt.textContent = o.nombre;
        selObra.appendChild(opt);
    });

    const selVal = document.getElementById('pgVal');
    valsNoPagadas.forEach(v => {
        const opt = document.createElement('option');
        opt.value = v.idValorizacion;
        opt.textContent = `N° ${v.numero || v.idValorizacion} · Obra #${v.idObra} · ${v.periodo} (S/ ${Number(v.montoValorizado).toFixed(2)})`;
        selVal.appendChild(opt);
    });

    const selComp = document.getElementById('pgComp');
    compsEmitidos.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.idComprobante;
        opt.textContent = `#${c.idComprobante} · ${c.serieNumero} (S/ ${Number(c.total).toFixed(2)})`;
        selComp.appendChild(opt);
    });

    const tbody = document.getElementById('tablaPagosBody');
    pagos.forEach(p => {
        const tr = document.createElement('tr');

        const tdId = document.createElement('td');
        tdId.textContent = String(p.idPago);

        const tdObra = document.createElement('td');
        tdObra.textContent = `#${p.idObra}`;

        const tdComp = document.createElement('td');
        tdComp.textContent = p.idComprobante ? `#${p.idComprobante}` : '-';

        const tdIng = document.createElement('td');
        tdIng.textContent = p.idIngresoGenerado ? `#${p.idIngresoGenerado}` : '-';

        const tdMetodo = document.createElement('td');
        tdMetodo.textContent = `${p.pasarela || '-'} / ${p.metodoPago || ''}`;

        const tdCod = document.createElement('td');
        const strongCod = document.createElement('strong');
        strongCod.textContent = String(p.codigoOperacion || '');
        tdCod.appendChild(strongCod);

        const tdMonto = document.createElement('td');
        tdMonto.textContent = `S/ ${Number(p.monto).toFixed(2)}`;

        const tdCobro = document.createElement('td');
        tdCobro.textContent = p.montoCobro ? `${Number(p.montoCobro).toFixed(2)} ${p.monedaCobro || 'PEN'}` : '-';

        const tdTarj = document.createElement('td');
        tdTarj.textContent = p.ultimos4 ? `${p.marcaTarjeta || ''} *${p.ultimos4}` : '-';

        const tdEstado = document.createElement('td');
        if (p.motivo) tdEstado.title = p.motivo;
        const spEstado = document.createElement('span');
        spEstado.className = `badge ${p.estado === 'APROBADO' ? 'badge-ok' : 'badge-danger'}`;
        spEstado.textContent = String(p.estado || '');
        tdEstado.appendChild(spEstado);

        tr.append(tdId, tdObra, tdComp, tdIng, tdMetodo, tdCod, tdMonto, tdCobro, tdTarj, tdEstado);
        tbody.appendChild(tr);
    });

    if (esFinanzas) {
        document.getElementById('formTc').onsubmit = async (e) => {
            e.preventDefault();
            try {
                await api('/api/tipo-cambio', {
                    method: 'PUT',
                    body: { valor: Number(document.getElementById('tcValor').value) }
                });
                mostrarAlerta('Tipo de cambio USD/PEN actualizado.');
                await renderPagos();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }

    document.getElementById('pgVal').onchange = (e) => {
        const v = valsNoPagadas.find(x => x.idValorizacion == e.target.value);
        if (v) {
            document.getElementById('pgObra').value = v.idObra;
            document.getElementById('pgMonto').value = v.montoValorizado;
        }
    };

    document.getElementById('pgComp').onchange = (e) => {
        const c = compsEmitidos.find(x => x.idComprobante == e.target.value);
        if (c) {
            const v = valorizaciones.find(x => x.idValorizacion == c.idValorizacion);
            if (v) {
                document.getElementById('pgObra').value = v.idObra;
                document.getElementById('pgVal').value = v.idValorizacion;
            }
            document.getElementById('pgMonto').value = c.total;
        }
    };

    document.getElementById('formPago').onsubmit = async (e) => {
        e.preventDefault();
        const idVal = document.getElementById('pgVal').value;
        const idComp = document.getElementById('pgComp').value;
        try {
            const p = await api('/api/pagos', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('pgObra').value),
                    idValorizacion: idVal ? Number(idVal) : null,
                    idComprobante: idComp ? Number(idComp) : null,
                    monto: Number(document.getElementById('pgMonto').value),
                    metodoPago: document.getElementById('pgMetodo').value,
                    codigoOperacion: document.getElementById('pgCodigo').value.trim() || null
                }
            });
            mostrarAlerta(`Pago aprobado (${p.codigoOperacion}). Ingreso automático #${p.idIngresoGenerado} creado.`);
            await renderPagos();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

// Cambio de contraseña y cierre de sesión
