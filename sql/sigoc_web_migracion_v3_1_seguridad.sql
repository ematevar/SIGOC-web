-- =====================================================================
-- SIGOC Web v3.1 - Migración de Seguridad Operativa
-- Tabla intento_login para persistir bloqueos por fuerza bruta en BD
-- =====================================================================

CREATE TABLE IF NOT EXISTS intento_login (
    clave_login VARCHAR(60) NOT NULL,
    fallos INT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    actualizado_el DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (clave_login)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
