// SIGOC Web - Enrutador principal, Dashboard y Usuarios

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
    if (!['GERENTE', 'ALMACENERO', 'RESIDENTE'].includes(sesion.rol) || sesion.requiereCambioClave) {
        stockAlertBanner.classList.add('hidden');
        return [];
    }
    try {
        const alertas = await api('/api/materiales/alertas');
        if (alertas && alertas.length > 0) {
            const lista = alertas.map(a => `${a.nombre} (${a.stockActual} / min ${a.stockMinimo} ${a.unidadMedida})`).join(' · ');
            stockAlertBanner.replaceChildren();
            const strong = document.createElement('strong');
            strong.textContent = 'Alerta de Stock Mínimo (RF-06): ';
            const span = document.createElement('span');
            span.textContent = lista;
            stockAlertBanner.append(strong, span);
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
            case 'AUDITORIA': await renderAuditoria(); break;
            case 'USUARIOS': await renderUsuarios(); break;
            default: moduleContainer.innerHTML = '<div class="card">Módulo no disponible.</div>';
        }
    } catch (e) {
        mostrarAlerta(e.message, 'error');
        moduleContainer.replaceChildren();
        const errCard = document.createElement('div');
        errCard.className = 'card';
        errCard.textContent = e.message || 'Error al cargar el módulo.';
        moduleContainer.appendChild(errCard);
    }
}

async function renderInicio() {
    const obras = await api('/api/obras').catch(() => []);
    const alertas = await cargarAlertasStock();
    const permitidos = MODULOS.filter(m => m.id !== 'INICIO' && m.roles.includes(sesion.rol));

    moduleContainer.replaceChildren();

    const gridKpis = document.createElement('div');
    gridKpis.className = 'kpi-grid';

    const kpi1 = document.createElement('div');
    kpi1.className = 'kpi-card';
    const lbl1 = document.createElement('div');
    lbl1.className = 'kpi-label';
    lbl1.textContent = 'Obras activas';
    const val1 = document.createElement('div');
    val1.className = 'kpi-value';
    val1.textContent = String(obras.length);
    kpi1.append(lbl1, val1);

    const kpi2 = document.createElement('div');
    kpi2.className = 'kpi-card';
    const lbl2 = document.createElement('div');
    lbl2.className = 'kpi-label';
    lbl2.textContent = 'Perfil activo';
    const val2 = document.createElement('div');
    val2.className = 'kpi-value';
    val2.style.fontSize = '20px';
    val2.textContent = String(sesion.rolEtiqueta || '');
    kpi2.append(lbl2, val2);

    const kpi3 = document.createElement('div');
    kpi3.className = 'kpi-card';
    const lbl3 = document.createElement('div');
    lbl3.className = 'kpi-label';
    lbl3.textContent = 'Alertas stock';
    const val3 = document.createElement('div');
    val3.className = 'kpi-value';
    val3.textContent = String(alertas.length);
    kpi3.append(lbl3, val3);

    gridKpis.append(kpi1, kpi2, kpi3);

    const cardAccesos = document.createElement('div');
    cardAccesos.className = 'card';
    const h3 = document.createElement('h3');
    h3.textContent = 'Accesos directos';
    cardAccesos.appendChild(h3);

    const gridModulos = document.createElement('div');
    gridModulos.className = 'kpi-grid';

    permitidos.forEach(m => {
        const item = document.createElement('div');
        item.className = 'kpi-card';
        item.style.cursor = 'pointer';
        item.onclick = () => { seleccionarModulo(m.id).catch(console.error); };

        const lbl = document.createElement('div');
        lbl.className = 'kpi-label';
        lbl.textContent = 'Módulo';

        const tit = document.createElement('div');
        tit.style.cssText = 'font-size:17px;font-weight:700;color:var(--navy);margin-top:6px;';
        tit.textContent = m.label;

        const link = document.createElement('div');
        link.style.cssText = 'font-size:12px;color:var(--blue);margin-top:8px;';
        link.textContent = 'Abrir módulo →';

        item.append(lbl, tit, link);
        gridModulos.appendChild(item);
    });

    cardAccesos.appendChild(gridModulos);
    moduleContainer.append(gridKpis, cardAccesos);
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
seleccionarModulo('INICIO').catch(console.error);
