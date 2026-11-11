# Especificación de Requisitos de Software — SIGOC Web y Decisiones de Arquitectura (SAD)

> **Propósito:** Documentar las precisiones funcionales, reglas de negocio (`RN-01`..`RN-28`), estructura de base de datos (**22 tablas**: 17 base + 5 módulo pagos/cambio) y decisiones de arquitectura hexagonal implementadas en **SIGOC Web** (`Spring Boot 4.1.2`, `Java 21`, `MySQL 8`, `Thymeleaf`).

---

## 1. Arquitectura y Naturaleza de la Aplicación

- **Naturaleza:** Aplicación web corporativa bajo **Arquitectura Hexagonal** (`domain`, `application`, `infrastructure`), desplegable en nube y accesible mediante navegador moderno (y portal público `/pago/{codigoPago}` sin login interno).
- **Puertos de Entrada y Salida:**
  - `domain/ports/in`: `*UseCase` (`AutenticarUsuarioUseCase`, `GestionarObraUseCase`, `RegistrarAvanceUseCase`, `RegistrarCompraUseCase`, `RegistrarConsumoUseCase`, `RegistrarAsistenciaUseCase`, `CerrarPlanillaUseCase`, `GestionarValorizacionPagoUseCase`, `GenerarReporteUseCase`).
  - `domain/ports/out`: repositorios de dominio, `PasswordHasherPort` (PBKDF2-SHA256 120 000 iteraciones), `PasarelaPagoPort` (PayPal Sandbox + tarjeta simulada), `TipoCambioPort`, `ExportadorComprobantePdfPort`, `ReporteQueryPort` y `ExportadorReportePort`.
- **Seguridad Web:** Sesiones HTTP de 30 minutos con revalidación continua contra `usuario`/`rol` en BD (`AutenticacionInterceptor`), protección CSRF (`X-CSRF-TOKEN`), auditoría automática `@PrePersist` (`id_usuario_registro`, `fecha_registro` en `America/Lima`) y bloqueo pesimista transaccional (`PESSIMISTIC_WRITE`) en stock, obras, planillas y valorizaciones/pagos.

---

## 2. Perfiles de Usuario y Matriz de Permisos (4 Roles)

El sistema contempla **cuatro (4) perfiles de usuario**:

| Módulo / Caso de Uso | Requisito | `ADMIN` | `RESIDENTE` | `ALMACENERO` | `CONTADOR` |
|---|---|:---:|:---:|:---:|:---:|
| Iniciar / Cerrar Sesión y Cambiar Clave | RF-01 | ✔ | ✔ | ✔ | ✔ |
| Gestionar Usuarios y Roles | RF-02 | ✔ | — | — | — |
| Gestionar Clientes / Contratantes | Módulo Web | ✔ | Lectura | — | Lectura |
| Gestionar Obras (Crear, Editar, Cerrar) | RF-03 | ✔ | Lectura | Lectura | Lectura |
| Gestionar Partidas de Obra | RF-04 | ✔ | ✔ | — | Lectura |
| Registrar Avance Físico de Partidas | RF-05 | Lectura | ✔ | — | Lectura |
| Gestionar Catálogo de Materiales | RF-06 | ✔ | — | ✔ | Lectura |
| Registrar Compras Multi-ítem | RF-07 | ✔ | — | ✔ | Lectura |
| Registrar Consumo de Materiales | RF-08 | Lectura | ✔ | Lectura | Lectura |
| Gestionar Proveedores | RF-09 | ✔ | — | ✔ | Lectura |
| Gestionar Trabajadores y Producción | RF-10, RF-12 | ✔ / Lectura | ✔ | — | Lectura |
| Registrar Asistencia (Individual / Lote) | RF-11 | Lectura | ✔ | — | Lectura |
| Ingresos, Egresos y Planillas (Previsualización + Cierre) | RF-13..15 | ✔ | — | — | ✔ |
| Valorizaciones Automáticas por Periodo | RF-18 | ✔ | ✔ | — | ✔ |
| Comprobante CV01 PDF, Enlace 72h y Pagos / Tipo Cambio | RF-19..21 | ✔ | Lectura Val/CV01 | — | ✔ |
| Reportes Gerenciales (5 reportes + SVG + PDF/XLSX/CSV) | RF-16, RF-22 | ✔ | — | — | ✔ |

---

## 3. Reglas Críticas Implementadas

1. **Curva S Ponderada Real (`RN` / `C07`):** El avance físico acumulado ponderado divide entre `100.0` (total contractual), arrastrando el último avance de cada partida en fechas de corte cronológicas.
2. **Consumo Real vs. Presupuestado (`C08`):** Únicamente considera cantidades presupuestadas en `partida_material`; cuando no existen asignaciones registradas muestra `"Sin presupuesto"` sin sustituir con compras históricas.
3. **Valorizaciones e Integridad de Cobro (`RF-18..RF-21`, `C02..C05`):**
   - El servidor calcula automáticamente el delta físico en un rango sin solapamientos (`RN-20`), determina `subtotal`, `IGV 18%` y `total` respetando el presupuesto contractual (`RN-22`), emite comprobante interno correlativo `CV01-XXXXXX` (`Sin valor tributario SUNAT`) con enlace público vigente por 72 horas (`/pago/{codigoPago}`), valida importes y unicidad (`id_captura_externa`, `id_pago`) en el servidor, y genera automáticamente el `Ingreso` vinculado 1:1.
4. **Reportes Gerenciales (`RF-16`, `RF-22`, `C19`, `C20`):** Incluye 5 reportes (`Curva S`, `Consumo de Materiales`, `Personal y Productividad`, `Financiero por Obra`, `Valorizaciones y Cobranzas`), visualizaciones SVG en los 5 reportes y exportación completa mediante OpenPDF (`%PDF-1.4`) y Excel OpenXML (`.xlsx`) sin truncamientos.
