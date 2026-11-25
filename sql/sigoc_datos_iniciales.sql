-- ============================================================
-- SIGOC — sigoc_datos_iniciales.sql
-- Roles según los cinco perfiles ISO/IEC 27001 A.5.3 (RF-01):
--   1. ADMIN (Administrador de TI y Seguridad)
--   2. GERENTE (Gerente de Obras)
--   3. RESIDENTE (Residente de Obra)
--   4. ALMACENERO (Almacenero)
--   5. CONTADOR (Contador)
-- ============================================================
USE sigoc_db;

INSERT INTO rol(nombre_rol)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='ADMIN');

INSERT INTO rol(nombre_rol)
SELECT 'GERENTE' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='GERENTE');

INSERT INTO rol(nombre_rol)
SELECT 'RESIDENTE' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='RESIDENTE');

INSERT INTO rol(nombre_rol)
SELECT 'ALMACENERO' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='ALMACENERO');

INSERT INTO rol(nombre_rol)
SELECT 'CONTADOR' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol='CONTADOR');

-- Cuenta inicial de Administrador (TI y Seguridad)
INSERT INTO usuario(nombre_completo, usuario_login, contrasena_hash, id_rol, estado, activo)
SELECT 'Administrador SIGOC', 'admin', 'PBKDF2$120000$U0lHT0MtREVNT1NUUkFDSU9OLVNBTFQ=$rTzG6iZdG+6OFOOfY11QKZauNQ6Az1Yie4YbgDMcUT8=', id_rol, 1, 1
FROM rol WHERE nombre_rol='ADMIN' AND NOT EXISTS (SELECT 1 FROM usuario WHERE usuario_login='admin');

-- Cuenta inicial de Gerente de Obras (Dirección y Auditoría)
INSERT INTO usuario(nombre_completo, usuario_login, contrasena_hash, id_rol, estado, activo)
SELECT 'Gerente de Obras SIGOC', 'gerente', 'PBKDF2$120000$U0lHT0MtREVNT1NUUkFDSU9OLVNBTFQ=$rTzG6iZdG+6OFOOfY11QKZauNQ6Az1Yie4YbgDMcUT8=', id_rol, 1, 1
FROM rol WHERE nombre_rol='GERENTE' AND NOT EXISTS (SELECT 1 FROM usuario WHERE usuario_login='gerente');

INSERT INTO material(nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario_ref)
SELECT 'Cemento Portland Tipo I', 'bolsa', 0, 20, 30.00 WHERE NOT EXISTS (SELECT 1 FROM material WHERE nombre='Cemento Portland Tipo I');

INSERT INTO material(nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario_ref)
SELECT 'Arena gruesa', 'm3', 0, 5, 55.00 WHERE NOT EXISTS (SELECT 1 FROM material WHERE nombre='Arena gruesa');

INSERT INTO material(nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario_ref)
SELECT 'Piedra chancada', 'm3', 0, 5, 75.00 WHERE NOT EXISTS (SELECT 1 FROM material WHERE nombre='Piedra chancada');
