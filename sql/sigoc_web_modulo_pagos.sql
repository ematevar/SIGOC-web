-- ============================================================
-- SIGOC Web (Unidad II) — Módulo Clientes, Valorizaciones, Comprobantes y Pagos
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
    fecha_borrado DATETIME NULL,
    id_usuario_registro INT NULL,
    fecha_registro DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS valorizacion (
    id_valorizacion INT AUTO_INCREMENT PRIMARY KEY,
    id_obra INT NOT NULL,
    numero INT NOT NULL DEFAULT 1,
    periodo VARCHAR(30) NOT NULL,
    periodo_inicio DATE NULL,
    periodo_fin DATE NULL,
    fecha_emision DATE NOT NULL,
    porcentaje_avance_periodo DECIMAL(5,2) NOT NULL,
    avance_acumulado DECIMAL(5,2) NOT NULL DEFAULT 0,
    monto_valorizado DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    igv DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(25) NOT NULL DEFAULT 'EMITIDA',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    id_usuario_registro INT NULL,
    fecha_registro DATETIME NULL,
    CONSTRAINT fk_valorizacion_obra FOREIGN KEY (id_obra) REFERENCES obra(id_obra)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS comprobante (
    id_comprobante INT AUTO_INCREMENT PRIMARY KEY,
    id_valorizacion INT NOT NULL,
    tipo_comprobante VARCHAR(30) NOT NULL DEFAULT 'COMPROBANTE_INTERNO',
    serie VARCHAR(10) NOT NULL DEFAULT 'CV01',
    numero INT NOT NULL DEFAULT 1,
    serie_numero VARCHAR(25) NOT NULL UNIQUE,
    codigo_pago VARCHAR(64) NULL UNIQUE,
    vence_el DATETIME NULL,
    fecha_emision DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    igv DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'EMITIDO',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    id_usuario_registro INT NULL,
    fecha_registro DATETIME NULL,
    UNIQUE KEY uq_comprobante_valorizacion (id_valorizacion),
    CONSTRAINT fk_comprobante_valorizacion FOREIGN KEY (id_valorizacion) REFERENCES valorizacion(id_valorizacion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_obra INT NOT NULL,
    id_valorizacion INT NULL,
    id_comprobante INT NULL,
    id_ingreso_generado INT NULL,
    pasarela VARCHAR(30) NOT NULL DEFAULT 'PASARELA_SIMULADA',
    id_orden_externa VARCHAR(80) NULL,
    id_captura_externa VARCHAR(80) NULL UNIQUE,
    monto DECIMAL(12,2) NOT NULL,
    monto_cobro DECIMAL(12,2) NULL,
    moneda_cobro VARCHAR(3) NOT NULL DEFAULT 'PEN',
    tipo_cambio DECIMAL(8,4) NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    marca_tarjeta VARCHAR(20) NULL,
    ultimos4 VARCHAR(4) NULL,
    codigo_operacion VARCHAR(50) NOT NULL UNIQUE,
    fecha_pago DATETIME NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE',
    motivo VARCHAR(255) NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_borrado DATETIME NULL,
    id_usuario_registro INT NULL,
    fecha_registro DATETIME NULL,
    CONSTRAINT fk_pago_obra FOREIGN KEY (id_obra) REFERENCES obra(id_obra),
    CONSTRAINT fk_pago_valorizacion FOREIGN KEY (id_valorizacion) REFERENCES valorizacion(id_valorizacion),
    CONSTRAINT fk_pago_comprobante FOREIGN KEY (id_comprobante) REFERENCES comprobante(id_comprobante),
    CONSTRAINT fk_pago_ingreso FOREIGN KEY (id_ingreso_generado) REFERENCES ingreso(id_ingreso)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tipo_cambio (
    id_tipo_cambio INT AUTO_INCREMENT PRIMARY KEY,
    moneda_origen VARCHAR(3) NOT NULL,
    moneda_destino VARCHAR(3) NOT NULL,
    valor DECIMAL(8,4) NOT NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_registro INT NULL,
    UNIQUE KEY uq_moneda_par (moneda_origen, moneda_destino)
) ENGINE=InnoDB;

INSERT INTO tipo_cambio(moneda_origen, moneda_destino, valor)
SELECT 'USD', 'PEN', 3.7500
WHERE NOT EXISTS (SELECT 1 FROM tipo_cambio WHERE moneda_origen='USD' AND moneda_destino='PEN');
