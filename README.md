# SIGOC Web — Sistema de Gestión y Control de Obras Civiles

Migración integral de `sigoc-desktop` (Java Swing / JDBC) hacia una aplicación web en **Spring Boot 4.1.2 (Java 21)** implementada bajo **Arquitectura Hexagonal** (Puertos y Adaptadores).

## Arquitectura Hexagonal

```text
com.sigoc.sigoc_web
├── domain
│   ├── models          # Entidades y VOs puros de dominio (sin Spring/JPA)
│   ├── exceptions      # ReglaNegocioException, AccesoDenegadoException, RecursoNoEncontradoException
│   ├── services        # Reglas puras (PoliticaAcceso, CaptchaService)
│   └── ports.out       # Puertos de salida (persistencia, hashing, bloqueo, reportes, exportación)
├── aplication.usecases # Casos de uso puros desacoplados del framework
└── infraestructure
    ├── entities        # Entidades JPA (@Entity) mapeadas al esquema sigoc_db
    ├── repositories    # Repositorios Spring Data JPA
    ├── adapters        # Adaptadores JPA / JDBC / PBKDF2 / CSV-XLSX-PDF
    ├── controllers     # API REST (/api/**), DTOs, GlobalExceptionHandler y vistas Thymeleaf
    ├── security        # Sesiones HTTP, AutenticacionInterceptor y UsuarioSesionArgumentResolver
    └── config          # CasosUsoConfig y WebConfig
```

## Módulos y Requisitos Migrados (SRS V4)

| Módulo | Requisitos | Casos de Uso / Endpoints |
|---|---|---|
| Seguridad y Usuarios | RF-01, RF-02 | Login con CAPTCHA de un solo uso, PBKDF2-SHA256 (`PBKDF2$120000$...`), bloqueo tras 5 intentos fallidos, cambio obligatorio de clave inicial, CRUD usuarios y roles (`/api/auth`, `/api/usuarios`, `/api/roles`) |
| Obras y Partidas | RF-03, RF-04 | CRUD Obras con cierre inmutable, Partidas por obra y asignación de materiales presupuestados (`/api/obras`, `/api/partidas`) |
| Avances Físicos | RF-05 | Registro de avances por partida y cálculo ponderado acumulado de obra (`/api/avances`) |
| Materiales y Proveedores | RF-06, RF-09 | Catálogo de materiales con alertas de stock mínimo (`stock_actual <= stock_minimo`), gestión de proveedores con RUC (`/api/materiales`, `/api/proveedores`) |
| Compras y Consumos | RF-07, RF-08 | Compras con incremento de stock y egreso automático tipo `Material`; consumos con validación de stock y alerta (`/api/compras`, `/api/consumos`) |
| Personal, Asistencia y Producción | RF-10, RF-11, RF-12 | Gestión de trabajadores, asistencia diaria con bloqueo en periodos cerrados y registro de producción (`/api/empleados`, `/api/asistencias`, `/api/produccion`) |
| Finanzas y Planillas | RF-13, RF-14, RF-15 | Ingresos validados dentro del cronograma planificado de la obra, cierre inmutable de planilla sin solapamientos con generación de egresos `Planilla`, y egresos `Otro` (`/api/finanzas/**`) |
| Clientes, Valorizaciones y Pagos | Módulo Financiero Web | Gestión de clientes contratantes con validación DNI/RUC SUNAT, valorizaciones periódicas por obra, emisión de comprobantes con cálculo de IGV (18%) y pasarela de pagos sandbox con ingreso automático (`/api/clientes`, `/api/valorizaciones`, `/api/pagos`) |
| Reportes Gerenciales | RF-16 | Curva S (planificado vs. ejecutado), consumo real vs. presupuestado, asistencia/productividad, estado financiero y exportación nativa **PDF 1.4**, **Excel (.xlsx)** y **CSV UTF-8** (`/api/reportes`, `/api/reportes/exportar`) |
| Interfaz Web | RNF-05, RNF-07 | Panel web Thymeleaf + JavaScript (`/login`, `/`) con control de acceso por perfil (`ADMIN`, `RESIDENTE`, `ALMACENERO`), alertas de stock y gráfico SVG Curva S |

## Base de Datos (MySQL 8)

Los scripts originales del esquema, datos iniciales y datos de prueba se encuentran en `sql/`:
- `sql/sigoc_esquema.sql`
- `sql/sigoc_datos_iniciales.sql`
- `sql/sigoc_datos_prueba.sql`
- `sql/sigoc_web_modulo_pagos.sql` (tablas `cliente`, `valorizacion`, `comprobante`, `pago`)

La configuración de conexión JDBC/JPA está en `src/main/resources/application.properties` (`spring.jpa.hibernate.ddl-auto=none`).

## Ejecución y Pruebas

```powershell
# Ejecutar toda la suite de pruebas (unitarias y carga de contexto Spring + MySQL)
.\mvnw.cmd test

# Levantar el servidor web en http://localhost:8080
.\mvnw.cmd spring-boot:run
```
