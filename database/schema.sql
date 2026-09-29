-- ============================================================
--  habit_tracker_db — Diseño físico (MySQL 8.0+ / InnoDB)
--  Aplicación: seguimiento de hábitos y metas diarias.
--  Contenido: DDL de 6 tablas + rutinas de integridad/conectividad.
--  Ejecutar en MySQL Workbench o en la consola SQL de IntelliJ IDEA.
-- ============================================================

CREATE DATABASE IF NOT EXISTS habit_tracker_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE habit_tracker_db;

-- ============================================================
-- CATÁLOGO 1: usuarios (maestro)
-- ============================================================
CREATE TABLE usuarios (
    id_usuario BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    es_admin BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_usuarios_email UNIQUE (email)
) ENGINE=InnoDB;

-- ============================================================
-- CATÁLOGO 2: categorias (clasifican a los hábitos)
-- ============================================================
CREATE TABLE categorias (
    id_categoria BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    descripcion VARCHAR(255),
    color_hex CHAR(7) NOT NULL,
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre),
    CONSTRAINT chk_categorias_color CHECK (color_hex REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB;

-- ============================================================
-- ENTIDAD 3: habitos (los FK conservan las relaciones 1:N)
-- ============================================================
CREATE TABLE habitos (
    id_habito BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT UNSIGNED NOT NULL,
    id_categoria BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    tipo_meta ENUM('BOOLEANO', 'CUANTITATIVO') NOT NULL DEFAULT 'BOOLEANO',
    meta_diaria DECIMAL(10,2) NULL,
    unidad_medida VARCHAR(30) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATE NOT NULL,
    CONSTRAINT fk_habitos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_habitos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
    CONSTRAINT chk_habitos_meta CHECK ((tipo_meta = 'BOOLEANO' AND meta_diaria IS NULL AND unidad_medida IS NULL) OR (tipo_meta = 'CUANTITATIVO' AND meta_diaria > 0 AND unidad_medida IS NOT NULL))
) ENGINE=InnoDB;

-- ============================================================
-- CATÁLOGO 4: etiquetas (taxonomía transversal)
-- ============================================================
CREATE TABLE etiquetas (
    id_etiqueta BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uq_etiquetas_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA PUENTE 5: habitos_etiquetas (relación N:M habitos-etiquetas)
-- ============================================================
CREATE TABLE habitos_etiquetas (
    id_habito BIGINT UNSIGNED NOT NULL,
    id_etiqueta BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id_habito, id_etiqueta),
    CONSTRAINT fk_he_habito FOREIGN KEY (id_habito) REFERENCES habitos(id_habito) ON DELETE CASCADE,
    CONSTRAINT fk_he_etiqueta FOREIGN KEY (id_etiqueta) REFERENCES etiquetas(id_etiqueta) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- ENTIDAD 6: registros_diarios (transaccional; descompone el Excel)
-- ============================================================
CREATE TABLE registros_diarios (
    id_registro BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    id_habito BIGINT UNSIGNED NOT NULL,
    fecha DATE NOT NULL,
    valor_completado DECIMAL(10,2) NOT NULL DEFAULT 0,
    cumplido BOOLEAN NOT NULL DEFAULT FALSE,
    nota VARCHAR(255),
    registrado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registros_habito FOREIGN KEY (id_habito) REFERENCES habitos(id_habito) ON DELETE CASCADE,
    CONSTRAINT uq_registro_habito_fecha UNIQUE (id_habito, fecha),
    CONSTRAINT chk_registros_valor CHECK (valor_completado >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_habitos_usuario_activo ON habitos (id_usuario, activo);
CREATE INDEX idx_registros_habito_fecha ON registros_diarios (id_habito, fecha);

-- ============================================================
-- RUTINAS DE CONECTIVIDAD E INTEGRIDAD
-- ============================================================
DELIMITER //

-- Prueba de conectividad: disponible, sesión activa y zona horaria.
CREATE PROCEDURE sp_test_conectividad()
BEGIN
    SELECT 'Conectado a habit_tracker_db' AS estado,
           CURRENT_USER() AS sesion,
           @@session.time_zone AS zona_horaria,
           CURRENT_TIMESTAMP() AS timestamp_servidor;
END //

-- Registro/actualización transaccional del avance diario de un hábito.
-- Valida la meta configurada y evita duplicados con la UNIQUE (id_habito, fecha).
CREATE PROCEDURE sp_registrar_habito_diario(IN p_id_habito BIGINT UNSIGNED, IN p_fecha DATE, IN p_valor DECIMAL(10,2), IN p_nota VARCHAR(255))
BEGIN
    DECLARE v_meta DECIMAL(10,2);
    DECLARE v_tipo VARCHAR(20);
    DECLARE v_cumplido BOOLEAN;
    SELECT meta_diaria, tipo_meta INTO v_meta, v_tipo FROM habitos WHERE id_habito = p_id_habito AND activo = TRUE;
    IF v_tipo IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El hábito no existe o está inactivo'; END IF;
    SET v_cumplido = IF(v_tipo = 'BOOLEANO', p_valor >= 1, p_valor >= v_meta);
    INSERT INTO registros_diarios (id_habito, fecha, valor_completado, cumplido, nota)
    VALUES (p_id_habito, p_fecha, p_valor, v_cumplido, p_nota) AS fila_nueva
    ON DUPLICATE KEY UPDATE
        valor_completado = fila_nueva.valor_completado,
        cumplido = fila_nueva.cumplido,
        nota = fila_nueva.nota;
END //

-- Calcula la racha (streak) de días consecutivos cumplidos hasta una fecha.
-- Usa MAX(cumplido) porque un SELECT...INTO sin filas conserva el valor anterior,
-- mientras que MAX devuelve NULL cuando no hay registros.
CREATE FUNCTION fn_racha_habito(p_id_habito BIGINT UNSIGNED, p_hasta_fecha DATE) RETURNS INT DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_racha INT DEFAULT 0;
    DECLARE v_fecha DATE DEFAULT p_hasta_fecha;
    DECLARE v_cumplido BOOLEAN;
    racha: LOOP
        SELECT MAX(cumplido) INTO v_cumplido FROM registros_diarios WHERE id_habito = p_id_habito AND fecha = v_fecha;
        IF v_cumplido IS NULL OR v_cumplido = FALSE THEN LEAVE racha; END IF;
        SET v_racha = v_racha + 1;
        SET v_fecha = DATE_SUB(v_fecha, INTERVAL 1 DAY);
    END LOOP;
    RETURN v_racha;
END //
DELIMITER ;
