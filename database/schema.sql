-- =====================================================================
-- SISTEMA KARDEX VALORADO (PROMEDIO PONDERADO MÓVIL) - JAVA 21 & MYSQL
-- Script de Creación y Poblado de Base de Datos
-- =====================================================================
CREATE DATABASE kardex_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE kardex_db;
CREATE DATABASE IF NOT EXISTS kardex_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE kardex_db;

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'OPERADOR') NOT NULL DEFAULT 'OPERADOR',
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabla de Categorías
CREATE TABLE IF NOT EXISTS categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 3. Tabla de Proveedores
CREATE TABLE IF NOT EXISTS proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(100),
    direccion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 4. Tabla de Clientes
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombre_razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(100),
    direccion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 5. Tabla de Productos
CREATE TABLE IF NOT EXISTS productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL DEFAULT 'UND',
    stock_actual DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    stock_minimo DECIMAL(12, 2) NOT NULL DEFAULT 5.00,
    costo_promedio DECIMAL(12, 4) NOT NULL DEFAULT 0.0000,
    precio_venta DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
) ENGINE=InnoDB;

-- 6. Tabla de Compras (Encabezado)
CREATE TABLE IF NOT EXISTS compras (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT NOT NULL,
    id_usuario INT NOT NULL,
    tipo_comprobante VARCHAR(30) NOT NULL,
    serie_numero VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(14, 2) NOT NULL,
    observaciones VARCHAR(255),
    CONSTRAINT fk_compras_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
    CONSTRAINT fk_compras_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- 7. Tabla Detalle de Compras
CREATE TABLE IF NOT EXISTS detalle_compras (
    id_detalle_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(12, 2) NOT NULL,
    costo_unitario DECIMAL(12, 4) NOT NULL,
    subtotal DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_detcompras_compra FOREIGN KEY (id_compra) REFERENCES compras(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_detcompras_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

-- 8. Tabla de Ventas (Encabezado)
CREATE TABLE IF NOT EXISTS ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL,
    tipo_comprobante VARCHAR(30) NOT NULL,
    serie_numero VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(14, 2) NOT NULL,
    costo_total_estimado DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    observaciones VARCHAR(255),
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- 9. Tabla Detalle de Ventas
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(12, 2) NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    costo_unitario_historico DECIMAL(12, 4) NOT NULL,
    subtotal DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_detventas_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detventas_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

-- 10. Tabla de Kardex Valorado (Historial Transaccional)
CREATE TABLE IF NOT EXISTS kardex (
    id_kardex INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento ENUM('SALDO_INICIAL', 'ENTRADA_COMPRA', 'SALIDA_VENTA', 'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO') NOT NULL,
    tipo_comprobante VARCHAR(30) NOT NULL,
    documento_referencia VARCHAR(60) NOT NULL,
    motivo VARCHAR(150),
    -- Entradas
    entrada_cantidad DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    entrada_costo_unitario DECIMAL(12, 4) NOT NULL DEFAULT 0.0000,
    entrada_costo_total DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    -- Salidas
    salida_cantidad DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    salida_costo_unitario DECIMAL(12, 4) NOT NULL DEFAULT 0.0000,
    salida_costo_total DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    -- Saldos resultantes
    saldo_cantidad DECIMAL(12, 2) NOT NULL,
    saldo_costo_unitario DECIMAL(12, 4) NOT NULL,
    saldo_costo_total DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_kardex_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE,
    INDEX idx_kardex_producto_fecha (id_producto, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- DATOS INICIALES (DEMO / PRUEBAS)
-- =====================================================================

-- Usuarios iniciales (Pass: 'admin123' y 'operador123' con hash SHA-256)
-- Hash de 'admin123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa82280f1a65d1d1b
-- Hash de 'operador123': a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
INSERT INTO usuarios (username, password_hash, nombre_completo, rol, estado) VALUES
('admin', '123456', 'Administrador Principal', 'ADMINISTRADOR', TRUE),
('operador', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'Carlos Gómez (Operador)', 'OPERADOR', TRUE)
ON DUPLICATE KEY UPDATE id_usuario=id_usuario;

-- Categorías iniciales
INSERT INTO categorias (nombre, descripcion) VALUES
('Electrónica y Cómputo', 'Equipos, laptops, partes y accesorios de tecnología'),
('Oficina y Papelería', 'Materiales de escritorio, resmas de papel y suministros'),
('Mobiliario', 'Sillas ergonómicas, escritorios y archivadores'),
('Accesorios de Red', 'Cables, routers, switches y conectores')
ON DUPLICATE KEY UPDATE id_categoria=id_categoria;

-- Proveedores iniciales
INSERT INTO proveedores (documento, razon_social, telefono, email, direccion) VALUES
('20556677881', 'Distribuidora Global Tech S.A.C.', '987654321', 'ventas@globaltech.com', 'Av. Industrial 1250, Lima'),
('20443322119', 'Importaciones y Suministros Alfa S.A.', '976543210', 'contacto@suministrosalfa.com', 'Jr. Los Cedros 340, Lima')
ON DUPLICATE KEY UPDATE id_proveedor=id_proveedor;

-- Clientes iniciales
INSERT INTO clientes (documento, nombre_razon_social, telefono, email, direccion) VALUES
('10458796321', 'Corporación Logística del Sur E.I.R.L.', '998877665', 'admin@corpsur.pe', 'Av. Central 500, Arequipa'),
('10748596325', 'Juan Pérez Morales', '955443322', 'juan.perez@gmail.com', 'Calle Los Sauces 120, Lima'),
('20987654321', 'Consultora de Negocios Orion S.A.C.', '944332211', 'facturacion@orion.com', 'Av. Primavera 890, Lima')
ON DUPLICATE KEY UPDATE id_cliente=id_cliente;

-- Productos con inventario y saldo inicial
INSERT INTO productos (codigo, nombre, id_categoria, unidad_medida, stock_actual, stock_minimo, costo_promedio, precio_venta) VALUES
('PROD-001', 'Mouse Ergonómico Inalámbrico Logitech', 1, 'UND', 35.00, 10.00, 45.0000, 75.00),
('PROD-002', 'Teclado Mecánico RGB Redragon', 1, 'UND', 20.00, 8.00, 120.0000, 185.00),
('PROD-003', 'Monitor 24 Pulgadas IPS Full HD LG', 1, 'UND', 12.00, 5.00, 380.0000, 520.00),
('PROD-004', 'Resma de Papel Bond A4 75g (Caja x 5 millares)', 2, 'CAJA', 15.00, 6.00, 85.0000, 115.00),
('PROD-005', 'Silla de Oficina Ergonómica Giratoria', 3, 'UND', 4.00, 5.00, 220.0000, 340.00),
('PROD-006', 'Cable de Red UTP Cat 6 Bobina 305m', 4, 'BOBINA', 6.00, 3.00, 190.0000, 275.00)
ON DUPLICATE KEY UPDATE id_producto=id_producto;

-- Kardex para productos iniciales (Registro de Inventario Inicial)
INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, entrada_cantidad, entrada_costo_unitario, entrada_costo_total, salida_cantidad, salida_costo_unitario, salida_costo_total, saldo_cantidad, saldo_costo_unitario, saldo_costo_total)
VALUES
(1, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-001', 'Inventario inicial de apertura', 35.00, 45.0000, 1575.00, 0, 0, 0, 35.00, 45.0000, 1575.00),
(2, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-002', 'Inventario inicial de apertura', 20.00, 120.0000, 2400.00, 0, 0, 0, 20.00, 120.0000, 2400.00),
(3, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-003', 'Inventario inicial de apertura', 12.00, 380.0000, 4560.00, 0, 0, 0, 12.00, 380.0000, 4560.00),
(4, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-004', 'Inventario inicial de apertura', 15.00, 85.0000, 1275.00, 0, 0, 0, 15.00, 85.0000, 1275.00),
(5, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-005', 'Inventario inicial de apertura', 4.00, 220.0000, 880.00, 0, 0, 0, 4.00, 220.0000, 880.00),
(6, NOW() - INTERVAL 10 DAY, 'SALDO_INICIAL', 'INVENTARIO_INICIAL', 'INI-006', 'Inventario inicial de apertura', 6.00, 190.0000, 1140.00, 0, 0, 0, 6.00, 190.0000, 1140.00);

-- Compra de demostración
INSERT INTO compras (id_compra, id_proveedor, id_usuario, tipo_comprobante, serie_numero, fecha, total, observaciones)
VALUES (1, 1, 1, 'Factura', 'F001-000452', NOW() - INTERVAL 5 DAY, 980.00, 'Reposición de stock mouses y teclados');

INSERT INTO detalle_compras (id_compra, id_producto, cantidad, costo_unitario, subtotal)
VALUES 
(1, 1, 10.00, 43.0000, 430.00),
(1, 2, 5.00, 110.0000, 550.00);

-- Movimiento Kardex de la compra demo (Cálculo promedio ponderado)
-- Para PROD-001: Tenía 35 @ 45 = 1575. Entra 10 @ 43 = 430. Nuevo saldo = 45 und, total = 2005 -> Nuevo costo = 2005 / 45 = 44.5556
UPDATE productos SET stock_actual = 45.00, costo_promedio = 44.5556 WHERE id_producto = 1;
INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, entrada_cantidad, entrada_costo_unitario, entrada_costo_total, salida_cantidad, salida_costo_unitario, salida_costo_total, saldo_cantidad, saldo_costo_unitario, saldo_costo_total)
VALUES (1, NOW() - INTERVAL 5 DAY, 'ENTRADA_COMPRA', 'Factura', 'F001-000452', 'Compra a Distribuidora Global Tech', 10.00, 43.0000, 430.00, 0, 0, 0, 45.00, 44.5556, 2005.00);

-- Para PROD-002: Tenía 20 @ 120 = 2400. Entra 5 @ 110 = 550. Nuevo saldo = 25 und, total = 2950 -> Nuevo costo = 2950 / 25 = 118.0000
UPDATE productos SET stock_actual = 25.00, costo_promedio = 118.0000 WHERE id_producto = 2;
INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, entrada_cantidad, entrada_costo_unitario, entrada_costo_total, salida_cantidad, salida_costo_unitario, salida_costo_total, saldo_cantidad, saldo_costo_unitario, saldo_costo_total)
VALUES (2, NOW() - INTERVAL 5 DAY, 'ENTRADA_COMPRA', 'Factura', 'F001-000452', 'Compra a Distribuidora Global Tech', 5.00, 110.0000, 550.00, 0, 0, 0, 25.00, 118.0000, 2950.00);

-- Venta de demostración
INSERT INTO ventas (id_venta, id_cliente, id_usuario, tipo_comprobante, serie_numero, fecha, total, costo_total_estimado, observaciones)
VALUES (1, 1, 1, 'Factura', 'B001-000101', NOW() - INTERVAL 2 DAY, 535.00, 327.11, 'Venta al cliente Corp Sur');

INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, costo_unitario_historico, subtotal)
VALUES 
(1, 1, 5.00, 75.00, 44.5556, 375.00),
(1, 2, 2.00, 185.00, 118.0000, 370.00);

-- Movimiento Kardex de la venta demo (Salida valorada al costo promedio actual)
-- PROD-001: Salen 5 und al costo 44.5556 = 222.78. Saldo final = 40 und @ 44.5556 = 1782.22
UPDATE productos SET stock_actual = 40.00 WHERE id_producto = 1;
INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, entrada_cantidad, entrada_costo_unitario, entrada_costo_total, salida_cantidad, salida_costo_unitario, salida_costo_total, saldo_cantidad, saldo_costo_unitario, saldo_costo_total)
VALUES (1, NOW() - INTERVAL 2 DAY, 'SALIDA_VENTA', 'Factura', 'B001-000101', 'Venta a Corp Sur', 0, 0, 0, 5.00, 44.5556, 222.78, 40.00, 44.5556, 1782.22);

-- PROD-002: Salen 2 und al costo 118.0000 = 236.00. Saldo final = 23 und @ 118.0000 = 2714.00
UPDATE productos SET stock_actual = 23.00 WHERE id_producto = 2;
INSERT INTO kardex (id_producto, fecha, tipo_movimiento, tipo_comprobante, documento_referencia, motivo, entrada_cantidad, entrada_costo_unitario, entrada_costo_total, salida_cantidad, salida_costo_unitario, salida_costo_total, saldo_cantidad, saldo_costo_unitario, saldo_costo_total)
VALUES (2, NOW() - INTERVAL 2 DAY, 'SALIDA_VENTA', 'Factura', 'B001-000101', 'Venta a Corp Sur', 0, 0, 0, 2.00, 118.0000, 236.00, 23.00, 118.0000, 2714.00);

SELECT * FROM usuarios;