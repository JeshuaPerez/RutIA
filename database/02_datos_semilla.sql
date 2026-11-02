SET client_encoding = 'UTF8';

-- =====================================================================
-- RutaIA - Datos semilla: 10 estudiantes y 30 cursos (28 activos y 2
-- inactivos). Sin IDs explícitos y en orden fijo: sobre tablas recién
-- creadas por 01_esquema.sql los IDs salen 1..10 y 1..30.
-- Las consultas, recomendaciones y calificaciones no se siembran: se
-- generan en las pruebas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Estudiantes ficticios
-- ---------------------------------------------------------------------
INSERT INTO estudiante (nombre_completo, correo, nivel_experiencia, area_interes) VALUES
('Valeria Gómez Rojas',      'valeria.gomez@rutaia.test',     'Principiante', 'Desarrollo web'),
('Andrés Castillo Méndez',   'andres.castillo@rutaia.test',   'Intermedio',   'Desarrollo backend con Java'),
('Lucía Fernández Ortiz',    'lucia.fernandez@rutaia.test',   'Avanzado',     'Inteligencia artificial'),
('Mateo Herrera Salinas',    'mateo.herrera@rutaia.test',     'Principiante', 'Análisis de datos'),
('Camila Torres Paredes',    'camila.torres@rutaia.test',     'Intermedio',   'Automatización de procesos'),
('Diego Ramírez Luna',       'diego.ramirez@rutaia.test',     'Avanzado',     'Ciberseguridad'),
('Sofía Morales Acosta',     'sofia.morales@rutaia.test',     'Intermedio',   'DevOps y contenedores'),
('Javier Núñez Cabrera',     'javier.nunez@rutaia.test',      'Principiante', 'Programación'),
('Isabela Vargas Quintero',  'isabela.vargas@rutaia.test',    'Avanzado',     'Ciencia de datos'),
('Tomás Aguilar Peña',       'tomas.aguilar@rutaia.test',     'Intermedio',   'Bases de datos');

-- ---------------------------------------------------------------------
-- Cursos (orden fijo: IDs 1..30; los dos últimos están inactivos)
-- ---------------------------------------------------------------------
INSERT INTO curso (nombre, descripcion, categoria, nivel, duracion_horas, activo) VALUES

-- 1
('Fundamentos de HTML y CSS para páginas web',
 'Curso para quienes quieren aprender a crear páginas web desde cero sin experiencia previa en programación. Aprenderás la estructura de un documento HTML5, etiquetas semánticas, encabezados, párrafos, listas, enlaces, imágenes, tablas y formularios. Luego darás estilo a tus páginas con CSS: selectores, colores, tipografías, el modelo de caja, márgenes, rellenos y posicionamiento de elementos. Usarás Visual Studio Code, las herramientas de desarrollo del navegador y buenas prácticas de accesibilidad. Como proyecto práctico construirás un sitio web personal de varias páginas, con portafolio, formulario de contacto y menú de navegación, y lo publicarás gratis en GitHub Pages. Está dirigido a estudiantes, emprendedores y personas que desean hacer su propia página web o iniciar una carrera en desarrollo web frontend. Al terminar podrás maquetar sitios estáticos sencillos y tendrás la base necesaria para continuar con JavaScript.',
 'Desarrollo web', 'Básico', 40, TRUE),

-- 2
('JavaScript y manipulación del DOM',
 'Aprende a programar en JavaScript para dar interactividad y comportamiento dinámico a tus páginas web. El curso cubre variables, tipos de datos, condicionales, bucles, funciones, arreglos, objetos y módulos de JavaScript moderno (ES6 o posterior). Después trabajarás con el DOM para seleccionar y modificar elementos, responder a eventos como clics y envíos de formularios, validar datos en el navegador y crear contenido dinámico. También verás programación asíncrona con promesas, async y await, y el consumo de APIs REST con fetch y JSON. Como proyecto práctico desarrollarás una aplicación web de lista de tareas y un buscador que consulta una API pública y muestra los resultados en tarjetas. Va dirigido a quienes ya conocen HTML y CSS y quieren construir sitios web interactivos y aplicaciones frontend. Es el paso natural para convertirte en desarrollador web y prepararte para frameworks como React.',
 'Desarrollo web', 'Intermedio', 50, TRUE),

-- 3
('Diseño web responsive con Flexbox y Grid',
 'Aprende a crear páginas web que se vean bien en celulares, tabletas y computadoras. Este curso de diseño web responsive enseña el enfoque mobile first, las media queries, unidades relativas, imágenes adaptables y tipografía fluida. Dominarás CSS Flexbox para alinear y distribuir elementos y CSS Grid para construir maquetaciones completas de sitios web con filas y columnas. También usarás variables CSS, buenas prácticas de accesibilidad y las herramientas del navegador para simular distintos dispositivos y depurar estilos. Como proyecto práctico diseñarás la página de inicio adaptable de una tienda en línea, con menú desplegable para móviles, galería de productos y pie de página. Está dirigido a personas que ya saben HTML y CSS básico y quieren que sus sitios web luzcan profesionales en cualquier pantalla. Es útil para desarrolladores frontend, diseñadores web y emprendedores que crean su propia página.',
 'Desarrollo web', 'Intermedio', 30, TRUE),

-- 4
('Seguridad de aplicaciones web con OWASP Top 10',
 'Aprende a proteger aplicaciones web frente a los ataques más comunes usando como guía el OWASP Top 10. El curso explica control de acceso roto, fallas criptográficas, inyección SQL, cross site scripting (XSS), diseño inseguro, configuraciones de seguridad incorrectas, componentes vulnerables y falsificación de solicitudes (CSRF y SSRF). Para cada vulnerabilidad verás cómo se explota, cómo detectarla y cómo corregirla con validación de entradas, consultas parametrizadas, escape de salida, cabeceras de seguridad HTTP, CORS bien configurado y gestión segura de secretos. Como proyecto práctico auditarás una aplicación web vulnerable de pruebas, documentarás los hallazgos y aplicarás las correcciones en el código. Está dirigido a desarrolladores web frontend y backend que quieren escribir código seguro y a quienes se inician en ciberseguridad. Sirve para reducir riesgos, proteger los datos de los usuarios y cumplir buenas prácticas de seguridad web.',
 'Desarrollo web', 'Intermedio', 40, TRUE),

-- 5
('Programación orientada a objetos con Java',
 'Curso para aprender Java desde cero y dominar la programación orientada a objetos (POO), base indispensable para trabajar con Spring Boot. Estudiarás la sintaxis de Java, tipos de datos, estructuras de control, métodos, arreglos y cadenas. Después aprenderás los pilares de la POO: clases, objetos, constructores, encapsulamiento, herencia, polimorfismo, clases abstractas e interfaces. También verás colecciones (List, Map, Set), manejo de excepciones, records, lambdas y streams de Java moderno. Trabajarás con el JDK 21, IntelliJ IDEA y Gradle, y escribirás pruebas unitarias con JUnit. Como proyecto práctico desarrollarás un sistema de gestión de biblioteca por consola con préstamos, usuarios y reportes. Está dirigido a principiantes en programación y a estudiantes que necesitan aprender Java para la universidad o para trabajar como desarrolladores backend. Al terminar estarás listo para crear aplicaciones empresariales con Spring Boot.',
 'Programación', 'Básico', 60, TRUE),

-- 6
('Desarrollo de APIs REST con Spring Boot',
 'Aprende a construir APIs REST profesionales en Java con Spring Boot, el framework más usado para desarrollo backend empresarial. El curso cubre la creación de proyectos con Spring Initializr y Gradle, controladores con anotaciones, rutas, verbos HTTP, códigos de estado, validación de datos con Bean Validation, manejo global de errores y configuración por perfiles y variables de entorno. También verás inyección de dependencias, arquitectura en capas (controlador, servicio y repositorio), DTO, documentación con OpenAPI y Swagger, CORS y pruebas con JUnit, MockMvc y Postman. Como proyecto práctico desarrollarás la API de un sistema de reservas con endpoints para crear, consultar, actualizar y listar recursos. Está dirigido a programadores que ya conocen Java y la programación orientada a objetos y quieren trabajar como desarrolladores backend. Sirve para crear servicios web y microservicios que consumen aplicaciones web y móviles.',
 'Programación', 'Intermedio', 50, TRUE),

-- 7
('Persistencia de datos con Spring Data JPA',
 'Aprende a conectar aplicaciones Java con Spring Boot a bases de datos relacionales usando Spring Data JPA e Hibernate. El curso explica el mapeo objeto relacional (ORM), entidades, claves primarias, relaciones uno a muchos y muchos a muchos, carga perezosa y ansiosa, y transacciones. Crearás repositorios, consultas derivadas por nombre de método, consultas JPQL y nativas, paginación y ordenamiento. También verás cómo validar el esquema existente con ddl-auto en modo validate, cómo evitar el problema N+1 y cómo probar la capa de datos con Testcontainers y PostgreSQL. Como proyecto práctico construirás la capa de persistencia de una tienda en línea con productos, pedidos y clientes. Está dirigido a desarrolladores Java que ya crean APIs REST con Spring Boot y necesitan guardar y consultar información en una base de datos. Es fundamental para cualquier trabajo de desarrollo backend con Java.',
 'Programación', 'Intermedio', 40, TRUE),

-- 8
('Autenticación y autorización segura con Spring Security',
 'Aprende a proteger aplicaciones y APIs web implementando autenticación y autorización seguras con Spring Security. El curso cubre el almacenamiento seguro de contraseñas con BCrypt o Argon2, inicio de sesión, sesiones y cookies seguras, tokens JWT, OAuth 2.0 y OpenID Connect, control de acceso basado en roles y permisos, y protección contra CSRF, fuerza bruta y secuestro de sesión. También verás buenas prácticas para el manejo de secretos y claves, expiración y revocación de tokens, y autenticación multifactor. Como proyecto práctico asegurarás una API REST de Spring Boot con registro de usuarios, login con JWT, rutas protegidas por rol y pruebas automatizadas de seguridad. Está dirigido a desarrolladores backend con experiencia en Java y Spring Boot que quieren aprender a proteger aplicaciones web. Sirve para evitar accesos no autorizados y robo de cuentas en sistemas reales.',
 'Programación', 'Avanzado', 40, TRUE),

-- 9
('Consultas SQL para análisis de datos',
 'Aprende SQL desde cero para consultar, filtrar y analizar datos almacenados en bases de datos relacionales. El curso cubre SELECT, WHERE, ORDER BY, funciones de agregación, GROUP BY y HAVING, uniones entre tablas con JOIN, subconsultas, expresiones de tabla comunes (CTE) y funciones de ventana para rankings, acumulados y comparaciones entre periodos. Practicarás con PostgreSQL y DBeaver sobre bases de datos de ventas, clientes e inventarios. También aprenderás a limpiar datos, trabajar con fechas y preparar vistas que alimentan reportes y dashboards. Como proyecto práctico responderás preguntas de negocio reales, como los productos más vendidos o el crecimiento mensual de ingresos, y exportarás los resultados para visualizarlos. Está dirigido a principiantes, analistas de datos y profesionales de negocio que quieren obtener información por sí mismos. Es la habilidad más pedida para trabajar en análisis de datos.',
 'Bases de datos', 'Básico', 30, TRUE),

-- 10
('Diseño de bases de datos relacionales con PostgreSQL',
 'Aprende a diseñar bases de datos relacionales bien estructuradas y a implementarlas en PostgreSQL. El curso cubre el modelado conceptual con diagramas entidad relación, la transformación a modelo relacional, normalización hasta tercera forma normal, tipos de datos, claves primarias y foráneas, restricciones CHECK, UNIQUE y NOT NULL, e integridad referencial. También verás índices y cómo justificarlos con EXPLAIN, transacciones, vistas, usuarios y permisos, respaldo y restauración, y cómo ejecutar PostgreSQL en un contenedor Docker. Como proyecto práctico diseñarás y crearás con scripts SQL la base de datos de un sistema académico con estudiantes, cursos e inscripciones, incluyendo datos de prueba. Está dirigido a estudiantes de informática y desarrolladores que necesitan crear la base de datos de sus aplicaciones. Sirve para garantizar datos consistentes, consultas rápidas y sistemas fáciles de mantener.',
 'Bases de datos', 'Intermedio', 40, TRUE),

-- 11
('Bases de datos vectoriales para búsqueda semántica',
 'Aprende a construir búsqueda semántica con bases de datos vectoriales, componente clave de las aplicaciones de inteligencia artificial y de los sistemas RAG. El curso explica qué son los embeddings, cómo un modelo convierte textos en vectores y cómo se mide la similitud con distancia coseno o producto punto. Trabajarás con Qdrant en Docker: colecciones, puntos, payloads, filtros por metadatos, índices HNSW y búsqueda de vecinos más cercanos. También verás cómo dividir documentos en fragmentos, generar embeddings mediante una API y elegir umbrales de similitud. Como proyecto práctico indexarás un catálogo de productos o cursos y crearás un buscador que entiende preguntas en lenguaje natural en lugar de palabras clave exactas. Está dirigido a desarrolladores y científicos de datos con experiencia en programación y APIs. Sirve para crear buscadores inteligentes, recomendadores y asistentes basados en modelos de lenguaje.',
 'Bases de datos', 'Avanzado', 25, TRUE),

-- 12
('Fundamentos de inteligencia artificial para principiantes',
 'Curso introductorio para entender qué es la inteligencia artificial y cómo empezar a trabajar con ella, sin necesidad de matemáticas avanzadas. Conocerás la diferencia entre inteligencia artificial, machine learning y deep learning, los tipos de aprendizaje supervisado y no supervisado, las redes neuronales, la IA generativa y los modelos de lenguaje como ChatGPT, Claude o Gemini. Aprenderás a escribir buenos prompts, a usar herramientas de IA en el estudio y el trabajo, y a reconocer sus limitaciones, sesgos y riesgos éticos. También verás ejemplos de aplicaciones reales en salud, educación, finanzas y atención al cliente. Como proyecto práctico entrenarás un clasificador de imágenes sencillo con una herramienta sin código y lo probarás con tus propios datos. Está dirigido a cualquier persona que quiera estudiar inteligencia artificial o trabajar con ella. Es el primer paso de una ruta de aprendizaje en IA.',
 'Inteligencia artificial', 'Básico', 20, TRUE),

-- 13
('Machine learning con Python y scikit-learn',
 'Aprende machine learning, la rama de la inteligencia artificial que permite a las computadoras aprender de los datos para hacer predicciones. El curso cubre el flujo completo de un proyecto: preparación y limpieza de datos con pandas, ingeniería de características, división en entrenamiento y prueba, y validación cruzada. Entrenarás modelos de regresión lineal y logística, árboles de decisión, random forest, k vecinos más cercanos y k-means para agrupamiento, usando Python, Jupyter y scikit-learn. También aprenderás a medir el desempeño con exactitud, precisión, recall, F1 y error cuadrático, y a evitar el sobreajuste. Como proyecto práctico construirás un modelo que predice la deserción de clientes y explicarás sus resultados. Está dirigido a personas con bases de Python que quieren trabajar con inteligencia artificial o ciencia de datos. Sirve para crear modelos predictivos aplicables a negocios, educación e industria.',
 'Inteligencia artificial', 'Intermedio', 50, TRUE),

-- 14
('Modelos de lenguaje y generación aumentada por recuperación',
 'Aprende a crear aplicaciones con modelos de lenguaje grandes (LLM) y a conectarlos con tus propios datos mediante generación aumentada por recuperación (RAG). El curso cubre cómo funcionan los LLM, tokens, ventana de contexto, temperatura, ingeniería de prompts, salidas estructuradas en JSON y llamadas a herramientas. Construirás una arquitectura RAG completa: fragmentación de documentos, embeddings, base de datos vectorial, búsqueda por similitud y construcción del contexto para que el modelo responda sin inventar información. Usarás APIs como OpenRouter, Python o n8n y Qdrant, y evaluarás la calidad de las respuestas. También verás límites de uso, costos, privacidad y alucinaciones. Como proyecto práctico desarrollarás un asistente que responde preguntas sobre un conjunto de documentos y cita sus fuentes. Está dirigido a desarrolladores con experiencia en programación que quieren trabajar con inteligencia artificial generativa y chatbots.',
 'Inteligencia artificial', 'Avanzado', 40, TRUE),

-- 15
('Procesamiento de lenguaje natural con Python',
 'Aprende procesamiento de lenguaje natural (PLN o NLP), el área de la inteligencia artificial que permite a las computadoras entender y analizar textos escritos por personas. El curso cubre limpieza y tokenización de texto, lematización, eliminación de palabras vacías, bolsa de palabras, TF-IDF, embeddings de palabras y modelos Transformer como BERT. Realizarás clasificación de textos, análisis de sentimiento, extracción de entidades nombradas y resumen automático usando Python, spaCy, scikit-learn y Hugging Face. También verás cómo evaluar modelos y cómo trabajar con textos en español. Como proyecto práctico construirás un sistema que clasifica opiniones de clientes y detecta automáticamente los temas más mencionados. Está dirigido a programadores y científicos de datos con bases de Python y machine learning que quieren especializarse en inteligencia artificial aplicada al lenguaje. Sirve para crear chatbots, analizar redes sociales y automatizar documentos.',
 'Inteligencia artificial', 'Avanzado', 45, TRUE),

-- 16
('Visión por computadora con redes neuronales',
 'Aprende visión por computadora, la rama de la inteligencia artificial que permite a las máquinas reconocer y entender imágenes y video. El curso cubre procesamiento de imágenes con OpenCV, redes neuronales convolucionales (CNN), aprendizaje por transferencia con modelos preentrenados, aumento de datos, clasificación de imágenes, detección de objetos con YOLO y segmentación. Trabajarás con Python, PyTorch o TensorFlow y Google Colab con GPU gratuita, y aprenderás a preparar y etiquetar conjuntos de imágenes y a medir el desempeño de los modelos. Como proyecto práctico entrenarás un modelo de deep learning que detecta el uso de casco de seguridad en fotografías y lo probarás con la cámara del computador. Está dirigido a programadores con bases de Python y machine learning que quieren especializarse en inteligencia artificial y deep learning. Sirve para aplicaciones de seguridad, salud, industria y vehículos autónomos.',
 'Inteligencia artificial', 'Avanzado', 50, TRUE),

-- 17
('Automatización de flujos de trabajo con n8n',
 'Aprende a automatizar procesos empresariales y tareas repetitivas sin necesidad de ser programador experto usando n8n, una plataforma de automatización de código abierto. El curso cubre disparadores, webhooks, nodos, expresiones, condiciones, bucles, manejo de errores y credenciales seguras. Conectarás aplicaciones como Gmail, Google Sheets, Slack, Telegram, bases de datos y APIs REST mediante el nodo HTTP Request. También verás cómo ejecutar n8n con Docker, programar flujos por horario y añadir pasos con inteligencia artificial para clasificar correos o resumir textos. Como proyecto práctico automatizarás el proceso de atención de solicitudes de una empresa: recibir un formulario, registrar los datos en una hoja de cálculo, notificar al equipo y responder al cliente. Está dirigido a profesionales administrativos, emprendedores y desarrolladores que quieren ahorrar tiempo y reducir errores manuales. Sirve para digitalizar y optimizar procesos de negocio.',
 'Automatización', 'Básico', 25, TRUE),

-- 18
('Automatización robótica de procesos administrativos',
 'Aprende automatización robótica de procesos (RPA) para que robots de software realicen tareas administrativas repetitivas como lo haría una persona frente al computador. El curso cubre la identificación y documentación de procesos automatizables, el diseño de robots con UiPath Studio y Power Automate Desktop, la lectura de correos y archivos Excel, la extracción de datos de facturas y PDF, el llenado de formularios en sistemas web y aplicaciones de escritorio, y el manejo de excepciones. También verás orquestación, programación de ejecuciones y cómo medir el ahorro de tiempo. Como proyecto práctico automatizarás un proceso contable completo: descargar facturas del correo, extraer sus datos, registrarlos en una planilla y generar un reporte. Está dirigido a personal administrativo, contable y de recursos humanos, y a analistas de procesos que quieren automatizar procesos empresariales. Sirve para eliminar trabajo manual y aumentar la productividad de las oficinas.',
 'Automatización', 'Intermedio', 35, TRUE),

-- 19
('Scripts de automatización con Python',
 'Aprende a escribir scripts en Python para automatizar tareas repetitivas del trabajo diario y ahorrar horas de esfuerzo manual. El curso empieza con lo básico de Python y avanza hacia la automatización de archivos y carpetas, renombrado masivo, lectura y escritura de Excel y CSV con openpyxl y pandas, generación de reportes en PDF, envío automático de correos y consumo de APIs con requests. También verás extracción de datos de páginas web (web scraping) con BeautifulSoup, automatización del navegador con Selenium y programación de tareas con el programador de Windows o cron en Linux. Como proyecto práctico crearás un script que consolida reportes de ventas de varias hojas de cálculo y envía un resumen por correo cada mañana. Está dirigido a principiantes y profesionales de cualquier área que quieren automatizar procesos de oficina. Sirve para eliminar trabajo repetitivo y reducir errores.',
 'Automatización', 'Básico', 30, TRUE),

-- 20
('Análisis de datos con Python y pandas',
 'Aprende a analizar datos con Python usando pandas, la librería más utilizada por analistas y científicos de datos. El curso cubre la carga de datos desde CSV, Excel, JSON y bases de datos SQL, la limpieza de valores faltantes y duplicados, transformaciones, filtros, agrupaciones, tablas dinámicas, uniones de tablas y manejo de fechas y series de tiempo. Trabajarás en Jupyter Notebook con NumPy y crearás gráficos con Matplotlib y Seaborn para explorar los datos y comunicar hallazgos. También aprenderás buenas prácticas para documentar y reproducir un análisis. Como proyecto práctico realizarás un análisis exploratorio completo de las ventas de una empresa, identificarás tendencias y presentarás conclusiones con visualizaciones. Está dirigido a personas con bases de programación que quieren trabajar como analistas de datos o tomar decisiones basadas en datos. Es el punto de partida para la ciencia de datos y el machine learning.',
 'Análisis de datos', 'Intermedio', 40, TRUE),

-- 21
('Visualización de datos y dashboards con Power BI',
 'Aprende a construir dashboards interactivos y reportes de inteligencia de negocios con Power BI. El curso cubre la conexión a fuentes de datos como Excel, CSV, bases de datos SQL y servicios en la nube, la limpieza y transformación de datos con Power Query, el modelado de datos con relaciones y esquema en estrella, y el cálculo de indicadores con el lenguaje DAX. Aprenderás principios de visualización de datos para elegir el gráfico adecuado, diseñar tableros claros, usar filtros, segmentadores y drill down, y contar historias con datos. También verás cómo publicar y compartir reportes y programar su actualización. Como proyecto práctico crearás un dashboard de ventas con indicadores clave, mapas y tendencias mensuales para la gerencia de una empresa. Está dirigido a analistas de datos, profesionales de negocio y estudiantes que quieren presentar información de forma visual. Sirve para monitorear resultados y tomar mejores decisiones.',
 'Análisis de datos', 'Intermedio', 30, TRUE),

-- 22
('Estadística aplicada al análisis de datos',
 'Aprende la estadística que necesitas para analizar datos e interpretar resultados correctamente, sin fórmulas complicadas. El curso cubre tipos de variables, medidas de tendencia central y de dispersión, percentiles, distribuciones de frecuencia, distribución normal, correlación, muestreo, intervalos de confianza, pruebas de hipótesis y pruebas A/B. Practicarás con hojas de cálculo de Excel o Google Sheets y con Python, y aprenderás a representar los datos con histogramas, diagramas de caja y gráficos de dispersión. También verás errores comunes al interpretar datos, como confundir correlación con causalidad. Como proyecto práctico analizarás los resultados de una encuesta de satisfacción y presentarás conclusiones respaldadas con evidencia estadística. Está dirigido a principiantes, estudiantes y profesionales que quieren empezar en el análisis de datos o entender mejor los reportes y dashboards que usan en su trabajo. Es la base para la ciencia de datos y el machine learning.',
 'Análisis de datos', 'Básico', 30, TRUE),

-- 23
('Control de versiones con Git y GitHub',
 'Aprende a usar Git y GitHub, herramientas indispensables para cualquier desarrollador de software. El curso cubre la instalación y configuración de Git, repositorios, commits, el área de preparación, historial, ramas, fusiones y resolución de conflictos. Después trabajarás con repositorios remotos en GitHub: clonar, enviar y traer cambios, pull requests, revisiones de código, issues y flujos de trabajo en equipo como GitHub Flow. También verás cómo escribir buenos mensajes de commit, usar .gitignore para no publicar contraseñas ni claves, deshacer cambios y etiquetar versiones. Como proyecto práctico colaborarás con otros estudiantes en un repositorio compartido, crearás ramas para nuevas funcionalidades y las integrarás mediante pull requests. Está dirigido a estudiantes de programación y desarrolladores principiantes que quieren trabajar de forma ordenada y colaborativa. Sirve para no perder trabajo, revisar cambios y participar en proyectos de código abierto.',
 'Herramientas tecnológicas', 'Básico', 20, TRUE),

-- 24
('Contenedores con Docker para desplegar aplicaciones',
 'Aprende a empaquetar y desplegar aplicaciones usando contenedores con Docker, la herramienta estándar de la industria para ejecutar software de forma igual en cualquier máquina. El curso cubre imágenes, contenedores, Dockerfile, capas, construcción multietapa, registros como Docker Hub, puertos, variables de entorno, volúmenes para persistir datos y redes entre contenedores. Después usarás Docker Compose para levantar aplicaciones de varios servicios, por ejemplo un backend, una base de datos PostgreSQL y una herramienta de automatización, con healthchecks y secretos fuera del código. También verás buenas prácticas de seguridad y tamaño de imágenes. Como proyecto práctico contenerizarás una aplicación web con su base de datos y la desplegarás en un servidor en la nube. Está dirigido a desarrolladores y estudiantes de sistemas que quieren desplegar aplicaciones de forma confiable. Es el primer paso hacia DevOps, Kubernetes y la nube.',
 'Herramientas tecnológicas', 'Intermedio', 30, TRUE),

-- 25
('Orquestación de contenedores con Kubernetes',
 'Aprende a desplegar, escalar y administrar aplicaciones en contenedores con Kubernetes, el orquestador más usado en la nube. El curso cubre la arquitectura de un clúster, pods, deployments, réplicas, services, ingress, ConfigMaps, Secrets, volúmenes persistentes, namespaces y sondas de salud. Practicarás con kubectl, Minikube o kind en tu computador, y aprenderás a escalar automáticamente según la carga, actualizar aplicaciones sin caídas y revertir versiones. También verás empaquetado con Helm, monitoreo básico con Prometheus y Grafana, y nociones de servicios administrados como EKS, AKS y GKE. Como proyecto práctico desplegarás en Kubernetes una aplicación de microservicios con frontend, API y base de datos, con escalado y actualización continua. Está dirigido a desarrolladores y administradores de sistemas que ya usan Docker y quieren trabajar en DevOps o ingeniería de plataformas. Sirve para operar aplicaciones con alta disponibilidad.',
 'Herramientas tecnológicas', 'Avanzado', 45, TRUE),

-- 26
('Integración y entrega continua con contenedores',
 'Aprende a automatizar la construcción, las pruebas y el despliegue de aplicaciones con integración continua y entrega continua (CI/CD) basadas en contenedores. El curso cubre la creación de pipelines con GitHub Actions y GitLab CI, la ejecución automática de pruebas en cada cambio, el análisis de calidad y seguridad del código, la construcción de imágenes Docker, su publicación en un registro de contenedores y el despliegue automático en un servidor o en Kubernetes. También verás manejo de secretos en los pipelines, ambientes de pruebas y producción, versionado semántico y estrategias de despliegue como blue green y canary. Como proyecto práctico configurarás un pipeline completo que prueba, construye y despliega una API en contenedores cada vez que se integra una rama. Está dirigido a desarrolladores que ya conocen Git y Docker y quieren adoptar prácticas DevOps. Sirve para entregar software más rápido y con menos errores.',
 'Herramientas tecnológicas', 'Intermedio', 35, TRUE),

-- 27
('Pruebas de seguridad y hacking ético web',
 'Aprende a realizar pruebas de seguridad a aplicaciones web de forma ética y autorizada para descubrir vulnerabilidades antes que los atacantes. El curso cubre la metodología de una prueba de penetración: reconocimiento, mapeo de la aplicación, búsqueda de vulnerabilidades, explotación controlada y elaboración del informe. Usarás herramientas como Burp Suite, OWASP ZAP, Nmap y sqlmap en laboratorios aislados, y practicarás con aplicaciones vulnerables de entrenamiento. Verás pruebas de inyección SQL, XSS, autenticación y gestión de sesiones, control de acceso, carga de archivos y configuración del servidor, además del escaneo automático en pipelines. Como proyecto práctico realizarás una auditoría completa a una aplicación de pruebas y entregarás un informe con hallazgos, evidencias, nivel de riesgo y recomendaciones. Está dirigido a desarrolladores y profesionales de ciberseguridad con conocimientos de desarrollo web. Sirve para proteger aplicaciones web y prepararse para certificaciones de seguridad.',
 'Herramientas tecnológicas', 'Avanzado', 45, TRUE),

-- 28
('Linux y línea de comandos para desarrolladores',
 'Aprende a trabajar con Linux y la terminal, una habilidad esencial para programadores, administradores de servidores y profesionales de DevOps. El curso cubre la estructura del sistema de archivos, navegación y manipulación de archivos, permisos y usuarios, procesos, variables de entorno, gestión de paquetes con apt, redirecciones y tuberías, y herramientas como grep, find, sed y awk. También aprenderás a conectarte a servidores remotos con SSH, transferir archivos, revisar registros, administrar servicios con systemd y escribir scripts en bash para automatizar tareas. Practicarás con Ubuntu en una máquina virtual o en WSL en Windows. Como proyecto práctico configurarás un servidor Linux desde cero, instalarás un servidor web y crearás un script de respaldo programado con cron. Está dirigido a estudiantes y desarrolladores que nunca han usado Linux o solo conocen lo básico. Sirve para trabajar con servidores, la nube y contenedores.',
 'Herramientas tecnológicas', 'Básico', 25, TRUE),

-- 29 (inactivo: pertinente a «crear páginas web», nunca debe recomendarse)
('Maquetación de sitios web con Bootstrap',
 'Aprende a crear páginas web modernas y adaptables de forma rápida usando Bootstrap, el framework CSS más popular. El curso cubre la instalación de Bootstrap, su sistema de rejilla de doce columnas, contenedores, filas y puntos de quiebre para diseño responsive, y los componentes listos para usar: barras de navegación, botones, tarjetas, formularios, carruseles, modales y alertas. También verás utilidades de espaciado, colores y tipografía, íconos, y cómo personalizar el tema con variables. Trabajarás con HTML, CSS y Visual Studio Code, y publicarás el resultado en internet. Como proyecto práctico construirás la página web de presentación de un negocio local con secciones de servicios, galería, testimonios y formulario de contacto, adaptada a celulares y computadoras. Está dirigido a principiantes que conocen lo básico de HTML y quieren crear sitios web atractivos sin diseñar todo desde cero. Sirve para lanzar páginas web profesionales en poco tiempo.',
 'Desarrollo web', 'Básico', 25, FALSE),

-- 30 (inactivo: pertinente a «contenedores», nunca debe recomendarse)
('Despliegue de contenedores con Docker Swarm',
 'Aprende a desplegar y administrar aplicaciones en contenedores en varios servidores usando Docker Swarm, el orquestador integrado en Docker. El curso cubre la creación de un clúster con nodos administradores y trabajadores, servicios, réplicas, stacks definidos con archivos Compose, redes overlay, balanceo de carga interno, secretos y configuraciones. Aprenderás a escalar servicios, hacer actualizaciones progresivas sin interrumpir la aplicación, revertir versiones y mantener la alta disponibilidad cuando falla un nodo. También verás volúmenes para datos persistentes, monitoreo de servicios y cuándo conviene migrar a Kubernetes. Como proyecto práctico desplegarás en un clúster de tres máquinas virtuales una aplicación web con su API y base de datos, y simularás la caída de un servidor. Está dirigido a desarrolladores y administradores de sistemas que ya usan Docker y Docker Compose y quieren llevar sus contenedores a producción. Sirve para operar aplicaciones distribuidas.',
 'Herramientas tecnológicas', 'Intermedio', 25, FALSE);
