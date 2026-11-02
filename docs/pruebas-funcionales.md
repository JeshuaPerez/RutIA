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
