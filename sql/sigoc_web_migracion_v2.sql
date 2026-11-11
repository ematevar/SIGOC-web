-- ============================================================
-- SIGOC Web — Migración incremental v2 (compatible con MySQL 8)
-- Agrega rol CONTADOR, relación obra -> cliente, campos de
-- valorización/comprobante/pago en línea, tabla tipo_cambio
-- y columnas de auditoría general (RNF-13 / RN-28).
-- ============================================================
USE sigoc_db;

-- 1. Rol CONTADOR y tipo de cambio base USD -> PEN
INSERT INTO rol(nombre_rol)
SELECT 'CONTADOR' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='CONTADOR');

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

-- 2. Relación obra -> cliente y auditoría de obra
ALTER TABLE obra ADD COLUMN id_cliente INT NULL;
ALTER TABLE obra ADD CONSTRAINT fk_obra_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente);

-- 3. Ampliación de valorizacion (numeración, rango periodo, desglose IGV)
ALTER TABLE valorizacion MODIFY COLUMN periodo VARCHAR(30) NOT NULL;
ALTER TABLE valorizacion ADD COLUMN numero INT NOT NULL DEFAULT 1;
ALTER TABLE valorizacion ADD COLUMN periodo_inicio DATE NULL;
ALTER TABLE valorizacion ADD COLUMN periodo_fin DATE NULL;
ALTER TABLE valorizacion ADD COLUMN avance_acumulado DECIMAL(5,2) NOT NULL DEFAULT 0;
ALTER TABLE valorizacion ADD COLUMN subtotal DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE valorizacion ADD COLUMN igv DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE valorizacion ADD COLUMN total DECIMAL(12,2) NOT NULL DEFAULT 0;

-- 4. Ampliación de comprobante (serie CV01, correlativo, enlace de 72h y unicidad)
ALTER TABLE comprobante ADD COLUMN serie VARCHAR(10) NOT NULL DEFAULT 'CV01';
ALTER TABLE comprobante ADD COLUMN numero INT NOT NULL DEFAULT 1;
ALTER TABLE comprobante ADD COLUMN codigo_pago VARCHAR(64) NULL;
ALTER TABLE comprobante ADD COLUMN vence_el DATETIME NULL;
ALTER TABLE comprobante ADD UNIQUE KEY uq_comprobante_codigo_pago (codigo_pago);
ALTER TABLE comprobante ADD UNIQUE KEY uq_comprobante_valorizacion (id_valorizacion);

-- 5. Ampliación de pago (pasarela PayPal / simulada, USD, tarjeta sin PAN)
ALTER TABLE pago ADD COLUMN pasarela VARCHAR(30) NOT NULL DEFAULT 'PASARELA_SIMULADA';
ALTER TABLE pago ADD COLUMN id_orden_externa VARCHAR(80) NULL;
ALTER TABLE pago ADD COLUMN id_captura_externa VARCHAR(80) NULL;
ALTER TABLE pago ADD COLUMN monto_cobro DECIMAL(12,2) NULL;
ALTER TABLE pago ADD COLUMN moneda_cobro VARCHAR(3) NOT NULL DEFAULT 'PEN';
ALTER TABLE pago ADD COLUMN tipo_cambio DECIMAL(8,4) NULL;
ALTER TABLE pago ADD COLUMN marca_tarjeta VARCHAR(20) NULL;
ALTER TABLE pago ADD COLUMN ultimos4 VARCHAR(4) NULL;
ALTER TABLE pago ADD COLUMN motivo VARCHAR(255) NULL;
ALTER TABLE pago ADD UNIQUE KEY uq_pago_captura_externa (id_captura_externa);

-- 6. Vínculo único ingreso <-> pago
ALTER TABLE ingreso ADD COLUMN id_pago INT NULL;
ALTER TABLE ingreso ADD UNIQUE KEY uq_ingreso_pago (id_pago);
ALTER TABLE ingreso ADD CONSTRAINT fk_ingreso_pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago);

-- 7. Columnas de auditoría (id_usuario_registro, fecha_registro)
ALTER TABLE cliente ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE obra ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE partida ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE material ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE proveedor ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE compra_material ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE empleado ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE asistencia ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE produccion_empleado ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE ingreso ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE egreso ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE usuario ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE valorizacion ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE comprobante ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE pago ADD COLUMN id_usuario_registro INT NULL, ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE avance_partida ADD COLUMN fecha_registro DATETIME NULL;
ALTER TABLE consumo_material ADD COLUMN fecha_registro DATETIME NULL;
