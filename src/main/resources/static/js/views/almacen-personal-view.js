// SIGOC Web - Almacen y Personal

async function renderMateriales() {
    const materiales = await api('/api/materiales');
    const esAdmin = sesion.rol === 'ALMACENERO';
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
    const puedeEscribir = sesion.rol === 'GERENTE';
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
    const puedeEscribir = sesion.rol === 'ALMACENERO';
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
    const esAdmin = sesion.rol === 'GERENTE' || sesion.rol === 'RESIDENTE';
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

