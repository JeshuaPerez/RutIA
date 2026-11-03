# Frontend de RutaIA

Interfaz web de una sola página, en HTML, CSS y JavaScript nativos (módulos ES, Fetch API, `async`/`await` y DOM), sin frameworks, librerías, CDN ni fuentes externas. Solo habla con la API de Spring Boot (`http://localhost:8080/api`); nunca con n8n, Qdrant ni OpenRouter.

## 1. Cómo servirlo

Orden: contenedores → API → frontend (ver [entorno-local.md](entorno-local.md)). El frontend se sirve con `jwebserver`, que viene con el JDK 21, en el puerto 5500, que es uno de los orígenes permitidos por CORS (`RUTAIA_CORS_ORIGINS=http://localhost:5500,http://127.0.0.1:5500`).

Windows (PowerShell), con la ruta absoluta de la carpeta `frontend`:

```powershell
jwebserver -p 5500 -d "<ruta absoluta>\frontend"
# por ejemplo, desde la raíz del repositorio:
jwebserver -p 5500 -d "$((Resolve-Path .\frontend).Path)"
```

Linux (bash), desde la raíz del repositorio:

```bash
jwebserver -p 5500 -d "$(pwd)/frontend"
```

Después se abre http://localhost:5500. `jwebserver` escucha solo en la interfaz de bucle local y entrega `.js` como `text/javascript`, que es lo que exigen los módulos. Se detiene con Ctrl+C.

Si la API no está en marcha, la página carga igual y cada sección muestra «No se pudo conectar con el servidor».

## 2. Estructura

```
frontend/
├── index.html          una sola página: cabecera, 4 pestañas con sus secciones y la plantilla de la animación de carga
├── css/
│   └── estilos.css     paleta, tipografía, componentes y adaptación (barra inferior bajo 640 px)
└── js/
    ├── config.js       única constante: API_BASE_URL = "http://localhost:8080/api"
    ├── api.js          fetch, manejo de errores de C6 (ApiError) y una función por endpoint
    ├── app.js          punto de entrada: pestañas, chip del estudiante y restauración del estudiante guardado
    ├── estudiante.js   estudiante activo (id en localStorage) y aviso de cambios a las secciones
    ├── pestanas.js     pestañas accesibles (flechas, Inicio, Fin)
    ├── registro.js     E-01 registro y E-02 elegir un estudiante existente
    ├── catalogo.js     E-05 tarjetas y filtros de categoría y nivel
    ├── consulta.js     E-11 formulario, espera «Trazando tu ruta…» y resultado
    ├── resultado.js    vista de un resultado (E-11 o E-12): pregunta, estado, recomendación, mensaje y fuentes
    ├── calificacion.js E-13 estrellas y comentario, o la calificación ya registrada
    ├── historial.js    E-04 bitácora de consultas y E-12 detalle
    ├── componentes.js  piezas compartidas: estado, chips del curso, fechas, similitud, descripción plegable
    ├── formulario.js   errores por campo (detalles de C6) y alertas
    └── dom.js          crear nodos con textContent; íconos del sprite
```

Todos los nombres están en minúsculas y cada `import` y cada `href`/`src` coincide exactamente con el nombre del archivo, para que funcione igual en Linux. `index.html` carga un solo `<script type="module" src="js/app.js">`; el resto se importa desde ahí.

### Endpoints que usa

| Sección | Endpoint | Uso |
|---|---|---|
| Al cargar | E-03 `GET /estudiantes/{id}` | Valida el id guardado; si da 404 lo olvida |
| Registro | E-01 `POST /estudiantes` | Crea el perfil y lo deja como estudiante activo |
| Registro | E-02 `GET /estudiantes` | Lista para elegir un estudiante existente |
| Catálogo | E-05 `GET /cursos?categoria=&nivel=` | Sin filtros al cargar (también da las categorías); con filtros al cambiarlos |
| Consultar | E-11 `POST /consultas` | Envía `{estudianteId, pregunta}` y pinta el resultado |
| Historial | E-04 `GET /estudiantes/{id}/historial` | Lista de consultas, fecha descendente |
| Historial | E-12 `GET /consultas/{id}` | Detalle completo con la calificación o su formulario; también tras un 409 de E-13 |
| Resultado | E-13 `POST /recomendaciones/{id}/calificacion` | Calificación de 1 a 5 y comentario opcional |

## 3. Decisiones de diseño

### Concepto «cuaderno de ruta»

- **Papel y tinta.** Fondo `#F5F1E8` con una retícula de puntos (como una libreta punteada), texto en tinta `#1E2A38`, superficies blancas `#FFFFFF` con borde `#E5DED0`, radio de 12 px y sombra suave. El primario verde azulado `#0F766E` marca acciones y la pestaña activa; el ámbar `#D97706` es el acento de la ruta (líneas punteadas, marcador de destino, estrellas).
- **Tipografía del sistema.** Títulos en `Georgia, "Iowan Old Style", serif`; texto en `system-ui, "Segoe UI", Roboto, Arial, sans-serif`. No se descarga ninguna fuente.
- **Cabecera.** Marca «RutaIA» con un ícono de ruta (punto de partida, camino punteado y marcador) y un chip con el estudiante activo («Estudiante activo · id N» y su nombre). El chip lleva a Registro, donde se cambia de estudiante.
- **Pestañas.** Registro · Catálogo · Consultar · Historial bajo la cabecera, que es fija en escritorio; bajo 640 px pasan a una barra inferior fija con ícono y texto, y el cuerpo deja espacio para ella. El contenido mide como máximo 1100 px.
- **Rótulos de sección.** Cada pestaña tiene un rótulo pequeño en ámbar («Punto de partida», «Mapa de cursos», «Tu necesidad», «Bitácora») y un título con serifa subrayado con puntos.
- **Consulta.** El área de texto imita una hoja rayada; debajo, una nota ámbar avisa que la respuesta puede tardar hasta un minuto y medio (el tiempo máximo que la API espera a n8n).
- **Carga.** Mientras E-11 responde, una línea punteada se dibuja desde el punto de partida hasta el marcador, con el texto «Trazando tu ruta…» (SVG con una máscara animada).
- **Recomendación y fuentes.** La recomendación va en un bloque oscuro (fondo tinta, texto claro) con la etiqueta «Recomendación de RutaIA», como texto plano que respeta los saltos de línea (`white-space: pre-line`). Las fuentes son tarjetas claras numeradas, unidas por tramos de una línea punteada vertical, con chips de categoría y nivel, la duración y la similitud con 3 decimales y una barra. Así la recomendación y las fuentes no se confunden.
- **Estados.** Con texto y color: Pendiente `#64748B`, Respondida `#15803D`, Sin resultados `#B45309` y Error `#B91C1C`, como etiqueta rellena con texto blanco. En el historial, el punto de la línea de tiempo usa el color del estado. En Sin resultados y Error se muestra el campo `mensaje` en un aviso con borde del color del estado.
- **Historial.** Una bitácora vertical: cada consulta muestra fecha (formato `es-GT`), estado, pregunta, resumen, cursos recomendados y «Ver detalle». El detalle se abre dentro de la misma pestaña, con «Volver al historial».
- **Movimiento.** Transiciones de 180 ms en botones, pestañas, tarjetas y estrellas. Con `prefers-reduced-motion: reduce` se anulan las transiciones y animaciones, y la línea de carga aparece completa y quieta.

### Accesibilidad

- Contraste AA en todas las combinaciones de texto (mínimo 4,76:1, en Pendiente) y 3:1 en bordes de controles, foco y estrellas; el detalle está en `docs/evidencias/etapa8-frontend.txt`. El ámbar `#D97706` sobre blanco no llega a 4,5:1, por eso no se usa para texto: los textos ámbar usan `#8A4B06` sobre papel y `#F6C27A` sobre tinta.
- Todos los campos tienen `<label>`; los grupos de radios, `<fieldset>` y `<legend>`. Los errores se enlazan con `aria-describedby`, los campos con error llevan `aria-invalid` y el foco va al primero.
- Regiones vivas: alertas con `role="alert"`, estados de listas y avisos con `role="status"`, y el resultado de la consulta y del detalle con `aria-live="polite"`. Al llegar un resultado, el foco pasa a su título.
- Teclado: enlace «Saltar al contenido»; pestañas con el patrón `tablist` (flechas, Inicio y Fin; solo la activa está en el orden de tabulación); estrellas como radios nativos (flechas para cambiar la puntuación); al volver del detalle, el foco regresa al botón «Ver detalle» que lo abrió. El foco es visible siempre (contorno de 3 px `#0F766E`) y `scroll-padding` evita que la cabecera fija o la barra inferior lo tapen.
- Íconos decorativos con `aria-hidden`; las estrellas de una calificación registrada se leen como «4 de 5 estrellas».

### Datos, seguridad y errores

- **Solo texto.** Ningún dato de la API se inserta como HTML: `dom.js` crea los nodos y usa `textContent` o nodos de texto. No hay `innerHTML` en el código.
- **Una sola puerta.** Toda petición sale de `api.js` con `API_BASE_URL` delante. Solo se envían las cabeceras `Accept` y `Content-Type`, dentro de lo que permite CORS.
- **Errores de C6.** `api.js` convierte toda respuesta 4xx/5xx en `ApiError` con `status`, `mensaje` y `detalles`. Los formularios muestran `mensaje` en una alerta y cada detalle bajo su campo (con la primera letra en mayúscula). Si `fetch` falla sin respuesta (API detenida, red caída o CORS), el mensaje es «No se pudo conectar con el servidor».
- **Estudiante activo.** Al registrarse o elegir uno, su id se guarda en `localStorage` (`rutaia.estudianteId`), siempre dentro de `try/catch`; si el almacenamiento no está disponible, el estudiante dura lo que la página. Al cargar, el id guardado se usa de inmediato y se valida con E-03: con 404 se olvida y se avisa; sin conexión se conserva hasta poder validarlo. Consultar e Historial muestran un aviso con «Ir a Registro» mientras no hay estudiante activo.
- **Validación.** El cliente solo evita enviar una pregunta vacía (tras quitar espacios) y una calificación sin estrellas; `maxlength` limita lo que se puede escribir (120, 254, 100, 500 y 500). Todo lo demás lo decide la API: el registro se envía sin validar en el cliente para que se vean sus errores por campo (400) y el de correo duplicado (409).
- **Respuestas viejas.** El catálogo, el historial y el detalle numeran sus peticiones y descartan la respuesta si llegó otra más nueva (por ejemplo, al cambiar dos filtros seguidos).
- **El frontend nunca cambia estados.** Solo muestra el `estado` que devuelve la API.

### Decisiones donde el contrato no dice nada

- **Pestaña inicial.** Registro si no hay estudiante guardado; Consultar si lo hay.
- **Lista de estudiantes (E-02).** Se carga cada vez que se abre Registro y muestra «nombre · nivel · área»; no muestra correos.
- **Recorte de espacios.** El registro envía nombre, correo y área sin espacios al inicio ni al final; la pregunta y el comentario se envían tal cual (la API los recorta). Un comentario vacío o solo de espacios se envía como `null`.
- **Descripciones largas.** En el catálogo y en las fuentes, las descripciones de más de 220 caracteres se recortan a 4 líneas con el botón «Leer descripción completa» (`aria-expanded`); no se pierde texto.
- **Reintento sin botón.** Si el catálogo no pudo cargarse, se vuelve a pedir al abrir de nuevo su pestaña; el historial se recarga cada vez que se abre.
- **Calificación tras un 409.** E-13 no devuelve la calificación existente, así que se lee de E-12 (la consulta del resultado) y se muestra con el aviso «Esta recomendación ya estaba calificada». El 409 se ve cuando la misma recomendación está abierta en dos lugares (por ejemplo, en Consultar y en el detalle del historial, o en dos pestañas del navegador).
- **Resultado de una consulta.** Muestra también el número de consulta y la fecha, que vienen en `ConsultaResultado`.
- **Cambio de estudiante.** Al cambiar de estudiante se limpian el resultado de Consultar y el historial; si una consulta en curso termina después del cambio, su resultado no se pinta (queda en el historial del estudiante anterior).

## 4. Limitaciones conocidas

- Cuando la API responde 4xx (por ejemplo, un correo duplicado) o está detenida, el propio navegador escribe en la consola «Failed to load resource…». Son avisos de red del navegador, no errores de JavaScript; la página los maneja y muestra su mensaje.
- El aviso de espera habla de «hasta un minuto y medio» porque es el valor de `RUTAIA_N8N_TIMEOUT_SEGUNDOS` en C11 (90). Si se cambia esa variable, el texto queda desactualizado.
- No hay estadísticas, administración de cursos, autenticación ni modo oscuro: están fuera del alcance de esta etapa.
