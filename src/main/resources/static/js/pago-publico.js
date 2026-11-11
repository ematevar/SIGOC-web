(function () {
    const codigoPago = window.__CODIGO_PAGO__ || window.location.pathname.split('/').pop();
    const pubAlerta = document.getElementById('pubAlerta');
    const pubCargando = document.getElementById('pubCargando');
    const pubDetalle = document.getElementById('pubDetalle');
    const pubMetodosCard = document.getElementById('pubMetodosCard');
    const pubReciboCard = document.getElementById('pubReciboCard');
    const btnPdfPublico = document.getElementById('btnPdfPublico');

    function mostrarMensaje(texto, tipo = 'ok') {
        pubAlerta.textContent = texto;
        pubAlerta.className = 'alert ' + (tipo === 'error' ? 'alert-error' : tipo === 'warning' ? 'alert-warning' : 'alert-ok');
        pubAlerta.classList.remove('hidden');
    }

    async function fetchJson(url, opts = {}) {
        const res = await fetch(url, {
            method: opts.method || 'GET',
            headers: opts.body ? { 'Content-Type': 'application/json' } : {},
            body: opts.body ? JSON.stringify(opts.body) : undefined
        });
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
            throw new Error(data.mensaje || data.error || 'No se pudo procesar la solicitud.');
        }
        return data;
    }

    function mostrarRecibo(pago) {
        pubMetodosCard.classList.add('hidden');
        pubReciboCard.classList.remove('hidden');
        document.getElementById('pubReciboContenido').innerHTML = `
            <div class="kv-grid">
                <div class="kv-item"><small>Código de Operación</small><strong>${pago.codigoOperacion}</strong></div>
                <div class="kv-item"><small>Pasarela / Método</small><strong>${pago.pasarela} (${pago.metodoPago})</strong></div>
                <div class="kv-item"><small>Importe Aprobado</small><strong>S/ ${Number(pago.monto).toFixed(2)}</strong></div>
                <div class="kv-item"><small>Ingreso Contable</small><strong>#${pago.idIngresoGenerado || '-'}</strong></div>
            </div>
            <p>El comprobante y su valorización han quedado conciliados automáticamente en SIGOC Web.</p>`;
    }

    async function cargarDetalle() {
        try {
            const d = await fetchJson(`/api/public/pagos/${encodeURIComponent(codigoPago)}`);
            pubCargando.classList.add('hidden');
            pubDetalle.classList.remove('hidden');
            btnPdfPublico.href = `/api/public/pagos/${encodeURIComponent(codigoPago)}/pdf`;
            btnPdfPublico.classList.remove('hidden');

            document.getElementById('pubSerie').textContent = d.serieNumero;
            document.getElementById('pubEstado').textContent = d.estado;
            document.getElementById('pubObra').textContent = d.nombreObra;
            document.getElementById('pubCliente').textContent = d.clienteNombre;
            document.getElementById('pubVal').textContent = `N° ${d.numeroValorizacion} · ${d.periodoValorizacion} (${Number(d.porcentajeAvancePeriodo).toFixed(2)}%)`;
            document.getElementById('pubDesglose').textContent = `S/ ${Number(d.subtotal).toFixed(2)} + IGV S/ ${Number(d.igv).toFixed(2)}`;
            document.getElementById('pubTotalPen').textContent = `S/ ${Number(d.totalPen).toFixed(2)}`;
            document.getElementById('pubTotalUsd').textContent = `$ ${Number(d.montoUsdEstimado).toFixed(2)} USD (T.C. ${Number(d.tipoCambioUsdPen).toFixed(4)})`;
            document.getElementById('pubVence').textContent = d.venceEl ? d.venceEl.replace('T', ' ').slice(0, 16) : '72 horas';

            if (d.pagado) {
                mostrarMensaje('Este comprobante ya se encuentra PAGADO.', 'ok');
                return;
            }
            if (d.expirado) {
                mostrarMensaje('El enlace de pago ha expirado (vigencia máxima de 72 horas). Solicita su renovación al área contable.', 'error');
                return;
            }

            pubMetodosCard.classList.remove('hidden');
            if (!d.paypalHabilitado) {
                document.getElementById('paypalInfo').textContent =
                    'PayPal Sandbox requiere credenciales PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET en el servidor. Puedes usar Tarjeta Simulada para pruebas inmediatas.';
                document.getElementById('btnPagarPaypal').disabled = true;
            }

            // Captura automática al volver de PayPal Sandbox (?paypalResultado=ok&token=ORDER_ID)
            const params = new URLSearchParams(window.location.search);
            const paypalToken = params.get('token');
            const paypalResultado = params.get('paypalResultado');
            if (paypalResultado === 'cancelado') {
                mostrarMensaje('El pago en PayPal Sandbox fue cancelado por el usuario.', 'warning');
            } else if (paypalToken) {
                mostrarMensaje('Confirmando captura en PayPal Sandbox...', 'warning');
                const pago = await fetchJson(`/api/public/pagos/${encodeURIComponent(codigoPago)}/paypal/capturar`, {
                    method: 'POST',
                    body: { idOrdenExterna: paypalToken }
                });
                mostrarMensaje(`Pago en PayPal aprobado (${pago.codigoOperacion}).`, 'ok');
                mostrarRecibo(pago);
                await cargarDetalleSinLoops();
            }
        } catch (err) {
            pubCargando.classList.add('hidden');
            mostrarMensaje(err.message, 'error');
        }
    }

    async function cargarDetalleSinLoops() {
        try {
            const d = await fetchJson(`/api/public/pagos/${encodeURIComponent(codigoPago)}`);
            document.getElementById('pubEstado').textContent = d.estado;
        } catch { /* ignore */ }
    }

    document.getElementById('tabTarjeta').onclick = () => {
        document.getElementById('tabTarjeta').classList.add('active');
        document.getElementById('tabPaypal').classList.remove('active');
        document.getElementById('panelTarjeta').classList.remove('hidden');
        document.getElementById('panelPaypal').classList.add('hidden');
    };

    document.getElementById('tabPaypal').onclick = () => {
        document.getElementById('tabPaypal').classList.add('active');
        document.getElementById('tabTarjeta').classList.remove('active');
        document.getElementById('panelPaypal').classList.remove('hidden');
        document.getElementById('panelTarjeta').classList.add('hidden');
    };

    document.getElementById('formTarjetaPublica').onsubmit = async (e) => {
        e.preventDefault();
        const btn = document.getElementById('btnPagarTarjeta');
        btn.disabled = true;
        try {
            const pago = await fetchJson(`/api/public/pagos/${encodeURIComponent(codigoPago)}/simulada`, {
                method: 'POST',
                body: {
                    numeroTarjeta: document.getElementById('cardNum').value.trim(),
                    titular: document.getElementById('cardHolder').value.trim(),
                    vencimiento: document.getElementById('cardExp').value.trim(),
                    cvv: document.getElementById('cardCvv').value.trim()
                }
            });
            mostrarMensaje(`Pago aprobado (${pago.codigoOperacion}).`, 'ok');
            mostrarRecibo(pago);
            await cargarDetalleSinLoops();
        } catch (err) {
            mostrarMensaje(err.message, 'error');
        } finally {
            btn.disabled = false;
        }
    };

    document.getElementById('btnPagarPaypal').onclick = async () => {
        const btn = document.getElementById('btnPagarPaypal');
        btn.disabled = true;
        try {
            const orden = await fetchJson(`/api/public/pagos/${encodeURIComponent(codigoPago)}/paypal/orden`, {
                method: 'POST'
            });
            if (orden.approvalUrl) {
                window.location.href = orden.approvalUrl;
            } else {
                throw new Error('PayPal Sandbox no devolvió la URL de aprobación.');
            }
        } catch (err) {
            mostrarMensaje(err.message, 'error');
            btn.disabled = false;
        }
    };

    cargarDetalle();
})();
