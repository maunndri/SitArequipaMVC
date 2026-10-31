-- SIT Arequipa - esquema PostgreSQL (basado en el MER del informe)
-- Ejecutar en pgAdmin (Query Tool) sobre la base de datos 'sitdb'
-- o con: psql -U postgres -d sitdb -f sql/schema.sql

DROP TABLE IF EXISTS registro_rechazo CASCADE;
DROP TABLE IF EXISTS viaje CASCADE;
DROP TABLE IF EXISTS lector_bus CASCADE;
DROP TABLE IF EXISTS unidad CASCADE;
DROP TABLE IF EXISTS paradero CASCADE;
DROP TABLE IF EXISTS ruta CASCADE;
DROP TABLE IF EXISTS recarga CASCADE;
DROP TABLE IF EXISTS venta_tarjeta CASCADE;
DROP TABLE IF EXISTS comprobante CASCADE;
DROP TABLE IF EXISTS tarjeta CASCADE;
DROP TABLE IF EXISTS motivo_rechazo CASCADE;
DROP TABLE IF EXISTS estado_viaje CASCADE;
DROP TABLE IF EXISTS tipo_comprobante CASCADE;
DROP TABLE IF EXISTS perfil_tarifa CASCADE;
DROP TABLE IF EXISTS estado_tarjeta CASCADE;
DROP TABLE IF EXISTS bitacora CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS personal_control CASCADE;
DROP TABLE IF EXISTS operador_punto_venta CASCADE;
DROP TABLE IF EXISTS pasajero CASCADE;
DROP TABLE IF EXISTS persona_telefono CASCADE;
DROP TABLE IF EXISTS persona CASCADE;
DROP TABLE IF EXISTS rol CASCADE;

CREATE TABLE rol (
    id_rol      SERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE persona (
    id_persona          SERIAL PRIMARY KEY,
    nombres             VARCHAR(100) NOT NULL,
    apellido_paterno    VARCHAR(50)  NOT NULL,
    apellido_materno    VARCHAR(50),
    tipo_documento      VARCHAR(10)  NOT NULL CHECK (tipo_documento IN ('DNI','CE')),
    documento_identidad VARCHAR(20)  NOT NULL UNIQUE,      -- RF-001: documento único
    email               VARCHAR(150),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE persona_telefono (
    id_telefono SERIAL PRIMARY KEY,
    id_persona  INTEGER NOT NULL REFERENCES persona(id_persona),
    telefono    VARCHAR(15) NOT NULL
);

CREATE TABLE pasajero (
    id_persona INTEGER PRIMARY KEY REFERENCES persona(id_persona)
);

CREATE TABLE operador_punto_venta (
    id_persona  INTEGER PRIMARY KEY REFERENCES persona(id_persona),
    punto_venta VARCHAR(150) NOT NULL
);

CREATE TABLE personal_control (
    id_persona INTEGER PRIMARY KEY REFERENCES persona(id_persona),
    turno      VARCHAR(20)
);

CREATE TABLE usuario (
    id_usuario    SERIAL PRIMARY KEY,
    id_persona    INTEGER      NOT NULL REFERENCES persona(id_persona),
    id_rol        INTEGER      NOT NULL REFERENCES rol(id_rol),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,                  -- nunca texto plano (RNF-003)
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE bitacora (
    id_bitacora SERIAL PRIMARY KEY,
    id_usuario  INTEGER REFERENCES usuario(id_usuario),   -- NULL en intentos de login fallidos
    accion      VARCHAR(100) NOT NULL,
    entidad     VARCHAR(50),
    fecha_hora  TIMESTAMP    NOT NULL DEFAULT now(),
    ip          VARCHAR(45)
);

-- Catálogos
CREATE TABLE estado_tarjeta  (id_estado_tarjeta  INTEGER PRIMARY KEY, nombre_estado VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE perfil_tarifa   (id_perfil_tarifa   INTEGER PRIMARY KEY, nombre_perfil VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE tipo_comprobante(id_tipo_comprobante SERIAL PRIMARY KEY, nombre_tipo   VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE estado_viaje    (id_estado_viaje    INTEGER PRIMARY KEY, nombre_estado VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE motivo_rechazo  (id_motivo_rechazo  INTEGER PRIMARY KEY, descripcion   VARCHAR(150) NOT NULL);

CREATE TABLE tarjeta (
    numero_tarjeta    VARCHAR(30) PRIMARY KEY,
    id_pasajero       INTEGER NOT NULL REFERENCES pasajero(id_persona),
    id_perfil_tarifa  INTEGER NOT NULL REFERENCES perfil_tarifa(id_perfil_tarifa),
    id_estado_tarjeta INTEGER NOT NULL REFERENCES estado_tarjeta(id_estado_tarjeta),
    saldo             NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (saldo >= 0),   -- RF-004: sin saldo negativo
    fecha_emision     DATE NOT NULL DEFAULT CURRENT_DATE
);
-- RF-002: una tarjeta ACTIVA solo puede pertenecer a un pasajero a la vez (y viceversa)
CREATE UNIQUE INDEX uq_tarjeta_activa_por_pasajero ON tarjeta(id_pasajero) WHERE id_estado_tarjeta = 1;

CREATE TABLE comprobante (
    numero_comprobante  VARCHAR(30) PRIMARY KEY,
    id_tipo_comprobante INTEGER NOT NULL REFERENCES tipo_comprobante(id_tipo_comprobante),
    monto               NUMERIC(10,2) NOT NULL CHECK (monto >= 0),
    fecha               TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE venta_tarjeta (
    id_venta           SERIAL PRIMARY KEY,
    numero_tarjeta     VARCHAR(30) NOT NULL REFERENCES tarjeta(numero_tarjeta),
    id_operador        INTEGER NOT NULL REFERENCES operador_punto_venta(id_persona),
    numero_comprobante VARCHAR(30) NOT NULL UNIQUE REFERENCES comprobante(numero_comprobante),
    monto_pagado       NUMERIC(10,2) NOT NULL CHECK (monto_pagado >= 0),
    fecha              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE recarga (
    id_recarga         SERIAL PRIMARY KEY,
    numero_tarjeta     VARCHAR(30) NOT NULL REFERENCES tarjeta(numero_tarjeta),
    id_operador        INTEGER NOT NULL REFERENCES operador_punto_venta(id_persona),
    numero_comprobante VARCHAR(30) NOT NULL UNIQUE REFERENCES comprobante(numero_comprobante),
    monto              NUMERIC(10,2) NOT NULL CHECK (monto > 0),
    saldo_anterior     NUMERIC(10,2) NOT NULL,
    saldo_posterior    NUMERIC(10,2) NOT NULL,
    fecha              TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (saldo_posterior = saldo_anterior + monto)
);

CREATE TABLE ruta (
    id_ruta     SERIAL PRIMARY KEY,
    codigo_ruta VARCHAR(20)  NOT NULL UNIQUE,
    nombre_ruta VARCHAR(100) NOT NULL,
    tipo_ruta   VARCHAR(30),
    color_hex   CHAR(7),
    tarifa_base NUMERIC(10,2) NOT NULL CHECK (tarifa_base >= 0),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE paradero (
    id_paradero     SERIAL PRIMARY KEY,
    id_ruta         INTEGER NOT NULL REFERENCES ruta(id_ruta),
    nombre          VARCHAR(100) NOT NULL,
    latitud         NUMERIC(9,6) NOT NULL,
    longitud        NUMERIC(9,6) NOT NULL,
    orden_secuencia INTEGER NOT NULL,
    UNIQUE (id_ruta, orden_secuencia)
);

CREATE TABLE unidad (
    id_unidad SERIAL PRIMARY KEY,
    placa     VARCHAR(10) NOT NULL UNIQUE,
    modelo    VARCHAR(50)
);

CREATE TABLE lector_bus (
    id_lector VARCHAR(20) PRIMARY KEY,
    id_unidad INTEGER NOT NULL REFERENCES unidad(id_unidad)
);

CREATE TABLE viaje (
    id_viaje        SERIAL PRIMARY KEY,
    numero_tarjeta  VARCHAR(30) NOT NULL REFERENCES tarjeta(numero_tarjeta),
    id_lector       VARCHAR(20) NOT NULL REFERENCES lector_bus(id_lector),
    id_ruta         INTEGER     NOT NULL REFERENCES ruta(id_ruta),
    id_paradero     INTEGER     REFERENCES paradero(id_paradero),
    id_estado_viaje INTEGER     NOT NULL REFERENCES estado_viaje(id_estado_viaje),
    fecha_hora      TIMESTAMP   NOT NULL DEFAULT now(),
    tarifa          NUMERIC(10,2) NOT NULL
);

CREATE TABLE registro_rechazo (
    id_rechazo        SERIAL PRIMARY KEY,
    id_viaje          INTEGER NOT NULL UNIQUE REFERENCES viaje(id_viaje),
    id_motivo_rechazo INTEGER NOT NULL REFERENCES motivo_rechazo(id_motivo_rechazo),
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- Índices de consulta frecuente (dashboard y reportes)
CREATE INDEX idx_viaje_fecha   ON viaje(fecha_hora);
CREATE INDEX idx_recarga_fecha ON recarga(fecha);

-- ======================= DATOS INICIALES =======================
INSERT INTO rol (nombre_rol) VALUES ('ADMINISTRADOR'), ('OPERADOR_PV'), ('PERSONAL_CONTROL');
INSERT INTO estado_tarjeta VALUES (1,'ACTIVA'), (2,'BLOQUEADA'), (3,'ANULADA');
-- Catálogos de perfiles de tarifa oficiales SIT: 1: GENERAL (S/ 1.30), 2: UNIVERSITARIO (S/ 0.70), 3: ESCOLAR (S/ 0.50)
INSERT INTO perfil_tarifa  VALUES (1,'GENERAL'), (2,'UNIVERSITARIO'), (3,'ESCOLAR');
INSERT INTO tipo_comprobante (nombre_tipo) VALUES ('BOLETA'), ('FACTURA'), ('RECIBO');
INSERT INTO estado_viaje   VALUES (1,'APROBADO'), (2,'RECHAZADO');
INSERT INTO motivo_rechazo VALUES (1,'Saldo insuficiente'), (2,'Tarjeta bloqueada'),
                                  (3,'Tarjeta no registrada'), (4,'Tarjeta anulada');

INSERT INTO ruta (codigo_ruta, nombre_ruta, tipo_ruta, color_hex, tarifa_base)
VALUES ('TRONCAL-C1', 'Río Seco - Socabaya', 'Troncal', '#108981', 1.30);
INSERT INTO unidad (placa, modelo) VALUES ('ABC-123', 'Bus articulado demo');
INSERT INTO lector_bus (id_lector, id_unidad) VALUES ('LECTOR-001', 1);

-- Usuarios del sistema:
-- admin: password "admin"
-- operador1: password "operador1"
INSERT INTO persona (nombres, apellido_paterno, tipo_documento, documento_identidad)
VALUES ('Administrador', 'SIT', 'DNI', '00000001'), ('Operador', 'Demo', 'DNI', '00000002');
INSERT INTO operador_punto_venta (id_persona, punto_venta) VALUES (1, 'Oficina central'), (2, 'Terminal Río Seco');
INSERT INTO usuario (id_persona, id_rol, username, password_hash) VALUES
 (1, (SELECT id_rol FROM rol WHERE nombre_rol='ADMINISTRADOR'), 'admin',     'pbkdf2$210000$OgRueFBv4mFEgyEZkKDf3w==$UELKd0NWNvVAOmSQTIxhF1dPc3M4mi6Q0dwt4woflzU='),
 (2, (SELECT id_rol FROM rol WHERE nombre_rol='OPERADOR_PV'),   'operador1', 'pbkdf2$210000$QCr9zawAi4vsS3DM+TAe6g==$2BHaAo2HD4Y5iGu5DRDSv+I4XvDOLFW8btHTMImNdN4=');

-- Pasajeros y tarjetas de prueba iniciales
INSERT INTO persona (nombres, apellido_paterno, apellido_materno, tipo_documento, documento_identidad, email)
VALUES ('Carlos Alberto', 'Quispe', 'Mamani', 'DNI', '70123456', 'carlos.quispe@gmail.com'),
       ('Lucía Elena', 'Flores', 'Vargas', 'DNI', '72987654', 'lucia.flores@utp.edu.pe');
INSERT INTO persona_telefono (id_persona, telefono) VALUES (3, '958123456'), (4, '984567890');
INSERT INTO pasajero (id_persona) VALUES (3), (4);

INSERT INTO tarjeta (numero_tarjeta, id_pasajero, id_perfil_tarifa, id_estado_tarjeta, saldo, fecha_emision)
VALUES ('SIT-100001', 3, 1, 1, 15.50, CURRENT_DATE - 3),
       ('SIT-100002', 4, 2, 1, 8.00, CURRENT_DATE - 1);
