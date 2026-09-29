-- ============================================================
--  habit_tracker_db — Datos de muestra (≥ 5 registros por tabla)
--  Orden de carga: catalógicos → entidad → puente → transaccional.
--  Ejecutar DESPUÉS de database/schema.sql.
-- ============================================================
USE habit_tracker_db;

-- 5 usuarios (contraseña: Demo1234, hash PBKDF2-HMAC-SHA256)
-- Alejandra (usuario 1) es administradora (es_admin = TRUE).
INSERT INTO usuarios (nombre, apellido, email, password_hash, es_admin) VALUES
('Alejandra', 'López', 'ana.lopez@example.com', '210000:jaozskDhi7KvHCus4OagvQ==:9M6BeuhMjDgg7e1rW5+W1RVbPKaqiuQKjmg7+8LlfEs=', TRUE),
('Carlos', 'Ramírez', 'carlos.ramirez@example.com', '210000:jaozskDhi7KvHCus4OagvQ==:9M6BeuhMjDgg7e1rW5+W1RVbPKaqiuQKjmg7+8LlfEs=', FALSE),
('Sofía', 'Martínez', 'sofia.martinez@example.com', '210000:jaozskDhi7KvHCus4OagvQ==:9M6BeuhMjDgg7e1rW5+W1RVbPKaqiuQKjmg7+8LlfEs=', FALSE),
('Diego', 'Hernández', 'diego.hernandez@example.com', '210000:jaozskDhi7KvHCus4OagvQ==:9M6BeuhMjDgg7e1rW5+W1RVbPKaqiuQKjmg7+8LlfEs=', FALSE),
('Valeria', 'Torres', 'valeria.torres@example.com', '210000:jaozskDhi7KvHCus4OagvQ==:9M6BeuhMjDgg7e1rW5+W1RVbPKaqiuQKjmg7+8LlfEs=', FALSE);

-- 5 categorías

INSERT INTO categorias (nombre, descripcion, color_hex) VALUES
('Salud', 'Hábitos físicos y de bienestar.', '#22C55E'), ('Productividad', 'Actividades de enfoque y organización.', '#3B82F6'),
('Aprendizaje', 'Estudio y adquisición de conocimientos.', '#A855F7'), ('Finanzas', 'Control financiero personal.', '#F59E0B'),
('Bienestar', 'Descanso y salud emocional.', '#EC4899');

-- 6 hábitos (cubren ambas modalidades de meta y las 5 categorías)

INSERT INTO habitos (id_usuario, id_categoria, nombre, descripcion, tipo_meta, meta_diaria, unidad_medida, fecha_creacion) VALUES
(1, 1, 'Beber agua', 'Mantener hidratación diaria.', 'CUANTITATIVO', 2.00, 'litros', '2026-08-01'),
(1, 2, 'Planificar el día', 'Definir tareas prioritarias.', 'BOOLEANO', NULL, NULL, '2026-08-01'),
(2, 3, 'Estudiar inglés', 'Práctica diaria de vocabulario.', 'CUANTITATIVO', 30.00, 'minutos', '2026-08-02'),
(3, 1, 'Caminar', 'Realizar caminata ligera.', 'CUANTITATIVO', 8000.00, 'pasos', '2026-08-03'),
(4, 5, 'Meditar', 'Sesión breve de respiración.', 'CUANTITATIVO', 10.00, 'minutos', '2026-08-04'),
(5, 4, 'Registrar gastos', 'Anotar compras del día.', 'BOOLEANO', NULL, NULL, '2026-08-05');

-- 5 etiquetas

INSERT INTO etiquetas (nombre) VALUES ('mañana'), ('casa'), ('prioridad'), ('saludable'), ('sin pantalla');

-- 10 relaciones M:N (habitos - etiquetas)
INSERT INTO habitos_etiquetas (id_habito, id_etiqueta) VALUES
(1, 1), (1, 4), (2, 1), (2, 3), (3, 3), (3, 5), (4, 4), (5, 5), (6, 2), (6, 3);

-- 15 registros diarios
INSERT INTO registros_diarios (id_habito, fecha, valor_completado, cumplido, nota) VALUES
(1, '2026-08-24', 2.00, TRUE, 'Meta alcanzada'), (1, '2026-08-25', 2.50, TRUE, NULL), (1, '2026-08-26', 1.50, FALSE, NULL), (1, '2026-08-27', 2.00, TRUE, NULL), (1, '2026-08-28', 2.00, TRUE, NULL),
(2, '2026-08-24', 1, TRUE, 'Lista creada'), (2, '2026-08-25', 1, TRUE, NULL), (2, '2026-08-26', 0, FALSE, NULL),
(3, '2026-08-27', 30, TRUE, 'Lección 4'), (3, '2026-08-28', 45, TRUE, NULL), (4, '2026-08-26', 7500, FALSE, NULL), (4, '2026-08-27', 8400, TRUE, NULL),
(5, '2026-08-27', 10, TRUE, NULL), (5, '2026-08-28', 15, TRUE, NULL), (6, '2026-08-28', 1, TRUE, 'Compras registradas');
