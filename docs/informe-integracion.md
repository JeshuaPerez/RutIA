# Informe de integración de RutaIA

Fecha: 2026-11-03 (hora local UTC−6) · Autor del proyecto: Jeshua Perez (proyecto individual) · Máquina: Windows 11 Home 10.0.26200.

Este informe cierra las Etapas 0 a 8. Contiene:
- el inventario de entregables;
- la verificación del contrato (`docs/contrato.md`);
- la conexión de las piezas;
- las 10 consultas de prueba, de punta a punta;
- la trazabilidad (`docs/trazabilidad.md`) con su evidencia;
- los ajustes de configuración;
- la verificación cruzada;
- las inconsistencias, cada una con su etapa responsable.

Evidencias:
- `docs/evidencias/integracion-verificacion.txt`: contrato, CORS y secretos;
- `docs/evidencias/integracion-consultas.txt`: consultas, calificaciones, estadísticas y robustez;
- sección «Integración» de `docs/pruebas-funcionales.md`.

## 1. Resumen

| Criterio de aceptación | Estado |
|---|---|
| Las 7 consultas temáticas terminan en Respondida y la de cocina en Sin resultados | **Cumple**: consultas 7, 8, 9, 10, 11, 13 y 14 Respondida; consulta 2 Sin resultados |
| La pregunta vacía se rechaza sin llegar a n8n y la del estudiante inexistente no crea la consulta | **Cumple**: 400 y 404, sin filas nuevas ni ejecuciones en n8n; el 400 también con n8n detenido |
| La base tiene al menos 10 consultas y 5 calificaciones | **Cumple**: 15 consultas y 5 calificaciones |
| El contrato se verificó sin diferencias, o cada diferencia está asignada a una etapa | **Cumple**: única diferencia, el modelo del LLM (C8/C10), asignada a la Etapa 5 y resuelta por decisión del autor, con registro en el contrato |
| Toda fila de la trazabilidad tiene evidencia | **Cumple**: 63 filas; 61 Cumple y 2 Parcial (sección 7) |
| Los 14 entregables de la rúbrica existen | **Cumple**: ENT-10 (evidencia de Swagger) está completa en `openapi.json`, pero sus capturas son parciales |
| Sin secretos, `.env`, dependencias ni compilados en el repositorio ni en su historial | **Cumple** (detalle en la sección 3.11) |
| Presupuesto ≤ 25 solicitudes a OpenRouter | **No cumple**: 27. Las 2 de más vienen de 7 intentos fallidos por causas externas (sección 5.4) |

Durante la integración, el LLM gratuito dejó de responder: la capacidad compartida devolvía 429, y Google AI Studio, con la clave propia, 500 y 504. Por decisión del autor, el LLM pasó a la variante de pago del mismo modelo del contrato, `google/gemma-4-31b-it`, y se compraron 10 créditos en OpenRouter, lo que además sube el tope diario de los modelos `:free` a 1000. Las 8 llamadas al LLM completadas costaron menos de 0,01 USD.

## 2. Inventario

### 2.1 Entregables de la rúbrica

| ID | Entregable | Archivo | Estado |
|---|---|---|---|
| ENT-01 | Enlace al repositorio | `README.md` §1: https://github.com/JeshuaPerez/RutIA (remoto `origin` configurado) | Existe |
| ENT-02 | Código fuente del frontend | `frontend/` (`index.html`, `css/estilos.css`, 14 módulos en `js/`) | Existe |
| ENT-03 | Código fuente del backend | `backend/` (paquete `com.rutaia`, 8 subpaquetes, 61 archivos Java) | Existe |
| ENT-04 | Script SQL | `database/01_esquema.sql`, `database/02_datos_semilla.sql` | Existe |
| ENT-05 | Diagrama entidad-relación | `docs/base-de-datos.md` §1 (Mermaid `erDiagram`) | Existe |
| ENT-06 | Diagrama de arquitectura | `docs/arquitectura.md` (componentes y puertos, flujo RAG y flujo de indexación, en Mermaid) | Existe |
| ENT-07 | JSON de los flujos de n8n | `n8n/rutaia-indexacion-cursos.json`, `n8n/rutaia-calibracion-umbral.json`, `n8n/rutaia-consulta-rag.json` | Existe |
| ENT-08 | Docker Compose | `docker-compose.yml` | Existe |
| ENT-09 | `.env.example` | `.env.example` | Existe |
| ENT-10 | Evidencia de Swagger (en lugar de Postman) | `docs/openapi.json` (14 operaciones, idéntico a `/v3/api-docs`) y 8 capturas: `etapa2-swagger-*` (5), `etapa6-swagger-*` (2), `etapa7-swagger-e02-respuestas.png` | Existe; capturas parciales (inconsistencia I-03) |
| ENT-11 | Documento de pruebas | `docs/pruebas-funcionales.md` (Etapas 0–8 e Integración) | Existe |
| ENT-12 | README con instrucciones | `README.md` (8 secciones) | Existe |
| ENT-13 | Presentación final | `docs/presentacion-final.md` (guion por diapositiva) | Existe |
| ENT-14 | Repo sin contraseñas, claves, `.env`, dependencias ni compilados | Verificado (sección 3.11) | Cumple |

### 2.2 Entregables de las etapas (estructura de C3)

| Etapa | Entregables | Estado |
|---|---|---|
| E0 | `docker-compose.yml`, `.env.example`, `.gitignore`, `.gitattributes`, `README.md` (esqueleto, completado aquí), `docs/entorno-local.md`, `docs/arquitectura.md`, `docs/evidencias/etapa0-*` | Existen |
| E1 | `database/01_esquema.sql`, `database/02_datos_semilla.sql`, `docs/base-de-datos.md`, `pruebas/bd/etapa1-*.sql`, `docs/evidencias/etapa1-*` | Existen |
| E2 | `backend/` (E-01 a E-10), `pruebas/api/etapa2-*.json` (15), `docs/evidencias/etapa2-api.txt` y 5 capturas de Swagger | Existen |
| E3 | `n8n/rutaia-indexacion-cursos.json`, `docs/n8n.md` (indexación), `docs/evidencias/etapa3-*` | Existen |
| E4 | `n8n/rutaia-calibracion-umbral.json`, `docs/calibracion-umbral.md`, `docs/calibracion-resultados.json`, `pruebas/n8n/etapa4-calibracion-preguntas.json`, `docs/evidencias/etapa4-calibracion.txt` | Existen |
| E5 | `n8n/rutaia-consulta-rag.json`, `docs/n8n.md` (Consulta RAG), `pruebas/n8n/etapa5-01..12`, `docs/evidencias/etapa5-consulta-rag.txt` | Existen |
| E6 | E-11 (`ConsultaControlador`, `ConsultaServicio`, `ConsultaTransacciones`, `ClienteN8n`), `pruebas/api/etapa6-01..10`, `docs/evidencias/etapa6-api.txt` y 2 capturas | Existen |
| E7 | E-04, E-12, E-13, E-14, `docs/openapi.json`, `pruebas/api/etapa7-01..08`, `pruebas/bd/etapa7-*.sql`, `docs/evidencias/etapa7-api.txt` y 1 captura | Existen (capturas parciales, I-03) |
| E8 | `frontend/`, `docs/frontend.md`, `docs/evidencias/etapa8-frontend.txt` y 18 capturas (1280 y 360 px) | Existen |
| Insumos | `docs/contrato.md`, `docs/trazabilidad.md` (copias literales de las Partes 3 y 1 del plan) | Existen; **sin versionar** hasta el commit de la integración |
| INT | `docs/informe-integracion.md`, `docs/presentacion-final.md`, README completo, sección «Integración» de las pruebas, `docs/evidencias/integracion-*.txt`, `pruebas/api/integracion-01..05` | Creados en esta etapa |

## 3. Verificación del contrato

### 3.1 C1 y C2 — versiones, puertos y URLs

**Cumple.** Spring Boot 4.1.2 con springdoc 3.2.0 y JDK 21.0.13; PostgreSQL 17.11; Qdrant v1.20.0; n8n 2.45.7. Los contenedores escuchan solo en `127.0.0.1` (5433, 6333 y 5678), la API en todas las interfaces (`:::8080`) y `jwebserver` en `127.0.0.1:5500`. n8n llega a la API por `host.docker.internal:8080` («Cursos activos» → HTTP 200 en todas las ejecuciones).

### 3.2 C3 — estructura del repositorio

**Cumple.**
- La raíz, `backend/` (paquete `com.rutaia` con `configuracion`, `controlador`, `dto`, `entidad`, `excepcion`, `integracion`, `repositorio` y `servicio`), `frontend/` (nombres en minúsculas), `database/`, `n8n/`, `pruebas/api/`, `pruebas/n8n/` y `docs/` coinciden con C3. `pruebas/bd/` es un extra de las Etapas 1 y 7.
- `.gitignore` y `.gitattributes` tienen exactamente las entradas de C3.
- `git ls-files --eol backend/gradlew` → `i/lf w/lf attr/text eol=lf`; `git ls-files -s backend/gradlew` → `100755`.
- Observación: `backend/.gitattributes` y `backend/.gitignore` vienen de Spring Initializr y no contradicen C3.

### 3.3 C4 — modelo relacional

**Cumple, sin diferencias.** Se comparó `information_schema.columns`, `pg_constraint` y `pg_indexes` (vía `docker compose exec`) con C4:
- 6 tablas con sus tipos, longitudes, nulos y defaults (`CURRENT_TIMESTAMP`, `'Pendiente'`, `true`);
- IDs `IDENTITY ALWAYS`;
- restricciones con nombre (`pk_`, `fk_`, `uq_`, `ck_`), «no vacío» con `btrim`, correo normalizado, FK sin cascada (`on delete a`);
- los 8 índices de C4 (`uq_estudiante_correo`, `idx_curso_catalogo (activo, categoria, nivel)`, `idx_consulta_estudiante_fecha (estudiante_id, fecha DESC)`, `idx_consulta_estado`, `uq_recomendacion_consulta`, `uq_fuente_recomendacion_curso`, `idx_fuente_curso`, `uq_calificacion_recomendacion`);
- codificación UTF8.

### 3.4 C5 — enumerados y estados

**Cumple.**
- Los CHECK usan exactamente `'Principiante','Intermedio','Avanzado'`, `'Básico','Intermedio','Avanzado'` y `'Pendiente','Respondida','Sin resultados','Error'`.
- Los enums de Java (`NivelExperiencia`, `NivelCurso`, `EstadoConsulta`) llevan el mismo texto, con `AttributeConverter` y `@JsonValue`.
- El JSON real trae `"Sin resultados"`, `"Básico"` y `"Principiante"`.
- Solo Spring Boot escribe estados.

### 3.5 C6 — convenciones de la API

**Cumple.**
- Todo 201 trae `Location`.
- Los 400 y 404 tienen el cuerpo `{status, error, mensaje, ruta, fecha, detalles}`.
- MSG_SIN_RESULTADOS y MSG_ERROR son los textos fijos.
- CORS: solo `/api/**`, con los orígenes de `RUTAIA_CORS_ORIGINS`, métodos GET, POST, PUT, PATCH y OPTIONS, y cabecera Content-Type. Prueba: el preflight desde `http://localhost:5500` y desde `http://127.0.0.1:5500` → 200 con `Access-Control-Allow-Origin`; desde `http://otro.test` → **403** «Invalid CORS request»; con método DELETE → 403.

### 3.6 C7 — endpoints

**Cumple.**
- `docs/openapi.json` es **idéntico** a `GET /v3/api-docs` (comparación con claves ordenadas).
- Contiene exactamente E-01 a E-14, con sus métodos, rutas, cuerpos (EstudianteRequest, CursoRequest, ConsultaRequest, CalificacionRequest) y respuestas (Estudiante, CursoCatalogo, CursoAdmin, ConsultaResultado, ConsultaDetalle, HistorialItem, Calificacion, Estadisticas, Error), con los campos de C7.

### 3.7 C8 — webhooks de n8n

**Cumple, salvo el modelo del LLM (I-01, resuelta).**
- Los 3 flujos están publicados (`n8n list:workflow --active=true`) en `/webhook/rutaia-indexar-cursos`, `/webhook/rutaia-calibrar-umbral` y `/webhook/rutaia-consulta-rag`.
- Antes de cambiar nada, la versión publicada era idéntica al JSON versionado (mismo `versionId`, nodos, conexiones y ajustes). Tras los cambios de modelo se exportó la versión publicada `--published` y se quitó el bloque `shared`, así que siguen coincidiendo.
- «Configuración» tiene los valores de C8: `coleccion`, `modeloEmbeddings`, `dimensionVector` 2048, `qdrantUrl`, `apiBaseUrl` y `openRouterUrl`; en el flujo RAG, además, `umbralSimilitud` **0.34** (igual a la última línea de `docs/calibracion-umbral.md`) y `limiteResultados` 5. **`modeloLlm` difería**: `google/gemma-4-26b-a4b-it:free` frente a `google/gemma-4-31b-it:free`.
- En W-1, OpenRouter se llama solo después de «Validar entrada» y «Cursos activos». Qdrant se consulta después del embedding porque necesita el vector; es una decisión documentada en la Etapa 5 (I-07).

### 3.8 C9 — Qdrant

**Cumple.**
- `GET /collections/rutaia_cursos` → `size` 2048, `distance` Cosine, índice de payload `activo` (`bool`, 30 puntos) y `points_count` 30, igual al número de cursos.
- El payload trae exactamente los 7 campos (`cursoId`, `nombre`, `descripcion`, `categoria`, `nivel`, `duracionHoras`, `activo`), con id del punto = `cursoId`. Los inactivos (29 y 30) tienen `activo=false`.

### 3.9 C10 — OpenRouter

**Cumple, con el modelo del LLM registrado como excepción.**
- Embeddings `nvidia/nemotron-3-embed-1b:free` en los tres flujos, con `input_type` `passage` en la indexación y `query` en la calibración y en el RAG.
- LLM: temperature 0.2, max_tokens 700 y un solo mensaje `user`.
- El modelo del LLM pasó a la variante de pago (sección 4). La alternativa de C10 para el LLM ya no existe en OpenRouter (I-02).

### 3.10 C11 y C12 — variables y Docker Compose

**Cumple.**
- `.env.example` tiene las 6 variables, con `RUTAIA_DB_PASSWORD` vacía.
- El `.env` local tiene los valores de C11; no se ajustó nada.
- `application.properties` importa `optional:file:../.env[.properties]`.
- `docker-compose.yml`:
  - `name: rutaia` y 3 servicios con etiquetas exactas (`postgres:17`, `qdrant/qdrant:v1.20.0`, `docker.n8n.io/n8nio/n8n:2.45.7`; ninguna `latest`);
  - puertos `127.0.0.1:…`, volúmenes `pgdata`, `qdrant_storage` y `n8n_data` y montajes `./database` y `./n8n`;
  - `extra_hosts: host.docker.internal:host-gateway` y healthcheck con `pg_isready`.
- El backend y el frontend no están en contenedores.

### 3.11 Secretos y frontend

**Cumple.**
- `git ls-files` y el historial completo: ningún `.env`, `node_modules/`, `build/`, `.gradle/` ni `*.class`.
- Claves reales (`sk-or-v1-` seguido de 32 o más caracteres hexadecimales): 0 en los archivos versionados y 0 en `git log --all -p`.
- La contraseña del `.env` local aparece 0 veces en los archivos y en el historial (se buscó sin imprimirla).
- No hay `Bearer` con un valor real, solo `Bearer <clave>`, `Bearer $OPENROUTER_API_KEY` y el marcador `Bearer sk-or-v1-…`.
- La búsqueda literal de «sk-or-» sí encuentra líneas, pero todas son marcadores `sk-or-v1-…` o comandos de búsqueda en la documentación (I-06).
- El frontend tiene 0 referencias a `5678`, `6333`, `openrouter` y `webhook`. Su única URL es `API_BASE_URL = "http://localhost:8080/api"`.

## 4. Ajustes de configuración realizados

| # | Ajuste | Motivo | Autorizó |
|---|---|---|---|
| 1 | Ninguno en `.env` ni en CORS | Ya coincidían con C11 | — |
| 2 | Base reiniciada con `01_esquema.sql` y `02_datos_semilla.sql` (comandos de C12), con respaldo previo de los datos de las etapas, y re-indexado con W-2 | Estado limpio pedido por la integración | Prompt de integración |
| 3 | `modeloLlm` → `google/gemma-4-31b-it:free` en los 3 flujos (import, publish, `restart`, export `--published` sin `shared`) | Con 26b, la capacidad compartida daba 429 y Google AI Studio, con la clave propia, 500 INTERNAL (#58, #61) | Autor |
| 4 | `modeloLlm` → **`google/gemma-4-31b-it`** (variante de pago) con el mismo procedimiento | Con 31b `:free`: 429 compartido y Google 504 «The operation was aborted» (#62) | Autor |
| 5 | Compra de 5 + 5 créditos en OpenRouter (10 en total) | Pagar el LLM y subir el tope diario de `:free` de 50 a 1000, después del 429 «free-models-per-day» (#68) | Autor |
| 6 | Curso 1 desactivado (E-10) y restaurado con `UPDATE curso SET activo = true WHERE id = 1;`, y re-indexado con W-2 | Prueba de robustez (no hay endpoint de reactivación) | Prompt de integración |
| 7 | Documentación coherente con el modelo nuevo: `docs/arquitectura.md` (modelo y límites), `docs/n8n.md` (nota de la integración) y el **registro de cambios** al final de `docs/contrato.md` (sin tocar el texto de C1–C12) | C10: «Todo cambio se registra en este contrato» | Autor |

Los dos cambios de modelo solo tocaron el valor `modeloLlm`. El diff de cada flujo se limita a `modeloLlm`, `updatedAt`, `versionId`, `activeVersionId` y `versionCounter`.

## 5. Resultados de las 10 consultas de prueba

Estudiantes de la semilla. Las consultas 1–8 se enviaron desde el frontend (navegador integrado); la 9 y la 10, por la API. Los números de consulta son los ids de la base, y `#n` es la ejecución de n8n.

| # | Consulta | Estudiante | Esperado | Resultado | Evidencia |
|---|---|---|---|---|---|
| 1 | Quiero aprender a crear páginas web. | 1 Valeria (Principiante) | Respondida | **Respondida**, consulta 7: fuentes 1, 2, 3, 4 (0,5930 / 0,3843 / 0,3750 / 0,3644) | #63 |
| 2 | Necesito aprender Java para trabajar con Spring Boot. | 2 Andrés (Intermedio) | Respondida | **Respondida**, consulta 8: 5, 6, 7, 8 (0,6286–0,3974) | #64 |
| 3 | Me interesa analizar datos y construir dashboards. | 4 Mateo (Principiante) | Respondida | **Respondida**, consulta 9: 21, 20, 9, 22 (0,5322–0,3856) | #65 |
| 4 | Quiero automatizar procesos empresariales. | 5 Camila (Intermedio) | Respondida | **Respondida**, consulta 10: 17, 18, 19 (0,5534–0,4471) | #66 |
| 5 | ¿Qué puedo estudiar para trabajar con inteligencia artificial? | 3 Lucía (Avanzado) | Respondida | **Respondida**, consulta 11: 12, 16, 14, 15, 13 (0,5256–0,3730) | #67 |
| 6 | Quiero aprender a proteger aplicaciones web. | 6 Diego (Avanzado) | Respondida | **Respondida**, consulta 13: 4, 27, 8 (0,5599–0,4087) | #69 |
| 7 | Necesito desplegar aplicaciones usando contenedores. | 7 Sofía (Intermedio) | Respondida | **Respondida**, consulta 14: 24, 26, 25 (0,6088–0,4204) | #70 |
| 8 | Quiero aprender cocina italiana. | 8 Javier (Principiante) | Sin resultados, sin recomendación, sin LLM | **Sin resultados**, consulta 2: MSG_SIN_RESULTADOS; 0 filas en `recomendacion`; Qdrant no devolvió puntos sobre 0,34; sin «Consultar LLM» | #59 |
| 9 | `"   "` | 1 | 400, sin consulta ni ejecución en n8n; 400 también con n8n detenido | **400** `detalles` [pregunta: «es obligatoria»]; consultas 2 → 2; última ejecución #59 → #59; con n8n detenido, otra vez 400 | — |
| 10 | Quiero aprender a crear páginas web. | 999999 | 404, sin consulta nueva | **404** «No existe un estudiante con id 999999»; consultas 2 → 2 | — |

Las fuentes coinciden exactamente con las medidas en la calibración (`docs/calibracion-umbral.md`). Para las 7 respondidas se comprobó por la API que:
- tienen de 3 a 5 fuentes, todas activas, con similitud ≥ 0,34 y ordenadas de mayor a menor;
- la respuesta está en texto plano, sin Markdown ni HTML;
- solo nombra cursos de sus fuentes (0 cursos ajenos).

### 5.1 Intentos fallidos antes de los resultados válidos (causas externas)

| Consulta | Caso | Causa | Ejecución |
|---|---|---|---|
| 1 | CP 01 | LLM `gemma-4-26b-a4b-it:free`: 429 compartido y Google 500 con la clave propia | #58 |
| 5 | CP 01 (reintento) | Igual | #61 |
| 6 | CP 01 | LLM `gemma-4-31b-it:free`: 429 compartido y Google 504 | #62 |
| 12 | CP 06 | Embeddings: 429 «free-models-per-day» (tope de 50 diarias con 5 créditos comprados) | #68 |

Las cuatro terminaron en **Error** con MSG_ERROR y `detalle_error`, sin quedar en Pendiente. Es el comportamiento que piden RN 10 y C8.

### 5.2 Calificaciones, datos mínimos y estadísticas

- Hay 5 calificaciones sobre las recomendaciones 1–5, con puntuaciones 5, 4, 3, 5 (sin comentario) y 2. La de la recomendación 1 se hizo desde el frontend (Historial → Ver detalle → estrellas y comentario); las otras cuatro, por la API (`pruebas/api/integracion-01..04-*.json`). Todas dieron 201 con `Location`.
- La base tiene 15 consultas (8 Respondida, 1 Sin resultados, 6 Error, **0 Pendiente**), 8 recomendaciones, 29 fuentes y 5 calificaciones.
- `GET /api/estadisticas` → `{"totalConsultas":15,"consultasRespondidas":8,"consultasSinResultados":1,"promedioCalificaciones":3.8,"cursoMasRecomendado":{"cursoId":4,"nombre":"Seguridad de aplicaciones web con OWASP Top 10","vecesRecomendado":3}}`. Es igual a `pruebas/bd/etapa7-control-estadisticas.sql` (15 / 8 / 1 / 3,80 / curso 4 con 3).

### 5.3 Robustez

| Prueba | Resultado | Solicitudes |
|---|---|---|
| `docker compose stop n8n` | Consulta 3: 201 **Error**, «n8n no respondió: conexión rechazada», en 0,11 s; la pregunta vacía, 400 | 0 |
| `docker compose stop qdrant` | Consulta 4: 201 **Error**, «Qdrant (/points/query) no respondió (EAI_AGAIN)» (#60) | 1 |
| Curso 1 desactivado sin re-indexar | Consulta 15 (CP 01 repetida): **Respondida** con 2, 3 y 4. Qdrant aún devolvió el 1 con `activo=true` y «Filtrar fuentes» lo descartó porque no está en `GET /api/cursos` (#71) | 2 |
| Restauración y re-indexado | `UPDATE` + W-2 (#72): 30 puntos, 28 activos, el punto 1 con `activo=true` | 1 |
| Consultas en Pendiente | 0 | 0 |

### 5.4 Solicitudes a OpenRouter

| Concepto | Solicitudes |
|---|---|
| W-2 (estado limpio y re-indexado) | 2 |
| 7 temáticas válidas (embedding + LLM) | 14 |
| Cocina (solo embedding) | 1 |
| Qdrant detenido (embedding) | 1 |
| Curso desactivado | 2 |
| Intentos fallidos externos (consultas 1, 5 y 6: 2 cada una; consulta 12: 1 rechazada) | 7 |
| **Total de la integración** | **27** (presupuesto ≤ 25) |

Total del proyecto: E0 8 (y 2 rechazadas por autenticación), E3 2, E4 1, E5 41, E6 7, E8 3 e integración 27, es decir, **89**.

## 6. Interfaz (RF 15 y RF 16)

El navegador integrado operó el frontend en las consultas 1–8:
- **Resultado:** separa «Recomendación de RutaIA» de «Fuentes del catálogo (n)», con la similitud de cada curso.
- **Mensaje de Sin resultados:** es el texto fijo.
- **Historial de Valeria:** 4 consultas de la más reciente a la más antigua, con fecha, estado, pregunta, resumen cortado con «…», cursos recomendados y «Ver detalle».
- **Detalle (E-12):** muestra la consulta completa y el formulario de calificación, que confirmó «Gracias: tu calificación quedó registrada».

## 7. Trazabilidad con evidencia

Referencias: «INT-n» son las filas de la sección «Integración» de `docs/pruebas-funcionales.md`; `ic` es `docs/evidencias/integracion-consultas.txt` e `iv` es `docs/evidencias/integracion-verificacion.txt`.

| ID | Requisito (resumen) | Etapa | Evidencia | Estado |
|---|---|---|---|---|
| RF 01 | Registro, correo único, niveles | E1 · E2 · E8 | `uq_estudiante_correo`, `ck_estudiante_nivel` (C4, `iv`); E-01 201/400/409 en `etapa2-api.txt`; capturas `etapa8-*-01-registro.png` | Cumple |
| RF 02 | Estudiante por id, listado, historial; 404 | E2 · E7 | E-02/E-03 en `etapa2-api.txt`; E-04 en `etapa7-api.txt`; historial en la interfaz (INT-14) | Cumple |
| RF 03 | Cursos: registrar, consultar, actualizar, desactivar | E1 · E2 | E-06 a E-10 en `etapa2-api.txt`; E-10 en INT-20 | Cumple |
| RF 04 | Catálogo: solo activos, filtros | E2 · E8 | E-05 en `etapa2-api.txt`; `etapa8-*-02-catalogo.png`; 27 activos sin el curso 1 (INT-20) | Cumple |
| RF 05 | Carga en Qdrant | E3 | W-2 #57 y #72 (30 puntos); C9 (`iv`): payload de 7 campos, id = `cursoId`, upsert sin duplicados | Cumple |
| RF 06 | El frontend solo llama a Spring Boot | E6 · E8 · INT | INT-04 a INT-11 desde el frontend; INT-23 (0 referencias); CORS (INT-02) | Cumple |
| RF 07 | Consulta Pendiente antes de n8n; 4 estados | E1 · E6 | `ck_consulta_estado`; orden de E-11 en `etapa6-api.txt`; 0 Pendiente (INT-21) | Cumple |
| RF 08 | Spring Boot envía id, pregunta, nivel y área | E5 · E6 | Petición W-1 en `etapa6-api.txt`; «PERFIL: nivel …; área …» en el mensaje al LLM (`ic`, #61) | Cumple |
| RF 09 | Embedding de la pregunta validado | E5 | «Validar entrada» y «Validar vector» en #63–#70; `input_type` `query` (C10) | Cumple |
| RF 10 | 3–5 cursos con payload y similitud, sin vectores, activos, por relevancia | E3 · E5 | INT-04 a INT-10: 3–5 fuentes ordenadas; `with_vector` false; filtro `activo` | Cumple |
| RF 11 | Umbral justificado; Sin resultados; nada irrelevante al modelo | E4 · E5 | `docs/calibracion-umbral.md` (0,34); INT-11 (sin LLM) | Cumple |
| RF 12 | Contexto RAG e instrucciones | E5 | `docs/n8n.md` («Mensaje enviado al modelo»); cuerpo real en #61 | Cumple |
| RF 13 | Respuesta con id, pregunta, respuesta, fuentes, similitud y estado | E5 · E6 | Respuestas E-11 y E-12 en `ic` | Cumple |
| RF 14 | Recomendación con varias fuentes | E1 · E6 | 8 recomendaciones con 29 fuentes (`ic`, estado final) | Cumple |
| RF 15 | Presentación de resultados y mensajes | E8 | Sección 6; `etapa8-*-04`, `-08` y `-09` | Cumple |
| RF 16 | Historial y detalle | E7 · E8 | Sección 6; E-04 y E-12 en `etapa7-api.txt` | Cumple |
| RF 17 | Calificación 1–5, comentario opcional, una por recomendación | E1 · E7 · E8 | INT-15; `uq_calificacion_recomendacion`; 400 y 409 en `etapa7-api.txt` | Cumple |
| RF 18 | Estadísticas (por Swagger) | E7 | INT-17; E-14 en `openapi.json` (sin captura, I-03) | Cumple |
| 5.2 | Administración de cursos por Swagger o Postman | E2 | `etapa2-swagger-e06-registrar-curso.png`, `etapa2-swagger-e10-desactivar-curso.png`; `openapi.json` | Cumple |
| RN 01 | Un correo, un estudiante | E1 · E2 | `uq_estudiante_correo` + `ck_estudiante_correo_normalizado`; 409 en `etapa2-api.txt` | Cumple |
| RN 02 | Solo cursos activos | E3 · E5 · E6 | INT-20 (doble filtro); validación de fuentes activas en `ClienteN8n` (`etapa6-api.txt`) | Cumple |
| RN 03 | Cursos con nombre y descripción | E1 · E2 | `ck_curso_nombre_no_vacio`, `ck_curso_descripcion_no_vacia`; 400 en `etapa2-api.txt` | Cumple |
| RN 04 | Duración > 0 | E1 · E2 | `ck_curso_duracion_positiva`; 400 para 0 y para decimal en `etapa2-api.txt` | Cumple |
| RN 05 | Consulta de un estudiante existente | E1 · E6 | `fk_consulta_estudiante`; INT-13 (404 sin fila) | Cumple |
| RN 06 | Preguntas vacías no llegan a n8n | E6 (E5 valida de nuevo) | INT-12 (sin ejecución, también con n8n detenido); 400 de W-1 en `etapa5-consulta-rag.txt` | Cumple |
| RN 07 | Las fuentes existen en la base | E5 · E6 | `fk_fuente_curso`; «Filtrar fuentes» contra `GET /api/cursos`; validación en `ClienteN8n` | Cumple |
| RN 08 | La respuesta no incluye cursos no recuperados | E5 | «Validar salida»; 0 cursos ajenos en las 8 Respondida (`ic`) | Cumple |
| RN 09 | Sin resultados relevantes → Sin resultados | E5 · E6 | INT-11 | Cumple |
| RN 10 | Falla de n8n, Qdrant u OpenRouter → Error | E5 · E6 | INT-18, INT-19; consultas 1, 5, 6 y 12 (OpenRouter) | Cumple |
| RN 11 | Calificación entre 1 y 5 | E1 · E7 | `ck_calificacion_puntuacion`; 400 para 0, 6 y decimal en `etapa7-api.txt` | Cumple |
| RN 12 | Claves fuera del código y del repositorio | E0 (y todas) | INT-22; sección 3.11 | Cumple |
| Sec. 2 | Un único proceso funcional | INT | INT-04 a INT-21 | Cumple |
| 8.1 | Frontend: HTML, CSS, JS, Fetch, async/await, DOM, adaptable | E8 | `docs/frontend.md`; 18 capturas a 1280 y 360 px | Cumple |
| 8.2 | Backend: capas, DTO, excepciones globales, CORS, Swagger | E2 · E6 · E7 | `backend/` (C3), `ManejadorGlobalExcepciones`, `WebConfiguracion`, `openapi.json` | Cumple |
| 8.3 | BD: entidades, claves, restricciones, índices justificados | E1 | C4 sin diferencias (`iv`); `docs/base-de-datos.md` §1 y §4 | Cumple |
| 8.4 | Qdrant: colección, inserción, consulta, payload y score | E3 · E4 | `etapa3-qdrant.txt`; C9 (`iv`); `docs/calibracion-resultados.json` | Cumple |
| 8.5a | Flujo «Indexación de cursos» | E3 | `n8n/rutaia-indexacion-cursos.json`; #57 y #72 | Cumple |
| 8.5b | Flujo «Consulta RAG» | E5 | `n8n/rutaia-consulta-rag.json`; #59–#71 | Cumple |
| OE 7 | Validaciones, errores, documentación y pruebas | E1–E8 · INT | `docs/pruebas-funcionales.md`, este informe, README | Cumple |
| D1 | 10 estudiantes | E1 | Semilla: 10 | Cumple |
| D2 | 20 cursos con descripciones detalladas | E1 (E3) | Semilla: 30 cursos (28 activos) | Cumple |
| D3 | 5 categorías | E1 | 7 categorías | Cumple |
| D4 | 3 niveles | E1 | Básico, Intermedio y Avanzado | Cumple |
| D5 | 10 consultas de prueba | INT | 15 consultas (INT-16) | Cumple |
| D6 | 5 calificaciones | INT | 5 (INT-15) | Cumple |
| CP 01–07 | Siete consultas temáticas | E4 · E5 · E6 · INT | INT-04 a INT-10 | Cumple |
| CP 08 | Cocina italiana → Sin resultados | E4 · E5 · E6 · INT | INT-11 | Cumple |
| CP 09 | Pregunta vacía rechazada sin llegar a n8n | E6 · INT | INT-12 | Cumple |
| CP 10 | Estudiante inexistente sin crear la consulta | E6 · INT | INT-13 | Cumple |
| ENT-01 | Enlace al repositorio | E0 · INT | README §1 | Cumple |
| ENT-02 | Frontend | E8 | `frontend/` | Cumple |
| ENT-03 | Backend | E2 · E6 · E7 | `backend/` | Cumple |
| ENT-04 | Script SQL | E1 | `database/` | Cumple |
| ENT-05 | Diagrama entidad-relación | E1 | `docs/base-de-datos.md` §1 | Cumple |
| ENT-06 | Diagrama de arquitectura | E0 (INT lo verifica) | `docs/arquitectura.md` (actualizado con el modelo vigente) | Cumple |
| ENT-07 | JSON de los flujos | E3 · E4 · E5 | `n8n/*.json`, iguales a la versión publicada | Cumple |
| ENT-08 | Docker Compose | E0 | `docker-compose.yml` (C12) | Cumple |
| ENT-09 | `.env.example` | E0 | C11 | Cumple |
| ENT-10 | Evidencia de Swagger | E2 · E6 · E7 · INT | `openapi.json` completo; capturas solo de E-01, E-02, E-05, E-06, E-10, E-11 y los listados | **Parcial** (I-03) |
| ENT-11 | Documento de pruebas | E0–E8 · INT | `docs/pruebas-funcionales.md` (con «Integración», totales y resumen) | Cumple |
| ENT-12 | README con 8 secciones | E0 · INT | `README.md` | Cumple |
| ENT-13 | Presentación final | INT | `docs/presentacion-final.md` (guion) | **Parcial**: falta armar las diapositivas a partir del guion |
| ENT-14 | Repo sin secretos, `.env`, dependencias ni compilados | E0 · INT | Sección 3.11 | Cumple |

Total: **63 filas**, 61 Cumple y 2 Parcial (ENT-10 y ENT-13); ninguna No cumple.

## 8. Verificación cruzada (otro sistema operativo)

**Resultado: pendiente.** El proyecto es individual y la integración se hizo en Windows 11. Para cubrir Linux, el autor debe seguir el README desde cero en Ubuntu, por ejemplo en WSL2, en una máquina virtual o en otro equipo, y anotar el resultado en la tabla.

Lista de verificación:
1. Versiones: `docker --version`, `docker compose version`, `java -version` (21), `git --version`.
2. `git clone https://github.com/JeshuaPerez/RutIA.git && cd RutIA`, `cp .env.example .env` y escribir la contraseña.
3. `docker compose up -d` y `docker compose ps`: los 3 servicios arriba y postgres `healthy`.
4. `docker compose exec postgres psql -U rutaia_app -d rutaia -c "select count(*) from curso;"` → 30.
5. n8n en http://localhost:5678:
   - crear la cuenta y la credencial «RutaIA OpenRouter»;
   - importar los 3 flujos (README §6), asignar la credencial, publicar y `docker compose restart n8n`;
   - `list:workflow --active=true` → 3.
6. `ls -l backend/gradlew` muestra `-rwxr-xr-x` y `file backend/gradlew` no dice CRLF. Luego `cd backend && ./gradlew bootRun` → `curl -s http://localhost:8080/api/estudiantes` devuelve 10 estudiantes.
7. Si ufw está activo: `sudo ufw allow from 172.16.0.0/12 to any port 8080 proto tcp`. Después, `docker compose exec n8n wget -qO- http://host.docker.internal:8080/api/cursos | head -c 80`.
8. `curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos` → `totalPuntos` 30.
9. `jwebserver -p 5500 -d "$(pwd)/frontend"` y abrir http://localhost:5500 → elegir un estudiante → «Necesito desplegar aplicaciones usando contenedores.» → Respondida con fuentes.
10. Anotar el resultado:

| Paso | Resultado (OK / falla + mensaje) | Tiempo |
|---|---|---|
| Contenedores arriba | | |
| Backend | | |
| Frontend | | |
| Consulta temática | | |

## 9. Inconsistencias

| ID | Descripción | Evidencia | Etapa responsable | Corrección sugerida | Estado |
|---|---|---|---|---|---|
| I-01 | `modeloLlm` de los flujos (`google/gemma-4-26b-a4b-it:free`) distinto del de C8/C10 (`google/gemma-4-31b-it:free`); el cambio no estaba registrado en el contrato | §3.7; `docs/n8n.md` (Etapa 5) | E5 | Registrar el cambio en el contrato o volver al modelo del contrato | **Resuelta**: el autor decidió usar `google/gemma-4-31b-it` (variante de pago, excepción a «nunca con modelos de pago»), registrada en `docs/contrato.md` |
| I-02 | La alternativa de LLM de C10, `nvidia/nemotron-3-super-120b-a12b:free`, ya no existe en OpenRouter (2026-11-03) | Listado público de modelos | Contrato (C10) | Anotar como alternativa la variante de pago de Gemma u otro modelo gratuito que no razone (los que razonan agotan `max_tokens`, ver `docs/n8n.md`) | Registrada en el contrato |
| I-03 | Capturas de Swagger incompletas: faltan E-03, E-04, E-07, E-08, E-09, E-12, E-13 y E-14. `openapi.json` sí documenta las 14 operaciones | `docs/evidencias/` | E7 (E-04, E-12 a E-14) y E2 (E-03, E-07 a E-09) | Tomar en Swagger UI la vista expandida y una respuesta de «Try it out» de E-04, E-12, E-13 y E-14 (las de la rúbrica de RF 18). E-13 crea datos: hacerlo sobre una recomendación sin calificar | Abierta (el autor decidió en la Etapa 7 incluir solo E-02) |
| I-04 | La verificación de la clave en `docs/entorno-local.md` §5 y el cuerpo `pruebas/n8n/openrouter-chat.json` siguen usando `google/gemma-4-26b-a4b-it:free`, que hoy no responde | `git grep gemma-4-26b` | E0 (guía) y E5 (cuerpo) | Cambiar el cuerpo a `google/gemma-4-31b-it` o marcar la verificación del chat como opcional (cuesta menos de 0,001 USD) | Abierta |
| I-05 | En el n8n local queda el flujo de prueba `RutaIAPruebaError` («tmp prueba error»), inactivo. No está en el repositorio | `n8n list:workflow` | E5 | Borrarlo desde el editor de n8n (la CLI no borra flujos) | Abierta |
| I-06 | La búsqueda literal de «sk-or-» no da 0: la documentación tiene marcadores `sk-or-v1-…` y comandos `grep` que buscan ese texto. No son claves | §3.11; `iv` | E0 (guía), E3–E5 (evidencias) | Ninguna obligatoria. Si se exige un 0 literal, cambiar los marcadores por `<clave>` | Aceptada |
| I-07 | En W-1, Qdrant se consulta después del embedding, así que con Qdrant caído se gasta 1 solicitud. C8 pide «preparar Qdrant» antes de OpenRouter; la indexación y la calibración sí lo hacen | INT-19; `docs/n8n.md` («Orden que ahorra cuota») | E5 | Opcional: un `GET /collections/rutaia_cursos/exists` antes del embedding | Aceptada (decisión documentada por la Etapa 5) |
| I-08 | No hay endpoint para reactivar un curso: C7 solo define `desactivar`, y E-09 no cambia `activo`. La restauración se hizo por SQL | INT-20 | Contrato (C7) | Si se quiere, agregar `PATCH /api/admin/cursos/{id}/activar` en una versión futura | Observación |
| I-09 | El presupuesto de la integración (≤ 25) se superó: 27 | §5.4 | INT | Ninguna: las 2 de más vienen de fallas externas | Registrada |
| I-10 | En CP 01, «Seguridad de aplicaciones web con OWASP Top 10» (0,3644) aparece como cuarta fuente para un principiante que quiere hacer páginas web, y el LLM la propone como paso final | `ic` (consulta 7); `docs/calibracion-umbral.md` (único no pertinente sobre 0,34) | E4 | Ninguna: es el caso límite documentado de la calibración. Subirlo exigiría un umbral sin margen con S_pert | Aceptada |
| I-11 | En la copia de trabajo hay una carpeta sin versionar, `Claude outputs/`, con una copia de `contrato.md` | `git status` | — (copia local) | Borrarla o no agregarla al commit | Abierta |

## 10. Entregables de documentación

| Entregable | Estado |
|---|---|
| `README.md` con las 8 secciones | Hecho: comandos de Windows y Linux, variables de `.env.example`, importación de flujos (importar, asignar la credencial, publicar e indexar), tabla de endpoints con ejemplos, enlaces a `docs/arquitectura.md` y `docs/base-de-datos.md`, integrante y repositorio, y «Errores conocidos» con los 10 casos pedidos y los encontrados en la integración |
| `docs/pruebas-funcionales.md` | Hecho: sección «Integración» (INT-01 a INT-24), total de solicitudes por etapa (89) y resumen final |
| `docs/presentacion-final.md` | Hecho: guion de 12 diapositivas con la demo |
| `docs/openapi.json` | Existe; idéntico a `/v3/api-docs` |
| Capturas de Swagger en `docs/evidencias/` | Existen 8 (parciales, I-03) |

## 11. Pendientes para el autor

1. Hacer la verificación cruzada en Linux (sección 8) y anotar el resultado.
2. Revisar los cambios y hacer el commit de la integración. Incluye `docs/contrato.md` y `docs/trazabilidad.md`, que todavía no están versionados. No hay que agregar `Claude outputs/`.
3. Opcional: completar las capturas de Swagger (I-03), borrar `RutaIAPruebaError` en n8n (I-05) y actualizar la verificación del chat (I-04).
4. Armar las diapositivas a partir de `docs/presentacion-final.md`.
