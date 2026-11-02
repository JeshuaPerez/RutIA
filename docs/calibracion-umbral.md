# Calibración del umbral de similitud (Etapa 4)

RF 11 pide «establecer y justificar un umbral mínimo de similitud», «informar cuando ningún curso supere el umbral» y «no enviar al modelo información irrelevante». El umbral no se fijó de antemano: la escala de los scores depende del modelo de embeddings, así que se midió con las consultas de prueba del documento (sección 10).

## Datos de la medición

| Dato | Valor |
|---|---|
| Modelo de embeddings | `nvidia/nemotron-3-embed-1b:free` (OpenRouter, capa gratuita), 2048 dimensiones, asimétrico |
| Colección | `rutaia_cursos` (Qdrant v1.20.0, distancia Cosine), 30 puntos: 28 activos y 2 inactivos (29 y 30), indexados en la Etapa 3 con `input_type` `passage` |
| Fecha | 2026-11-02 17:42 (hora local, UTC−6) |
| Flujo | W-3 «RutaIA - Calibración de umbral» (`n8n/rutaia-calibracion-umbral.json`, ID `RutaIACalibracion`) |
| Entrada | `pruebas/n8n/etapa4-calibracion-preguntas.json`: las 7 consultas temáticas y la de cocina |
| Salida completa | `docs/calibracion-resultados.json` |
| Solicitudes a OpenRouter | 1 (las 8 preguntas en un solo arreglo) |

### Método

1. Una llamada a W-3 con las 8 consultas:
   ```bash
   curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-calibrar-umbral -H "Content-Type: application/json; charset=utf-8" --data-binary "@pruebas/n8n/etapa4-calibracion-preguntas.json"
   ```
2. W-3 vectoriza las 8 preguntas (con trim, sin prefijos) en **una** solicitud de embeddings con `"input_type":"query"` y busca una vez por pregunta en Qdrant con el cuerpo de calibración de C9: `POST /collections/rutaia_cursos/points/query` con el filtro `activo = true`, `limit` 10, **sin** `score_threshold`, `with_payload` true y `with_vector` false. El score se devuelve redondeado a 4 decimales.
3. Cada curso del top 10 se marcó como pertinente o no leyendo su descripción completa (`GET /api/admin/cursos`), con el criterio escrito bajo cada tabla. La pertinencia se decidió antes de calcular el umbral y no depende del score.
4. Con las tablas se calcularon S_cocina, S_pert y los umbrales candidatos.

Comprobaciones de la salida: las 8 consultas devolvieron 10 puntos cada una; ningún punto trae `vector`; los cursos inactivos 29 y 30 no aparecen (el filtro `activo = true` los excluye y el nodo «Armar respuesta» falla si llega un punto inactivo).

## Top 10 por consulta

### C1. «Quiero aprender a crear páginas web.»

Criterio: Pertinente si el curso enseña a construir o maquetar páginas web (HTML, CSS, JavaScript en el navegador, diseño responsive); la seguridad, el despliegue o las herramientas generales de desarrollo no responden a «crear páginas web».

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 1 | Fundamentos de HTML y CSS para páginas web | Desarrollo web | 0,5930 | sí |
| 2 | 2 | JavaScript y manipulación del DOM | Desarrollo web | 0,3843 | sí |
| 3 | 3 | Diseño web responsive con Flexbox y Grid | Desarrollo web | 0,3750 | sí |
| 4 | 4 | Seguridad de aplicaciones web con OWASP Top 10 | Desarrollo web | 0,3644 | no |
| 5 | 27 | Pruebas de seguridad y hacking ético web | Herramientas tecnológicas | 0,3346 | no |
| 6 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,3011 | no |
| 7 | 5 | Programación orientada a objetos con Java | Programación | 0,2783 | no |
| 8 | 28 | Linux y línea de comandos para desarrolladores | Herramientas tecnológicas | 0,2773 | no |
| 9 | 23 | Control de versiones con Git y GitHub | Herramientas tecnológicas | 0,2698 | no |
| 10 | 19 | Scripts de automatización con Python | Automatización | 0,2602 | no |

### C2. «Necesito aprender Java para trabajar con Spring Boot.»

Criterio: Pertinente si el curso enseña Java o el ecosistema Spring (Spring Boot, Spring Data JPA, Spring Security).

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 5 | Programación orientada a objetos con Java | Programación | 0,6286 | sí |
| 2 | 6 | Desarrollo de APIs REST con Spring Boot | Programación | 0,5746 | sí |
| 3 | 7 | Persistencia de datos con Spring Data JPA | Programación | 0,5112 | sí |
| 4 | 8 | Autenticación y autorización segura con Spring Security | Programación | 0,3974 | sí |
| 5 | 1 | Fundamentos de HTML y CSS para páginas web | Desarrollo web | 0,2766 | no |
| 6 | 2 | JavaScript y manipulación del DOM | Desarrollo web | 0,2730 | no |
| 7 | 28 | Linux y línea de comandos para desarrolladores | Herramientas tecnológicas | 0,2699 | no |
| 8 | 12 | Fundamentos de inteligencia artificial para principiantes | Inteligencia artificial | 0,2686 | no |
| 9 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,2404 | no |
| 10 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,2352 | no |

### C3. «Me interesa analizar datos y construir dashboards.»

Criterio: Pertinente si el curso enseña a analizar datos o a visualizarlos en reportes y dashboards (SQL para análisis, pandas, Power BI, estadística aplicada); el diseño de bases de datos y el machine learning no son análisis descriptivo ni dashboards.

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 21 | Visualización de datos y dashboards con Power BI | Análisis de datos | 0,5322 | sí |
| 2 | 20 | Análisis de datos con Python y pandas | Análisis de datos | 0,4445 | sí |
| 3 | 9 | Consultas SQL para análisis de datos | Bases de datos | 0,4344 | sí |
| 4 | 22 | Estadística aplicada al análisis de datos | Análisis de datos | 0,3856 | sí |
| 5 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,2128 | no |
| 6 | 13 | Machine learning con Python y scikit-learn | Inteligencia artificial | 0,2128 | no |
| 7 | 2 | JavaScript y manipulación del DOM | Desarrollo web | 0,2026 | no |
| 8 | 10 | Diseño de bases de datos relacionales con PostgreSQL | Bases de datos | 0,2012 | no |
| 9 | 1 | Fundamentos de HTML y CSS para páginas web | Desarrollo web | 0,1966 | no |
| 10 | 7 | Persistencia de datos con Spring Data JPA | Programación | 0,1918 | no |

### C4. «Quiero automatizar procesos empresariales.»

Criterio: Pertinente si el tema central del curso es automatizar procesos o tareas de trabajo (n8n, RPA, scripts de automatización); CI/CD automatiza el ciclo de software, no procesos empresariales.

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,5534 | sí |
| 2 | 18 | Automatización robótica de procesos administrativos | Automatización | 0,4886 | sí |
| 3 | 19 | Scripts de automatización con Python | Automatización | 0,4471 | sí |
| 4 | 21 | Visualización de datos y dashboards con Power BI | Análisis de datos | 0,2382 | no |
| 5 | 26 | Integración y entrega continua con contenedores | Herramientas tecnológicas | 0,2260 | no |
| 6 | 15 | Procesamiento de lenguaje natural con Python | Inteligencia artificial | 0,2131 | no |
| 7 | 5 | Programación orientada a objetos con Java | Programación | 0,2108 | no |
| 8 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,2028 | no |
| 9 | 14 | Modelos de lenguaje y generación aumentada por recuperación | Inteligencia artificial | 0,1983 | no |
| 10 | 9 | Consultas SQL para análisis de datos | Bases de datos | 0,1922 | no |

### C5. «¿Qué puedo estudiar para trabajar con inteligencia artificial?»

Criterio: Pertinente si el curso enseña inteligencia artificial o una técnica usada directamente para construir sistemas de IA (incluye el 11, bases de datos vectoriales para RAG); la automatización y el análisis de datos generales no lo son.

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 12 | Fundamentos de inteligencia artificial para principiantes | Inteligencia artificial | 0,5256 | sí |
| 2 | 16 | Visión por computadora con redes neuronales | Inteligencia artificial | 0,4204 | sí |
| 3 | 14 | Modelos de lenguaje y generación aumentada por recuperación | Inteligencia artificial | 0,3770 | sí |
| 4 | 15 | Procesamiento de lenguaje natural con Python | Inteligencia artificial | 0,3750 | sí |
| 5 | 13 | Machine learning con Python y scikit-learn | Inteligencia artificial | 0,3730 | sí |
| 6 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,2611 | no |
| 7 | 11 | Bases de datos vectoriales para búsqueda semántica | Bases de datos | 0,2559 | sí |
| 8 | 18 | Automatización robótica de procesos administrativos | Automatización | 0,2138 | no |
| 9 | 21 | Visualización de datos y dashboards con Power BI | Análisis de datos | 0,1964 | no |
| 10 | 22 | Estadística aplicada al análisis de datos | Análisis de datos | 0,1860 | no |

### C6. «Quiero aprender a proteger aplicaciones web.»

Criterio: Pertinente si el curso enseña a proteger aplicaciones web o a encontrar sus vulnerabilidades (OWASP, hacking ético, autenticación y autorización seguras).

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 4 | Seguridad de aplicaciones web con OWASP Top 10 | Desarrollo web | 0,5599 | sí |
| 2 | 27 | Pruebas de seguridad y hacking ético web | Herramientas tecnológicas | 0,4702 | sí |
| 3 | 8 | Autenticación y autorización segura con Spring Security | Programación | 0,4087 | sí |
| 4 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,3183 | no |
| 5 | 2 | JavaScript y manipulación del DOM | Desarrollo web | 0,3041 | no |
| 6 | 1 | Fundamentos de HTML y CSS para páginas web | Desarrollo web | 0,2899 | no |
| 7 | 3 | Diseño web responsive con Flexbox y Grid | Desarrollo web | 0,2592 | no |
| 8 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,2091 | no |
| 9 | 6 | Desarrollo de APIs REST con Spring Boot | Programación | 0,2068 | no |
| 10 | 5 | Programación orientada a objetos con Java | Programación | 0,2038 | no |

### C7. «Necesito desplegar aplicaciones usando contenedores.»

Criterio: Pertinente si el curso enseña a construir, desplegar u orquestar contenedores (Docker, Kubernetes, CI/CD con contenedores).

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,6088 | sí |
| 2 | 26 | Integración y entrega continua con contenedores | Herramientas tecnológicas | 0,4840 | sí |
| 3 | 25 | Orquestación de contenedores con Kubernetes | Herramientas tecnológicas | 0,4204 | sí |
| 4 | 28 | Linux y línea de comandos para desarrolladores | Herramientas tecnológicas | 0,2337 | no |
| 5 | 4 | Seguridad de aplicaciones web con OWASP Top 10 | Desarrollo web | 0,2155 | no |
| 6 | 10 | Diseño de bases de datos relacionales con PostgreSQL | Bases de datos | 0,2096 | no |
| 7 | 11 | Bases de datos vectoriales para búsqueda semántica | Bases de datos | 0,1977 | no |
| 8 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,1967 | no |
| 9 | 6 | Desarrollo de APIs REST con Spring Boot | Programación | 0,1806 | no |
| 10 | 27 | Pruebas de seguridad y hacking ético web | Herramientas tecnológicas | 0,1798 | no |

### C8. «Quiero aprender cocina italiana.»

Criterio: El catálogo no tiene cursos de cocina ni gastronomía (E1-07): ningún curso es pertinente.

| # | cursoId | Nombre | Categoría | Score | Pertinente |
|---|---|---|---|---|---|
| 1 | 12 | Fundamentos de inteligencia artificial para principiantes | Inteligencia artificial | 0,2515 | no |
| 2 | 13 | Machine learning con Python y scikit-learn | Inteligencia artificial | 0,2090 | no |
| 3 | 5 | Programación orientada a objetos con Java | Programación | 0,2016 | no |
| 4 | 1 | Fundamentos de HTML y CSS para páginas web | Desarrollo web | 0,1888 | no |
| 5 | 22 | Estadística aplicada al análisis de datos | Análisis de datos | 0,1874 | no |
| 6 | 17 | Automatización de flujos de trabajo con n8n | Automatización | 0,1837 | no |
| 7 | 16 | Visión por computadora con redes neuronales | Inteligencia artificial | 0,1806 | no |
| 8 | 9 | Consultas SQL para análisis de datos | Bases de datos | 0,1782 | no |
| 9 | 24 | Contenedores con Docker para desplegar aplicaciones | Herramientas tecnológicas | 0,1774 | no |
| 10 | 28 | Linux y línea de comandos para desarrolladores | Herramientas tecnológicas | 0,1689 | no |

## Cálculo

**S_cocina** = score máximo de la consulta de cocina = **0,2515** (C8, curso 12).

**S_pert** = menor score entre los 3 primeros pertinentes de cada consulta temática:

| Consulta | 3 primeros pertinentes (cursoId: score) | Mínimo |
|---|---|---|
| C1 páginas web | 1: 0,5930 · 2: 0,3843 · 3: 0,3750 | **0,3750** |
| C2 Java y Spring Boot | 5: 0,6286 · 6: 0,5746 · 7: 0,5112 | 0,5112 |
| C3 datos y dashboards | 21: 0,5322 · 20: 0,4445 · 9: 0,4344 | 0,4344 |
| C4 automatizar procesos | 17: 0,5534 · 18: 0,4886 · 19: 0,4471 | 0,4471 |
| C5 inteligencia artificial | 12: 0,5256 · 16: 0,4204 · 14: 0,3770 | 0,3770 |
| C6 proteger aplicaciones web | 4: 0,5599 · 27: 0,4702 · 8: 0,4087 | 0,4087 |
| C7 contenedores | 24: 0,6088 · 26: 0,4840 · 25: 0,4204 | 0,4204 |

S_pert = **0,3750** (C1, curso 3 «Diseño web responsive con Flexbox y Grid»).

Intervalo válido: (S_cocina, S_pert) = (0,2515; 0,3750). Como S_cocina < S_pert, existe; y todas las consultas temáticas tienen al menos un curso pertinente por encima de S_cocina (en cada una, sus 3 primeros pertinentes). No se cumple ninguna condición de detención.

Punto medio: (0,2515 + 0,3750) / 2 = 0,31325.

## Umbral elegido: 0,34

Con 2 decimales y margen ≥ 0,02 respecto de ambos extremos, los candidatos son de 0,28 (0,2515 + 0,02 = 0,2715 → el primer valor de 2 decimales por encima es 0,28) a 0,35 (0,3750 − 0,02 = 0,3550 → el último por debajo es 0,35). Para cada uno se contaron, en los top 10 de las 8 consultas, los cursos no pertinentes que quedarían por encima del umbral:

| Umbral | Margen con S_cocina | Margen con S_pert | No pertinentes por encima | Cuáles |
|---|---|---|---|---|
| 0,28 | 0,0285 | 0,0950 | 6 | C1: 4, 27, 24 · C6: 24, 2, 1 |
| 0,29 | 0,0385 | 0,0850 | 5 | C1: 4, 27, 24 · C6: 24, 2 |
| 0,30 | 0,0485 | 0,0750 | 5 | C1: 4, 27, 24 · C6: 24, 2 |
| 0,31 (punto medio) | 0,0585 | 0,0650 | 3 | C1: 4, 27 · C6: 24 |
| 0,32 | 0,0685 | 0,0550 | 2 | C1: 4, 27 |
| 0,33 | 0,0785 | 0,0450 | 2 | C1: 4, 27 |
| **0,34** | **0,0885** | **0,0350** | **1** | C1: 4 |
| 0,35 | 0,0985 | 0,0250 | 1 | C1: 4 |

Hay varios candidatos, así que se aplica la regla de desempate: el que deja fuera más cursos no pertinentes. 0,34 y 0,35 empatan (solo queda el curso 4 en C1, con 0,3644; quitarlo exigiría un umbral > 0,3644, sin margen de 0,02 con S_pert). Entre los dos se elige **0,34**, el más cercano al punto medio y con más margen respecto de S_pert (0,035 frente a 0,025).

Frente al punto medio (0,31), 0,34 deja fuera además el 27 «Pruebas de seguridad y hacking ético web» en C1 (0,3346) y el 24 «Contenedores con Docker para desplegar aplicaciones» en C6 (0,3183), y no pierde ningún curso pertinente que entrara en las 5 fuentes: el único pertinente bajo 0,34 es el 11 en C5 (0,2559), que con cualquier candidato quedaría fuera, porque es el sexto pertinente y `limiteResultados` es 5.

## Interpretación del score

- El score es la **similitud coseno** entre el vector de la pregunta y el del curso: el coseno del ángulo entre los dos vectores de 2048 dimensiones. Cuanto mayor, más parecido; el máximo es 1 (misma dirección). No es una probabilidad ni un porcentaje de acierto: solo ordena y separa.
- El modelo es **asimétrico**: las preguntas se vectorizan con `input_type` `query` y los cursos con `passage`. Una pregunta corta y una descripción de 130 a 146 palabras nunca quedan en la misma dirección, así que los scores pregunta–curso quedan muy por debajo de 1: en esta medición, de 0,1689 a 0,6286. Comparar un curso consigo mismo (`passage` contra `passage`) da 1,0000, pero esa escala no sirve para fijar el umbral.
- En esta escala, los 3 primeros pertinentes de cada consulta temática quedan entre 0,3750 y 0,6286 (todos los pertinentes, entre 0,3730 y 0,6286, salvo el 11 en C5 con 0,2559); los no pertinentes de las consultas temáticas, entre 0,1798 y 0,3644; y la consulta de cocina no supera 0,2515. Su mejor curso (12, «Fundamentos de inteligencia artificial para principiantes») no trata de cocina: probablemente se acerca por el vocabulario genérico de quien empieza a aprender («curso introductorio», «cómo empezar»), no por el tema.
- Los valores dependen del modelo, del texto indexado (nombre, categoría, nivel y descripción, C9) y de cómo se escribe la pregunta. Si cambia cualquiera de ellos, hay que recalibrar con W-3.

## Resultado esperado con UMBRAL = 0,34 (`limit` 5)

| Consulta | Fuentes | Cursos (cursoId) | Pertinentes | Observación |
|---|---|---|---|---|
| C1 páginas web | 4 | 1, 2, 3, 4 | 3 | Entra el 4 (seguridad web, 0,3644), no pertinente pero del área web |
| C2 Java y Spring Boot | 4 | 5, 6, 7, 8 | 4 | — |
| C3 datos y dashboards | 4 | 21, 20, 9, 22 | 4 | — |
| C4 automatizar procesos | 3 | 17, 18, 19 | 3 | — |
| C5 inteligencia artificial | 5 | 12, 16, 14, 15, 13 | 5 | Llega al límite de 5; el 11 (0,2559) queda fuera |
| C6 proteger aplicaciones web | 3 | 4, 27, 8 | 3 | — |
| C7 contenedores | 3 | 24, 26, 25 | 3 | — |
| C8 cocina italiana | **0** | — | 0 | El flujo RAG debe informar que ningún curso supera el umbral (RF 11) y no llamar al LLM |

- La consulta de cocina queda con 0 cursos (su máximo, 0,2515, está 0,0885 por debajo).
- Todas las consultas temáticas conservan de 3 a 5 cursos pertinentes. **Ninguna queda con menos de 3.**
- En total llegan al modelo 26 fuentes, de las cuales 25 son pertinentes; la única no pertinente (curso 4 en C1) es del mismo campo (desarrollo web).

## Riesgos

- **Margen estrecho por arriba.** Hay pertinentes cerca del umbral: C5 curso 13 (0,3730), C1 curso 3 y C5 curso 15 (0,3750), C5 curso 14 (0,3770). Una pregunta más vaga o con otras palabras («hacer una web», «aprender IA») puede bajarlos de 0,34 y dejar menos de 3 fuentes. El flujo RAG debe tolerar 1 o 2 fuentes.
- **Una sola consulta negativa.** S_cocina sale de una sola pregunta fuera de tema. Otras preguntas fuera del catálogo que usen palabras técnicas o de estudio («quiero estudiar algo con computadoras», «marketing digital») pueden superar 0,34 y llegar al modelo; el prompt del LLM debe seguir pidiendo recomendar solo lo que responda a la necesidad.
- **No pertinentes del mismo campo.** Cursos cercanos en tema, como el 4 en C1, superan el umbral; el umbral separa temas, no matices dentro de un tema.
- **Pertinentes por debajo del umbral.** El 11 (bases de datos vectoriales) es útil para IA pero puntúa 0,2559 en C5: preguntas sobre IA no lo recomendarán.
- **Dependencia del modelo y del texto.** Cambiar el modelo de embeddings (por ejemplo, si `nvidia/nemotron-3-embed-1b:free` deja de estar en la capa gratuita), el formato del texto de C9, `input_type` o las descripciones de los cursos invalida la escala: hay que re-indexar (Etapa 3) y repetir esta calibración. Un curso nuevo también puede quedar por debajo del umbral si su descripción no usa el vocabulario de las preguntas.
- **Redondeo.** W-3 redondea a 4 decimales; ningún score está a menos de 0,0001 de 0,34, así que el redondeo no cambia qué cursos pasan.

UMBRAL_SIMILITUD = 0.34
