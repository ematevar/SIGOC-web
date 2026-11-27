// Script de prueba de carga k6 para RNF-04 (SIGOC Web)
// Uso: k6 run -e BASE_URL=https://sigoc.ematevar.com perf/k6-reportes.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const resHealth = http.get(`${BASE}/actuator/health`);
    check(resHealth, { 'health 200': (r) => r.status === 200 });

    const resCaptcha = http.get(`${BASE}/api/auth/captcha`);
    check(resCaptcha, {
        'captcha 200': (r) => r.status === 200,
        'captcha sin texto plano': (r) => r.body.includes('data:image/png;base64,'),
    });

    sleep(0.5);
}
