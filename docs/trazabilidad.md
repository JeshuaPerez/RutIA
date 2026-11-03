# Tabla de trazabilidad — RutaIA

Copia literal de la tabla de la Parte 1 del plan. Cada requisito obligatorio del documento y cada entregable de la rúbrica tiene la etapa que lo cubre. La integración (INT) agrega a cada fila su evidencia y su estado en `docs/informe-integracion.md`.

| ID | Requisito (resumen) | Etapa |
|---|---|---|
| RF 01 | Registro: nombre, correo, nivel, área; correo único; niveles Principiante/Intermedio/Avanzado | E1 · E2 · E8 |
| RF 02 | Estudiante por id, listado, historial; respuesta si no existe | E2 · E7 |
| RF 03 | Cursos: campos; niveles Básico/Intermedio/Avanzado; registrar, consultar, actualizar, desactivar | E1 · E2 |
| RF 04 | Catálogo: solo activos, campos visibles, filtros por categoría y nivel | E2 · E8 |
| RF 05 | Carga en Qdrant: flujo n8n, colección, payload, relación con el curso, sin duplicados, ≥20 cursos | E3 |
| RF 06 | Formulario; la consulta va primero a Spring Boot; el frontend no llama a n8n, Qdrant ni OpenRouter | E6 · E8 · INT |
| RF 07 | Guardar la consulta (Pendiente) antes de pedir la recomendación; 4 estados | E1 · E6 |
| RF 08 | Spring Boot envía id, pregunta, nivel y área a n8n; intermediario obligatorio | E5 · E6 |
| RF 09 | Embedding de la pregunta: validar, mismo modelo, vector válido | E5 |
| RF 10 | Búsqueda: 3–5 cursos con payload y similitud, sin vectores, solo activos, por relevancia | E3 · E5 |
| RF 11 | Umbral establecido y justificado; informar sin resultados; nada irrelevante al modelo | E4 · E5 |
| RF 12 | Contexto RAG con sus campos e instrucciones al modelo | E5 |
| RF 13 | Respuesta: id, pregunta, respuesta, fuentes, similitudes, estado; campos por fuente | E5 · E6 |
| RF 14 | Guardar la recomendación con varias fuentes | E1 · E6 |
| RF 15 | Presentación de resultados y mensajes; recomendación distinta de fuentes | E8 |
| RF 16 | Historial (fecha, pregunta, estado, resumen, cursos) y detalle | E7 · E8 |
| RF 17 | Calificación 1–5, comentario opcional, una por recomendación | E1 · E7 · E8 |
| RF 18 | Estadísticas básicas (por Swagger) | E7 |
| 5.2 | Operaciones administrativas de cursos probables por Swagger o Postman | E2 |
| RN 01 | Un correo, un estudiante | E1 · E2 |
| RN 02 | Solo se recomiendan cursos activos | E3 · E5 · E6 |
| RN 03 | Sin cursos sin nombre o descripción | E1 · E2 |
| RN 04 | Duración > 0 | E1 · E2 |
| RN 05 | Toda consulta pertenece a un estudiante existente | E1 · E6 |
| RN 06 | Preguntas vacías no llegan a n8n | E6 (E5 valida de nuevo) |
| RN 07 | Las fuentes existen en la base relacional | E5 · E6 |
| RN 08 | La respuesta no incluye cursos no recuperados | E5 |
| RN 09 | Sin resultados relevantes → Sin resultados | E5 · E6 |
| RN 10 | Falla de n8n, Qdrant u OpenRouter → Error | E5 · E6 |
| RN 11 | Calificación entre 1 y 5 | E1 · E7 |
| RN 12 | Claves fuera del código y del repositorio | E0 (y todas) |
| Sec. 2 | Todos los componentes en un único proceso funcional | INT |
| 8.1 | Frontend: HTML, CSS, JS, Fetch, async/await, DOM, adaptable, todas las secciones | E8 |
| 8.2 | Backend: Java, Spring Boot, Web, JPA, Validation, Gradle, driver; REST, códigos, capas, DTO, excepciones globales, CORS, Swagger | E2 (base) · E6 · E7 |
| 8.3 | BD: entidades, PK, FK, restricciones, relaciones, índices justificados | E1 |
| 8.4 | Qdrant: id, vector y payload; demostrar colección, inserción, consulta, payload y score | E3 · E4 |
| 8.5a | Flujo «Indexación de cursos» | E3 |
| 8.5b | Flujo «Consulta RAG» | E5 |
| OE 7 | Validaciones, manejo de errores, documentación y pruebas funcionales | E1–E8 · INT |
| D1 | 10 estudiantes | E1 |
| D2 | 20 cursos con descripciones detalladas | E1 (indexados en E3) |
| D3 | 5 categorías | E1 |
| D4 | 3 niveles de dificultad | E1 |
| D5 | 10 consultas de prueba | INT |
| D6 | 5 calificaciones | INT |
| CP 01–07 | Siete consultas temáticas | E4 · E5 · E6 · INT |
| CP 08 | Cocina italiana → Sin resultados | E4 · E5 · E6 · INT |
| CP 09 | Pregunta vacía → rechazada sin llegar a n8n | E6 · INT |
| CP 10 | Estudiante inexistente → sin crear la consulta | E6 · INT |
| ENT-01 | Enlace al repositorio | E0 (repo Git) · INT (README) |
| ENT-02 | Código fuente del frontend | E8 |
| ENT-03 | Código fuente del backend | E2 · E6 · E7 |
| ENT-04 | Script SQL | E1 |
| ENT-05 | Diagrama entidad-relación | E1 |
| ENT-06 | Diagrama de arquitectura | E0 (INT lo verifica) |
| ENT-07 | JSON de los flujos de n8n | E3 · E4 · E5 |
| ENT-08 | Archivo Docker Compose | E0 |
| ENT-09 | Archivo .env.example | E0 |
| ENT-10 | Evidencia de Swagger (en lugar de Postman) | E2 · E6 · E7 · INT |
| ENT-11 | Documento de pruebas | E0–E8 (secciones) · INT (consolidado) |
| ENT-12 | README con las 8 secciones mínimas | E0 (esqueleto) · INT |
| ENT-13 | Presentación final | INT (guion) |
| ENT-14 | Repo sin contraseñas, claves, .env, dependencias ni compilados | E0 · INT |
