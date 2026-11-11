# Verificación de Requisitos No Funcionales y Pruebas Automatizadas — SIGOC Web

## 1. Entorno de Ejecución Verificado

- **Lenguaje / Runtime:** Java 21 (`--release 21`), Spring Boot `4.1.2`, Maven Wrapper (`mvnw.cmd`).
- **Base de Datos:** MySQL `8.0.46` (`sigoc_db`, 22 tablas mapeadas — 17 base + 5 módulo pagos/cambio — con `spring.jpa.hibernate.ddl-auto=none`).
- **Zona Horaria:** `America/Lima` centralizada mediante bean `Clock`.
- **Variables Externalizadas:** `.env` / `.env.example` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PAYPAL_BASE_URL`, `PAYPAL_CLIENT_ID`, `PAYPAL_CLIENT_SECRET`, `PAGO_ENLACE_HORAS`).

## 2. Suite de Pruebas Automatizadas (`mvnw.cmd clean test`)

Cobertura unitaria y de arranque de contexto completo Spring Boot + MySQL ejecutada después de cada iteración:
- `AvancesTest`: avance físico acumulado ponderado e inmutabilidad en obras cerradas.
- `ComprasConsumosTest`: compras unitarias y multi-ítem con detección de materiales duplicados, actualización de stock y egreso automático.
- `FacturacionPagosTest`: cálculo automático de valorizaciones sin solapamientos, emisión de comprobante interno `CV01`, PDF OpenPDF, validez 72h, rechazo simulado de tarjeta (`PagoRechazadoException`), captura PayPal Sandbox idempotente, integridad de monto (`C02`) y tipo de cambio `CONTADOR`.
- `FinanzasPlanillaTest`: ingresos en rango, previsualización y cierre de planillas (`ADMIN`/`CONTADOR`), inclusión de retirados con asistencia (`C17`).
- `MaterialesProveedoresTest`, `ObrasPartidasTest` (incl. `C09`, `C10`, `C15`), `PersonalAsistenciaProduccionTest` (incl. `RegistrarAsistenciaLote`), `ReportesTest` (5 reportes incl. `RF-22`, exportación CSV/XLSX/PDF OpenPDF sin truncamiento), `SeguridadUsuariosTest`, `PoliticaAccesoTest`, `Pbkdf2PasswordHasherAdapterTest`, `WebViewControllerTest` y `SigocWebApplicationTests`.
