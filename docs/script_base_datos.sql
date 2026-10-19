-- ---------------------------------------------------------------------
-- SIGRE - Modulo de cursos e inscripciones
-- Script OPCIONAL para crear la base de datos MySQL 8 manualmente.
-- Sigue el diccionario de datos del modelo logico (GA4-220501095-AA1-EV02).
-- Con la configuracion por defecto, Hibernate crea las tablas automaticamente.
-- ---------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS sigre_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sigre_db;

CREATE TABLE IF NOT EXISTS rol (
    id_rol     BIGINT      NOT NULL AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_rol),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre_rol)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario       BIGINT       NOT NULL AUTO_INCREMENT,
    numero_documento VARCHAR(20)  NOT NULL,
    tipo_documento   VARCHAR(10)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    id_rol           BIGINT       NOT NULL,
    fecha_registro   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_documento UNIQUE (numero_documento),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS instructor (
    id_instructor              BIGINT       NOT NULL AUTO_INCREMENT,
    id_usuario                 BIGINT       NOT NULL,
    especialidad               VARCHAR(100) NOT NULL,
    numero_licencia_alturas    VARCHAR(50)  NOT NULL,
    fecha_vencimiento_licencia DATE         NOT NULL,
    PRIMARY KEY (id_instructor),
    CONSTRAINT uk_instructor_usuario UNIQUE (id_usuario),
    CONSTRAINT uk_instructor_licencia UNIQUE (numero_licencia_alturas),
    CONSTRAINT fk_instructor_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS curso_evento (
    id_curso            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre_curso        VARCHAR(150) NOT NULL,
    duracion_horas      INT          NOT NULL,
    fecha_inicio        DATETIME(6)  NOT NULL,
    fecha_fin           DATETIME(6)  NOT NULL,
    lugar               VARCHAR(150) NOT NULL,
    cupo_maximo         INT          NOT NULL,
    id_instructor_cargo BIGINT       NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADO',
    PRIMARY KEY (id_curso),
    CONSTRAINT ck_curso_duracion CHECK (duracion_horas > 0),
    CONSTRAINT ck_curso_cupo CHECK (cupo_maximo > 0),
    CONSTRAINT fk_curso_instructor FOREIGN KEY (id_instructor_cargo) REFERENCES instructor (id_instructor)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS inscripcion (
    id_inscripcion         BIGINT      NOT NULL AUTO_INCREMENT,
    id_usuario_participante BIGINT     NOT NULL,
    id_curso               BIGINT      NOT NULL,
    fecha_inscripcion      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    estado_inscripcion     VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (id_inscripcion),
    CONSTRAINT uk_inscripcion_usuario_curso UNIQUE (id_usuario_participante, id_curso),
    CONSTRAINT fk_inscripcion_usuario FOREIGN KEY (id_usuario_participante) REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inscripcion_curso FOREIGN KEY (id_curso) REFERENCES curso_evento (id_curso)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- Roles del sistema (la aplicacion tambien los crea al iniciar si no existen)
INSERT IGNORE INTO rol (nombre_rol) VALUES ('ADMIN'), ('COORDINADOR'), ('INSTRUCTOR'), ('ASISTENTE');
