# Pruebas de RutaIA

Documento de pruebas del proyecto. Todo se ejecutó en Windows 11 Home 10.0.26200 entre el 2026-11-01 y el 2026-11-03, con JDK 21.0.13, Docker Desktop y los contenedores de `docker-compose.yml`. Los comandos de Linux están documentados en el README, pero **no se han verificado**.

Resultado global: 62 de 63 requisitos cumplidos y 1 parcial (la presentación, que falta armar a partir del guion). Las 10 consultas de prueba pasan.

## Cómo probar

Con los contenedores, el backend y el frontend en marcha (README, «Ejecución de contenedores, backend y frontend») y los cursos indexados:

- **Swagger UI**: http://localhost:8080/swagger-ui.html → elige el endpoint → *Try it out* → pega el cuerpo de `pruebas/api/` → *Execute*. La especificación está en [openapi.json](openapi.json).
- **curl**: desde la raíz del repositorio. En PowerShell, escribe `curl.exe` en lugar de `curl`.
  ```bash
  curl -s -i -X POST "http://localhost:8080/api/consultas" --data-binary "@pruebas/api/consulta-cp01-paginas-web.json" -H "Content-Type: application/json"
  ```
- **Frontend**: http://localhost:5500. Elige un estudiante en Registro y escribe la pregunta en Consultar.
- **SQL de control** de las estadísticas, igual en PowerShell y bash:
  ```bash
  docker compose cp pruebas/bd/control-estadisticas.sql postgres:/tmp/control-estadisticas.sql
  docker compose exec postgres psql -U rutaia_app -d rutaia -f /tmp/control-estadisticas.sql
  ```

Cada consulta temática gasta 2 solicitudes a OpenRouter (embedding gratuito y LLM de pago). La de cocina gasta 1, y la 9 y la 10, ninguna.

## Requisito → caso → resultado → evidencia

Los cuerpos están en `pruebas/api/` y las capturas en `evidencias/`.

| Requisito | Caso | Resultado | Evidencia |
|---|---|---|---|
| RF 01 Registro, correo único, niveles | E-01 con `estudiante-valido`, `estudiante-correo-duplicado`, `estudiante-nivel-invalido` | 201 con el correo `ana.torres@rutaia.test`; 409 «ya está registrado»; 400 | [e01](evidencias/etapa2-swagger-e01-registrar-estudiante.png), [360-01](evidencias/etapa8-360-01-registro.png) |
| RF 02 Estudiante por id, listado, historial | E-02, E-03 `/1` y `/999999`, E-04 `/1/historial` | 200 con ids ascendentes; 200; 404 «No existe un estudiante con id 999999»; 200, fecha descendente | [openapi.json](openapi.json), [e04](evidencias/swagger-e04-historial.png), [1280-05](evidencias/etapa8-1280-05-historial.png) |
| RF 03 Registrar, consultar, actualizar y desactivar cursos | E-06 `curso-valido`, E-08, E-09 `curso-actualizado`, E-10 dos veces | 201 con `activo` true; 200; 200 sin tocar `activo`; 200 con `activo` false (idempotente); 404 con id 999999 | [e06](evidencias/etapa2-swagger-e06-registrar-curso.png), [e10](evidencias/etapa2-swagger-e10-desactivar-curso.png) |
| RF 04 Catálogo: solo activos y filtros | E-05 sin filtros, `?categoria=Desarrollo%20web`, `?nivel=Experto` | 28 cursos (sin el 29 ni el 30); 4; 400 | [e05](evidencias/etapa2-swagger-e05-catalogo.png) |
| RF 05 Carga en Qdrant | Indexación, dos veces | 30 puntos, `coleccionCreada` true y luego false; payload de 7 campos con id = `cursoId` | [n8n.md](n8n.md#indexación) |
| RF 06 El frontend solo habla con la API | Buscar `5678`, `6333`, `openrouter` y `webhook` en `frontend/` | 0 coincidencias; único `fetch` en `js/api.js` | [config.js](../frontend/js/config.js) |
| RF 07 Pendiente antes de n8n; 4 estados | Consulta con n8n detenido; conteo por estado | 201 Error con `detalle_error`; 0 consultas en Pendiente | [Robustez](#robustez) |
| RF 08 Spring Boot envía id, pregunta, nivel y área | CP 01 | El mensaje al LLM trae «PERFIL: nivel …; área …» | [n8n.md](n8n.md#consulta-rag) |
| RF 09 Embedding de la pregunta validado | CP 01–08 | «Validar entrada» y «Validar vector» (2048 números finitos) | [rutaia-consulta-rag.json](../n8n/rutaia-consulta-rag.json) |
| RF 10 3–5 cursos con payload y similitud, sin vectores | CP 01–07 | 3 a 5 fuentes activas, ≥ 0,34, ordenadas, sin vectores | [Las 10 consultas](#las-10-consultas) |
| RF 11 Umbral justificado; Sin resultados | Calibración; CP 08 | Umbral 0.34; la cocina termina en Sin resultados sin llamar al LLM | [n8n.md](n8n.md#calibración), [1280-08](evidencias/etapa8-1280-08-sin-resultados.png) |
| RF 12 Contexto RAG e instrucciones | Nodo «Construir mensaje» en CP 01 | Un mensaje `user` con 7 reglas y el contexto por fuente | [n8n.md](n8n.md#consulta-rag) |
| RF 13 Respuesta con id, pregunta, respuesta, fuentes, similitud y estado | E-11 con CP 01 | 9 campos y 7 por fuente; similitud con 4 decimales | [e11](evidencias/etapa6-swagger-e11-consultas.png) |
| RF 14 Recomendación con varias fuentes | Estado final de la integración | 8 recomendaciones con 29 fuentes | [Las 10 consultas](#las-10-consultas) |
| RF 15 Presentación de resultados y mensajes | Frontend: CP 03 y CP 08 | La recomendación va separada de las fuentes, con su similitud; mensaje fijo de Sin resultados | [1280-04](evidencias/etapa8-1280-04-resultado.png), [360-04](evidencias/etapa8-360-04-resultado.png), [1280-08](evidencias/etapa8-1280-08-sin-resultados.png) |
| RF 16 Historial y detalle | Historial → Ver detalle | Fecha descendente, resumen de 160 caracteres más «…» y cursos; el detalle es E-11 más la calificación | [e04](evidencias/swagger-e04-historial.png), [e12](evidencias/swagger-e12-detalle.png), [1280-05](evidencias/etapa8-1280-05-historial.png), [1280-07](evidencias/etapa8-1280-07-calificacion.png) |
| RF 17 Calificación 1–5, comentario opcional, una sola | E-13 con `calificacion-con-comentario`, `calificacion-sin-comentario`, `calificacion-comentario-501` y un segundo POST | 201; 201 con `comentario` null; 400; 409 «ya fue calificada» | [e13](evidencias/swagger-e13-calificacion.png), [1280-07](evidencias/etapa8-1280-07-calificacion.png) |
| RF 18 Estadísticas | E-14 frente a `control-estadisticas.sql` | Iguales: 15 / 8 / 1, promedio 3,8 y curso 4 con 3 | [e14](evidencias/swagger-e14-estadisticas.png), [control-estadisticas.sql](../pruebas/bd/control-estadisticas.sql) |
| 5.2 Administración de cursos por Swagger | E-06 y E-10 en Swagger UI | 201 y 200 | [e06](evidencias/etapa2-swagger-e06-registrar-curso.png), [e10](evidencias/etapa2-swagger-e10-desactivar-curso.png) |
| RN 01 Un correo, un estudiante | `estudiante-correo-duplicado`; 10 POST simultáneos con el mismo correo | 409; 1 × 201 y 9 × 409 | `uq_estudiante_correo` |
| RN 02 Solo cursos activos | Curso 1 desactivado sin re-indexar y CP 01 | Fuentes 2, 3 y 4: Qdrant devolvió el 1 y «Filtrar fuentes» lo descartó | [Robustez](#robustez) |
| RN 03 Cursos con nombre y descripción | E-06 con `curso-sin-nombre` | 400 «es obligatorio» | `ck_curso_nombre_no_vacio` |
| RN 04 Duración mayor que cero | E-06 con `curso-duracion-cero` | 400 «debe ser mayor que cero» | `ck_curso_duracion_positiva` |
| RN 05 Consulta de un estudiante existente | CP 10 | 404, sin crear la consulta | `fk_consulta_estudiante` |
| RN 06 Las preguntas vacías no llegan a n8n | CP 09, con n8n arriba y detenido | 400 en ambos, sin ejecución en n8n | `consulta-cp09-pregunta-espacios` |
| RN 07 Las fuentes existen en la base | n8n simulado con fuentes inexistentes o inactivas | 201 Error con su motivo, sin recomendación | `fk_fuente_curso` |
| RN 08 La respuesta no nombra cursos no recuperados | «Validar salida» en CP 01–07 | 0 cursos ajenos | [n8n.md](n8n.md#consulta-rag) |
| RN 09 Sin resultados relevantes → Sin resultados | CP 08 | Sin resultados, sin recomendación | [1280-08](evidencias/etapa8-1280-08-sin-resultados.png) |
| RN 10 Falla de n8n, Qdrant u OpenRouter → Error | n8n y Qdrant detenidos; LLM con 429/500/504 | 201 Error; nada en Pendiente | [Robustez](#robustez) |
| RN 11 Calificación entre 1 y 5 | E-13 con `calificacion-puntuacion-cero` y `calificacion-puntuacion-seis` | 400 «debe estar entre 1 y 5» | `ck_calificacion_puntuacion` |
| RN 12 Claves fuera del código y del repositorio | `git ls-files` y búsqueda de claves y de la contraseña en archivos e historial | Sin `.env`, sin claves reales y 0 apariciones de la contraseña | [.gitignore](../.gitignore) |
| CP 01–07 Consultas temáticas | Frontend | 7 de 7 Respondida | [Las 10 consultas](#las-10-consultas) |
| CP 08 Cocina italiana | Frontend | Sin resultados | [1280-08](evidencias/etapa8-1280-08-sin-resultados.png) |
| CP 09 Pregunta vacía | API | 400, sin consulta ni ejecución en n8n | [Las 10 consultas](#las-10-consultas) |
| CP 10 Estudiante inexistente | API | 404, sin consulta | [Las 10 consultas](#las-10-consultas) |
| 8.1 Frontend: HTML, CSS, JS, Fetch, DOM, adaptable | Recorrido a 1280 y 360 px | Sin desplazamiento horizontal a 360 px; barra inferior de pestañas | [360-04](evidencias/etapa8-360-04-resultado.png) |
| 8.2 Backend: REST, DTO, errores globales, CORS, Swagger | `json-mal-formado`; `GET /api/no-existe`; preflight desde `http://otro.test` | 400; 404 con el cuerpo de error; 403 | [listado](evidencias/etapa6-swagger-endpoints.png), [openapi.json](openapi.json) |
| 8.3 Base: claves, restricciones, índices justificados | 15 inserciones inválidas en `BEGIN … ROLLBACK` | Cada una falla con su restricción y no deja filas | [base-de-datos.md](base-de-datos.md) |
| 8.4 Qdrant: colección, inserción, consulta, payload y score | `GET /collections/rutaia_cursos`; calibración | 2048, Cosine, 30 puntos; top 10 con score | [calibracion-resultados.json](evidencias/calibracion-resultados.json) |
| 8.5a Flujo «Indexación de cursos» | Webhook de indexación | 200 con 30 puntos | [rutaia-indexacion-cursos.json](../n8n/rutaia-indexacion-cursos.json) |
| 8.5b Flujo «Consulta RAG» | CP 01–08 | Respondida o Sin resultados según el umbral | [rutaia-consulta-rag.json](../n8n/rutaia-consulta-rag.json) |

## Las 10 consultas

Integración del 2026-11-03, con la base reiniciada a la semilla y los cursos re-indexados. Las consultas 1–8 se hicieron desde el frontend y la 9 y la 10 por la API. Cada cuerpo es `pruebas/api/consulta-cpNN-*.json`.

| CP | Pregunta | Estudiante | Resultado | Fuentes (cursoId: similitud) |
|---|---|---|---|---|
| 01 | Quiero aprender a crear páginas web. | 1 | 201 Respondida | 1: 0,5930 · 2: 0,3843 · 3: 0,3750 · 4: 0,3644 |
| 02 | Necesito aprender Java para trabajar con Spring Boot. | 2 | 201 Respondida | 5: 0,6286 · 6 · 7 · 8: 0,3974 |
| 03 | Me interesa analizar datos y construir dashboards. | 4 | 201 Respondida | 21: 0,5322 · 20 · 9 · 22: 0,3856 |
| 04 | Quiero automatizar procesos empresariales. | 5 | 201 Respondida | 17: 0,5534 · 18 · 19: 0,4471 |
| 05 | ¿Qué puedo estudiar para trabajar con inteligencia artificial? | 3 | 201 Respondida | 12: 0,5256 · 16 · 14 · 15 · 13: 0,3730 |
| 06 | Quiero aprender a proteger aplicaciones web. | 6 | 201 Respondida | 4: 0,5599 · 27 · 8: 0,4087 |
| 07 | Necesito desplegar aplicaciones usando contenedores. | 7 | 201 Respondida | 24: 0,6088 · 26 · 25: 0,4204 |
| 08 | Quiero aprender cocina italiana. | 8 | 201 Sin resultados, sin llamar al LLM | — |
| 09 | `"   "` | 1 | 400 «es obligatoria», sin consulta nueva ni ejecución en n8n | — |
| 10 | Quiero aprender a crear páginas web. | 999999 | 404, sin consulta nueva | — |

En las 7 Respondida:
- las fuentes coinciden con la calibración y son las de la línea base;
- la respuesta está en texto plano;
- la respuesta solo nombra cursos de sus fuentes.

Antes de los resultados válidos, 4 intentos terminaron en Error por causas externas, sin quedar en Pendiente:
- 3 en CP 01, por el proveedor del LLM gratuito (429 y Google 500/504), lo que llevó a pasar al LLM de pago;
- 1 en CP 06, por el tope diario de los modelos `:free`.

**Regresión de R1** (commit `7b90ccf`, Windows 11): las CP 01 a 10 dieron lo mismo que la línea base. CP 08 terminó una vez en 201 Error porque n8n no pudo conectarse a OpenRouter (ECONNREFUSED); al repetirla dio Sin resultados. Además, 62 de 62 casos 400/404/405/409/415 fueron iguales a `entrega-v1`.

**Verificación final (2026-11-03)**, Windows 11 con la configuración por defecto: los comandos de «Puertos» del README muestran `com.docker.backend` en 5433, 6333 y 5678, Java en 8080 y `jwebserver` en 5500; las CP 01 a 10 dieron lo mismo que la línea base, con las mismas fuentes y similitudes (consultas 35 a 42; la 9 y la 10 sin consulta nueva ni ejecución en n8n); con n8n detenido, 201 Error «n8n no respondió: conexión rechazada» y 0 en Pendiente; `GET /v3/api-docs` es igual a [openapi.json](openapi.json) (14 operaciones); E-14 es igual a `control-estadisticas.sql` (43 / 28 / 6, promedio 4,14 y curso 4 con 9). Se gastaron 15 solicitudes a OpenRouter.

**Datos mínimos y calificaciones.**
- La base terminó con 15 consultas: 8 Respondida, 1 Sin resultados, 6 Error y 0 Pendiente.
- Hay 8 recomendaciones con 29 fuentes y 5 calificaciones: 5, 4, 3, 5 (sin comentario) y 2. La primera se hizo desde el detalle del frontend y las otras por la API; todas dieron 201 con `Location`.
- `GET /api/estadisticas` devolvió `{"totalConsultas":15,"consultasRespondidas":8,"consultasSinResultados":1,"promedioCalificaciones":3.8,"cursoMasRecomendado":{"cursoId":4,…,"vecesRecomendado":3}}`, igual a `control-estadisticas.sql`.

## Robustez

| Prueba | Resultado |
|---|---|
| `docker compose stop n8n` y una consulta (`consulta-robustez-bases-de-datos`) | 201 Error en 0,11 s, `detalle_error` «n8n no respondió: conexión rechazada», sin gastar cuota; CP 09 sigue dando 400 |
| `docker compose stop qdrant` y la misma consulta | 201 Error, «Qdrant (/points/query) no respondió (EAI_AGAIN)»; el embedding ya se había gastado, por diseño |
| API detenida y llamada directa al webhook de Consulta RAG | 502 «La API (GET /cursos) no respondió (ECONNREFUSED)», sin llamar a OpenRouter |
| Curso 1 desactivado sin re-indexar y CP 01 | Respondida con 2, 3 y 4. Después se restauró con `UPDATE curso SET activo = true WHERE id = 1;` y se re-indexó |
| n8n simulado con 11 respuestas inválidas: `consultaId` distinto, respuesta vacía, fuente inexistente o inactiva, más de 5 fuentes, similitud fuera de [0, 1]… | 11 de 11 en 201 Error con su motivo, sin recomendación |
| 10 calificaciones simultáneas de la misma recomendación | 1 × 201 y 9 × 409; `uq_calificacion_recomendacion` frena el caso concurrente con psql |
| Frontend con la API detenida | «No se pudo conectar con el servidor» en el aviso, en el catálogo y al consultar |
| Pregunta vacía en el frontend | Error en el campo y 0 peticiones a `/api/consultas` |
