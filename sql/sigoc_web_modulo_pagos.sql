-- ============================================================
-- SIGOC Web (Unidad II) — Nuevas tablas del Flujo Financiero y Pagos
-- ============================================================
USE sigoc_db;

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(11) NOT NULL UNIQUE,
    nombre_razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    correo VARCHAR(120) NULL,
    direccion VARCHAR(200) NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS valorizacion (
    id_valorizacion INT AUTO_INCREMENT PRIMARY KEY,
    id_obra INT NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    fecha_emision DATE NOT NULL,
    porcentaje_avance_periodo DECIMAL(5,2) NOT NULL,
    monto_valorizado DECIMAL(12,2) NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    CONSTRAINT fk_valorizacion_obra FOREIGN KEY (id_obra) REFERENCES obra(id_obra)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS comprobante (
    id_comprobante INT AUTO_INCREMENT PRIMARY KEY,
    id_valorizacion INT NOT NULL,
    tipo_comprobante VARCHAR(30) NOT NULL DEFAULT 'FACTURA_PRUEBA',
    serie_numero VARCHAR(25) NOT NULL UNIQUE,
    fecha_emision DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    igv DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'EMITIDO',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    CONSTRAINT fk_comprobante_valorizacion FOREIGN KEY (id_valorizacion) REFERENCES valorizacion(id_valorizacion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_obra INT NOT NULL,
    id_valorizacion INT NULL,
    id_comprobante INT NULL,
    id_ingreso_generado INT NULL,
    monto DECIMAL(12,2) NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    codigo_operacion VARCHAR(50) NOT NULL UNIQUE,
    fecha_pago DATETIME NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'APROBADO',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    CONSTRAINT fk_pago_obra FOREIGN KEY (id_obra) REFERENCES obra(id_obra),
    CONSTRAINT fk_pago_valorizacion FOREIGN KEY (id_valorizacion) REFERENCES valorizacion(id_valorizacion),
    CONSTRAINT fk_pago_comprobante FOREIGN KEY (id_comprobante) REFERENCES comprobante(id_comprobante),
    CONSTRAINT fk_pago_ingreso FOREIGN KEY (id_ingreso_generado) REFERENCES ingreso(id_ingreso)
) ENGINE=InnoDB;
