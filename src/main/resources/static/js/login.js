const captchaDisplay = document.getElementById('captchaDisplay');
const captchaInput = document.getElementById('captchaInput');
const btnRefresh = document.getElementById('btnRefreshCaptcha');
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');

async function cargarCaptcha() {
    captchaDisplay.textContent = '...';
    try {
        const res = await fetch('/api/auth/captcha', { credentials: 'same-origin' });
        if (!res.ok) {
            throw new Error('HTTP ' + res.status);
        }
        const data = await res.json();
        if (data.imagenDataUri) {
            captchaDisplay.innerHTML = `<img src="${data.imagenDataUri}" alt="Código CAPTCHA" style="height:46px;width:auto;border-radius:6px;display:block;">`;
        } else {
            captchaDisplay.textContent = 'ERR';
        }
        captchaInput.value = '';
    } catch {
        captchaDisplay.textContent = 'ERR';
    }
}

btnRefresh.addEventListener('click', cargarCaptcha);

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginError.classList.add('hidden');
    const usuario = document.getElementById('username').value.trim();
    const clave = document.getElementById('password').value;
    const captcha = captchaInput.value.trim();

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin',
            body: JSON.stringify({ usuario, clave, captcha })
        });
        if (!res.ok) {
            let msg = 'No se pudo iniciar sesión.';
            try {
                const err = await res.json();
                if (err.mensaje) msg = err.mensaje;
            } catch { /* ignore */ }
            loginError.textContent = msg;
            loginError.classList.remove('hidden');
            await cargarCaptcha();
            return;
        }
        window.location.href = '/';
    } catch {
        loginError.textContent = 'Error de conexión con el servidor.';
        loginError.classList.remove('hidden');
    }
});

cargarCaptcha().catch(console.error);
