# Diccionario de datos — Habit Tracker

| Tabla | Campo | Tipo MySQL | Clave / regla |
|---|---|---|---|
| usuarios | id_usuario | BIGINT UNSIGNED | PK, AUTO_INCREMENT |
| usuarios | nombre, apellido | VARCHAR(80) | NOT NULL |
| usuarios | email | VARCHAR(150) | NOT NULL, UNIQUE |
| usuarios | password_hash | VARCHAR(255) | NOT NULL |
| usuarios | fecha_registro | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| usuarios | es_admin | BOOLEAN | NOT NULL, DEFAULT FALSE; habilita la vista de administración |
| categorias | id_categoria | BIGINT UNSIGNED | PK, AUTO_INCREMENT |
| categorias | nombre | VARCHAR(60) | NOT NULL, UNIQUE |
| categorias | descripcion | VARCHAR(255) | NULL |
| categorias | color_hex | CHAR(7) | NOT NULL, CHECK hexadecimal |
| habitos | id_habito | BIGINT UNSIGNED | PK, AUTO_INCREMENT |
| habitos | id_usuario | BIGINT UNSIGNED | FK → usuarios.id_usuario |
| habitos | id_categoria | BIGINT UNSIGNED | FK → categorias.id_categoria |
| habitos | nombre | VARCHAR(120) | NOT NULL |
| habitos | descripcion | VARCHAR(500) | NULL |
| habitos | tipo_meta | ENUM | BOOLEANO o CUANTITATIVO |
| habitos | meta_diaria | DECIMAL(10,2) | obligatoria si es cuantitativo |
| habitos | unidad_medida | VARCHAR(30) | obligatoria si es cuantitativo |
| habitos | activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| habitos | fecha_creacion | DATE | NOT NULL |
| etiquetas | id_etiqueta | BIGINT UNSIGNED | PK, AUTO_INCREMENT |
| etiquetas | nombre | VARCHAR(50) | NOT NULL, UNIQUE |
| habitos_etiquetas | id_habito | BIGINT UNSIGNED | PK/FK → habitos.id_habito |
| habitos_etiquetas | id_etiqueta | BIGINT UNSIGNED | PK/FK → etiquetas.id_etiqueta |
| registros_diarios | id_registro | BIGINT UNSIGNED | PK, AUTO_INCREMENT |
| registros_diarios | id_habito | BIGINT UNSIGNED | FK → habitos.id_habito |
| registros_diarios | fecha | DATE | UNIQUE junto con id_habito |
| registros_diarios | valor_completado | DECIMAL(10,2) | NOT NULL, CHECK >= 0 |
| registros_diarios | cumplido | BOOLEAN | NOT NULL, DEFAULT FALSE |
| registros_diarios | nota | VARCHAR(255) | NULL |
| registros_diarios | registrado_en | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

Las relaciones son 1:N entre `usuarios`–`habitos`, `categorias`–`habitos` y `habitos`–`registros_diarios`; `habitos`–`etiquetas` es N:M mediante `habitos_etiquetas`.

## Rutinas (procedimientos y funciones)

| Rutina | Tipo | Descripción |
|---|---|---|
| `sp_test_conectividad()` | Procedimiento | Prueba de conectividad: servidor, sesión, zona horaria y timestamp. |
| `sp_registrar_habito_diario(p_id_habito, p_fecha, p_valor, p_nota)` | Procedimiento | Inserta/actualiza el avance diario, valida la meta y calcula `cumplido`. |
| `fn_racha_habito(p_id_habito, p_hasta_fecha)` | Función | Devuelve la racha de días consecutivos cumplidos. |
