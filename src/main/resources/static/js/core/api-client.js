const sesion = {
    username: document.body.dataset.username || '',
    nombre: document.body.dataset.nombre || '',
    rol: document.body.dataset.rol || '',
    rolEtiqueta: document.body.dataset.rolEtiqueta || '',
    requiereCambioClave: document.body.dataset.cambioClave === 'true',
    csrfToken: document.body.dataset.csrf || ''
};

// Matriz de visibilidad alineada con ISO/IEC 27001 A.5.3 (5 perfiles)
const MODULOS = [
    { id: 'INICIO', label: 'Panel Principal', roles: ['ADMIN', 'GERENTE', 'RESIDENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'OBRAS', label: 'Obras', roles: ['GERENTE', 'RESIDENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'PARTIDAS', label: 'Partidas y Presupuesto', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'AVANCES', label: 'Avances Físicos', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'MATERIALES', label: 'Materiales y Stock', roles: ['GERENTE', 'ALMACENERO', 'RESIDENTE', 'CONTADOR'] },
    { id: 'PROVEEDORES', label: 'Proveedores', roles: ['GERENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'COMPRAS', label: 'Compras de Materiales', roles: ['GERENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'CONSUMOS', label: 'Consumo de Materiales', roles: ['GERENTE', 'RESIDENTE', 'ALMACENERO', 'CONTADOR'] },
    { id: 'PERSONAL', label: 'Personal', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'ASISTENCIA', label: 'Asistencia', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'PRODUCCION', label: 'Producción Diaria', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'FINANZAS', label: 'Finanzas y Planillas', roles: ['GERENTE', 'CONTADOR'] },
    { id: 'CLIENTES', label: 'Clientes', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'VALORIZACIONES', label: 'Valorizaciones y Facturas', roles: ['GERENTE', 'RESIDENTE', 'CONTADOR'] },
    { id: 'PAGOS', label: 'Pasarela de Pagos', roles: ['GERENTE', 'CONTADOR'] },
    { id: 'REPORTES', label: 'Reportes Gerenciales', roles: ['GERENTE', 'CONTADOR'] },
    { id: 'AUDITORIA', label: 'Bitácora de Auditoría', roles: ['GERENTE'] },
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
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
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
