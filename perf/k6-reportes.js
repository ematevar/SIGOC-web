// Script de prueba de carga k6 para RNF-04 (SIGOC Web - Reportes Gerenciales)
// Uso: k6 run -e BASE_URL=https://sigoc.ematevar.com -e JSESSIONID=<cookie_sesion_gerente> perf/k6-reportes.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
    thresholds: {
        'http_req_duration{tipo:consulta}': ['p(95)<2000'],
        'http_req_duration{tipo:exportacion}': ['p(95)<3000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const JSESSIONID = __ENV.JSESSIONID || '';

export default function () {
    const paramsConsulta = {
        headers: { Cookie: `JSESSIONID=${JSESSIONID}` },
        tags: { tipo: 'consulta' },
    };
    const paramsExport = {
        headers: { Cookie: `JSESSIONID=${JSESSIONID}` },
        tags: { tipo: 'exportacion' },
    };

    for (const tipo of ['CURVA_S', 'MATERIALES', 'PERSONAL', 'FINANCIERO', 'VALORIZACIONES']) {
        const res = http.get(`${BASE}/api/reportes?tipo=${tipo}`, paramsConsulta);
        check(res, { [`reporte ${tipo} 200`]: (r) => r.status === 200 });
    }

    const resPdf = http.get(`${BASE}/api/reportes/exportar?tipo=FINANCIERO&formato=PDF`, paramsExport);
    check(resPdf, { 'export PDF 200': (r) => r.status === 200 && r.body.length > 500 });

    const resXlsx = http.get(`${BASE}/api/reportes/exportar?tipo=FINANCIERO&formato=XLSX`, paramsExport);
    check(resXlsx, { 'export XLSX 200': (r) => r.status === 200 && r.body.length > 500 });

    sleep(0.5);
}
