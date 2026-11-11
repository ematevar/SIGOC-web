# SIGOC Web — Sistema de Gestión y Control de Obras Civiles

Aplicación web en **Spring Boot 4.1.2 (Java 21)** implementada bajo **Arquitectura Hexagonal** (Puertos y Adaptadores) para gestión integral de obras civiles, recursos, planillas, valorizaciones, comprobantes internos y cobranza en línea.

## Arquitectura Hexagonal

```text
com.sigoc.sigoc_web
├── domain
│   ├── models          # Entidades de dominio puras (Obra, Partida, Material, Valorizacion, Comprobante, Pago, TipoCambio...)
│   ├── exceptions      # ReglaNegocioException, AccesoDenegadoException, RecursoNoEncontradoException, PagoRechazadoException
│   ├── services        # Reglas puras (PoliticaAcceso, CaptchaService)
│   ├── validators      # Validadores estrictos (ValidadorSigoc)
│   └── ports
│       ├── in          # Puertos de entrada (*UseCase)
│       └── out         # Puertos de salida (repositorios, PBKDF2, pasarelas de pago, PDF, reportes)
├── application.usecases # Casos de uso puros desacoplados del framework
└── infrastructure
    ├── entities        # Entidades JPA (@Entity) con EntidadAuditable (id_usuario_registro, fecha_registro)
    ├── repositories    # Repositorios Spring Data JPA (con @Lock pesimista en stock/obra/cierre/valorización)
    ├── adapters        # JPA, JDBC Reportes, PBKDF2, OpenPDF CV01/Reportes, XLSX/CSV, PayPalSandbox y PasarelaSimulada
    ├── controllers     # REST API (/api/**), portal público (/pago/{codigoPago}, /api/public/pagos/**) y vistas Thymeleaf
    ├── security        # Sesiones HTTP 30m, revalidación BD en AutenticacionInterceptor y CSRF (X-CSRF-TOKEN)
    └── config          # CasosUsoConfig (Clock America/Lima), WebConfig y GlobalExceptionHandler
```

## Módulos y Requisitos (SRS / SAD)

| Módulo | Requisitos | Casos de Uso / Endpoints |
|---|---|---|
| Seguridad y Usuarios | RF-01, RF-02, RNF-06, RNF-07 | Login con CAPTCHA de un solo uso, PBKDF2-SHA256 (`PBKDF2$120000$...`), bloqueo tras 5 intentos fallidos, cambio obligatorio de clave inicial, revalidación de sesión contra BD + CSRF y CRUD de usuarios con roles `ADMIN`, `RESIDENTE`, `ALMACENERO`, `CONTADOR` (`/api/auth`, `/api/usuarios`, `/api/roles`) |
| Obras, Partidas y Clientes | RF-03, RF-04 | CRUD Obras con FK `id_cliente`, inmutabilidad de obras finalizadas, Partidas por obra con validación de suma de pesos ≤ 100%, bloqueo de edición de obra/unidad con movimientos y asignación de materiales presupuestados (`/api/obras`, `/api/partidas`, `/api/clientes`) |
| Avances Físicos | RF-05 | Registro de avances por partida y cálculo ponderado acumulado sobre peso presupuestal (`/api/avances`) |
| Materiales, Proveedores, Compras y Consumos | RF-06, RF-07, RF-08, RF-09 | Catálogo con alertas de stock mínimo, compras multi-ítem por comprobante con egreso automático `Material`, consumos con bloqueo pesimista de stock (`/api/materiales`, `/api/proveedores`, `/api/compras`, `/api/consumos`) |
| Personal, Asistencia y Producción | RF-10, RF-11, RF-12 | Empleados, asistencia individual y matriz en lote por obra/fecha (`/api/asistencias/lote`), producción (`/api/empleados`, `/api/asistencias`, `/api/produccion`) |
| Finanzas y Planillas | RF-13, RF-14, RF-15 | Ingresos en rango planificado, previsualización y cierre inmutable de planilla sin solapamientos ni exclusión indebida de retirados (`/api/finanzas/**`) |
| Valorizaciones, CV01 PDF, Enlace 72h y Pasarelas | RF-18..RF-21 | Valorización automática por rango de fechas y avance físico acumulado, comprobante interno `CV01-XXXXXX` PDF (`Sin valor tributario SUNAT`), enlace público 72h (`/pago/{codigoPago}`), tarjeta simulada Visa/Mastercard (Luhn + rechazo prueba `0002`), PayPal REST v2 Sandbox (`USD`) con tipo de cambio (`/api/tipo-cambio`) e ingreso automático 1:1 (`/api/valorizaciones`, `/api/pagos`, `/api/public/pagos`) |
| Reportes Gerenciales | RF-16, RF-22 | Curva S, Consumo real vs. presupuestado, Productividad personal, Financiero y Valorizaciones/Cobranzas con gráficos SVG en los 5 reportes y exportación completa **PDF 1.4 (OpenPDF)**, **Excel (.xlsx)** y **CSV UTF-8** (`/api/reportes`, `/api/reportes/exportar`) |

## Base de Datos y Variables de Entorno (MySQL 8)

Los scripts SQL del esquema y migraciones se encuentran en `sql/`:
- `sql/sigoc_esquema.sql`
- `sql/sigoc_datos_iniciales.sql`
- `sql/sigoc_web_modulo_pagos.sql`
- `sql/sigoc_web_migracion_v2.sql` (rol `CONTADOR`, `tipo_cambio`, FK `obra.id_cliente`, columnas de valorización/comprobante/pago en línea y auditoría general)

Copia `.env.example` a `.env` y define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, y opcionalmente `PAYPAL_CLIENT_ID` / `PAYPAL_CLIENT_SECRET`.

## Ejecución y Pruebas

```powershell
# Ejecutar toda la suite de pruebas (unitarias + arranque completo contra MySQL)
.\mvnw.cmd clean test

# Levantar el servidor web en http://localhost:8080
.\mvnw.cmd spring-boot:run
```
