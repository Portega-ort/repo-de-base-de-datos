# Habit Tracker — Documentación del programa

Plataforma web de **seguimiento de hábitos y rutinas saludables** pensada para que una
universidad ayude a sus estudiantes a mantener **constancia** en sus actividades diarias y a
**conectar sus talleres universitarios** (bienestar, salud, deporte, idiomas, finanzas,
productividad) con un sistema medible y fácil de usar.

Cada estudiante tiene su propia cuenta con contraseña segura, crea sus rutinas, registra su
avance diario y acumula rachas; los coordinadores y la dirección monitorean el cumplimiento en
tiempo real desde un panel general.

> **Propuesta comercial:** ver [docs/problematica_y_costos.md](docs/problematica_y_costos.md),
> documento fuente para generar la presentación de venta en Gemini Notebook.

---

## Contenido

- [Tecnología](#tecnología)
- [Funcionalidades](#funcionalidades)
- [Base de datos MySQL](#base-de-datos-mysql)
- [Cómo ejecutar la aplicación](#cómo-ejecutar-la-aplicación)
- [API REST](#api-rest)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Documentación complementaria](#documentación-complementaria)

---

## Tecnología

- **Backend:** Java (JDK) con `com.sun.net.httpserver` (sin frameworks) y conexión JDBC a MySQL.
- **Base de datos:** MySQL 8.0+ / InnoDB, con procedimientos almacenados, funciones y
  restricciones de integridad (`UNIQUE`, `CHECK`, llaves foráneas).
- **Frontend:** HTML, CSS y JavaScript (SPA) servidos por el mismo backend.
- **Autenticación:** contraseñas con hash **PBKDF2-HMAC-SHA256** (210 000 iteraciones) y sesiones
  con tokens en memoria (`Authorization: Bearer <token>`, vencimiento de 7 días).

## Funcionalidades

- **Cuenta propia**: registro, inicio/cierre de sesión y perfil editable (nombre, apellido,
  correo y nueva contraseña). Política de contraseña: 8+ caracteres con letra y número.
- **Hábitos del estudiante**: crear, listar y eliminar hábitos, cada uno con categoría, tipo de
  meta (*booleano* o *cuantitativo*), meta diaria y unidad de medida.
- **Registro diario**: avance por fecha con validación y cálculo automático de *cumplido*.
- **Expediente de constancia**: última semana por hábito, porcentaje de la semana y tabla de
  registros, con **racha de días consecutivos** calculada por el servidor.
- **Tour guiado**: asistente paso a paso al primer ingreso (recordado con `localStorage` y
  repetible con el botón «Guía»).
- **Sugerencias por categoría**: al elegir la categoría (ej. *Salud*, *Aprendizaje*, *Finanzas*),
  el sistema propone rutinas recomendadas y rellena el formulario con un clic.
- **Vista de administración** (solo usuarios con `es_admin = TRUE`): panel con resumen de la
  plataforma, actividad diaria de los últimos 14 días, hábitos por categoría y tabla de
  usuarios (hábitos, registros, % de cumplimiento y racha).

## Base de datos MySQL

1. Ejecuta `database/schema.sql` y después `database/seed.sql` en MySQL Workbench (o en la
   consola SQL de IntelliJ IDEA).
2. Los 5 usuarios de ejemplo usan la contraseña `Demo1234`.
3. Valida conectividad e integridad con:

```sql
CALL sp_test_conectividad();
CALL sp_registrar_habito_diario(1, '2026-08-29', 2.00, 'Prueba de integridad');
SELECT fn_racha_habito(1, '2026-08-28');
```

## Cómo ejecutar la aplicación

1. Configura `DB_URL`, `DB_USER` y `DB_PASSWORD` con los valores de `backend/.env.example`.
2. Lanza `com.retfinalo.Main` desde IntelliJ IDEA (la configuración *Backend* ya incluye las
   variables y `--add-modules jdk.httpserver`).
3. Abre [http://localhost:8080](http://localhost:8080). El servidor publica la API en `/api` y
   sirve el frontend (`frontend/src/`) en la raíz.

## API REST

| Método y ruta | Descripción | Auth |
|---|---|---|
| `GET /api/health` | Comprueba la conexión con MySQL | No |
| `POST /api/auth/register` | Crea una cuenta y devuelve perfil + token | No |
| `POST /api/auth/login` | Inicia sesión y devuelve perfil + token | No |
| `POST /api/auth/logout` | Invalida el token | Sí |
| `GET /api/auth/me` | Perfil del usuario | Sí |
| `PUT /api/auth/me` | Actualiza perfil (y contraseña si se envía `password`) | Sí |
| `GET /api/categorias` | Lista las categorías de hábitos | No |
| `GET /api/habitos` | Lista los hábitos activos del usuario | Sí |
| `POST /api/habitos` | Crea un hábito | Sí |
| `PUT /api/habitos/{id}` | Edita un hábito propio | Sí |
| `DELETE /api/habitos/{id}` | Elimina (baja lógica) un hábito propio | Sí |
| `GET /api/habitos/{id}/registros` | Progreso del hábito | Sí |
| `POST /api/registros` | Registra el avance diario `{idHabito, fecha, valor, nota}` | Sí |
| `GET /api/admin` | Reportes globales (solo admin) | Sí (admin) |

## Estructura del proyecto

- `database/`: scripts DDL y DML finales (`schema.sql`, `seed.sql`).
- `docs/`: documentación del programa.
  - `problematica_y_costos.md` — propuesta comercial para Gemini Notebook.
  - `analisis_y_diseno.md` — análisis de industria, diseño conceptual, diagrama E-R y normalización.
  - `diccionario_de_datos.md` — tipos de dato, PK/FK y reglas de cada campo.
- `backend/src/main/java/com/retfinalo/`: servidor y API REST (`auth`, `api`, `config`,
  `controller`, `model`, `repository`, `service`, `util`).
- `frontend/src/`: HTML, CSS y JavaScript de la interfaz (SPA con vistas de acceso, hábitos,
  progreso, perfil y administración).

## Documentación complementaria

| Recurso | Contenido |
|---|---|
| [docs/problematica_y_costos.md](docs/problematica_y_costos.md) | Propuesta comercial: problema, solución, beneficios para la universidad, escalabilidad y modelo de venta. Fuente para Gemini Notebook. |
| [docs/analisis_y_diseno.md](docs/analisis_y_diseno.md) | Análisis de industria, diseño conceptual, diagrama E-R con atributos, normalización (1FN–3FN) y pruebas de integridad/conectividad. |
| [docs/diccionario_de_datos.md](docs/diccionario_de_datos.md) | Tipos MySQL, PK/FK y reglas de cada campo, más rutinas. |
| [database/schema.sql](database/schema.sql) | Creación de las 6 tablas, índices, relaciones, procedimientos y función. |
| [database/seed.sql](database/seed.sql) | Datos de muestra: 5 usuarios, 5 categorías, 6 hábitos, 5 etiquetas, 10 relaciones M:N y 15 registros diarios. |