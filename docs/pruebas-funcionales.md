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
