# Contrato compartido — RutaIA (C1–C12)

Copia literal de la Parte 3 del plan. Es la referencia que verifica la integración; las etapas E0–E8 llevan copiados en su propio prompt los fragmentos que usan.

## C1. Versiones y entorno

- **Sistemas:** Windows 10/11 con PowerShell y Linux con bash; todo comando se documenta en ambas variantes. En Windows se usa `curl.exe` (no el alias `curl` de PowerShell). Los cuerpos JSON van en archivos UTF-8 sin BOM y se envían con `--data-binary "@archivo.json" -H "Content-Type: application/json"`.
- **Infraestructura:** Docker Compose v2 (`docker compose`): Docker Desktop con WSL2 en Windows; Docker Engine con el plugin Compose en Linux.
- **En la máquina** solo hacen falta Docker, JDK 21 (LTS, incluye `jwebserver`) y Git. PostgreSQL, Qdrant y n8n corren en contenedores y no se instalan aparte.
- **Backend:**
  - Spring Boot 4.1.x: la última versión de parche de la línea 4.1 que ofrezca Spring Initializr (no 4.2, milestones ni snapshots).
  - En Boot 4 el iniciador web es `spring-boot-starter-webmvc` («Spring Web» en Initializr) y Jackson es la versión 3: paquetes `tools.jackson.*`, aunque las anotaciones siguen en `com.fasterxml.jackson.annotation`.
  - Gradle: wrapper generado por Initializr, DSL Groovy (`build.gradle`).
  - Swagger: springdoc-openapi v3 (`org.springdoc:springdoc-openapi-starter-webmvc-ui`, la última 3.x compatible con Boot 4.1).
- **n8n 2.x:** los flujos se publican (Publish); solo la versión publicada atiende la URL de producción `/webhook/…`.

## C2. Puertos y URLs

| Componente | Corre en | Desde la máquina | Desde el contenedor n8n |
|---|---|---|---|
| Frontend (jwebserver) | máquina | http://localhost:5500 | — |
| API Spring Boot | máquina | http://localhost:8080/api | http://host.docker.internal:8080/api |
| Swagger UI | máquina | http://localhost:8080/swagger-ui.html | — |
| PostgreSQL | contenedor `postgres` | 127.0.0.1:5433, base `rutaia` | — |
| Qdrant (REST) | contenedor `qdrant` | http://127.0.0.1:6333 (panel en /dashboard) | http://qdrant:6333 |
| n8n | contenedor `n8n` | editor http://localhost:5678 · webhooks http://127.0.0.1:5678/webhook/{ruta} | — |

Los contenedores publican sus puertos solo en 127.0.0.1. PostgreSQL usa el 5433 de la máquina para no chocar con una instalación local. Spring Boot escucha en todas las interfaces (valor por defecto) para que n8n lo alcance por host.docker.internal.

## C3. Estructura del repositorio

- **Raíz:** `docker-compose.yml`, `.env.example`, `.gitignore`, `.gitattributes` y `README.md`. El `.env` también va en la raíz, pero no se versiona.
- **`backend/`:** Spring Boot, paquete base `com.rutaia`. Paquetes: `com.rutaia.configuracion`, `.controlador`, `.dto`, `.entidad`, `.excepcion`, `.integracion`, `.repositorio`, `.servicio`.
- **`frontend/`:** `index.html`, `css/`, `js/`; nombres de archivo en minúsculas.
- **`database/`:** `01_esquema.sql` y `02_datos_semilla.sql`. Se monta en el contenedor como `/docker-entrypoint-initdb.d`.
- **`n8n/`:** `rutaia-indexacion-cursos.json`, `rutaia-calibracion-umbral.json` y `rutaia-consulta-rag.json`. Se monta en el contenedor como `/home/node/flujos`.
- **`pruebas/api/` y `pruebas/n8n/`:** cuerpos JSON de las pruebas (UTF-8 sin BOM).
- **`docs/`:** `entorno-local.md`, `arquitectura.md`, `base-de-datos.md`, `n8n.md`, `calibracion-umbral.md`, `calibracion-resultados.json`, `openapi.json`, `frontend.md`, `pruebas-funcionales.md`, `evidencias/`, `contrato.md`, `trazabilidad.md`, `informe-integracion.md` y `presentacion-final.md`.
- **`.gitignore`:** `.env` y `*.env` salvo `.env.example`, `node_modules/`, `build/`, `.gradle/`, `out/`, `bin/`, `target/`, `*.class`, `*.log`, `.idea/` y `.vscode/`.
- **`.gitattributes`:** `* text=auto eol=lf`, `*.bat text eol=crlf`, `*.cmd text eol=crlf` y `*.ps1 text eol=crlf`. Además, `backend/gradlew` lleva permiso de ejecución en Git.
- **`docs/pruebas-funcionales.md`:** cada etapa agrega su sección con la tabla `ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter`.

## C4. Modelo relacional

Convenciones:
- PostgreSQL 17 (contenedor), esquema `public`, codificación UTF8.
- IDs `BIGINT GENERATED ALWAYS AS IDENTITY` (JPA `GenerationType.IDENTITY`).
- «No vacío» significa `CHECK (btrim(col) <> '')`.
- Restricciones con nombre (`pk_`, `fk_`, `uq_`, `ck_`, `idx_`).
- El esquema lo crean solo los scripts de `database/`; Spring Boot usa `spring.jpa.hibernate.ddl-auto=validate`.
- Tipos en Java: BIGINT→Long, INTEGER→Integer, VARCHAR→String, TEXT→String con `@Column(columnDefinition = "text")`, BOOLEAN→Boolean, TIMESTAMP→LocalDateTime, DOUBLE PRECISION→Double.
- Toda columna de fecha es `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`.

Tablas:
- **estudiante:** id PK · nombre_completo VARCHAR(120) NOT NULL no vacío · correo VARCHAR(254) NOT NULL UNIQUE, CHECK (correo = lower(btrim(correo))) · nivel_experiencia VARCHAR(20) NOT NULL CHECK IN ('Principiante','Intermedio','Avanzado') · area_interes VARCHAR(100) NOT NULL no vacío · fecha_registro.
- **curso:** id PK · nombre VARCHAR(150) NOT NULL no vacío · descripcion TEXT NOT NULL no vacío · categoria VARCHAR(80) NOT NULL no vacío · nivel VARCHAR(20) NOT NULL CHECK IN ('Básico','Intermedio','Avanzado') · duracion_horas INTEGER NOT NULL CHECK (duracion_horas > 0) · activo BOOLEAN NOT NULL DEFAULT TRUE.
- **consulta:** id PK · estudiante_id BIGINT NOT NULL FK→estudiante · pregunta VARCHAR(500) NOT NULL no vacío · fecha · estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente' CHECK IN ('Pendiente','Respondida','Sin resultados','Error') · detalle_error TEXT NULL.
- **recomendacion:** id PK · consulta_id BIGINT NOT NULL UNIQUE FK→consulta · respuesta TEXT NOT NULL no vacío · fecha.
- **recomendacion_fuente:** id PK · recomendacion_id BIGINT NOT NULL FK→recomendacion · curso_id BIGINT NOT NULL FK→curso · similitud DOUBLE PRECISION NOT NULL CHECK (similitud BETWEEN 0 AND 1) · UNIQUE (recomendacion_id, curso_id).
- **calificacion:** id PK · recomendacion_id BIGINT NOT NULL UNIQUE FK→recomendacion · puntuacion INTEGER NOT NULL CHECK (puntuacion BETWEEN 1 AND 5) · comentario VARCHAR(500) NULL · fecha.

Relaciones: estudiante 1–N consulta · consulta 1–0..1 recomendacion (solo si terminó Respondida) · recomendacion 1–N recomendacion_fuente N–1 curso · recomendacion 1–0..1 calificacion. No hay borrados físicos (FK sin cascada).

Índices:
- `uq_estudiante_correo` (RF 01)
- `idx_curso_catalogo (activo, categoria, nivel)` (RF 04)
- `idx_consulta_estudiante_fecha (estudiante_id, fecha DESC)` (historial)
- `idx_consulta_estado (estado)` (estadísticas)
- `uq_recomendacion_consulta` (1:1)
- `uq_fuente_recomendacion_curso (recomendacion_id, curso_id)`
- `idx_fuente_curso (curso_id)` (curso más recomendado)
- `uq_calificacion_recomendacion` (RF 17)

## C5. Enumerados y estados

Se usa el texto exacto (mayúsculas, tildes y espacio) en BD, JSON de la API y de n8n, payload de Qdrant y frontend. Cualquier otro valor ("basico", "Basico", "SIN_RESULTADOS"…) → 400.
- Nivel del estudiante: `Principiante` · `Intermedio` · `Avanzado`
- Nivel del curso: `Básico` · `Intermedio` · `Avanzado`
- Estado de la consulta: `Pendiente` · `Respondida` · `Sin resultados` · `Error`

En Java: enum con el texto como atributo + `AttributeConverter` + `@JsonValue`/`@JsonCreator` (o un String validado con `@Pattern` en el DTO).

Solo Spring Boot escribe estados; n8n solo propone el resultado en su respuesta y el frontend nunca cambia estados.

| Estado | Lo asigna | Cuándo |
|---|---|---|
| Pendiente | Spring Boot | Al insertar la consulta; la transacción se confirma antes de llamar a n8n. |
| Respondida | Spring Boot | n8n devolvió una Respondida válida; en la misma transacción se guardan la recomendación y las fuentes. |
| Sin resultados | Spring Boot | n8n devolvió Sin resultados (ningún curso activo superó el umbral). |
| Error | Spring Boot | n8n no respondió (conexión rechazada o timeout), respondió no-2xx, devolvió Error o una respuesta inválida. |

Transiciones: Pendiente → Respondida | Sin resultados | Error. Los tres son finales.

## C6. Convenciones de la API

- Base `http://localhost:8080/api`; JSON UTF-8; campos en camelCase; fechas ISO-8601 sin zona (`2026-11-01T10:32:00`), en hora local.
- Códigos: 200 lectura o actualización · 201 creación, con cabecera Location · 400 validación, JSON mal formado, enumerado o parámetro inválido · 404 recurso inexistente · 409 correo duplicado o recomendación ya calificada · 500 error inesperado (mensaje genérico, sin traza).
- Cuerpo de todo 4xx/5xx: `{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/estudiantes","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"correo","mensaje":"debe ser un correo válido"}]}`. `detalles` es `[]` si el error no es de campos.
- Mensajes fijos:
  - MSG_SIN_RESULTADOS = "No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras."
  - MSG_ERROR = "No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde."
  - MSG_PENDIENTE = "Consulta en proceso."
- CORS: solo `/api/**`, con los orígenes de `RUTAIA_CORS_ORIGINS`, métodos GET, POST, PUT, PATCH y OPTIONS, y cabecera Content-Type.

## C7. Endpoints de Spring Boot

Objetos:
- **EstudianteRequest** `{nombreCompleto, correo, nivelExperiencia, areaInteres}`: todos obligatorios. nombreCompleto ≤120; correo con formato válido, ≤254, se guarda con trim y en minúsculas; nivelExperiencia según C5; areaInteres ≤100.
- **Estudiante** `{id, nombreCompleto, correo, nivelExperiencia, areaInteres, fechaRegistro}`
- **CursoRequest** `{nombre, descripcion, categoria, nivel, duracionHoras}`: todos obligatorios. nombre ≤150; categoria ≤80; nivel según C5; duracionHoras entero > 0.
- **CursoCatalogo** `{id, nombre, descripcion, categoria, nivel, duracionHoras}` · **CursoAdmin** = CursoCatalogo + `activo`
- **Fuente** `{cursoId, nombre, descripcion, categoria, nivel, duracionHoras, similitud}`: similitud con 4 decimales; las listas van ordenadas por similitud descendente.
- **ConsultaRequest** `{estudianteId, pregunta}`: estudianteId entero > 0; pregunta no vacía tras trim, ≤500.
- **ConsultaResultado** `{consultaId, estudianteId, pregunta, fecha, estado, respuesta, mensaje, recomendacionId, fuentes}`
- **ConsultaDetalle** = ConsultaResultado + `calificacion` (`null` o `{puntuacion, comentario, fecha}`)
- **HistorialItem** `{consultaId, fecha, pregunta, estado, resumen, cursosRecomendados:[{cursoId, nombre}]}`
- **CalificacionRequest** `{puntuacion, comentario}`: puntuacion entero 1–5, obligatorio; comentario opcional, ≤500.
- **Calificacion** `{recomendacionId, puntuacion, comentario, fecha}`
- **Estadisticas** `{totalConsultas, consultasRespondidas, consultasSinResultados, promedioCalificaciones, cursoMasRecomendado}`; cursoMasRecomendado es `null` o `{cursoId, nombre, vecesRecomendado}`.

| # | Método y ruta | Petición | Respuesta OK | Errores | Etapa |
|---|---|---|---|---|---|
| E-01 | POST /api/estudiantes | EstudianteRequest | 201 Estudiante | 400 · 409 correo ya registrado (sin distinguir mayúsculas) | E2 |
| E-02 | GET /api/estudiantes | — | 200 [Estudiante], id asc | — | E2 |
| E-03 | GET /api/estudiantes/{id} | — | 200 Estudiante | 404 | E2 |
| E-04 | GET /api/estudiantes/{id}/historial | — | 200 [HistorialItem], fecha desc ([] si no hay) | 404 | E7 |
| E-05 | GET /api/cursos?categoria=&nivel= | filtros opcionales; categoría exacta | 200 [CursoCatalogo], solo activos, id asc | 400 nivel inválido | E2 |
| E-06 | POST /api/admin/cursos | CursoRequest | 201 CursoAdmin (activo = true) | 400 | E2 |
| E-07 | GET /api/admin/cursos | — | 200 [CursoAdmin], activos e inactivos, id asc | — | E2 |
| E-08 | GET /api/admin/cursos/{id} | — | 200 CursoAdmin | 404 | E2 |
| E-09 | PUT /api/admin/cursos/{id} | CursoRequest | 200 CursoAdmin (activo no cambia) | 400 · 404 | E2 |
| E-10 | PATCH /api/admin/cursos/{id}/desactivar | — | 200 CursoAdmin (activo = false; idempotente) | 404 | E2 |
| E-11 | POST /api/consultas | ConsultaRequest | 201 ConsultaResultado (cualquier estado final) | 400 · 404 (en ambos, sin crear consulta ni llamar a n8n) | E6 |
| E-12 | GET /api/consultas/{id} | — | 200 ConsultaDetalle | 404 | E7 |
| E-13 | POST /api/recomendaciones/{id}/calificacion | CalificacionRequest | 201 Calificacion | 400 · 404 · 409 | E7 |
| E-14 | GET /api/estadisticas | — | 200 Estadisticas | — | E7 |

Reglas:
- **E-11, en este orden:**
  1. Validar el cuerpo → 400.
  2. Comprobar que el estudiante existe → si no, 404.
  3. Insertar la consulta en Pendiente y confirmar.
  4. Llamar a W-1.
  5. Procesar la respuesta según C8 y fijar el estado final.
  6. Responder 201 con Location `/api/consultas/{id}`.
- **ConsultaResultado según el estado:**
  - Respondida → respuesta, mensaje null, recomendacionId y de 1 a 5 fuentes (datos de PostgreSQL más la similitud de n8n).
  - Sin resultados → respuesta null, mensaje MSG_SIN_RESULTADOS, recomendacionId null, fuentes [].
  - Error → respuesta null, mensaje MSG_ERROR, recomendacionId null, fuentes []. `detalle_error` solo queda en la BD y los logs.
- **resumen (E-04):** Respondida → los primeros 160 caracteres de la respuesta, más "…" si se cortó · Sin resultados → MSG_SIN_RESULTADOS · Error → MSG_ERROR · Pendiente → MSG_PENDIENTE. cursosRecomendados son las fuentes por similitud desc ([] si no hay).
- **Estadísticas:** totalConsultas cuenta todas las consultas. promedioCalificaciones lleva 2 decimales, o null si no hay calificaciones. cursoMasRecomendado es el curso con más filas en recomendacion_fuente; en empate gana el menor cursoId; null si no hay.
- Ejemplo de E-11 Respondida: `{"consultaId":12,"estudianteId":3,"pregunta":"Quiero aprender a crear páginas web.","fecha":"2026-11-01T10:32:00","estado":"Respondida","respuesta":"Te sugiero empezar por…","mensaje":null,"recomendacionId":7,"fuentes":[{"cursoId":1,"nombre":"…","descripcion":"…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.6421}]}`

## C8. Webhooks de n8n

- **Nodo «Configuración»** (Edit Fields/Set), al inicio de cada flujo:
  - coleccion = "rutaia_cursos"
  - modeloEmbeddings = "nvidia/nemotron-3-embed-1b:free"
  - dimensionVector = 2048
  - modeloLlm = "google/gemma-4-31b-it:free"
  - umbralSimilitud = valor de la Etapa 4 (solo en el flujo RAG)
  - limiteResultados = 5 (solo en el flujo RAG)
  - qdrantUrl = "http://qdrant:6333"
  - apiBaseUrl = "http://host.docker.internal:8080/api"
  - openRouterUrl = "https://openrouter.ai/api/v1"

  No se usa `$env`: en n8n 2.x el acceso desde el nodo Code está bloqueado por defecto. Las llamadas externas usan nodos HTTP Request, y las de OpenRouter, la credencial «RutaIA OpenRouter».
- **Ahorro de cuota:** cada flujo hace todo lo que no gasta solicitudes (validar la entrada, leer la API, preparar Qdrant) antes de llamar a OpenRouter.
- **W-1 «RutaIA - Consulta RAG»**: POST http://127.0.0.1:5678/webhook/rutaia-consulta-rag. Solo lo llama Spring Boot.
  - Petición: `{"consultaId":12,"pregunta":"Quiero aprender a crear páginas web.","nivelExperiencia":"Principiante","areaInteres":"Desarrollo web"}`
  - 200 Respondida: `{"consultaId":12,"pregunta":"Quiero aprender a crear páginas web.","estado":"Respondida","respuesta":"Texto plano…","fuentes":[{"cursoId":1,"nombre":"…","descripcion":"…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.6421}],"error":null}`. Lleva de 1 a 5 fuentes en orden descendente, todas activas y con score igual o mayor que el umbral, sin vectores; la similitud es el score de Qdrant con 4 decimales. La respuesta va en texto plano, sin Markdown ni HTML.
  - 200 Sin resultados: `{"consultaId":18,"pregunta":"Quiero aprender cocina italiana.","estado":"Sin resultados","respuesta":null,"fuentes":[],"error":null}`. En este caso no se llama al LLM.
  - 400 Error (entrada inválida) y 502 Error (falla de OpenRouter, Qdrant, la API o la validación de la salida del modelo): `{"consultaId":12,"pregunta":"…","estado":"Error","respuesta":null,"fuentes":[],"error":"descripción técnica breve"}`
  - **Qué hace Spring Boot:** usa connect timeout de 5 s y read timeout de `RUTAIA_N8N_TIMEOUT_SEGUNDOS` (90). Ante conexión rechazada, timeout, HTTP no-2xx, cuerpo ilegible, estado Error o respuesta inválida, deja la consulta en Error con detalle_error y responde 201 con MSG_ERROR, sin reintentos. Una respuesta válida trae el mismo consultaId enviado y estado Respondida o Sin resultados. Si es Respondida, además trae respuesta no vacía y de 1 a 5 fuentes sin cursoId repetido, cada una existente y activa en PostgreSQL y con similitud entre 0 y 1.
- **W-2 «RutaIA - Indexación de cursos»**: POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos (sin cuerpo), más un Manual Trigger. Lee GET {apiBaseUrl}/admin/cursos (E-07).
  - 200: `{"coleccion":"rutaia_cursos","coleccionCreada":false,"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}` · 500: `{"error":"…"}`
  - Lo ejecuta el administrador (curl, Postman o el editor) después de cargar o cambiar cursos; nunca el frontend ni Spring Boot.
- **W-3 «RutaIA - Calibración de umbral»** (auxiliar, fuera de la aplicación): POST http://127.0.0.1:5678/webhook/rutaia-calibrar-umbral con `{"preguntas":["…"]}`.
  - 200: `{"modelo":"nvidia/nemotron-3-embed-1b:free","coleccion":"rutaia_cursos","resultados":[{"pregunta":"…","puntos":[{"cursoId":1,"nombre":"…","categoria":"…","score":0.6421}]}]}`, con los 10 mejores puntos por pregunta, filtro activo = true y sin umbral · 500: `{"error":"…"}`

## C9. Qdrant

- Colección `rutaia_cursos`: un único vector sin nombre, `size` 2048, `distance` "Cosine". Índice de payload en el campo `activo`, esquema `bool`.
- ID del punto = curso.id (entero). El upsert es por ID, así que re-indexar no duplica.
- Payload exacto: `{"cursoId":1,"nombre":"…","descripcion":"…","categoria":"…","nivel":"Básico","duracionHoras":40,"activo":true}`
- Texto que se vectoriza:
  - Por curso, con `input_type` = `passage`, 4 líneas: `{nombre}` / `Categoría: {categoria}` / `Nivel: {nivel}` / `{descripcion}`.
  - Por pregunta, con `input_type` = `query`: la pregunta con trim, sin prefijos.
- **Crear o reutilizar la colección:**
  1. GET /collections/rutaia_cursos/exists → `result.exists`.
  2. Si es false: PUT /collections/rutaia_cursos `{"vectors":{"size":2048,"distance":"Cosine"}}`.
  3. Si es true: GET /collections/rutaia_cursos y comprobar 2048 y Cosine; si no coinciden, fallar sin borrar.
  4. Índice: PUT /collections/rutaia_cursos/index `{"field_name":"activo","field_schema":"bool"}`.
- **Upsert:** PUT /collections/rutaia_cursos/points?wait=true `{"points":[{"id":1,"vector":[…],"payload":{…}}]}`
- **Búsqueda RAG:** POST /collections/rutaia_cursos/points/query `{"query":[…],"filter":{"must":[{"key":"activo","match":{"value":true}}]},"limit":5,"score_threshold":UMBRAL,"with_payload":true,"with_vector":false}` → `result.points[]` con id, score y payload. La calibración usa la misma consulta con limit 10 y sin score_threshold.
- El score es la similitud coseno: cuanto mayor, más parecido; el máximo es 1.
- **Curso desactivado después de indexar:** lo excluye el filtro activo = true (estado del último indexado). Además, antes de armar el contexto, el flujo RAG descarta todo punto cuyo cursoId no esté en GET {apiBaseUrl}/cursos (E-05, solo activos).

## C10. OpenRouter (capa gratuita)

- **Embeddings:** POST https://openrouter.ai/api/v1/embeddings.
  - Cuerpo: `{"model":"nvidia/nemotron-3-embed-1b:free","input":["…"],"input_type":"passage","encoding_format":"float"}` para cursos, o con `"input_type":"query"` para preguntas.
  - Respuesta: `data[i].embedding` y `data[i].index`.
  - El modelo es asimétrico: sin `input_type` responde 400.
  - Se usa el mismo modelo para cursos y preguntas. Un vector válido tiene exactamente 2048 números finitos.
- **LLM:** POST https://openrouter.ai/api/v1/chat/completions con el modelo `google/gemma-4-31b-it:free`, temperature 0.2 y max_tokens 700. Lleva un único mensaje `user` con instrucciones y contexto, sin rol `system`. La respuesta está en `choices[0].message.content`.
- **Límites de la capa gratuita:** 20 solicitudes por minuto y 50 por día por cuenta. Errores típicos: 429, cuota agotada; 404 en rutas `:free`, privacidad no habilitada; 402, saldo negativo.
- **Privacidad:** cada cuenta habilita en OpenRouter los proveedores gratuitos que pueden guardar o entrenar con los datos. A OpenRouter solo viajan la pregunta, el nivel, el área y los datos de los cursos; nunca nombres ni correos.
- **Autenticación:** `Authorization: Bearer <clave>`, solo a través de la credencial de n8n «RutaIA OpenRouter». La clave no aparece en código, JSON exportados ni documentos.
- **Presupuesto:** cada etapa declara cuántas solicitudes gasta. Si la cuota del día no alcanza, se continúa al día siguiente o con la cuenta de otro integrante, nunca con modelos de pago.
- **Alternativas si un modelo se retira:** para embeddings, `nvidia/nemotron-3.5-embed-1b:free` (también 2048; obliga a re-indexar y recalibrar); para el LLM, `nvidia/nemotron-3-super-120b-a12b:free` (solo cambia «Configuración»). Todo cambio se registra en este contrato.

## C11. Variables de entorno y credenciales

Hay un único `.env` en la raíz (no versionado) y un `.env.example` (versionado, sin secretos). Docker Compose lo lee solo; Spring Boot lo importa con `spring.config.import=optional:file:../.env[.properties]`, porque se ejecuta desde `backend/`. Los valores van sin comillas, espacios ni `\`.

| Variable | Valor | Secreta | La usan |
|---|---|---|---|
| RUTAIA_DB_USER | rutaia_app | no | Compose (POSTGRES_USER) y Spring Boot |
| RUTAIA_DB_PASSWORD | (sin valor por defecto) | sí | Compose (POSTGRES_PASSWORD) y Spring Boot |
| RUTAIA_DB_URL | jdbc:postgresql://127.0.0.1:5433/rutaia | no | Spring Boot |
| RUTAIA_N8N_WEBHOOK_URL | http://127.0.0.1:5678/webhook/rutaia-consulta-rag | no | Spring Boot |
| RUTAIA_N8N_TIMEOUT_SEGUNDOS | 90 | no | Spring Boot |
| RUTAIA_CORS_ORIGINS | http://localhost:5500,http://127.0.0.1:5500 | no | Spring Boot |

- **Solo de sesión, nunca en archivos:** `OPENROUTER_API_KEY`, para las dos llamadas de verificación de la Etapa 0.
- **Credencial de n8n:** «RutaIA OpenRouter», tipo Header Auth, Name `Authorization`, Value `Bearer <clave>`. La crea cada integrante en su n8n. Qdrant (sin API key) y los GET de la API no usan credencial.

## C12. Docker Compose

- `docker-compose.yml` va en la raíz, con `name: rutaia` y tres servicios. El backend y el frontend no van en contenedores.
  - **`postgres`:** imagen `postgres:17`; `POSTGRES_DB=rutaia`, `POSTGRES_USER=${RUTAIA_DB_USER}`, `POSTGRES_PASSWORD=${RUTAIA_DB_PASSWORD}`; puerto `127.0.0.1:5433:5432`; volúmenes `pgdata:/var/lib/postgresql/data` y `./database:/docker-entrypoint-initdb.d:ro`; healthcheck con `pg_isready`.
  - **`qdrant`:** imagen `qdrant/qdrant` con etiqueta exacta v1.x (la última estable al crear el archivo, ≥ v1.10); puerto `127.0.0.1:6333:6333`; volumen `qdrant_storage:/qdrant/storage`.
  - **`n8n`:** imagen `docker.n8n.io/n8nio/n8n` con etiqueta exacta 2.x (la última estable al crear el archivo); puerto `127.0.0.1:5678:5678`; volúmenes `n8n_data:/home/node/.n8n` y `./n8n:/home/node/flujos`; `extra_hosts: ["host.docker.internal:host-gateway"]`.
  - Volúmenes con nombre: `pgdata`, `qdrant_storage` y `n8n_data`. Nunca se usa la etiqueta `latest`.
- Con el volumen `pgdata` vacío, PostgreSQL ejecuta solo `01_esquema.sql` y `02_datos_semilla.sql`, en ese orden.
- **Comandos:**
  - Levantar: `docker compose up -d`. Estado: `docker compose ps`. Detener: `docker compose stop` o `docker compose down` (sin `-v`).
  - SQL a mano: `docker compose exec postgres psql -U rutaia_app -d rutaia -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/01_esquema.sql` (igual con `02_datos_semilla.sql`).
  - CLI de n8n:
    - `docker compose exec n8n n8n import:workflow --input=/home/node/flujos/<archivo>.json`
    - `docker compose exec n8n n8n list:workflow`
    - `docker compose exec n8n n8n publish:workflow --id=<ID>`
    - `docker compose exec n8n n8n export:workflow --id=<ID> --output=/home/node/flujos/<archivo>.json --pretty`
  - `docker compose down -v` borra la base, la colección de Qdrant y los flujos y credenciales de n8n: úsalo solo para empezar de cero.

## Registro de cambios

Agregado en la integración (2026-11-03) para cumplir C10 («Todo cambio se registra en este contrato»). El texto de C1–C12 de arriba no se modificó. RutaIA es un proyecto individual: donde el plan dice «equipo» o «integrante», quien decide es el autor (Jeshua Perez).

| Fecha | Cláusulas | Cambio | Motivo | Evidencia |
|---|---|---|---|---|
| 2026-11-02 (Etapa 5) | C8, C10 | `modeloLlm`: `google/gemma-4-31b-it:free` → `google/gemma-4-26b-a4b-it:free` en los tres flujos | El modelo del contrato respondía 429 «rate-limited upstream» y la alternativa de C10 (`nvidia/nemotron-3-super-120b-a12b:free`) ya no tenía proveedores | `docs/n8n.md`, `docs/evidencias/etapa5-consulta-rag.txt` |
| 2026-11-03 (integración) | C8, C10 | `modeloLlm`: `google/gemma-4-26b-a4b-it:free` → `google/gemma-4-31b-it:free` (vuelta al modelo del contrato) | Con 26b, la capacidad compartida daba 429 y Google AI Studio con la clave propia (BYOK), 500 «Internal error encountered» | `docs/evidencias/integracion-consultas.txt` (ejecuciones #58 y #61) |
| 2026-11-03 (integración) | C8, C10 | `modeloLlm`: `google/gemma-4-31b-it:free` → `google/gemma-4-31b-it`, la variante de pago del mismo modelo, pagada con crédito de OpenRouter. **Excepción aprobada por el autor** a «nunca con modelos de pago» (C10) | Con `:free`, la capacidad compartida daba 429 y Google AI Studio, 504 «The operation was aborted». La variante de pago la sirven unos 18 proveedores; cada consulta cuesta menos de 0,001 USD | `docs/evidencias/integracion-consultas.txt` (ejecución #62 y desde la #63) |
| 2026-11-03 (integración) | C10 | Límites: con 10 créditos comprados (total histórico) el tope diario de los modelos `:free` sube de 50 a 1000 solicitudes. Los embeddings siguen en `nvidia/nemotron-3-embed-1b:free`, que no tiene variante de pago. La alternativa del LLM de C10 ya no existe en OpenRouter | Medido en la integración: 429 «free-models-per-day» con 5 créditos comprados | `docs/informe-integracion.md` |
