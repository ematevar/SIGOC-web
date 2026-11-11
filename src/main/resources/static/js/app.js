const sesion = {
    username: document.body.dataset.username || '',
    nombre: document.body.dataset.nombre || '',
    rol: document.body.dataset.rol || '',
    rolEtiqueta: document.body.dataset.rolEtiqueta || '',
    requiereCambioClave: document.body.dataset.cambioClave === 'true',
    csrfToken: document.body.dataset.csrf || ''
};

const MODULOS = [
    { id: 'INICIO', label: 'Panel Principal', roles: ['ADMIN', 'RESIDENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'OBRAS', label: 'Obras', roles: ['ADMIN', 'CONTADOR'] },
    { id: 'PARTIDAS', label: 'Partidas y Presupuesto', roles: ['ADMIN', 'RESIDENTE', 'CONTADOR'] },
    { id: 'AVANCES', label: 'Avances Físicos', roles: ['RESIDENTE', 'ADMIN', 'CONTADOR'] },
    { id: 'MATERIALES', label: 'Materiales y Stock', roles: ['ALMACENERO', 'ADMIN', 'CONTADOR'] },
    { id: 'PROVEEDORES', label: 'Proveedores', roles: ['ADMIN', 'CONTADOR'] },
    { id: 'COMPRAS', label: 'Compras de Materiales', roles: ['ALMACENERO', 'ADMIN', 'CONTADOR'] },
    { id: 'CONSUMOS', label: 'Consumo de Materiales', roles: ['RESIDENTE', 'ADMIN', 'CONTADOR'] },
    { id: 'PERSONAL', label: 'Personal', roles: ['ADMIN', 'RESIDENTE', 'CONTADOR'] },
    { id: 'ASISTENCIA', label: 'Asistencia', roles: ['RESIDENTE', 'ADMIN', 'CONTADOR'] },
    { id: 'PRODUCCION', label: 'Producción Diaria', roles: ['RESIDENTE', 'ADMIN', 'CONTADOR'] },
    { id: 'FINANZAS', label: 'Finanzas y Planillas', roles: ['ADMIN', 'CONTADOR'] },
    { id: 'CLIENTES', label: 'Clientes', roles: ['ADMIN', 'RESIDENTE', 'CONTADOR'] },
    { id: 'VALORIZACIONES', label: 'Valorizaciones y Facturas', roles: ['ADMIN', 'RESIDENTE', 'CONTADOR'] },
    { id: 'PAGOS', label: 'Pasarela de Pagos', roles: ['ADMIN', 'CONTADOR'] },
    { id: 'REPORTES', label: 'Reportes Gerenciales', roles: ['ADMIN', 'CONTADOR'] },
    { id: 'USUARIOS', label: 'Usuarios y Roles', roles: ['ADMIN'] }
];

const sidebarNav = document.getElementById('sidebarNav');
const moduleContainer = document.getElementById('moduleContainer');
const viewTitle = document.getElementById('viewTitle');
const globalAlert = document.getElementById('globalAlert');
const stockAlertBanner = document.getElementById('stockAlertBanner');
const modalClave = document.getElementById('modalClave');
const btnCancelarClave = document.getElementById('btnCancelarClave');
const hoyIso = () => new Date().toISOString().slice(0, 10);

let moduloActual = 'INICIO';

function esc(v) {
    if (v === null || v === undefined) return '';
    return String(v)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function mostrarAlerta(msg, tipo = 'success') {
    globalAlert.className = `alert ${tipo === 'error' ? 'alert-error' : tipo === 'warning' ? 'alert-warning' : 'alert-success'}`;
    globalAlert.textContent = msg;
    globalAlert.classList.remove('hidden');
}

function ocultarAlerta() {
    globalAlert.classList.add('hidden');
}

async function api(url, options = {}) {
    const opts = { credentials: 'same-origin', ...options };
    const headers = { ...(opts.headers || {}) };
    const method = (opts.method || 'GET').toUpperCase();
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method) && sesion.csrfToken) {
        headers['X-CSRF-TOKEN'] = sesion.csrfToken;
    }
    if (opts.body && typeof opts.body !== 'string') {
        headers['Content-Type'] = 'application/json';
        opts.body = JSON.stringify(opts.body);
    }
    opts.headers = headers;
    const res = await fetch(url, opts);
    if (res.status === 401) {
        window.location.href = '/login';
        throw new Error('Sesión expirada.');
    }
    if (res.status === 204) return null;
    const text = await res.text();
    let json = null;
    try { json = text ? JSON.parse(text) : null; } catch { /* ignore */ }
    if (!res.ok) {
        const msg = (json && (json.mensaje || json.message)) || `Error HTTP ${res.status}`;
        throw new Error(msg);
    }
    return json;
}

function renderSidebar() {
    sidebarNav.innerHTML = '';
    MODULOS.filter(m => m.roles.includes(sesion.rol)).forEach(m => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `nav-btn ${m.id === moduloActual ? 'active' : ''}`;
        btn.textContent = m.label;
        btn.addEventListener('click', () => seleccionarModulo(m.id));
        sidebarNav.appendChild(btn);
    });
}

async function cargarAlertasStock() {
    if (!['ADMIN', 'ALMACENERO'].includes(sesion.rol) || sesion.requiereCambioClave) {
        stockAlertBanner.classList.add('hidden');
        return [];
    }
    try {
        const alertas = await api('/api/materiales/alertas');
        if (alertas && alertas.length > 0) {
            const lista = alertas.map(a => `${a.nombre} (${a.stockActual} / min ${a.stockMinimo} ${a.unidadMedida})`).join(' · ');
            stockAlertBanner.innerHTML = `<strong>Alerta de Stock Mínimo (RF-06):</strong> ${esc(lista)}`;
            stockAlertBanner.classList.remove('hidden');
        } else {
            stockAlertBanner.classList.add('hidden');
        }
        return alertas || [];
    } catch {
        return [];
    }
}

async function seleccionarModulo(id) {
    moduloActual = id;
    ocultarAlerta();
    renderSidebar();
    const def = MODULOS.find(m => m.id === id);
    viewTitle.textContent = def ? def.label : 'SIGOC Web';
    moduleContainer.innerHTML = '<div class="card">Cargando...</div>';
    if (sesion.requiereCambioClave) {
        moduleContainer.innerHTML = '<div class="card">Debes cambiar la contraseña inicial antes de continuar.</div>';
        return;
    }
    try {
        switch (id) {
            case 'INICIO': await renderInicio(); break;
            case 'OBRAS': await renderObras(); break;
            case 'PARTIDAS': await renderPartidas(); break;
            case 'AVANCES': await renderAvances(); break;
            case 'MATERIALES': await renderMateriales(); break;
            case 'PROVEEDORES': await renderProveedores(); break;
            case 'COMPRAS': await renderCompras(); break;
            case 'CONSUMOS': await renderConsumos(); break;
            case 'PERSONAL': await renderPersonal(); break;
            case 'ASISTENCIA': await renderAsistencia(); break;
            case 'PRODUCCION': await renderProduccion(); break;
            case 'FINANZAS': await renderFinanzas(); break;
            case 'CLIENTES': await renderClientes(); break;
            case 'VALORIZACIONES': await renderValorizaciones(); break;
            case 'PAGOS': await renderPagos(); break;
            case 'REPORTES': await renderReportes(); break;
            case 'USUARIOS': await renderUsuarios(); break;
            default: moduleContainer.innerHTML = '<div class="card">Módulo no disponible.</div>';
        }
    } catch (e) {
        mostrarAlerta(e.message, 'error');
        moduleContainer.innerHTML = `<div class="card">${esc(e.message)}</div>`;
    }
}

async function renderInicio() {
    const obras = await api('/api/obras').catch(() => []);
    const alertas = await cargarAlertasStock();
    const permitidos = MODULOS.filter(m => m.id !== 'INICIO' && m.roles.includes(sesion.rol));
    const tarjetas = permitidos.map(m =>
        `<div class="kpi-card" style="cursor:pointer;" onclick="seleccionarModulo('${m.id}')">
            <div class="kpi-label">Módulo</div>
            <div style="font-size:17px;font-weight:700;color:var(--navy);margin-top:6px;">${esc(m.label)}</div>
            <div style="font-size:12px;color:var(--blue);margin-top:8px;">Abrir módulo →</div>
         </div>`
    ).join('');
    moduleContainer.innerHTML = `
        <div class="kpi-grid">
            <div class="kpi-card"><div class="kpi-label">Obras activas</div><div class="kpi-value">${obras.length}</div></div>
            <div class="kpi-card"><div class="kpi-label">Perfil activo</div><div class="kpi-value" style="font-size:20px;">${esc(sesion.rolEtiqueta)}</div></div>
            <div class="kpi-card"><div class="kpi-label">Alertas stock</div><div class="kpi-value">${alertas.length}</div></div>
        </div>
        <div class="card">
            <h3>Accesos directos</h3>
            <div class="kpi-grid">${tarjetas}</div>
        </div>`;
}

async function renderObras() {
    const [obras, clientes] = await Promise.all([
        api('/api/obras'),
        api('/api/clientes').catch(() => [])
    ]);
    const puedeEscribir = sesion.rol === 'ADMIN';
    let editId = null;
    moduleContainer.innerHTML = `
        ${puedeEscribir ? `
        <div class="card">
            <h3 id="obraFormTitulo">Registrar Obra</h3>
            <form id="formObra" class="form-grid">
                <div class="form-group"><label>Nombre *</label><input id="obNombre" required></div>
                <div class="form-group"><label>Cliente Registrado</label>
                    <select id="obIdCliente">
                        <option value="">-- Otro / ingreso manual --</option>
                        ${clientes.map(c => `<option value="${c.idCliente}">${esc(c.nombreRazonSocial)} (${esc(c.numeroDocumento)})</option>`).join('')}
                    </select>
                </div>
                <div class="form-group"><label>Razón Social Cliente *</label><input id="obCliente" required></div>
                <div class="form-group"><label>Ubicación</label><input id="obUbicacion"></div>
                <div class="form-group"><label>Presupuesto (S/) *</label><input id="obPresup" type="number" step="0.01" min="1" required></div>
                <div class="form-group"><label>Inicio Plan *</label><input id="obIni" type="date" required></div>
                <div class="form-group"><label>Fin Plan *</label><input id="obFin" type="date" required></div>
                <div class="form-group"><label>Estado</label>
                    <select id="obEstado">
                        <option value="Planificada">Planificada</option>
                        <option value="En Ejecución" selected>En Ejecución</option>
                        <option value="Suspendida">Suspendida</option>
                    </select>
                </div>
                <div class="form-group" style="display:flex;gap:8px;">
                    <button type="submit" class="btn btn-primary">Guardar</button>
                    <button type="button" id="btnObLimpiar" class="btn btn-secondary">Limpiar</button>
                </div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de consulta (solo el Administrador puede crear, editar o cerrar obras).</div>'}
        <div class="card">
            <h3>Listado de Obras</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Cliente</th><th>Presupuesto</th><th>Planificación</th><th>Estado</th>${puedeEscribir ? '<th>Acciones</th>' : ''}</tr></thead>
                <tbody id="tbObras"></tbody>
            </table></div>
        </div>`;

    const tb = document.getElementById('tbObras');
    tb.innerHTML = obras.map(o => `
        <tr>
            <td>${o.idObra}</td>
            <td>${esc(o.nombre)}</td>
            <td>${esc(o.cliente)}</td>
            <td>S/ ${Number(o.presupuestoTotal).toFixed(2)}</td>
            <td>${esc(o.fechaInicioPlan)} → ${esc(o.fechaFinPlan)}</td>
            <td><span class="badge ${o.operable ? 'badge-ok' : 'badge-info'}">${esc(o.estado)}</span></td>
            ${puedeEscribir ? `<td>
                ${o.operable ? `<button class="btn btn-secondary btn-sm" data-edit="${o.idObra}">Editar</button>` : ''}
                ${o.operable ? `<button class="btn btn-warning btn-sm" data-close="${o.idObra}">Cerrar</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del="${o.idObra}">Eliminar</button>
            </td>` : ''}
        </tr>`).join('');

    if (!puedeEscribir) return;

    const obIdCliente = document.getElementById('obIdCliente');
    const obCliente = document.getElementById('obCliente');
    obIdCliente.onchange = () => {
        const cli = clientes.find(c => String(c.idCliente) === obIdCliente.value);
        if (cli) {
            obCliente.value = cli.nombreRazonSocial;
            obCliente.readOnly = true;
        } else {
            obCliente.readOnly = false;
        }
    };

    document.getElementById('btnObLimpiar').onclick = () => {
        editId = null;
        document.getElementById('obraFormTitulo').textContent = 'Registrar Obra';
        document.getElementById('formObra').reset();
        obCliente.readOnly = false;
    };

    tb.querySelectorAll('[data-edit]').forEach(b => b.onclick = () => {
        const o = obras.find(x => x.idObra == b.dataset.edit);
        if (!o) return;
        editId = o.idObra;
        document.getElementById('obraFormTitulo').textContent = `Editar Obra #${editId}`;
        document.getElementById('obNombre').value = o.nombre;
        obIdCliente.value = o.idCliente ? String(o.idCliente) : '';
        obCliente.value = o.cliente;
        obCliente.readOnly = Boolean(o.idCliente);
        document.getElementById('obUbicacion').value = o.ubicacion || '';
        document.getElementById('obPresup').value = o.presupuestoTotal;
        document.getElementById('obIni').value = o.fechaInicioPlan;
        document.getElementById('obFin').value = o.fechaFinPlan;
        document.getElementById('obEstado').value = o.estado;
    });

    tb.querySelectorAll('[data-close]').forEach(b => b.onclick = async () => {
        if (!confirm('¿Cerrar obra definitivamente? No permitirá nuevos movimientos.')) return;
        try {
            await api(`/api/obras/${b.dataset.close}/cerrar`, { method: 'POST' });
            mostrarAlerta('Obra cerrada correctamente.');
            await renderObras();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    });

    tb.querySelectorAll('[data-del]').forEach(b => b.onclick = async () => {
        if (!confirm('¿Eliminar obra?')) return;
        try {
            await api(`/api/obras/${b.dataset.del}`, { method: 'DELETE' });
            mostrarAlerta('Obra eliminada.');
            await renderObras();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    });

    document.getElementById('formObra').onsubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                idCliente: obIdCliente.value ? Number(obIdCliente.value) : null,
                nombre: document.getElementById('obNombre').value.trim(),
                cliente: obCliente.value.trim(),
                ubicacion: document.getElementById('obUbicacion').value.trim(),
                presupuestoTotal: Number(document.getElementById('obPresup').value),
                fechaInicioPlan: document.getElementById('obIni').value,
                fechaFinPlan: document.getElementById('obFin').value,
                estado: document.getElementById('obEstado').value
            };
            if (editId) {
                await api(`/api/obras/${editId}`, { method: 'PUT', body: payload });
                mostrarAlerta('Obra actualizada.');
            } else {
                await api('/api/obras', { method: 'POST', body: payload });
                mostrarAlerta('Obra creada.');
            }
            await renderObras();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderPartidas() {
    const obras = await api('/api/obras');
    const materiales = await api('/api/materiales');
    const esAdmin = sesion.rol === 'ADMIN' || sesion.rol === 'RESIDENTE';
    let idObraSel = obras[0] ? obras[0].idObra : 0;

    moduleContainer.innerHTML = `
        <div class="card">
            <div class="form-grid">
                <div class="form-group">
                    <label>Seleccionar Obra</label>
                    <select id="parObraSel">${obras.map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select>
                </div>
            </div>
        </div>
        ${esAdmin ? `
        <div class="card">
            <h3>Nueva Partida</h3>
            <form id="formPartida" class="form-grid">
                <div class="form-group"><label>Nombre *</label><input id="parNombre" required></div>
                <div class="form-group"><label>Unidad *</label><input id="parUnidad" required placeholder="m3, m2, kg"></div>
                <div class="form-group"><label>Metrado Plan. *</label><input id="parMetrado" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><label>Peso Presup. (%) *</label><input id="parPeso" type="number" step="0.01" min="0.01" max="100" required></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Agregar Partida</button></div>
            </form>
        </div>` : ''}
        <div class="card">
            <h3>Partidas de la Obra</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Unidad</th><th>Metrado Plan.</th><th>Peso (%)</th><th>Acciones</th></tr></thead>
                <tbody id="tbPartidas"></tbody>
            </table></div>
        </div>
        <div id="cardPartidaMat" class="card hidden">
            <h3 id="tituloMatPartida">Materiales Presupuestados</h3>
            ${esAdmin ? `
            <form id="formMatPartida" class="form-grid" style="margin-bottom:14px;">
                <div class="form-group"><label>Material</label>
                    <select id="pmMaterial">${materiales.map(m => `<option value="${m.idMaterial}">${esc(m.nombre)} (${esc(m.unidadMedida)})</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Cantidad Presupuestada</label><input id="pmCant" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Asignar Material</button></div>
            </form>` : ''}
            <div class="table-wrap"><table>
                <thead><tr><th>Material</th><th>Cantidad Presupuestada</th><th>Acción</th></tr></thead>
                <tbody id="tbPartidaMats"></tbody>
            </table></div>
        </div>`;

    let idPartidaMatSel = null;

    async function cargarListaPartidas() {
        if (!idObraSel) { document.getElementById('tbPartidas').innerHTML = ''; return; }
        const partidas = await api(`/api/partidas?idObra=${idObraSel}`);
        const tb = document.getElementById('tbPartidas');
        tb.innerHTML = partidas.map(p => `
            <tr>
                <td>${p.idPartida}</td>
                <td>${esc(p.nombre)}</td>
                <td>${esc(p.unidadMedida)}</td>
                <td>${p.metradoPlanificado}</td>
                <td>${p.pesoPresupuestal}%</td>
                <td>
                    <button class="btn btn-secondary btn-sm" data-mats="${p.idPartida}" data-pnombre="${esc(p.nombre)}">Presupuesto Materiales</button>
                    ${esAdmin ? `<button class="btn btn-danger btn-sm" data-delp="${p.idPartida}">Eliminar</button>` : ''}
                </td>
            </tr>`).join('');

        tb.querySelectorAll('[data-mats]').forEach(b => b.onclick = async () => {
            idPartidaMatSel = Number(b.dataset.mats);
            document.getElementById('tituloMatPartida').textContent = `Materiales Presupuestados — ${b.dataset.pnombre}`;
            document.getElementById('cardPartidaMat').classList.remove('hidden');
            await cargarMatsPartida();
        });
        tb.querySelectorAll('[data-delp]').forEach(b => b.onclick = async () => {
            if (!confirm('¿Eliminar partida?')) return;
            try {
                await api(`/api/partidas/${b.dataset.delp}`, { method: 'DELETE' });
                mostrarAlerta('Partida eliminada.');
                await cargarListaPartidas();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        });
    }

    async function cargarMatsPartida() {
        if (!idPartidaMatSel) return;
        const rows = await api(`/api/partidas/${idPartidaMatSel}/materiales`);
        const tb = document.getElementById('tbPartidaMats');
        tb.innerHTML = rows.map(pm => {
            const mat = materiales.find(m => m.idMaterial === pm.idMaterial);
            return `<tr>
                <td>${esc(mat ? mat.nombre : 'Material #' + pm.idMaterial)}</td>
                <td>${pm.cantidadPresupuestada}</td>
                <td>${esAdmin ? `<button class="btn btn-danger btn-sm" data-delpm="${pm.idPartidaMaterial}">Quitar</button>` : '-'}</td>
            </tr>`;
        }).join('');
        tb.querySelectorAll('[data-delpm]').forEach(b => b.onclick = async () => {
            try {
                await api(`/api/partidas/materiales/${b.dataset.delpm}`, { method: 'DELETE' });
                await cargarMatsPartida();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        });
    }

    document.getElementById('parObraSel').onchange = async (e) => {
        idObraSel = Number(e.target.value);
        document.getElementById('cardPartidaMat').classList.add('hidden');
        await cargarListaPartidas();
    };

    if (esAdmin) {
        document.getElementById('formPartida').onsubmit = async (e) => {
            e.preventDefault();
            try {
                await api('/api/partidas', {
                    method: 'POST',
                    body: {
                        idObra: idObraSel,
                        nombre: document.getElementById('parNombre').value.trim(),
                        unidadMedida: document.getElementById('parUnidad').value.trim(),
                        metradoPlanificado: Number(document.getElementById('parMetrado').value),
                        pesoPresupuestal: Number(document.getElementById('parPeso').value)
                    }
                });
                mostrarAlerta('Partida creada.');
                e.target.reset();
                await cargarListaPartidas();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
        document.getElementById('formMatPartida').onsubmit = async (e) => {
            e.preventDefault();
            try {
                await api(`/api/partidas/${idPartidaMatSel}/materiales`, {
                    method: 'POST',
                    body: {
                        idMaterial: Number(document.getElementById('pmMaterial').value),
                        cantidadPresupuestada: Number(document.getElementById('pmCant').value)
                    }
                });
                mostrarAlerta('Material asignado a la partida.');
                document.getElementById('pmCant').value = '';
                await cargarMatsPartida();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }

    await cargarListaPartidas();
}

async function renderAvances() {
    const obras = await api('/api/obras');
    const esResidente = sesion.rol === 'RESIDENTE';
    let idObra = obras[0] ? obras[0].idObra : 0;

    moduleContainer.innerHTML = `
        <div class="card">
            <div class="form-grid">
                <div class="form-group"><label>Obra</label>
                    <select id="avObra">${obras.map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select>
                </div>
                <div class="form-group">
                    <label>Avance Físico Ponderado de Obra</label>
                    <div class="progress-track"><div id="avBar" class="progress-fill" style="width:0%"></div></div>
                    <small id="avPctTxt" style="font-weight:700;color:var(--navy);">0%</small>
                </div>
            </div>
        </div>
        ${esResidente ? `
        <div class="card">
            <h3>Registrar Avance de Partida (RF-05)</h3>
            <form id="formAvance" class="form-grid">
                <div class="form-group"><label>Partida</label><select id="avPartida"></select></div>
                <div class="form-group"><label>Fecha Reporte</label><input id="avFecha" type="date" value="${hoyIso()}" required></div>
                <div class="form-group"><label>% Avance (0 - 100)</label><input id="avPct" type="number" step="0.01" min="0" max="100" required></div>
                <div class="form-group"><label>Observaciones</label><input id="avObs"></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Registrar Avance</button></div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de solo lectura: solo el Residente de Obra registra avances físicos (RF-05).</div>'}
        <div class="card">
            <h3>Historial de Avances</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Partida</th><th>Fecha</th><th>% Avance</th><th>Observaciones</th></tr></thead>
                <tbody id="tbAvances"></tbody>
            </table></div>
        </div>`;

    async function cargarAvancesObra() {
        if (!idObra) return;
        const [partidas, pond, lista] = await Promise.all([
            api(`/api/partidas?idObra=${idObra}`),
            api(`/api/avances/obra/${idObra}`),
            api(`/api/avances?idObra=${idObra}`)
        ]);
        const pct = Number(pond.avanceAcumulado || 0);
        document.getElementById('avBar').style.width = `${Math.min(100, pct)}%`;
        document.getElementById('avPctTxt').textContent = `${pct.toFixed(2)}%`;
        const avPartida = document.getElementById('avPartida');
        if (avPartida) {
            avPartida.innerHTML = partidas.map(p => `<option value="${p.idPartida}">${esc(p.nombre)} (Peso ${p.pesoPresupuestal}%)</option>`).join('');
        }
        document.getElementById('tbAvances').innerHTML = lista.map(a => {
            const p = partidas.find(x => x.idPartida === a.idPartida);
            return `<tr><td>${a.idAvance}</td><td>${esc(p ? p.nombre : a.idPartida)}</td><td>${esc(a.fechaReporte)}</td><td><strong>${a.porcentajeAvance}%</strong></td><td>${esc(a.observaciones)}</td></tr>`;
        }).join('');
    }

    document.getElementById('avObra').onchange = async (e) => {
        idObra = Number(e.target.value);
        await cargarAvancesObra();
    };
    if (esResidente) {
        document.getElementById('formAvance').onsubmit = async (e) => {
            e.preventDefault();
            try {
                const res = await api('/api/avances', {
                    method: 'POST',
                    body: {
                        idPartida: Number(document.getElementById('avPartida').value),
                        fechaReporte: document.getElementById('avFecha').value,
                        porcentajeAvance: Number(document.getElementById('avPct').value),
                        observaciones: document.getElementById('avObs').value.trim()
                    }
                });
                mostrarAlerta(`Avance registrado. Avance acumulado de obra: ${res.avanceObra}%`);
                await cargarAvancesObra();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }
    await cargarAvancesObra();
}

async function renderMateriales() {
    const materiales = await api('/api/materiales');
    const esAdmin = sesion.rol === 'ADMIN' || sesion.rol === 'ALMACENERO';
    await cargarAlertasStock();

    moduleContainer.innerHTML = `
        ${esAdmin ? `
        <div class="card">
            <h3>Nuevo / Editar Material</h3>
            <form id="formMat" class="form-grid">
                <div class="form-group"><label>Nombre *</label><input id="matNombre" required></div>
                <div class="form-group"><label>Unidad Medida *</label><input id="matUnidad" required placeholder="bls, kg, m3"></div>
                <div class="form-group"><label>Stock Mínimo</label><input id="matMin" type="number" step="0.01" min="0" value="0"></div>
                <div class="form-group"><label>Costo Ref. (S/)</label><input id="matCosto" type="number" step="0.01" min="0" value="0"></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Guardar Material</button></div>
            </form>
        </div>` : ''}
        <div class="card">
            <h3>Catálogo e Inventario de Materiales</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Unidad</th><th>Stock Actual</th><th>Stock Mínimo</th><th>Estado Stock</th>${esAdmin ? '<th>Acciones</th>' : ''}</tr></thead>
                <tbody id="tbMats"></tbody>
            </table></div>
        </div>`;

    document.getElementById('tbMats').innerHTML = materiales.map(m => `
        <tr>
            <td>${m.idMaterial}</td>
            <td>${esc(m.nombre)}</td>
            <td>${esc(m.unidadMedida)}</td>
            <td><strong>${m.stockActual}</strong></td>
            <td>${m.stockMinimo}</td>
            <td><span class="badge ${m.alertaStock ? 'badge-warn' : 'badge-ok'}">${m.alertaStock ? 'Stock bajo' : 'Normal'}</span></td>
            ${esAdmin ? `<td><button class="btn btn-danger btn-sm" data-delm="${m.idMaterial}">Eliminar</button></td>` : ''}
        </tr>`).join('');

    if (esAdmin) {
        document.querySelectorAll('[data-delm]').forEach(b => b.onclick = async () => {
            if (!confirm('¿Eliminar material?')) return;
            try {
                await api(`/api/materiales/${b.dataset.delm}`, { method: 'DELETE' });
                mostrarAlerta('Material eliminado.');
                await renderMateriales();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        });
        document.getElementById('formMat').onsubmit = async (e) => {
            e.preventDefault();
            try {
                await api('/api/materiales', {
                    method: 'POST',
                    body: {
                        nombre: document.getElementById('matNombre').value.trim(),
                        unidadMedida: document.getElementById('matUnidad').value.trim(),
                        stockMinimo: Number(document.getElementById('matMin').value),
                        costoUnitarioRef: Number(document.getElementById('matCosto').value)
                    }
                });
                mostrarAlerta('Material guardado.');
                await renderMateriales();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }
}

async function renderProveedores() {
    const proveedores = await api('/api/proveedores');
    const puedeEscribir = sesion.rol === 'ADMIN';
    moduleContainer.innerHTML = `
        ${puedeEscribir ? `
        <div class="card">
            <h3>Registrar Proveedor</h3>
            <form id="formProv" class="form-grid">
                <div class="form-group"><label>Razón Social *</label><input id="prRazon" required></div>
                <div class="form-group"><label>RUC (11 dígitos) *</label><input id="prRuc" maxlength="11" required></div>
                <div class="form-group"><label>Teléfono</label><input id="prTel"></div>
                <div class="form-group"><label>Dirección</label><input id="prDir"></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Guardar</button></div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de consulta (solo el Administrador gestiona proveedores).</div>'}
        <div class="card">
            <h3>Proveedores Activos</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Razón Social</th><th>RUC</th><th>Teléfono</th><th>Dirección</th>${puedeEscribir ? '<th>Acción</th>' : ''}</tr></thead>
                <tbody>
                    ${proveedores.map(p => `<tr><td>${p.idProveedor}</td><td>${esc(p.razonSocial)}</td><td>${esc(p.ruc)}</td><td>${esc(p.telefono)}</td><td>${esc(p.direccion)}</td>${puedeEscribir ? `<td><button class="btn btn-danger btn-sm" data-delpr="${p.idProveedor}">Eliminar</button></td>` : ''}</tr>`).join('')}
                </tbody>
            </table></div>
        </div>`;

    if (!puedeEscribir) return;
    document.querySelectorAll('[data-delpr]').forEach(b => b.onclick = async () => {
        if (!confirm('¿Eliminar proveedor?')) return;
        try {
            await api(`/api/proveedores/${b.dataset.delpr}`, { method: 'DELETE' });
            mostrarAlerta('Proveedor eliminado.');
            await renderProveedores();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    });
    document.getElementById('formProv').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/proveedores', {
                method: 'POST',
                body: {
                    razonSocial: document.getElementById('prRazon').value.trim(),
                    ruc: document.getElementById('prRuc').value.trim(),
                    telefono: document.getElementById('prTel').value.trim(),
                    direccion: document.getElementById('prDir').value.trim()
                }
            });
            mostrarAlerta('Proveedor guardado.');
            await renderProveedores();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderCompras() {
    const [compras, obras, materiales, proveedores] = await Promise.all([
        api('/api/compras'), api('/api/obras'), api('/api/materiales'), api('/api/proveedores')
    ]);
    const puedeEscribir = sesion.rol === 'ADMIN' || sesion.rol === 'ALMACENERO';
    const matOpciones = materiales.map(m => `<option value="${m.idMaterial}">${esc(m.nombre)} (${esc(m.unidadMedida)})</option>`).join('');

    moduleContainer.innerHTML = `
        ${puedeEscribir ? `
        <div class="card">
            <h3>Registrar Compra de Material (RF-07)</h3>
            <form id="formCompra">
                <div class="form-grid">
                    <div class="form-group"><label>Proveedor</label><select id="coProv">${proveedores.map(p => `<option value="${p.idProveedor}">${esc(p.razonSocial)}</option>`).join('')}</select></div>
                    <div class="form-group"><label>Obra Destino</label><select id="coObra">${obras.filter(o => o.operable).map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select></div>
                    <div class="form-group"><label>Comprobante</label><select id="coTipo"><option>Factura</option><option>Boleta</option></select></div>
                    <div class="form-group"><label>Nro. Comprobante *</label><input id="coNum" required placeholder="F001-000123"></div>
                    <div class="form-group"><label>Fecha Compra *</label><input id="coFecha" type="date" value="${hoyIso()}" required></div>
                </div>
                <div style="margin-top:12px;">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                        <strong>Detalle de Materiales</strong>
                        <button type="button" id="btnAddItemCompra" class="btn btn-secondary btn-sm">+ Agregar Material</button>
                    </div>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Material</th><th>Cantidad *</th><th>Precio Unit. (S/) *</th><th>Subtotal (S/)</th><th></th></tr></thead>
                        <tbody id="tbCompraItems"></tbody>
                    </table></div>
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-top:10px;">
                        <strong id="coTotalTxt">Total Compra: S/ 0.00</strong>
                        <button type="submit" class="btn btn-primary">Registrar Compra</button>
                    </div>
                </div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de consulta (solo Almacenero o Administrador registran compras).</div>'}
        <div class="card">
            <h3>Historial de Compras</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Fecha</th><th>Tipo</th><th>Número</th><th>Proveedor</th><th>Total (S/)</th></tr></thead>
                <tbody>${compras.map(c => `<tr><td>${c.idCompra}</td><td>${esc(c.fechaCompra)}</td><td>${esc(c.tipoComprobante)}</td><td>${esc(c.numeroComprobante)}</td><td>#${c.idProveedor}</td><td>S/ ${Number(c.total).toFixed(2)}</td></tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    if (!puedeEscribir) return;

    const tbItems = document.getElementById('tbCompraItems');
    function recalcularTotalCompra() {
        let total = 0;
        tbItems.querySelectorAll('tr').forEach(tr => {
            const cant = Number(tr.querySelector('.ci-cant').value || 0);
            const precio = Number(tr.querySelector('.ci-precio').value || 0);
            const sub = cant * precio;
            tr.querySelector('.ci-sub').textContent = `S/ ${sub.toFixed(2)}`;
            total += sub;
        });
        document.getElementById('coTotalTxt').textContent = `Total Compra: S/ ${total.toFixed(2)}`;
    }
    function agregarFilaCompra() {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><select class="ci-mat">${matOpciones}</select></td>
            <td><input class="ci-cant" type="number" step="0.01" min="0.01" value="1" required></td>
            <td><input class="ci-precio" type="number" step="0.01" min="0.01" value="1" required></td>
            <td class="ci-sub">S/ 1.00</td>
            <td><button type="button" class="btn btn-danger btn-sm ci-del">Quitar</button></td>`;
        tr.querySelector('.ci-cant').oninput = recalcularTotalCompra;
        tr.querySelector('.ci-precio').oninput = recalcularTotalCompra;
        tr.querySelector('.ci-del').onclick = () => {
            if (tbItems.querySelectorAll('tr').length > 1) {
                tr.remove();
                recalcularTotalCompra();
            }
        };
        tbItems.appendChild(tr);
        recalcularTotalCompra();
    }
    agregarFilaCompra();
    document.getElementById('btnAddItemCompra').onclick = agregarFilaCompra;

    document.getElementById('formCompra').onsubmit = async (e) => {
        e.preventDefault();
        try {
            const items = Array.from(tbItems.querySelectorAll('tr')).map(tr => ({
                idMaterial: Number(tr.querySelector('.ci-mat').value),
                cantidad: Number(tr.querySelector('.ci-cant').value),
                precioUnitario: Number(tr.querySelector('.ci-precio').value)
            }));
            await api('/api/compras', {
                method: 'POST',
                body: {
                    idProveedor: Number(document.getElementById('coProv').value),
                    idObra: Number(document.getElementById('coObra').value),
                    tipoComprobante: document.getElementById('coTipo').value,
                    numeroComprobante: document.getElementById('coNum').value.trim(),
                    fechaCompra: document.getElementById('coFecha').value,
                    items
                }
            });
            mostrarAlerta('Compra registrada. Stock y egreso de material actualizados.');
            await cargarAlertasStock();
            await renderCompras();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderConsumos() {
    const [obras, materiales, consumos] = await Promise.all([
        api('/api/obras'), api('/api/materiales'), api('/api/consumos')
    ]);
    const esResidente = sesion.rol === 'RESIDENTE';
    const partidasAll = [];
    for (const o of obras) {
        const ps = await api(`/api/partidas?idObra=${o.idObra}`);
        ps.forEach(p => partidasAll.push({ ...p, obraNombre: o.nombre }));
    }

    moduleContainer.innerHTML = `
        ${esResidente ? `
        <div class="card">
            <h3>Registrar Consumo de Material en Partida (RF-08)</h3>
            <form id="formConsumo" class="form-grid">
                <div class="form-group"><label>Partida (Obra)</label><select id="csPartida">${partidasAll.map(p => `<option value="${p.idPartida}">${esc(p.obraNombre)} — ${esc(p.nombre)}</option>`).join('')}</select></div>
                <div class="form-group"><label>Material</label><select id="csMat">${materiales.map(m => `<option value="${m.idMaterial}">${esc(m.nombre)} (Disp: ${m.stockActual})</option>`).join('')}</select></div>
                <div class="form-group"><label>Fecha</label><input id="csFecha" type="date" value="${hoyIso()}" required></div>
                <div class="form-group"><label>Cantidad *</label><input id="csCant" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Registrar Consumo</button></div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de solo lectura: solo el Residente de Obra registra consumos de material (RF-08).</div>'}
        <div class="card">
            <h3>Consumos Registrados</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Partida</th><th>Material</th><th>Fecha</th><th>Cantidad</th></tr></thead>
                <tbody>${consumos.map(c => `<tr><td>${c.idConsumo}</td><td>#${c.idPartida}</td><td>#${c.idMaterial}</td><td>${esc(c.fechaConsumo)}</td><td>${c.cantidad}</td></tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    if (!esResidente) return;
    document.getElementById('formConsumo').onsubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await api('/api/consumos', {
                method: 'POST',
                body: {
                    idPartida: Number(document.getElementById('csPartida').value),
                    idMaterial: Number(document.getElementById('csMat').value),
                    fechaConsumo: document.getElementById('csFecha').value,
                    cantidad: Number(document.getElementById('csCant').value)
                }
            });
            mostrarAlerta(
                `Consumo registrado. Stock resultante: ${res.stockResultante}${res.alertaStockMinimo ? ' (ALERTA: bajo mínimo!)' : ''}`,
                res.alertaStockMinimo ? 'warning' : 'success'
            );
            await cargarAlertasStock();
            await renderConsumos();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderPersonal() {
    const empleados = await api('/api/empleados');
    const esAdmin = sesion.rol === 'ADMIN' || sesion.rol === 'RESIDENTE';
    moduleContainer.innerHTML = `
        ${esAdmin ? `
        <div class="card">
            <h3>Registrar Trabajador</h3>
            <form id="formEmp" class="form-grid">
                <div class="form-group"><label>Nombres y Apellidos *</label><input id="emNombre" required></div>
                <div class="form-group"><label>DNI (8 dígitos) *</label><input id="emDni" maxlength="8" required></div>
                <div class="form-group"><label>Cargo *</label><input id="emCargo" required placeholder="Operario, Oficial, Peón"></div>
                <div class="form-group"><label>Jornal Diario (S/) *</label><input id="emJornal" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><label>Fecha Ingreso *</label><input id="emFecha" type="date" value="${hoyIso()}" required></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Guardar</button></div>
            </form>
        </div>` : ''}
        <div class="card">
            <h3>Personal Activo</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>DNI</th><th>Nombres</th><th>Cargo</th><th>Jornal Diario</th><th>Ingreso</th>${esAdmin ? '<th>Acción</th>' : ''}</tr></thead>
                <tbody>${empleados.map(e => `<tr><td>${e.idEmpleado}</td><td>${esc(e.dni)}</td><td>${esc(e.nombresApellidos)}</td><td>${esc(e.cargo)}</td><td>S/ ${Number(e.jornalDiario).toFixed(2)}</td><td>${esc(e.fechaIngreso)}</td>${esAdmin ? `<td><button class="btn btn-danger btn-sm" data-delem="${e.idEmpleado}">Eliminar</button></td>` : ''}</tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    if (esAdmin) {
        document.querySelectorAll('[data-delem]').forEach(b => b.onclick = async () => {
            if (!confirm('¿Eliminar trabajador?')) return;
            try {
                await api(`/api/empleados/${b.dataset.delem}`, { method: 'DELETE' });
                mostrarAlerta('Trabajador eliminado.');
                await renderPersonal();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        });
        document.getElementById('formEmp').onsubmit = async (e) => {
            e.preventDefault();
            try {
                await api('/api/empleados', {
                    method: 'POST',
                    body: {
                        nombresApellidos: document.getElementById('emNombre').value.trim(),
                        dni: document.getElementById('emDni').value.trim(),
                        cargo: document.getElementById('emCargo').value.trim(),
                        jornalDiario: Number(document.getElementById('emJornal').value),
                        fechaIngreso: document.getElementById('emFecha').value
                    }
                });
                mostrarAlerta('Trabajador registrado.');
                await renderPersonal();
            } catch (err) { mostrarAlerta(err.message, 'error'); }
        };
    }
}

async function renderAsistencia() {
    const [empleados, obras, asistencias] = await Promise.all([
        api('/api/empleados'), api('/api/obras'), api('/api/asistencias')
    ]);
    const esResidente = sesion.rol === 'RESIDENTE';
    const obrasActivas = obras.filter(o => o.operable);
    moduleContainer.innerHTML = `
        ${esResidente ? `
        <div class="card">
            <h3>Control Rápido de Asistencia Diaria (RF-11)</h3>
            <form id="formAsisLote">
                <div class="form-grid" style="margin-bottom:12px;">
                    <div class="form-group"><label>Obra</label><select id="asObra">${obrasActivas.map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select></div>
                    <div class="form-group"><label>Fecha</label><input id="asFecha" type="date" value="${hoyIso()}" required></div>
                </div>
                <div class="table-wrap"><table>
                    <thead><tr><th>Incluir</th><th>Trabajador</th><th>DNI</th><th>Cargo</th><th>Estado</th><th>Entrada</th><th>Salida</th></tr></thead>
                    <tbody id="tbAsisMatrix">
                        ${empleados.map(e => `
                            <tr data-emp="${e.idEmpleado}">
                                <td><input type="checkbox" class="as-chk" checked></td>
                                <td>${esc(e.nombresApellidos)}</td>
                                <td>${esc(e.dni)}</td>
                                <td>${esc(e.cargo)}</td>
                                <td>
                                    <select class="as-est">
                                        <option value="PRESENTE" selected>PRESENTE</option>
                                        <option value="TARDANZA">TARDANZA</option>
                                        <option value="FALTA">FALTA</option>
                                        <option value="PERMISO">PERMISO</option>
                                    </select>
                                </td>
                                <td><input type="time" class="as-ent" value="08:00"></td>
                                <td><input type="time" class="as-sal" value="17:00"></td>
                            </tr>`).join('')}
                    </tbody>
                </table></div>
                <div style="margin-top:12px;">
                    <button type="submit" class="btn btn-primary">Registrar Asistencia del Día</button>
                </div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de solo lectura: solo el Residente de Obra registra asistencia diaria (RF-11).</div>'}
        <div class="card">
            <h3>Asistencias Registradas</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Trabajador</th><th>Obra</th><th>Fecha</th><th>Estado</th><th>Entrada</th><th>Salida</th></tr></thead>
                <tbody>${asistencias.map(a => `<tr><td>${a.idAsistencia}</td><td>#${a.idEmpleado}</td><td>#${a.idObra}</td><td>${esc(a.fecha)}</td><td><span class="badge badge-info">${esc(a.estado)}</span></td><td>${esc(a.horaEntrada || '-')}</td><td>${esc(a.horaSalida || '-')}</td></tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    if (!esResidente) return;

    document.querySelectorAll('#tbAsisMatrix tr').forEach(tr => {
        const sel = tr.querySelector('.as-est');
        const ent = tr.querySelector('.as-ent');
        const sal = tr.querySelector('.as-sal');
        const actualizarHoras = () => {
            const sinHoras = sel.value === 'FALTA' || sel.value === 'PERMISO';
            ent.disabled = sinHoras;
            sal.disabled = sinHoras;
            if (sinHoras) {
                ent.value = '';
                sal.value = '';
            } else if (!ent.value) {
                ent.value = '08:00';
                sal.value = '17:00';
            }
        };
        sel.onchange = actualizarHoras;
    });

    document.getElementById('formAsisLote').onsubmit = async (e) => {
        e.preventDefault();
        try {
            const filas = Array.from(document.querySelectorAll('#tbAsisMatrix tr'))
                .filter(tr => tr.querySelector('.as-chk').checked)
                .map(tr => {
                    const estado = tr.querySelector('.as-est').value;
                    const sinHoras = estado === 'FALTA' || estado === 'PERMISO';
                    return {
                        idEmpleado: Number(tr.dataset.emp),
                        estado,
                        horaEntrada: sinHoras ? null : (tr.querySelector('.as-ent').value || null),
                        horaSalida: sinHoras ? null : (tr.querySelector('.as-sal').value || null)
                    };
                });
            const res = await api('/api/asistencias/lote', {
                method: 'POST',
                body: {
                    idObra: Number(document.getElementById('asObra').value),
                    fecha: document.getElementById('asFecha').value,
                    filas
                }
            });
            mostrarAlerta(`Asistencia registrada (${res.length} trabajadores).`);
            await renderAsistencia();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderProduccion() {
    const [empleados, obras, lista] = await Promise.all([
        api('/api/empleados'), api('/api/obras'), api('/api/produccion')
    ]);
    const esResidente = sesion.rol === 'RESIDENTE';
    const partidasAll = [];
    for (const o of obras) {
        const ps = await api(`/api/partidas?idObra=${o.idObra}`);
        ps.forEach(p => partidasAll.push({ ...p, obraNombre: o.nombre }));
    }
    moduleContainer.innerHTML = `
        ${esResidente ? `
        <div class="card">
            <h3>Registrar Producción por Trabajador y Partida (RF-12)</h3>
            <form id="formProd" class="form-grid">
                <div class="form-group"><label>Trabajador</label><select id="pdEmp">${empleados.map(e => `<option value="${e.idEmpleado}">${esc(e.nombresApellidos)}</option>`).join('')}</select></div>
                <div class="form-group"><label>Partida</label><select id="pdPar">${partidasAll.map(p => `<option value="${p.idPartida}">${esc(p.obraNombre)} — ${esc(p.nombre)}</option>`).join('')}</select></div>
                <div class="form-group"><label>Fecha</label><input id="pdFecha" type="date" value="${hoyIso()}" required></div>
                <div class="form-group"><label>Cantidad Producida *</label><input id="pdCant" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Registrar Producción</button></div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de solo lectura: solo el Residente de Obra registra producción diaria (RF-12).</div>'}
        <div class="card">
            <h3>Historial de Producción</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Trabajador</th><th>Partida</th><th>Fecha</th><th>Cantidad</th></tr></thead>
                <tbody>${lista.map(p => `<tr><td>${p.idProduccion}</td><td>#${p.idEmpleado}</td><td>#${p.idPartida}</td><td>${esc(p.fecha)}</td><td>${p.cantidadProducida}</td></tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    if (!esResidente) return;
    document.getElementById('formProd').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/produccion', {
                method: 'POST',
                body: {
                    idEmpleado: Number(document.getElementById('pdEmp').value),
                    idPartida: Number(document.getElementById('pdPar').value),
                    fecha: document.getElementById('pdFecha').value,
                    cantidadProducida: Number(document.getElementById('pdCant').value)
                }
            });
            mostrarAlerta('Producción registrada.');
            await renderProduccion();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

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

function renderCurvaSSvg(reporte) {
    const filas = reporte.filas || [];
    if (filas.length === 0) return '<p style="color:var(--muted);">Sin datos para graficar Curva S.</p>';
    const w = 720, h = 250, pad = 42;
    const n = filas.length;
    const xAt = i => n === 1 ? w / 2 : pad + (i * (w - pad * 2)) / (n - 1);
    const yAt = v => h - pad - (Math.max(0, Math.min(100, Number(v) || 0)) * (h - pad * 2)) / 100;

    const ptsPlan = filas.map((r, i) => `${xAt(i)},${yAt(r[2])}`).join(' ');
    const ptsEjec = filas.map((r, i) => `${xAt(i)},${yAt(r[3])}`).join(' ');
    const grid = [0, 25, 50, 75, 100].map(v => {
        const y = yAt(v);
        return `<line x1="${pad}" y1="${y}" x2="${w - pad}" y2="${y}" stroke="#e2e8f0" stroke-width="1"/>
                <text x="${pad - 8}" y="${y + 4}" text-anchor="end" font-size="10" fill="#64748b">${v}%</text>`;
    }).join('');
    const dots = filas.map((r, i) => `
        <circle cx="${xAt(i)}" cy="${yAt(r[2])}" r="3.5" fill="#0066cc"/>
        <circle cx="${xAt(i)}" cy="${yAt(r[3])}" r="3.5" fill="#059669"/>`).join('');

    return `<div class="chart-box">
        <svg viewBox="0 0 ${w} ${h}" width="100%" style="max-width:740px;display:block;">
            ${grid}
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
            <h3>Reportes Gerenciales (RF-16)</h3>
            <form id="formRep" class="form-grid">
                <div class="form-group"><label>Tipo de Reporte</label>
                    <select id="rpTipo">
                        <option value="CURVA_S">Curva S — Avance Planificado vs Ejecutado</option>
                        <option value="CONSUMO_MATERIALES">Consumo de Materiales Real vs Presupuestado</option>
                        <option value="PERSONAL_PRODUCTIVIDAD">Personal — Asistencia y Productividad</option>
                        <option value="FINANCIERO">Estado Financiero y Rentabilidad</option>
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
        <div id="rpChartCard" class="card hidden">
            <h3>Gráfico Curva S</h3>
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
        const rep = await api(`/api/reportes?${p.toString()}`);
        document.getElementById('rpTitulo').textContent = rep.titulo;
        const chartCard = document.getElementById('rpChartCard');
        if (document.getElementById('rpTipo').value === 'CURVA_S') {
            chartCard.classList.remove('hidden');
            document.getElementById('rpChart').innerHTML = renderCurvaSSvg(rep);
        } else {
            chartCard.classList.add('hidden');
        }
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

async function renderUsuarios() {
    const [usuarios, roles] = await Promise.all([api('/api/usuarios'), api('/api/roles')]);
    moduleContainer.innerHTML = `
        <div class="card">
            <h3>Crear Usuario (RF-02)</h3>
            <form id="formUser" class="form-grid">
                <div class="form-group"><label>Nombre Completo *</label><input id="usNombre" required></div>
                <div class="form-group"><label>Usuario *</label><input id="usLogin" required></div>
                <div class="form-group"><label>Contraseña Inicial *</label><input id="usClave" type="password" minlength="6" required></div>
                <div class="form-group"><label>Rol *</label><select id="usRol">${roles.map(r => `<option value="${r.idRol}">${esc(r.nombreRol)}</option>`).join('')}</select></div>
                <div class="form-group"><button type="submit" class="btn btn-primary">Crear Usuario</button></div>
            </form>
        </div>
        <div class="card">
            <h3>Usuarios Registrados</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Usuario</th><th>Rol</th><th>Estado</th><th>Acción</th></tr></thead>
                <tbody>${usuarios.map(u => `<tr>
                    <td>${u.idUsuario}</td>
                    <td>${esc(u.nombreCompleto)}</td>
                    <td>${esc(u.usuario)}</td>
                    <td>${esc(u.rol)}</td>
                    <td><span class="badge ${u.activo ? 'badge-ok' : 'badge-danger'}">${u.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td><button class="btn btn-secondary btn-sm" data-toggle="${u.idUsuario}" data-act="${!u.activo}">${u.activo ? 'Desactivar' : 'Activar'}</button></td>
                </tr>`).join('')}</tbody>
            </table></div>
        </div>`;

    document.querySelectorAll('[data-toggle]').forEach(b => b.onclick = async () => {
        try {
            await api(`/api/usuarios/${b.dataset.toggle}/estado`, {
                method: 'PATCH',
                body: { activo: b.dataset.act === 'true' }
            });
            mostrarAlerta('Estado actualizado.');
            await renderUsuarios();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    });

    document.getElementById('formUser').onsubmit = async (e) => {
        e.preventDefault();
        try {
            await api('/api/usuarios', {
                method: 'POST',
                body: {
                    nombreCompleto: document.getElementById('usNombre').value.trim(),
                    usuario: document.getElementById('usLogin').value.trim(),
                    clave: document.getElementById('usClave').value,
                    idRol: Number(document.getElementById('usRol').value)
                }
            });
            mostrarAlerta('Usuario creado.');
            await renderUsuarios();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

function aplicarSoloDigitos(el) {
    el.addEventListener('input', () => {
        el.value = el.value.replace(/[^0-9]/g, '');
    });
}

async function renderClientes() {
    const clientes = await api('/api/clientes');
    const esAdmin = sesion.rol === 'ADMIN';
    let editId = null;

    moduleContainer.innerHTML = `
        ${esAdmin ? `
        <div class="card">
            <h3 id="clTitulo">Registrar Cliente / Contratante</h3>
            <form id="formCliente" class="form-grid">
                <div class="form-group"><label>Tipo Documento *</label>
                    <select id="clTipo">
                        <option value="RUC">RUC (11 dígitos SUNAT)</option>
                        <option value="DNI">DNI (8 dígitos)</option>
                    </select>
                </div>
                <div class="form-group"><label>Número Documento *</label>
                    <input id="clDoc" maxlength="11" minlength="11" inputmode="numeric" required placeholder="11 dígitos (RUC)">
                </div>
                <div class="form-group"><label>Nombre Completo / Razón Social *</label><input id="clNombre" maxlength="150" required></div>
                <div class="form-group"><label>Teléfono (7–9 dígitos) *</label>
                    <input id="clTel" maxlength="9" minlength="7" inputmode="numeric" required placeholder="952123456">
                </div>
                <div class="form-group"><label>Correo</label><input id="clCorreo" type="email" maxlength="120"></div>
                <div class="form-group"><label>Dirección</label><input id="clDir" maxlength="200"></div>
                <div class="form-group" style="display:flex;gap:8px;">
                    <button type="submit" class="btn btn-primary">Guardar</button>
                    <button type="button" id="btnClLimpiar" class="btn btn-secondary">Limpiar</button>
                </div>
            </form>
        </div>` : '<div class="alert alert-info">Vista de consulta (solo el Administrador crea, edita o elimina clientes).</div>'}
        <div class="card">
            <h3>Clientes Activos</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Tipo</th><th>Documento</th><th>Nombre / Razón Social</th><th>Teléfono</th><th>Correo</th>${esAdmin ? '<th>Acciones</th>' : ''}</tr></thead>
                <tbody id="tbClientes"></tbody>
            </table></div>
        </div>`;

    document.getElementById('tbClientes').innerHTML = clientes.map(c => `
        <tr>
            <td>${c.idCliente}</td>
            <td><span class="badge badge-info">${esc(c.tipoDocumento)}</span></td>
            <td>${esc(c.numeroDocumento)}</td>
            <td>${esc(c.nombreRazonSocial)}</td>
            <td>${esc(c.telefono)}</td>
            <td>${esc(c.correo || '-')}</td>
            ${esAdmin ? `<td>
                <button class="btn btn-secondary btn-sm" data-editc="${c.idCliente}">Editar</button>
                <button class="btn btn-danger btn-sm" data-delc="${c.idCliente}">Eliminar</button>
            </td>` : ''}
        </tr>`).join('');

    if (!esAdmin) return;

    const clDoc = document.getElementById('clDoc');
    const clTel = document.getElementById('clTel');
    const clTipo = document.getElementById('clTipo');
    aplicarSoloDigitos(clDoc);
    aplicarSoloDigitos(clTel);

    function ajustarDoc(limpiarValor) {
        if (limpiarValor) clDoc.value = '';
        if (clTipo.value === 'DNI') {
            clDoc.maxLength = 8;
            clDoc.minLength = 8;
            clDoc.placeholder = '8 dígitos (DNI)';
        } else {
            clDoc.maxLength = 11;
            clDoc.minLength = 11;
            clDoc.placeholder = '11 dígitos (RUC SUNAT)';
        }
    }
    clTipo.onchange = () => ajustarDoc(true);

    document.getElementById('btnClLimpiar').onclick = () => {
        editId = null;
        document.getElementById('clTitulo').textContent = 'Registrar Cliente / Contratante';
        document.getElementById('formCliente').reset();
        ajustarDoc(true);
    };

    document.querySelectorAll('[data-editc]').forEach(b => b.onclick = () => {
        const c = clientes.find(x => x.idCliente == b.dataset.editc);
        if (!c) return;
        editId = c.idCliente;
        document.getElementById('clTitulo').textContent = `Editar Cliente #${editId}`;
        clTipo.value = c.tipoDocumento;
        ajustarDoc(false);
        clDoc.value = c.numeroDocumento;
        document.getElementById('clNombre').value = c.nombreRazonSocial;
        clTel.value = c.telefono;
        document.getElementById('clCorreo').value = c.correo || '';
        document.getElementById('clDir').value = c.direccion || '';
    });

    document.querySelectorAll('[data-delc]').forEach(b => b.onclick = async () => {
        if (!confirm('¿Eliminar cliente?')) return;
        try {
            await api(`/api/clientes/${b.dataset.delc}`, { method: 'DELETE' });
            mostrarAlerta('Cliente eliminado.');
            await renderClientes();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    });

    document.getElementById('formCliente').onsubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                tipoDocumento: clTipo.value,
                numeroDocumento: clDoc.value.trim(),
                nombreRazonSocial: document.getElementById('clNombre').value.trim(),
                telefono: clTel.value.trim(),
                correo: document.getElementById('clCorreo').value.trim(),
                direccion: document.getElementById('clDir').value.trim()
            };
            if (editId) {
                await api(`/api/clientes/${editId}`, { method: 'PUT', body: payload });
                mostrarAlerta('Cliente actualizado.');
            } else {
                await api('/api/clientes', { method: 'POST', body: payload });
                mostrarAlerta('Cliente registrado.');
            }
            await renderClientes();
        } catch (err) { mostrarAlerta(err.message, 'error'); }
    };
}

async function renderValorizaciones() {
    const [obras, valorizaciones, comprobantes] = await Promise.all([
        api('/api/obras'), api('/api/valorizaciones'), api('/api/valorizaciones/comprobantes')
    ]);
    const esAdmin = sesion.rol === 'ADMIN' || sesion.rol === 'CONTADOR';
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
    const esFinanzas = sesion.rol === 'ADMIN' || sesion.rol === 'CONTADOR';

    moduleContainer.innerHTML = `
        <div class="card">
            <h3>Tipo de Cambio Referencial USD → PEN (PayPal Sandbox)</h3>
            <form id="formTc" class="form-grid">
                <div class="form-group"><label>Par de Moneda</label><input value="USD → PEN" disabled></div>
                <div class="form-group"><label>Valor (S/ por 1 USD) *</label>
                    <input id="tcValor" type="number" step="0.0001" min="0.1" value="${Number(tipoCambio.valor || 3.75).toFixed(4)}" ${esFinanzas ? '' : 'disabled'} required>
                </div>
                <div class="form-group"><label>Última actualización</label>
                    <input value="${tipoCambio.fechaActualizacion ? esc(String(tipoCambio.fechaActualizacion).replace('T', ' ').slice(0, 16)) : '-'}" disabled>
                </div>
                ${esFinanzas ? `<div class="form-group"><button type="submit" class="btn btn-secondary">Actualizar Tipo de Cambio</button></div>` : ''}
            </form>
        </div>
        <div class="card">
            <h3>Registro / Conciliación Interna de Pagos (Genera Ingreso Automático)</h3>
            <form id="formPago" class="form-grid">
                <div class="form-group"><label>Obra Destino *</label>
                    <select id="pgObra">${obras.filter(o => o.operable).map(o => `<option value="${o.idObra}">${esc(o.nombre)}</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Valorización (Opcional)</label>
                    <select id="pgVal"><option value="">-- Ninguna --</option>${valsNoPagadas.map(v => `<option value="${v.idValorizacion}">N° ${v.numero || v.idValorizacion} · Obra #${v.idObra} · ${esc(v.periodo)} (S/ ${Number(v.montoValorizado).toFixed(2)})</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Comprobante CV01 (Opcional)</label>
                    <select id="pgComp"><option value="">-- Ninguno --</option>${compsEmitidos.map(c => `<option value="${c.idComprobante}">#${c.idComprobante} · ${esc(c.serieNumero)} (S/ ${Number(c.total).toFixed(2)})</option>`).join('')}</select>
                </div>
                <div class="form-group"><label>Monto (S/) *</label><input id="pgMonto" type="number" step="0.01" min="0.01" required></div>
                <div class="form-group"><label>Método</label>
                    <select id="pgMetodo">
                        <option value="TARJETA_SIMULADA">Tarjeta Simulada</option>
                        <option value="TRANSFERENCIA_SIMULADA">Transferencia Simulada</option>
                        <option value="PAYPAL_SANDBOX">PayPal Sandbox</option>
                    </select>
                </div>
                <div class="form-group"><label>Cód. Operación (Opcional)</label><input id="pgCodigo" placeholder="Autogenerado si vacío"></div>
                <div class="form-group"><button type="submit" class="btn btn-success">Aprobar y Registrar Pago</button></div>
            </form>
        </div>
        <div class="card">
            <h3>Historial de Pagos y Auditoría de Pasarela</h3>
            <div class="table-wrap"><table>
                <thead><tr><th>ID</th><th>Obra</th><th>Comprob.</th><th>Ingreso</th><th>Pasarela / Método</th><th>Cód. Operación</th><th>Monto (S/)</th><th>Cobro</th><th>Tarjeta</th><th>Estado</th></tr></thead>
                <tbody>${pagos.map(p => `<tr>
                    <td>${p.idPago}</td><td>#${p.idObra}</td><td>${p.idComprobante ? '#' + p.idComprobante : '-'}</td>
                    <td>${p.idIngresoGenerado ? '#' + p.idIngresoGenerado : '-'}</td>
                    <td>${esc(p.pasarela || '-')} / ${esc(p.metodoPago)}</td><td><strong>${esc(p.codigoOperacion)}</strong></td>
                    <td>S/ ${Number(p.monto).toFixed(2)}</td>
                    <td>${p.montoCobro ? `${Number(p.montoCobro).toFixed(2)} ${esc(p.monedaCobro || 'PEN')}` : '-'}</td>
                    <td>${p.ultimos4 ? `${esc(p.marcaTarjeta || '')} *${esc(p.ultimos4)}` : '-'}</td>
                    <td title="${esc(p.motivo || '')}"><span class="badge ${p.estado === 'APROBADO' ? 'badge-ok' : 'badge-danger'}">${esc(p.estado)}</span></td>
                </tr>`).join('')}</tbody>
            </table></div>
        </div>`;

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
function abrirModalClave(obligatorio) {
    document.getElementById('claveError').classList.add('hidden');
    document.getElementById('formClave').reset();
    document.getElementById('modalClaveTitulo').textContent = obligatorio
        ? 'Cambio de Contraseña Obligatorio (RF-01)' : 'Cambiar Contraseña';
    btnCancelarClave.classList.toggle('hidden', obligatorio);
    modalClave.classList.remove('hidden');
}

document.getElementById('btnAbrirClave').onclick = () => abrirModalClave(false);
btnCancelarClave.onclick = () => modalClave.classList.add('hidden');

document.getElementById('formClave').onsubmit = async (e) => {
    e.preventDefault();
    const errDiv = document.getElementById('claveError');
    errDiv.classList.add('hidden');
    try {
        await api('/api/auth/cambiar-clave', {
            method: 'POST',
            body: {
                actual: document.getElementById('claveActual').value,
                nueva: document.getElementById('claveNueva').value
            }
        });
        sesion.requiereCambioClave = false;
        modalClave.classList.add('hidden');
        mostrarAlerta('Contraseña actualizada correctamente.');
        await cargarAlertasStock();
        await seleccionarModulo(moduloActual);
    } catch (err) {
        errDiv.textContent = err.message;
        errDiv.classList.remove('hidden');
    }
};

document.getElementById('btnLogout').onclick = async () => {
    try { await api('/api/auth/logout', { method: 'POST' }); } catch { /* ignore */ }
    window.location.href = '/login';
};

renderSidebar();
if (sesion.requiereCambioClave) {
    abrirModalClave(true);
}
seleccionarModulo('INICIO');
