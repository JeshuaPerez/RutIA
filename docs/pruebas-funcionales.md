# Pruebas funcionales de RutaIA

Cada etapa agrega su sección con la misma tabla. La columna «Solicitudes a OpenRouter» cuenta las solicitudes gastadas de la capa gratuita (20 por minuto y 50 por día por cuenta).

## Etapa 0 — Entorno, Docker Compose y repositorio

Fecha: 2026-11-01 / 2026-11-02 · Máquina: Windows 11 Home 10.0.26200

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E0-01 | Archivo Docker Compose | `docker compose up -d` y `docker compose ps` | 3 contenedores Up; `postgres` healthy | OK: n8n, postgres (healthy) y qdrant Up, puertos en 127.0.0.1 | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-02 | Base de datos relacional PostgreSQL | `docker compose exec postgres psql -U rutaia_app -d rutaia -c "select version();"` | Devuelve PostgreSQL 17.x | OK: PostgreSQL 17.11 (Debian 17.11-1.pgdg13+2) | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-03 | Base de datos vectorial Qdrant | `curl.exe -s http://127.0.0.1:6333/healthz` | `healthz check passed` | OK: `healthz check passed` | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-04 | Automatizaciones en n8n | `curl.exe -s http://127.0.0.1:5678/healthz` | `{"status":"ok"}` | OK: `{"status":"ok"}` | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-05 | n8n alcanza la API en la máquina | `jwebserver -b 0.0.0.0 -p 8080 -d <docs>` y `docker compose exec n8n wget -qO- http://host.docker.internal:8080/` | Listado HTML de `docs/` | OK: listado HTML recibido, sin ajustes de firewall | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-06 | Claves fuera del código | Crear en n8n la credencial Header Auth «RutaIA OpenRouter» | Credencial existe con ese nombre exacto | OK: «RutaIA OpenRouter», tipo `httpHeaderAuth`, Name `Authorization`, Value con prefijo `Bearer ` (comprobado en el n8n del contenedor sin mostrar la clave) | `docs/evidencias/etapa0-credencial.txt` | 0 |
| E0-07 | Embeddings mediante OpenRouter | POST /api/v1/embeddings con `pruebas/n8n/openrouter-embeddings-query.json` | `data[0].embedding` con 2048 números finitos | OK: HTTP 200, `data[0].index` 0, 2048 números finitos | `docs/evidencias/etapa0-openrouter.txt` | 1 |
| E0-08 | Modelo de lenguaje mediante OpenRouter | POST /api/v1/chat/completions con `pruebas/n8n/openrouter-chat.json` | `choices[0].message.content` con texto | **BLOQUEADO**: 7 intentos (16:11–16:23) respondieron HTTP 429 «google/gemma-4-31b-it:free is temporarily rate-limited upstream». La clave y la privacidad están bien (las embeddings respondieron 200 con la misma clave). Repetir en otro horario | `docs/evidencias/etapa0-openrouter.txt` | 7 (todas 429) |
| E0-09 | Etiquetas exactas de imagen | Revisar `docker-compose.yml` | `postgres:17`, `qdrant/qdrant:v1.20.0`, `docker.n8n.io/n8nio/n8n:2.45.7`; ninguna `latest` | OK: `docker compose config --images` lista las tres etiquetas exactas | `docker-compose.yml` | 0 |
| E0-10 | Sin secretos en el repositorio | `git check-ignore .env` y `git status` | Imprime `.env`; `git status` no lista `.env` ni claves | OK: `git check-ignore .env` imprime `.env`; `git status` no lista `.env`; ningún archivo versionable contiene `sk-or-v1-…` ni una contraseña | `git status --short --untracked-files=all` | 0 |

Total de solicitudes a OpenRouter previstas en la Etapa 0: **2** (E0-07 y E0-08).

Solicitudes realmente enviadas (detalle en `docs/evidencias/etapa0-openrouter.txt`):
- 2 con una clave que no era una API key: rechazadas por autenticación (503 y 401), no llegaron a ningún modelo.
- 1 de embeddings: OK.
- 7 de chat: todas 429 «google/gemma-4-31b-it:free is temporarily rate-limited upstream» (proveedor gratuito saturado; no es la cuota de la cuenta). E0-08 queda pendiente de repetir en otro horario; si persiste, el equipo decide si cambia el modelo del contrato (C10).
- Total con la clave válida: 8 de las 50 diarias.

## Etapa 1 — Base de datos relacional

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · PostgreSQL 17.11 (contenedor `postgres`). Detalle en `docs/base-de-datos.md`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E1-01 | Script SQL (8.3) | Comandos de C12 con `01_esquema.sql` y `02_datos_semilla.sql`, dos pasadas seguidas | Código de salida 0 en las cuatro ejecuciones | OK: 0, 0, 0, 0; tras la segunda pasada 10 estudiantes y 30 cursos (máx. id 30) | `docs/evidencias/etapa1-ejecucion-manual.txt` | 0 |
| E1-02 | Instalación limpia | `docker compose stop postgres`, `docker compose rm -f postgres`, `docker volume rm rutaia_pgdata`, `docker compose up -d postgres` | `postgres` healthy; el log muestra `running …01_esquema.sql` y `…02_datos_semilla.sql`; los volúmenes de Qdrant y n8n siguen | OK: healthy en ~5 s; `INSERT 0 10` e `INSERT 0 30`; `rutaia_qdrant_storage` y `rutaia_n8n_data` intactos | `docs/evidencias/etapa1-instalacion-limpia.txt` | 0 |
| E1-03 | Datos de prueba (9) | `pruebas/bd/etapa1-conteos.sql` | 10 estudiantes; 28 activos y 2 inactivos; 7 categorías con activos (≥ 3 cada una); 3 niveles; IDs 1..10 y 1..30 | OK: 10; 28/2; 7 categorías (3 a 6 activos); Básico 10, Intermedio 13, Avanzado 7; IDs 1..10 y 1..30 | `docs/evidencias/etapa1-semilla.txt` | 0 |
| E1-04 | Descripciones aptas para búsqueda semántica | Contar palabras por descripción | Ninguna < 90 ni > 150 | OK: mínimo 130, máximo 146 | `docs/evidencias/etapa1-semilla.txt` | 0 |
| E1-05 | Nombres únicos | Consulta «nombre contenido en otro nombre» (`strpos(lower(b.nombre), lower(a.nombre)) > 0`) y nombres de < 4 palabras | 0 filas en ambas | OK: 0 y 0 | `docs/evidencias/etapa1-semilla.txt` | 0 |
| E1-06 | Codificación UTF8 | `SELECT count(*) FROM curso WHERE nivel = 'Básico'` | > 0 | OK: 10 | `docs/evidencias/etapa1-semilla.txt` | 0 |
| E1-07 | Catálogo sin temas ajenos | Buscar cocina/gastronomía/receta en nombres y descripciones | 0 filas | OK: 0 | `docs/evidencias/etapa1-semilla.txt` | 0 |
| E1-08 | RF 01 / correo único | `BEGIN`; insertar `valeria.gomez@rutaia.test`; `ROLLBACK` | Falla `uq_estudiante_correo` | OK | `docs/evidencias/etapa1-restricciones.txt` (R01) | 0 |
| E1-09 | RF 01 / correo normalizado | Insertar `Prueba.Mayusculas@RutaIA.test` | Falla `ck_estudiante_correo_normalizado` | OK | `docs/evidencias/etapa1-restricciones.txt` (R02) | 0 |
| E1-10 | RF 01 y RF 03 / niveles | Nivel `Experto` en estudiante y en curso | Fallan `ck_estudiante_nivel` y `ck_curso_nivel` | OK | `docs/evidencias/etapa1-restricciones.txt` (R03, R04) | 0 |
| E1-11 | Duración mayor que cero | `duracion_horas` 0 | Falla `ck_curso_duracion_positiva` | OK | `docs/evidencias/etapa1-restricciones.txt` (R05) | 0 |
| E1-12 | Cursos sin nombre o descripción | Nombre `'  '`; descripción `''` | Fallan `ck_curso_nombre_no_vacio` y `ck_curso_descripcion_no_vacia` | OK | `docs/evidencias/etapa1-restricciones.txt` (R06, R07) | 0 |
| E1-13 | Consulta de un estudiante existente | Consulta con `estudiante_id` 999999 | Falla `fk_consulta_estudiante` | OK | `docs/evidencias/etapa1-restricciones.txt` (R08) | 0 |
| E1-14 | RF 07 / pregunta y estados | Pregunta `'   '`; estado `Cerrada` | Fallan `ck_consulta_pregunta_no_vacia` y `ck_consulta_estado` | OK | `docs/evidencias/etapa1-restricciones.txt` (R09, R10) | 0 |
| E1-15 | RF 14 / fuentes y similitudes | Similitud 1.5; mismo curso dos veces como fuente | Fallan `ck_fuente_similitud` y `uq_fuente_recomendacion_curso` | OK | `docs/evidencias/etapa1-restricciones.txt` (R11, R12) | 0 |
| E1-16 | RF 17 / puntuación 1 a 5 | Puntuación 0 y 6 | Falla `ck_calificacion_puntuacion` en ambos | OK | `docs/evidencias/etapa1-restricciones.txt` (R13, R14) | 0 |
| E1-17 | RF 17 / una calificación por recomendación | Segunda calificación de la misma recomendación | Falla `uq_calificacion_recomendacion` | OK | `docs/evidencias/etapa1-restricciones.txt` (R15) | 0 |
| E1-18 | ROLLBACK sin residuos | Contar filas tras los 15 casos | 0 consultas, recomendaciones, fuentes y calificaciones; 10 estudiantes y 30 cursos | OK | `docs/evidencias/etapa1-restricciones.txt` (Control) | 0 |
| E1-19 | Diagrama entidad relación (rúbrica) | Revisar `docs/base-de-datos.md` | `erDiagram` en Mermaid con las 6 tablas y 5 relaciones | OK | `docs/base-de-datos.md` | 0 |

Total de solicitudes a OpenRouter en la Etapa 1: **0**.

Después de E1-08…E1-18 se recargaron `01` y `02` (E1-01) porque los `INSERT` fallidos consumen valores de las secuencias IDENTITY.

## Etapa 2 — API núcleo: estudiantes y cursos

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · Java 21.0.13 (Temurin) · Spring Boot 4.1.2 · springdoc-openapi 3.2.0. Comandos ejecutados con `curl` desde Git Bash en la raíz del repositorio; en PowerShell son idénticos cambiando `curl` por `curl.exe` (los cuerpos se envían con `--data-binary "@pruebas/api/<archivo>.json" -H "Content-Type: application/json"`). La API se arranca con `cd backend; .\gradlew.bat bootRun` (PowerShell) o `cd backend && ./gradlew bootRun` (bash). Salida completa en `docs/evidencias/etapa2-api.txt`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E2-00a | Claves fuera del código / arranque con el .env | `bootRun` con el `.env` de la raíz | Arranca; `ddl-auto=validate` sin errores | OK: «Started RutaiaApiApplication» | Log de `bootRun` | 0 |
| E2-00b | Mensaje claro si falta la contraseña | Renombrar `.env` y ejecutar `bootRun` | Falla indicando qué variable falta y cómo corregirlo | OK: «APPLICATION FAILED TO START … Falta la variable RUTAIA_DB_PASSWORD o está vacía…» | `docs/evidencias/etapa2-arranque-sin-password.txt` | 0 |
| E2-00c | `gradlew` ejecutable en Git | `git ls-files -s backend/gradlew` | Modo 100755 | OK: `100755` | `git ls-files -s` | 0 |
| E2-01 | RF 01 registrar | POST /api/estudiantes con `etapa2-estudiante-valido.json` (correo `  Ana.Torres@RutaIA.test `) | 201, `Location`, correo con trim y en minúsculas | OK: 201, `Location: http://localhost:8080/api/estudiantes/11`, correo `ana.torres@rutaia.test`, `fechaRegistro` en hora local | `etapa2-api.txt` (E2-01) | 0 |
| E2-02 | RF 01 impedir correos duplicados | POST con `etapa2-estudiante-correo-mayusculas.json` (`ANA.TORRES@rutaia.TEST`) | 409 | OK: 409, detalle `correo` «ya está registrado» | `etapa2-api.txt` (E2-02) | 0 |
| E2-03 | RF 01 niveles permitidos | POST con nivelExperiencia `Experto` y `principiante` | 400 con detalles | OK: 400 y 400, detalle `nivelExperiencia` «debe ser Principiante, Intermedio o Avanzado» | `etapa2-api.txt` (E2-03, E2-04) | 0 |
| E2-04 | RF 01 campos obligatorios | POST sin nombreCompleto; con correo `marta.ruiz-sin-arroba` | 400 con detalles | OK: 400 «es obligatorio» y 400 «debe ser un correo válido» | `etapa2-api.txt` (E2-05, E2-06) | 0 |
| E2-05 | 8.2 JSON mal formado | POST con `etapa2-json-mal-formado.json` | 400, `detalles` [] | OK | `etapa2-api.txt` (E2-07) | 0 |
| E2-06 | RF 02 listar | GET /api/estudiantes | 200, ids ascendentes (10 de la semilla) | OK: ids 1..10 de la semilla + 11 (alta de E2-01) | `etapa2-api.txt` (E2-08) | 0 |
| E2-07 | RF 02 consultar por id | GET /api/estudiantes/1 | 200 | OK | `etapa2-api.txt` (E2-09) | 0 |
| E2-08 | RF 02 estudiante inexistente | GET /api/estudiantes/999999 | 404 con cuerpo de C6 | OK: 404 «No existe un estudiante con id 999999», `detalles` [] | `etapa2-api.txt` (E2-10) | 0 |
| E2-09 | 8.2 parámetro inválido | GET /api/estudiantes/abc | 400 | OK: 400, detalle `id` «debe ser un número entero» | `etapa2-api.txt` (E2-11) | 0 |
| E2-10 | RF 04 solo activos | GET /api/cursos | 28 cursos, ninguno inactivo | OK: 28 (ids 1..28); 29 y 30 no aparecen | `etapa2-api.txt` (E2-12) | 0 |
| E2-11 | RF 04 filtrar por categoría y nivel | `?categoria=Desarrollo%20web`; `?nivel=B%C3%A1sico`; `?categoria=Programaci%C3%B3n&nivel=Intermedio` | Solo cursos activos que cumplen | OK: 4 (1–4); 9 Básico activos; 2 (6, 7) | `etapa2-api.txt` (E2-13 a E2-15) | 0 |
| E2-12 | C5 nivel inválido en el filtro | `?nivel=Experto`; `?nivel=Basico` | 400 | OK: 400 y 400, detalle `nivel` «debe ser Básico, Intermedio o Avanzado» | `etapa2-api.txt` (E2-16, E2-17) | 0 |
| E2-13 | «No podrán registrarse cursos sin nombre o descripción» | POST /api/admin/cursos con `etapa2-curso-sin-nombre.json` | 400 | OK: 400, detalle `nombre` «es obligatorio» | `etapa2-api.txt` (E2-18) | 0 |
| E2-14 | «La duración de un curso deberá ser mayor que cero» | POST con duracionHoras 0; con 1.5 | 400 | OK: 400 «debe ser mayor que cero»; 400 «tiene un tipo o formato inválido» | `etapa2-api.txt` (E2-19, E2-38) | 0 |
| E2-15 | RF 03 niveles del curso | POST con nivel `Basico` | 400 | OK | `etapa2-api.txt` (E2-20) | 0 |
| E2-16 | RF 03 registrar | POST con `etapa2-curso-valido.json` | 201, `Location`, `activo` true | OK: 201, `Location: …/api/admin/cursos/31`, `activo: true` | `etapa2-api.txt` (E2-21) | 0 |
| E2-17 | RF 03 consultar | GET /api/admin/cursos/31; /999999 | 200; 404 | OK: 200; 404 | `etapa2-api.txt` (E2-22, E2-31) | 0 |
| E2-18 | RF 03 actualizar | PUT /api/admin/cursos/31 con `etapa2-curso-actualizado.json`; id 999999; duracionHoras 0 | 200 (activo no cambia); 404; 400 | OK: 200 con datos nuevos y `activo: true`; 404; 400 | `etapa2-api.txt` (E2-23 a E2-25) | 0 |
| E2-19 | RF 03 desactivar | PATCH /api/admin/cursos/31/desactivar dos veces; id 999999 | 200 `activo` false (idempotente); 404 | OK: 200 y 200 con `activo: false`; 404 | `etapa2-api.txt` (E2-26 a E2-28) | 0 |
| E2-20 | RF 04 / RF 03 curso desactivado | GET /api/cursos y GET /api/admin/cursos tras desactivar | El 31 no está en el catálogo y sí en administración | OK: catálogo 28 (sin el 31); administración 31 con 3 inactivos (29, 30, 31) | `etapa2-api.txt` (E2-29, E2-30) | 0 |
| E2-21 | «Un correo electrónico solamente podrá pertenecer a un estudiante» (concurrencia) | 10 POST simultáneos con el mismo correo; y un POST mientras una transacción de psql tiene el mismo correo sin confirmar | Una sola alta; el resto 409 | OK: 1×201 y 9×409; el POST concurrente con psql recibió 409 tras la violación de `uq_estudiante_correo` en la BD | `etapa2-api.txt` (E2-35, E2-40) | 0 |
| E2-22 | 8.2 CORS | Preflight OPTIONS /api/cursos con `Origin: http://localhost:5500`; `Origin: http://otro.test`; POST con `Content-Type` desde `http://127.0.0.1:5500`; DELETE | Allow-Origin solo para los orígenes de `RUTAIA_CORS_ORIGINS` | OK: 200 con `Access-Control-Allow-Origin: http://localhost:5500`; 403 sin la cabecera; 200 con `Allow-Headers: Content-Type`; DELETE 403 | `etapa2-api.txt` (E2-32, E2-33, E2-36, E2-37) | 0 |
| E2-23 | 8.2 manejo global de errores | GET /api/no-existe; DELETE /api/estudiantes/1 | 404 y 405 con el cuerpo de C6 | OK | `etapa2-api.txt` (E2-39) | 0 |
| E2-24 | 8.2 Swagger / rúbrica | Abrir http://localhost:8080/swagger-ui.html | Título «RutaIA API», 3 etiquetas, 9 endpoints con descripción, ejemplos y códigos | OK: 9 `operationId`, etiquetas «Estudiantes», «Catálogo de cursos», «Administración de cursos» | `etapa2-swagger-endpoints.png`, `etapa2-swagger-e01-registrar-estudiante.png`, `etapa2-swagger-e05-catalogo.png`, `etapa2-swagger-e06-registrar-curso.png`, `etapa2-swagger-e10-desactivar-curso.png` | 0 |
| E2-25 | Sin secretos en el repositorio | `git grep` de la contraseña del `.env` y de `sk-or-v1-` sobre los archivos versionados | Sin coincidencias de valores reales | OK: la contraseña no aparece; `sk-or-v1-` solo como texto explicativo en la documentación | — | 0 |

Total de solicitudes a OpenRouter en la Etapa 2: **0**.

El error 500 genérico (mensaje fijo, traza solo en el log) está implementado, pero no se provocó en estas pruebas para no detener PostgreSQL.

Al terminar se recargaron `01_esquema.sql` y `02_datos_semilla.sql` (comandos de C12): 10 estudiantes (ids 1..10) y 30 cursos (28 activos y 2 inactivos, ids 1..30).
