/**
 * CU-25: Vista de solo lectura de la bitácora de auditoría inmutable (exclusiva de GERENTE).
 */
async function renderAuditoria() {
    moduleContainer.innerHTML = `
        <div class="card">
            <h3 style="margin-top:0;">Bitácora de Auditoría Inmutable (CU-25)</h3>
            <p style="font-size:13px;color:var(--muted);margin-top:0;">
                Registro de solo lectura conforme a ISO/IEC 27001 A.8.15. Ningún perfil puede modificar ni eliminar eventos.
            </p>
            <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:end;margin-bottom:14px;">
                <div class="form-group" style="margin:0;min-width:220px;">
                    <label for="audFiltroAccion">Acción</label>
                    <select id="audFiltroAccion">
                        <option value="">Todas las acciones</option>
                        <option value="LOGIN_OK">LOGIN_OK</option>
                        <option value="LOGIN_FALLIDO">LOGIN_FALLIDO</option>
                        <option value="LOGIN_BLOQUEADO">LOGIN_BLOQUEADO</option>
                        <option value="ACCESO_DENEGADO_403">ACCESO_DENEGADO_403</option>
                        <option value="CREAR_USUARIO">CREAR_USUARIO</option>
                        <option value="ACTUALIZAR_USUARIO">ACTUALIZAR_USUARIO</option>
                        <option value="CAMBIAR_ESTADO_USUARIO">CAMBIAR_ESTADO_USUARIO</option>
                        <option value="CIERRE_OBRA">CIERRE_OBRA</option>
                        <option value="CIERRE_PLANILLA">CIERRE_PLANILLA</option>
                        <option value="CAMBIO_TIPO_CAMBIO">CAMBIO_TIPO_CAMBIO</option>
                        <option value="PAGO_EXTERNO_APROBADO">PAGO_EXTERNO_APROBADO</option>
                        <option value="PAGO_TARJETA_APROBADO">PAGO_TARJETA_APROBADO</option>
                    </select>
                </div>
                <div class="form-group" style="margin:0;width:120px;">
                    <label for="audLimite">Límite</label>
                    <input id="audLimite" type="number" min="10" max="500" value="100">
                </div>
                <button type="button" id="btnFiltrarAuditoria" class="btn btn-primary">Consultar</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Fecha / Hora</th>
                            <th>Usuario</th>
                            <th>Rol</th>
                            <th>Acción</th>
                            <th>Entidad</th>
                            <th>Resultado</th>
                            <th>Detalle</th>
                        </tr>
                    </thead>
                    <tbody id="tbodyAuditoria"></tbody>
                </table>
            </div>
        </div>
    `;

    const cargar = async () => {
        const accion = document.getElementById('audFiltroAccion').value;
        const limite = Number(document.getElementById('audLimite').value) || 100;
        const qs = new URLSearchParams({ limite: String(limite) });
        if (accion) qs.set('accion', accion);
        const eventos = await api(`/api/auditoria?${qs.toString()}`);
        const tbody = document.getElementById('tbodyAuditoria');
        if (!eventos || eventos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7">Sin eventos registrados para el filtro seleccionado.</td></tr>';
            return;
        }
        tbody.innerHTML = eventos.map(e => `
            <tr>
                <td>${esc(String(e.fechaHora || '').replace('T', ' '))}</td>
                <td>${esc(e.usuarioLogin)}</td>
                <td>${esc(e.rol)}</td>
                <td><strong>${esc(e.accion)}</strong></td>
                <td>${esc(e.entidad)}${e.idEntidad ? ' #' + esc(e.idEntidad) : ''}</td>
                <td>${esc(e.resultado)}</td>
                <td>${esc(e.detalle)}</td>
            </tr>
        `).join('');
    };

    document.getElementById('btnFiltrarAuditoria').addEventListener('click', () => {
        cargar().catch(err => mostrarAlerta(err.message, 'error'));
    });
    await cargar();
}
