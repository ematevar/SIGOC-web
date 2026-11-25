-- =====================================================================
-- SIGOC Web - Migración v3: Segregación de Funciones ISO/IEC 27001 A.5.3
-- Incorpora rol GERENTE y tabla inmutable de bitácora auditoria_evento
-- =====================================================================

INSERT IGNORE INTO rol (nombre_rol, activo) VALUES ('GERENTE', 1);

CREATE TABLE IF NOT EXISTS auditoria_evento (
    id_evento      BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora     DATETIME NOT NULL,
    id_usuario     INT NULL,
    usuario_login  VARCHAR(60) NULL,
    rol            VARCHAR(40) NULL,
    accion         VARCHAR(60) NOT NULL,
    entidad        VARCHAR(60) NULL,
    id_entidad     VARCHAR(60) NULL,
    resultado      VARCHAR(20) NOT NULL,
    detalle        VARCHAR(500) NULL,
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_accion (accion),
    INDEX idx_auditoria_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
