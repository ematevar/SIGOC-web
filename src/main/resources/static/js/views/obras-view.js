// SIGOC Web - Obras, Partidas, Avances y Clientes

async function renderObras() {
    const [obras, clientes] = await Promise.all([
        api('/api/obras'),
        api('/api/clientes').catch(() => [])
    ]);
    const puedeEscribir = sesion.rol === 'GERENTE';
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
    const esAdmin = sesion.rol === 'GERENTE' || sesion.rol === 'RESIDENTE';
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


function aplicarSoloDigitos(el) {
    el.addEventListener('input', () => {
        el.value = el.value.replace(/[^0-9]/g, '');
    });
}

async function renderClientes() {
    const clientes = await api('/api/clientes');
    const esAdmin = sesion.rol === 'GERENTE';
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

