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

## Etapa 3 — Flujo «Indexación de cursos» y colección de Qdrant

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · n8n 2.45.7 y Qdrant v1.20.0 (contenedores) · API de la Etapa 2 con la semilla (30 cursos: 28 activos, ids 29 y 30 inactivos). Flujo `n8n/rutaia-indexacion-cursos.json` (ID `RutaIAIndexacion`), documentado en `docs/n8n.md`. Comandos con `curl` desde Git Bash; en PowerShell cambia `curl` por `curl.exe`. Salida completa en `docs/evidencias/etapa3-indexacion.txt` y `docs/evidencias/etapa3-qdrant.txt`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E3-01 | Rúbrica: JSON de los flujos | `import:workflow`, `list:workflow`, `publish:workflow --id=RutaIAIndexacion` y `docker compose restart n8n` | El flujo aparece y atiende `/webhook/rutaia-indexar-cursos` | OK: `RutaIAIndexacion\|RutaIA - Indexación de cursos`; el webhook de producción responde | `etapa3-indexacion.txt` (Importación) | 0 |
| E3-02 | Ahorro de cuota / errores claros | `docker compose stop qdrant`; `curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos`; `docker compose start qdrant` | 500 con mensaje; sin llamar a OpenRouter | OK: 500 `{"error":"Falló el paso «¿Existe la colección?»: getaddrinfo EAI_AGAIN qdrant"}`; nodos ejecutados hasta «¿Existe la colección?» → «Armar error» → «Responder error»; «Generar embeddings» no se ejecutó | `etapa3-indexacion.txt` (Ejecución 1) | 0 |
| E3-03 | RF 05 / 8.5 crear la colección e insertar | Primera ejecución del webhook con la colección inexistente | `coleccionCreada` true, 30 leídos, 30 guardados, 28/2, `totalPuntos` 30 | OK: `{"coleccion":"rutaia_cursos","coleccionCreada":true,"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}` | `etapa3-indexacion.txt` (Ejecución 2) | 1 |
| E3-04 | RF 05 reutilizar la colección y evitar duplicados | Segunda ejecución del webhook | `coleccionCreada` false, `totalPuntos` 30 | OK: `{"coleccion":"rutaia_cursos","coleccionCreada":false,"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}`; `points/count` exacto: 30 | `etapa3-indexacion.txt` (Ejecución 3), `etapa3-qdrant.txt` | 1 |
| E3-05 | 8.4 / C9 configuración de la colección | `curl -s http://127.0.0.1:6333/collections/rutaia_cursos` | `size` 2048, `distance` Cosine, índice `activo` bool, `points_count` 30 | OK: `vectors {"size":2048,"distance":"Cosine"}`, `payload_schema {"activo":{"data_type":"bool","points":30}}`, `points_count` 30, `status` green | `etapa3-qdrant.txt` | 0 |
| E3-06 | 8.4 payload / recuperación sin vectores | `POST /collections/rutaia_cursos/points/scroll` con `pruebas/n8n/etapa3-qdrant-scroll.json` (`{"limit":2,"with_payload":true,"with_vector":false}`) | Puntos 1 y 2 con el payload de C9 y sin vector | OK: ids 1 y 2, claves `cursoId, nombre, descripcion, categoria, nivel, duracionHoras, activo`, sin `vector` | `etapa3-qdrant.txt` | 0 |
| E3-07 | RF 05 relacionar cada punto con el curso relacional | Comparar el payload de los 30 puntos con `json_build_object(...)` de la tabla `curso` | Id del punto = `cursoId` = `curso.id`; payload idéntico; id=1 exacto | OK: 30 de 30 idénticos (mismas claves, orden y valores), incluido id=1 | `etapa3-indexacion.txt` (Payload contra PostgreSQL) | 0 |
| E3-08 | «Solo los cursos activos podrán recomendarse» | Scroll con filtro `activo = false` | Solo ids 29 y 30 | OK: 29 «Maquetación de sitios web con Bootstrap» y 30 «Despliegue de contenedores con Docker Swarm» | `etapa3-qdrant.txt` | 0 |
| E3-09 | RF 09 vector válido | Nodo «Validar embeddings» (cantidad, orden por `index`, 2048 números finitos) y revisión de los 30 vectores guardados | 30 vectores de 2048 números finitos | OK: 30 de 30 (scroll con `with_vector` true) | `etapa3-indexacion.txt` | 0 |
| E3-10 | Claves fuera del código y del repositorio | `export:workflow --id=RutaIAIndexacion --published --pretty` al mismo archivo; `grep -ciE "@\|outlook\|gmail\|sk-or-v1\|bearer "` | 0 coincidencias; credencial solo por referencia | OK: 0; `"httpHeaderAuth": {"id":"zLvfa0tPU6IKdFZW","name":"RutaIA OpenRouter"}`. Se quitó el bloque `shared` del export (nombre y correo del propietario de n8n) | `etapa3-indexacion.txt` (JSON exportado) | 0 |

Total de solicitudes a OpenRouter en la Etapa 3: **2** (E3-03 y E3-04; presupuesto ≤ 3). Cada ejecución del flujo envía los 30 textos en una sola solicitud.

No se probaron, para no gastar cuota ni alterar la colección: la respuesta 500 ante un error de OpenRouter (429/404/402) y ante una colección existente con otra configuración. La lógica de esos nodos Code se probó fuera de n8n con entradas simuladas (429, 404, vector con un `null`, 29 vectores para 30 cursos, colección de `size` 768).

## Etapa 4 — Calibración del umbral de similitud

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · n8n 2.45.7 y Qdrant v1.20.0 (contenedores) · colección `rutaia_cursos` de la Etapa 3 (30 puntos: 28 activos, 29 y 30 inactivos). Flujo W-3 `n8n/rutaia-calibracion-umbral.json` (ID `RutaIACalibracion`), documentado en `docs/n8n.md`. Comandos con `curl` desde Git Bash; en PowerShell cambia `curl` por `curl.exe`. Salida completa en `docs/evidencias/etapa4-calibracion.txt`, `docs/calibracion-resultados.json` y `docs/calibracion-umbral.md`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E4-01 | Rúbrica: JSON de los flujos | `import:workflow`, `list:workflow`, `publish:workflow --id=RutaIACalibracion` y `docker compose restart n8n` | El flujo aparece y atiende `/webhook/rutaia-calibrar-umbral` | OK: `RutaIACalibracion\|RutaIA - Calibración de umbral`; el webhook de producción responde | `etapa4-calibracion.txt` (Importación) | 0 |
| E4-02 | W-3 valida la entrada antes de gastar cuota | POST con `{"preguntas":[]}`, `{}` y `{"preguntas":["  "]}` | 500 con mensaje; sin llamar a OpenRouter | OK: 500 «… lista no vacía de textos» (dos) y «La pregunta 0 está vacía o no es texto»; nodos hasta «Validar preguntas» → «Armar error» | `etapa4-calibracion.txt` (Ejecuciones 4 a 6) | 0 |
| E4-03 | W-3 comprueba la colección antes de gastar cuota | `docker compose stop qdrant`; POST con una pregunta; `docker compose start qdrant` | 500 con mensaje; «Generar embeddings» no se ejecuta | OK: 500 `{"error":"Falló el paso «Leer colección»: getaddrinfo EAI_AGAIN qdrant"}` | `etapa4-calibracion.txt` (Ejecución 7) | 0 |
| E4-04 | 8.4 consulta semántica y recuperación del payload; C9 calibración | POST con `pruebas/n8n/etapa4-calibracion-preguntas.json` (7 temáticas + cocina) | 200; 8 resultados con 10 puntos cada uno; sin vectores; sin cursos inactivos | OK: 200; 8 × 10 puntos con `cursoId`, `nombre`, `categoria` y `score`; 0 `vector`; 29 y 30 no aparecen | `docs/calibracion-resultados.json` | 1 |
| E4-05 | RF 11 establecer y justificar un umbral | Tablas por consulta, S_cocina, S_pert y candidatos en `docs/calibracion-umbral.md` | Intervalo (S_cocina, S_pert) no vacío; umbral de 2 decimales con margen ≥ 0,02 | OK: S_cocina 0,2515; S_pert 0,3750 (C1, curso 3); candidatos 0,28–0,35; elegido **0,34** (deja 1 no pertinente frente a 3 del punto medio 0,31); márgenes 0,0885 y 0,0350 | `docs/calibracion-umbral.md` | 0 |
| E4-06 | RF 11 informar cuando ningún curso supere el umbral | Aplicar 0,34 a la consulta de cocina | 0 cursos | OK: máximo 0,2515 < 0,34 → 0 fuentes | `docs/calibracion-umbral.md` (Resultado esperado) | 0 |
| E4-07 | RF 10 recuperar entre tres y cinco cursos; RF 11 no enviar información irrelevante | Aplicar 0,34 y `limit` 5 a las 7 consultas temáticas | Cada una con ≥ 1 pertinente (ideal 3–5); informar las que queden con < 3 | OK: 4, 4, 4, 3, 5, 3 y 3 fuentes; 3 a 5 pertinentes en todas; ninguna con < 3; 1 no pertinente en total (curso 4 en C1) | `docs/calibracion-umbral.md` (Resultado esperado) | 0 |
| E4-08 | Claves fuera del código y del repositorio | `export:workflow --id=RutaIACalibracion --published --pretty`; `grep -ciE "@\|outlook\|gmail\|sk-or-v1\|bearer "` | 0 coincidencias; credencial solo por referencia | OK: 0; se quitó el bloque `shared` | `etapa4-calibracion.txt` (JSON exportado) | 0 |

Total de solicitudes a OpenRouter en la Etapa 4: **1** (E4-04; presupuesto ≤ 2). Las 8 preguntas van en una sola solicitud de embeddings.

No se probó contra n8n, para no gastar cuota ni tocar la colección, la respuesta 500 ante un error de OpenRouter (429/404/402) ni ante una colección inexistente o con otra configuración. La lógica de esos nodos Code se probó fuera de n8n con entradas simuladas (ver `etapa4-calibracion.txt`).

## Etapa 5 — Flujo «Consulta RAG»

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · n8n 2.45.7 y Qdrant v1.20.0 (contenedores) · API de la Etapa 2 con la semilla (28 cursos activos) · colección `rutaia_cursos` (30 puntos) · umbral 0.34 (Etapa 4). Flujo W-1 `n8n/rutaia-consulta-rag.json` (ID `RutaIAConsultaRAG`), documentado en `docs/n8n.md`. Cuerpos en `pruebas/n8n/etapa5-*.json` (consultaId de prueba desde 9001). Comandos con `curl` desde Git Bash; en PowerShell cambia `curl` por `curl.exe`. Salida completa en `docs/evidencias/etapa5-consulta-rag.txt`.

**Cambio de modelo del LLM:** `google/gemma-4-31b-it:free` respondió 429 «rate-limited upstream» en todos los intentos (como en E0-08) y la alternativa de C10 ya no tenía proveedores. A pedido del equipo, los tres flujos usan `google/gemma-4-26b-a4b-it:free`, con la clave gratuita de Google AI Studio de la cuenta agregada en OpenRouter (BYOK, sección *Prioritized*). Hay que registrar el cambio en el contrato (C8 y C10).

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E5-01 | Rúbrica: JSON de los flujos | `import:workflow`, `publish:workflow --id=RutaIAConsultaRAG`, `docker compose restart n8n` y `list:workflow` | El flujo aparece y atiende `/webhook/rutaia-consulta-rag` | OK: `RutaIAConsultaRAG\|RutaIA - Consulta RAG`; el webhook de producción responde | `etapa5-consulta-rag.txt` (Importación) | 0 |
| E5-02 | RF 09 validar la pregunta; «Las preguntas vacías no deberán enviarse a n8n» | POST con `etapa5-09-pregunta-espacios.json` (`"   "`), `etapa5-10-pregunta-ausente.json` y `etapa5-11-consulta-id-texto.json` (`"abc"`) | 400 `Error`; sin llamar a OpenRouter | OK: 400 «Entrada inválida: pregunta debe ser un texto no vacío» (dos) y «… consultaId debe ser un entero positivo»; nodos: Webhook > Configuración > Validar entrada > Armar error 400 > Responder 400 | `etapa5-consulta-rag.txt` (Ejecuciones 17, 22…) | 0 |
| E5-03 | RF 10–13; 8.5 flujo «Consulta RAG» | POST con las 7 temáticas `etapa5-01` a `etapa5-07` | 200 `Respondida`; 1 a 5 fuentes (ideal 3–5) en orden descendente, activas, ≥ 0,34, con 7 campos y sin vectores; texto plano que solo nombra cursos de las fuentes | OK: 7 de 7 `Respondida`; 4, 4, 4, 3, 5, 3 y 3 fuentes (las mismas de `docs/calibracion-umbral.md`); orden descendente; todas activas y ≥ 0,34; 7 campos; similitud con ≤ 4 decimales; 0 vectores; sin Markdown ni HTML; 0 cursos ajenos nombrados; 143 a 171 palabras | `etapa5-consulta-rag.txt` (Las 7 temáticas, Verificación de criterios) | 14 |
| E5-04 | RF 11 informar cuando ningún curso supere el umbral; «… deberá finalizar como Sin resultados» | POST con `etapa5-08-cocina.json` | 200 `Sin resultados`, `fuentes` []; el nodo del LLM no corre | OK: `{"consultaId":9008,…,"estado":"Sin resultados","respuesta":null,"fuentes":[],"error":null}`; nodos hasta «¿Hay fuentes?» > Responder Sin resultados | `etapa5-consulta-rag.txt` (Ejecución 20) | 1 |
| E5-05 | «Si n8n, Qdrant u OpenRouter fallan … Error»; ahorro de cuota | Detener la API (`bootRun`) y POST con `etapa5-01` | 502 `Error`; sin gastar cuota | OK: 502 «La API (GET /cursos) no respondió (ECONNREFUSED) …»; «Embedding de la pregunta» no se ejecutó | `etapa5-consulta-rag.txt` (Ejecución 25) | 0 |
| E5-06 | Ídem, Qdrant | `docker compose stop qdrant`; POST con `etapa5-07`; `docker compose start qdrant` | 502 `Error` | OK: 502 «Qdrant (/points/query) no respondió (EAI_AGAIN): getaddrinfo EAI_AGAIN qdrant»; Qdrant de nuevo arriba con 30 puntos | `etapa5-consulta-rag.txt` (Ejecución 24) | 1 |
| E5-07 | Ídem, OpenRouter | Sin internet en la máquina (ocurrió durante la prueba) y modelo con 429 del proveedor | 502 `Error` con el código | OK: 502 «OpenRouter (/embeddings) no respondió (ENOTFOUND) …» y «OpenRouter (/chat/completions) respondió HTTP 429: Provider returned error · … rate-limited upstream» | `etapa5-consulta-rag.txt` (Ejecuciones 18–19, 21, 23 y cambio de modelo) | 0 (sin internet); los 429 se cuentan en el total |
| E5-08 | «Solo los cursos activos podrán recomendarse»; «Los cursos mostrados como fuentes deberán existir en la base relacional» | `PATCH /api/admin/cursos/3/desactivar` sin re-indexar; POST con `etapa5-12-paginas-web-sin-curso-3.json`; `UPDATE curso SET activo = true WHERE id = 3;` y re-indexar | El curso 3 no aparece ni en las fuentes ni en la respuesta | OK: Qdrant aún devolvió el 3 (0,3750, `activo` true en el payload) y «Filtrar fuentes» lo quitó; fuentes 1, 2 y 4; la respuesta no lo nombra. Restaurado: 28 activos, re-indexación `{"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}` | `etapa5-consulta-rag.txt` (Desactivación, Restauración) | 3 |
| E5-09 | RF 12 instrucciones del modelo | Revisar «Construir mensaje» en la ejecución 23 | Mensaje `user` único con el texto literal y el contexto por fuente | OK: texto literal; `[n] nombre \| Categoría \| Nivel \| Duración \| Similitud` y `Descripción:` por fuente, en orden | `docs/n8n.md` (Mensaje enviado al modelo) | 0 |
| E5-10 | Claves fuera del código y del repositorio | `export:workflow --published --pretty` de los tres flujos; `grep -ciE "@\|outlook\|gmail\|sk-or-v1\|bearer "` | 0 coincidencias; credencial solo por referencia | OK: 0, 0 y 0; se quitó el bloque `shared` | `etapa5-consulta-rag.txt` (JSON exportado, Cambio de modelo) | 0 |

Total de solicitudes a OpenRouter en la Etapa 5: **41**. Presupuesto de la etapa: ≤ 25; **superado en 16**. De las 41, 22 se gastaron en consultas fallidas (embedding + LLM) mientras se buscaba un modelo que respondiera: 6 en 3 consultas con 429 de `gemma-4-31b-it:free`, 4 en 2 consultas con `nemotron-3.5-super-vl:free` (agota `max_tokens` razonando y devuelve `content` null) y 12 en 6 consultas con 429 de `gemma-4-26b-a4b-it:free` antes de agregar la clave propia. Esas 6 se lanzaron seguidas y debieron cortarse al primer 429. Las pruebas válidas gastaron 19: 14 + 1 + 1 + 3 (E5-03, E5-04, E5-06 y E5-08).

No se probaron contra n8n, para no gastar cuota: la respuesta 502 cuando el modelo nombra un curso ajeno a las fuentes, ni la normalización con Markdown real (el modelo respondió en texto plano en los 8 casos). La lógica de «Normalizar salida» y «Validar salida» se probó fuera de n8n con entradas simuladas (Markdown, viñetas, `<br>` y un curso ajeno).

## Etapa 6 — API de consultas inteligentes (E-11)

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · Java 21.0.13 (Temurin) · Spring Boot 4.1.2 · springdoc-openapi 3.2.0 · n8n 2.45.7 con W-1 publicado (Etapa 5) · semilla de la Etapa 1 (10 estudiantes, 28 cursos activos). La API se arranca con `cd backend; .\gradlew.bat bootRun` (PowerShell) o `cd backend && ./gradlew bootRun` (bash), con `RUTAIA_N8N_WEBHOOK_URL` y `RUTAIA_N8N_TIMEOUT_SEGUNDOS` en el `.env` (C11). Comandos con `curl` desde Git Bash en la raíz del repositorio; en PowerShell son idénticos cambiando `curl` por `curl.exe`: `curl.exe -s -i -X POST "http://localhost:8080/api/consultas" --data-binary "@pruebas/api/<archivo>.json" -H "Content-Type: application/json"`. `docker compose stop n8n` y `docker compose start n8n` se escriben igual en ambas consolas. Cuerpos en `pruebas/api/etapa6-01` a `etapa6-10` (las 7 temáticas, cocina, pregunta con espacios y estudiante inexistente); en esta etapa se ejecutaron 01, 02, 07, 08, 09 y 10 (las otras 4 temáticas quedan para la integración). Salida completa, con las respuestas y las filas de la BD, en `docs/evidencias/etapa6-api.txt`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E6-01 | RF 06–08, RF 13, RF 14 | POST /api/consultas con `etapa6-01-paginas-web.json` (estudiante 1) | 201 Respondida, `Location`, 1 a 5 fuentes de PostgreSQL; recomendacion y recomendacion_fuente guardadas | OK: 201, `Location: …/api/consultas/2`, `recomendacionId` 1, 4 fuentes (1, 2, 3, 4; 0,593 → 0,3644). BD: consulta 2 `Respondida` sin `detalle_error`; recomendacion 1 (consulta 2, 965 caracteres); recomendacion_fuente 1–4 | `etapa6-api.txt` (E6-01, Estado de la BD) | 2 |
| E6-02 | Ídem | POST con `etapa6-02-java-spring.json` (estudiante 2) | Ídem | OK: 201, consulta 3, `recomendacionId` 2, 4 fuentes (5, 6, 7, 8; 0,6286 → 0,3974). BD: consulta 3 `Respondida`; recomendacion 2; recomendacion_fuente 5–8 | `etapa6-api.txt` (E6-02) | 2 |
| E6-03 | Ídem | POST con `etapa6-07-contenedores.json` (estudiante 7) | Ídem | OK: 201, consulta 4, `recomendacionId` 3, 3 fuentes (24, 26, 25; 0,6088 → 0,4204). BD: consulta 4 `Respondida`; recomendacion 3; recomendacion_fuente 9–11 | `etapa6-api.txt` (E6-07) | 2 |
| E6-04 | RF 13 formato de ConsultaResultado y Fuente (C7) | Revisar las respuestas de E6-01 a E6-03 | 9 campos; fuentes con 7 campos, datos del curso de PostgreSQL y similitud de n8n con 4 decimales, en orden descendente; `mensaje` null | OK: 9 campos; 7 campos por fuente; orden descendente; todas activas en PostgreSQL; respuesta en texto plano (sin `*`, `#`, `<` ni acentos graves) | `etapa6-api.txt` | 0 |
| E6-05 | «Si Qdrant no devuelve resultados relevantes … Sin resultados» | POST con `etapa6-08-cocina.json` (estudiante 8) | 201 Sin resultados, MSG_SIN_RESULTADOS, sin fila en recomendacion | OK: 201, consulta 5 `Sin resultados`, `mensaje` = MSG_SIN_RESULTADOS, `recomendacionId` null, `fuentes` []. BD: consulta 5 `Sin resultados`; sin fila en recomendacion | `etapa6-api.txt` (E6-08) | 1 |
| E6-06 | «Las preguntas vacías no deberán enviarse a n8n» | POST con `etapa6-09-pregunta-espacios.json` (`"   "`), con n8n arriba y con n8n detenido | 400 en ambos; sin fila nueva en consulta ni ejecución nueva de W-1 | OK: 400 y 400, detalle `pregunta` «es obligatoria». consulta seguía con 0 filas; la última ejecución de W-1 seguía siendo la 48 (anterior a esta etapa) | `etapa6-api.txt` (E6-09, E6-11) | 0 |
| E6-07 | «Toda consulta deberá pertenecer a un estudiante existente» | POST con `etapa6-10-estudiante-inexistente.json` (estudianteId 999999) | 404, sin fila nueva en consulta | OK: 404 «No existe un estudiante con id 999999», `detalles` []; consulta con 0 filas | `etapa6-api.txt` (E6-10) | 0 |
| E6-08 | RF 07 guardar antes de pedir la recomendación; «Si n8n … fallan … Error» | `docker compose stop n8n` (nada escucha en el 5678); POST con `etapa6-07-contenedores.json`; `docker compose start n8n` | 201 Error con MSG_ERROR; consulta en Error con el motivo en `detalle_error`; sin gastar cuota | OK: 201, consulta 1 `Error`, `mensaje` = MSG_ERROR, `fuentes` []. BD: `detalle_error` = «n8n no respondió: conexión rechazada». Log: `Consulta 1 finalizada: estado=Error, n8n=98 ms, motivo=…`. n8n de nuevo arriba (readiness 200) | `etapa6-api.txt` (E6-12) | 0 |
| E6-09 | RF 07 «ninguna consulta queda en Pendiente» | `select estado, count(*) from consulta group by estado` al terminar | 0 Pendiente | OK: Error 1, Respondida 3, Sin resultados 1 | `etapa6-api.txt` (Estado de la BD) | 0 |
| E6-10 | Logs SLF4J | Revisar el log de `bootRun` | consultaId, estado final, duración de la llamada a n8n y motivo del error; sin secretos ni respuesta del modelo | OK: una línea por consulta (`n8n=7272 ms` a `8440 ms` en las temáticas, `752 ms` en cocina, `98 ms` con n8n detenido); no se registra la respuesta | `etapa6-api.txt` (Logs de la API) | 0 |
| E6-11 | C8 respuestas inválidas de n8n → Error | Revisión del código (abajo) y verificación con un n8n simulado en una segunda instancia de la API (puerto 8081) | Error con `detalle_error`; sin recomendación | OK: 11 de 11 casos inválidos → 201 Error con su motivo; el caso válido reordena las fuentes y redondea a 4 decimales. Las filas simuladas se borraron después | `etapa6-api.txt` (Verificación complementaria) | 0 |
| E6-12 | 8.2 Swagger | Abrir http://localhost:8080/swagger-ui.html | Etiqueta «Consultas» con E-11; ejemplos 201 Respondida, Sin resultados y Error, 400 y 404 | OK: 4 etiquetas; `registrarConsulta` con respuestas 201 (3 ejemplos), 400, 404 y 500 | `etapa6-swagger-endpoints.png`, `etapa6-swagger-e11-consultas.png` | 0 |
| E6-13 | Sin secretos en el repositorio | `git grep` de la contraseña del `.env` sobre el repositorio | Sin coincidencias | OK: 0 coincidencias; la URL y el timeout de n8n se leen de `RUTAIA_N8N_*` | — | 0 |

Total de solicitudes a OpenRouter en la Etapa 6: **7** (3 temáticas × 2 —embedding y LLM— + 1 de cocina, solo embedding). Presupuesto: ≤ 10.

### Revisión del código: respuestas de n8n que llevan a Error

El servicio de E-11 usa tres pasos separados: `ConsultaTransacciones.registrarPendiente` inserta la consulta en Pendiente y confirma (`backend/src/main/java/com/rutaia/servicio/ConsultaTransacciones.java:57`); `ConsultaServicio.registrar` llama a n8n sin transacción (`ConsultaServicio.java:48`; la clase no tiene `@Transactional`); y el estado final se guarda en `guardarRespondida`, `guardarSinResultados` o `guardarError`, cada uno en su propia transacción (`ConsultaTransacciones.java:73`, `106` y `113`). `ClienteN8n` configura el connect timeout de 5 s y el read timeout de `RUTAIA_N8N_TIMEOUT_SEGUNDOS` en `JdkClientHttpRequestFactory` (`integracion/ClienteN8n.java:41` y `44`) y no reintenta.

| Caso | Dónde se detecta | Qué pasa |
|---|---|---|
| consultaId distinto del enviado | `ConsultaServicio.validar`, `ConsultaServicio.java:90` | Lanza `RespuestaN8nInvalidaException` antes de abrir la transacción final; `ConsultaServicio.java:64` la captura y `guardarError` deja la consulta en Error con «n8n devolvió consultaId X, distinto del enviado Y». |
| Respuesta vacía o solo espacios en una Respondida | `ConsultaServicio.java:103` | Ídem, con «n8n devolvió Respondida con la respuesta vacía». |
| Fuente inexistente en PostgreSQL | `ConsultaTransacciones.guardarRespondida`, `ConsultaTransacciones.java:86` | Los cursos se leen con `findAllById` dentro de la transacción final; si falta uno, lanza la excepción **antes** de guardar la recomendación (`:94`), la transacción se revierte sin escribir nada y `guardarError` registra «La fuente con cursoId X no existe en PostgreSQL». |
| Fuente inactiva | `ConsultaTransacciones.java:90` | Ídem, con «… está inactiva en PostgreSQL». |

Las otras reglas de C8 siguen el mismo camino: estado Error o fuera de C5, 0 o más de 5 fuentes, cursoId ausente o repetido y similitud fuera de [0, 1] (`ConsultaServicio.validar`); conexión rechazada, timeout, HTTP no-2xx y cuerpo ilegible (`ClienteN8n`, `FallaN8nException`). Cualquier otra excepción al guardar el resultado (`ConsultaServicio.java:67`) también deja la consulta en Error, para que ninguna petición termine en Pendiente. La verificación con el n8n simulado (E6-11) confirmó los cuatro casos de la tabla y los demás.

No se ejecutaron en esta etapa, para cuidar la cuota, las temáticas `etapa6-03` a `etapa6-06` (quedan para la integración), ni el timeout real de 90 s contra n8n (el timeout de lectura se comprobó con el simulado y 2 s).

## Etapa 7 — Historial, detalle, calificaciones y estadísticas (E-04, E-12, E-13, E-14)

Fecha: 2026-11-02 · Máquina: Windows 11 Home 10.0.26200 · Java 21.0.13 (Temurin) · Spring Boot 4.1.2 · springdoc-openapi 3.2.0 · BD con las consultas 1–5 de la Etapa 6 (1 Error, 3 Respondida con las recomendaciones 1–3, 1 Sin resultados) y sin calificaciones. Las pruebas se ejecutaron contra el jar de esta etapa en el puerto 8081 (`java -jar build/libs/rutaia-api-0.0.1-SNAPSHOT.jar --server.port=8081 --logging.level.org.hibernate.SQL=debug`, desde `backend/`), porque en el 8080 seguía corriendo el `bootRun` de la Etapa 6; el log de SQL sirve para contar las sentencias de cada petición. Con `bootRun` en el 8080 los comandos son los mismos cambiando el puerto. Comandos con `curl` desde Git Bash en la raíz del repositorio; en PowerShell son idénticos cambiando `curl` por `curl.exe`: `curl.exe -s -i -X POST "http://localhost:8080/api/recomendaciones/1/calificacion" --data-binary "@pruebas/api/<archivo>.json" -H "Content-Type: application/json"`. Cuerpos en `pruebas/api/etapa7-01` a `etapa7-08`. SQL de control en `pruebas/bd/etapa7-control-estadisticas.sql`, y datos temporales en `pruebas/bd/etapa7-datos-temporales.sql` y `etapa7-limpieza.sql` (se ejecutan en bash con `docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < pruebas/bd/<archivo>.sql`; en PowerShell, con `docker compose cp` y `psql -f`, porque PowerShell 5.1 no admite `<`). Salida completa en `docs/evidencias/etapa7-api.txt`.

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E7-01 | RF 02 / RF 16 historial, orden y mensajes fijos | GET /api/estudiantes/7/historial | 200; fecha desc; Respondida con resumen y cursos por similitud; Error con MSG_ERROR | OK: consulta 4 (22:34:47, Respondida, cursos 24, 26, 25) antes que la 1 (22:33:40, Error, resumen = MSG_ERROR, `cursosRecomendados` []) | `etapa7-api.txt` (E7-02) | 0 |
| E7-02 | RF 16 resumen de hasta 160 caracteres con «…» | GET /api/estudiantes/1/historial (respuesta de 965 caracteres) | Los primeros 160 caracteres más «…» | OK: 161 caracteres, igual a `left(respuesta, 160) \|\| '…'` en psql | `etapa7-api.txt` (E7-03) | 0 |
| E7-03 | RF 16 Sin resultados | GET /api/estudiantes/8/historial | resumen = MSG_SIN_RESULTADOS, cursos [] | OK | `etapa7-api.txt` (E7-04) | 0 |
| E7-04 | RF 16 límite exacto del resumen y Pendiente | Datos temporales del estudiante 3: Pendiente, Respondida de 160 caracteres y Respondida de 161, con fuentes insertadas en orden de similitud ascendente; GET historial y GET /api/consultas/{id} de la Pendiente | 160 sin «…»; 161 cortada a 160 más «…»; Pendiente con MSG_PENDIENTE; cursos por similitud desc | OK: resúmenes de 160 (sin «…») y 161 caracteres; Pendiente «Consulta en proceso.» en E-04 y como `mensaje` en E-12; cursos 9 (0,52) antes que 10 (0,41). Los datos se borraron al terminar | `etapa7-api.txt` (E7-28, E7-29) | 0 |
| E7-05 | RF 02 estudiante sin consultas | GET /api/estudiantes/3/historial | 200 [] | OK | `etapa7-api.txt` (E7-05) | 0 |
| E7-06 | RF 02 «Responder adecuadamente cuando el estudiante no exista» | GET /api/estudiantes/999999/historial; /abc/historial | 404; 400 | OK: 404 «No existe un estudiante con id 999999»; 400 detalle `id` «debe ser un número entero» | `etapa7-api.txt` (E7-06, E7-07) | 0 |
| E7-07 | Eficiencia de E-04 (sin N+1) | Contar las sentencias de `org.hibernate.SQL` en el log | Cantidad fija, sin una por consulta | OK: 4 sentencias (existe el estudiante, consultas, recomendaciones, fuentes con JOIN FETCH del curso) para 2 consultas; 2 para un estudiante sin consultas | `etapa7-api.txt` (E7-08) | 0 |
| E7-08 | RF 16 detalle = E-11 + calificación | GET /api/consultas/1 a /5 y comparación con las respuestas de E-11 guardadas en `etapa6-api.txt` | Mismos 9 campos y valores, más `calificacion` | OK: 5 de 5 iguales (Error, 3 Respondida, Sin resultados), `calificacion` null | `etapa7-api.txt` (E7-09) | 0 |
| E7-09 | E-12 inexistente / id inválido y eficiencia | GET /api/consultas/999999; /abc; contar SQL | 404; 400; sin N+1 | OK: 404 «No existe una consulta con id 999999»; 400; 4 sentencias para una Respondida (consulta, recomendación, fuentes con JOIN FETCH, calificación) y 1 para Sin resultados | `etapa7-api.txt` (E7-11 a E7-13) | 0 |
| E7-10 | RF 17 puntuación de 1 a 5 con o sin comentario | POST /api/recomendaciones/1/calificacion con `etapa7-01` (5 y comentario); /2 con `etapa7-02` (4, sin comentario); /3 con `etapa7-08` (4 y comentario de exactamente 500 caracteres) | 201 con `Location` | OK: 201, 201 y 201; `Location: …/api/recomendaciones/{id}/calificacion`; `comentario` null en la 2; 500 caracteres guardados en la 3 | `etapa7-api.txt` (E7-14, E7-15, E7-25) | 0 |
| E7-11 | Regla «solamente … valores entre uno y cinco»; comentario ≤ 500 | POST /3 con puntuación 0, 6, 3.5, ausente; comentario de 501; JSON mal formado | 400 sin crear la calificación | OK: 400 «debe estar entre 1 y 5» (0 y 6), «tiene un tipo o formato inválido» (3.5), «es obligatoria» (ausente), `comentario` «debe tener como máximo 500 caracteres», «El cuerpo de la solicitud no es un JSON válido»; la recomendación 3 seguía con 0 calificaciones | `etapa7-api.txt` (E7-16 a E7-21) | 0 |
| E7-12 | E-13 recomendación inexistente | POST /999999/calificacion; /abc/calificacion | 404; 400 | OK: 404 «No existe una recomendación con id 999999»; 400 | `etapa7-api.txt` (E7-22, E7-23) | 0 |
| E7-13 | RF 17 «una sola calificación por recomendación» | Segundo POST a /1 | 409 | OK: 409 «La recomendación 1 ya fue calificada», `detalles` [] | `etapa7-api.txt` (E7-24) | 0 |
| E7-14 | Ídem con concurrencia y con la restricción única | 10 POST simultáneos a una recomendación temporal; POST mientras psql tiene otra calificación de la misma recomendación sin confirmar | Una sola calificación; el resto 409, también si salta `uq_calificacion_recomendacion` | OK: 1×201 y 9×409 (los 9 los detectó la comprobación previa); el POST concurrente con psql esperó el commit y respondió 409 tras `duplicate key value violates unique constraint "uq_calificacion_recomendacion"` en el log | `etapa7-api.txt` (E7-31, E7-32) | 0 |
| E7-15 | E-12 con calificación | GET /api/consultas/2 y /3 | `calificacion` {puntuacion, comentario, fecha} | OK: {5, «La ruta me sirvió…», fecha} y {4, null, fecha} | `etapa7-api.txt` (E7-26) | 0 |
| E7-16 | RF 18 estadísticas sin calificaciones | GET /api/estadisticas antes de calificar; SQL de control | Coincide con count; promedio null | OK: 5, 3, 1, `promedioCalificaciones` null; psql 5, 3, 1 y avg NULL con 0 calificaciones | `etapa7-api.txt` (E7-01) | 0 |
| E7-17 | RF 18 promedio con 2 decimales | GET /api/estadisticas con calificaciones 5, 4 y 4; SQL de control | 4.33 | OK: 4.33 en la API y `round(avg(puntuacion)::numeric, 2)` = 4.33 | `etapa7-api.txt` (E7-27, E7-33) | 0 |
| E7-18 | RF 18 desempate del curso más recomendado | Con los datos de la Etapa 6 (11 cursos con 1 fila cada uno) y con los temporales (cursos 9 y 10 con 2 filas cada uno) | Gana el menor cursoId | OK: curso 1 con `vecesRecomendado` 1; con el empate en 2, curso 9 («Consultas SQL para análisis de datos»); tras la limpieza, de nuevo el 1. Coincide con el SQL de control en los tres casos | `etapa7-api.txt` (E7-01, E7-30, E7-33) | 0 |
| E7-19 | 8.2 CORS | Preflight OPTIONS de E-13 desde `http://localhost:5500`; GET de E-14 desde `http://127.0.0.1:5500` y desde `http://otro.test` | Permitido solo para los orígenes de `RUTAIA_CORS_ORIGINS` | OK: 200 con `Access-Control-Allow-Origin` y `Allow-Headers: Content-Type`; 200 con la cabecera; 403 «Invalid CORS request» | `etapa7-api.txt` (E7-34) | 0 |
| E7-20 | Rúbrica «evidencia de Swagger» / 8.2 | `curl -s http://localhost:8081/v3/api-docs` guardado en `docs/openapi.json` (con sangría) | E-01 a E-14 con etiquetas, ejemplos y códigos | OK: 14 operaciones (E-01 a E-14), 7 etiquetas (las 3 nuevas: «Historial y detalle», «Calificaciones», «Estadísticas»), 60 ejemplos JSON válidos; E-13 con 201, 400, 404, 409 y 500 | `docs/openapi.json` | 0 |
| E7-21 | Rúbrica: capturas de Swagger UI | Se pidieron capturas ejecutando E-11, E-04, E-13 y E-14 en http://localhost:8080/swagger-ui.html | Capturas con la petición y la respuesta | PARCIAL: el usuario entregó una sola captura, la documentación de respuestas de E-02 (`GET /api/estudiantes`: 200 «Lista de estudiantes» con su ejemplo y 500 «Error inesperado» con el cuerpo de C6), sin ejecutar. No hay capturas de E-11, E-04, E-13 ni E-14; su evidencia es `docs/openapi.json` y `etapa7-api.txt` | `etapa7-swagger-e02-respuestas.png` | 0 |

Total de solicitudes a OpenRouter en la Etapa 7: **0**. Presupuesto: ≤ 4.

### Consultas SQL de control de las estadísticas

Contenido de `pruebas/bd/etapa7-control-estadisticas.sql`. Cada valor de GET /api/estadisticas debe coincidir con la fila correspondiente:

```sql
-- totalConsultas, consultasRespondidas, consultasSinResultados
SELECT count(*)                                         AS total_consultas,
       count(*) FILTER (WHERE estado = 'Respondida')     AS consultas_respondidas,
       count(*) FILTER (WHERE estado = 'Sin resultados') AS consultas_sin_resultados
FROM consulta;

-- promedioCalificaciones (NULL si no hay calificaciones)
SELECT round(avg(puntuacion)::numeric, 2) AS promedio_calificaciones, count(*) AS calificaciones
FROM calificacion;

-- cursoMasRecomendado: la primera fila
SELECT f.curso_id, c.nombre, count(*) AS veces_recomendado
FROM recomendacion_fuente f
JOIN curso c ON c.id = f.curso_id
GROUP BY f.curso_id, c.nombre
ORDER BY veces_recomendado DESC, f.curso_id ASC
LIMIT 5;
```

En la API, los tres conteos salen de una sola consulta JPQL con `count(case when …)` (`ConsultaRepositorio.contarPorEstado`), el promedio de `avg(c.puntuacion)` redondeado a 2 decimales con `HALF_UP` (`EstadisticasServicio.java:35`), y el curso de un `group by … order by count(f) desc, cu.id asc` limitado a 1 fila (`RecomendacionFuenteRepositorio.buscarMasRecomendados`). Ninguna carga entidades.

### Decisiones donde el contrato no dice nada

- **Location de E-13.** C6 pide `Location` en todo 201, pero el contrato no define un GET de la calificación. La cabecera apunta a la misma URL del POST (`/api/recomendaciones/{id}/calificacion`), porque la calificación es un subrecurso único de la recomendación; esa URL no responde a GET.
- **Comentario vacío.** El comentario se guarda sin espacios al inicio o al final, y uno vacío o de solo espacios se guarda como null (igual que la pregunta en E-11, que también se recorta). La longitud máxima se comprueba después del recorte.
- **Resumen de 160 caracteres.** Se siguió C7 al pie de la letra: 160 caracteres de la respuesta más «…», es decir, 161 en total cuando se corta. Se cuentan caracteres Unicode (code points), no bytes ni unidades UTF-16, y no se recortan los saltos de línea ni los espacios del corte.
- **Desempate del historial.** Las fechas tienen precisión de segundos; dos consultas del mismo segundo se ordenan por id descendente.
- **E-11 sin cambios de comportamiento.** En `ConsultaControlador` la etiqueta «Consultas» pasó de la clase al método de E-11 (`ConsultaControlador.java:46`), porque springdoc suma la etiqueta de la clase a la del método y E-12 debe aparecer solo en «Historial y detalle» (`:85`). El `operationId`, la ruta, los ejemplos y el código de E-11 no cambiaron.
