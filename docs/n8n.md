# Flujos de n8n de RutaIA

Los flujos se guardan como JSON en `n8n/` (montada en el contenedor como `/home/node/flujos`). Todos empiezan con el nodo **Configuración** (Edit Fields/Set), que fija la colección, los modelos y las URL; no se usa `$env`, y las llamadas a OpenRouter usan la credencial «RutaIA OpenRouter» (la clave nunca aparece en los JSON).

## Indexación

Flujo **«RutaIA - Indexación de cursos»** (`n8n/rutaia-indexacion-cursos.json`, ID `RutaIAIndexacion`). Lee los cursos de la API, crea o reutiliza la colección `rutaia_cursos` de Qdrant, genera un embedding por curso con OpenRouter y guarda o actualiza un punto por curso.

- Entrada: `POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos` (sin cuerpo) o el botón *Execute workflow* del editor (Manual Trigger).
- 200: `{"coleccion":"rutaia_cursos","coleccionCreada":false,"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}`
- 500: `{"error":"Falló el paso «…»: …"}`. Si el fallo viene de OpenRouter, el mensaje incluye el código HTTP (429 cuota agotada, 404 privacidad no habilitada, 402 saldo negativo).
- Lo ejecuta el administrador; nunca el frontend ni Spring Boot.

### Diagrama de nodos

```mermaid
flowchart LR
  MT[Ejecutar manualmente] --> CFG[Configuración]
  WH[Webhook POST rutaia-indexar-cursos] --> CFG
  CFG --> LC[Leer cursos<br/>GET apiBaseUrl/admin/cursos] --> VC[Validar cursos]
  VC --> EX[¿Existe la colección?<br/>GET /collections/rutaia_cursos/exists] --> DC[Decidir colección] --> IF{¿Ya existe?}
  IF -- sí --> LCF[Leer configuración<br/>GET /collections/rutaia_cursos] --> CC[Comprobar colección<br/>2048 y Cosine]
  IF -- no --> CRC[Crear colección<br/>PUT /collections/rutaia_cursos] --> CC
  CC --> IDX[Crear índice activo<br/>PUT /index bool] --> PT[Preparar textos]
  PT --> EMB[Generar embeddings<br/>POST openRouterUrl/embeddings<br/>input_type passage] --> VE[Validar embeddings]
  VE --> GP[Guardar puntos<br/>PUT /points?wait=true] --> CP[Contar puntos<br/>GET /collections/rutaia_cursos] --> AR[Armar respuesta] --> R200[Responder 200]
  LC & VC & EX & DC & LCF & CRC & CC & IDX & PT & EMB & VE & GP & CP & AR -. error .-> AE[Armar error] --> R500[Responder error 500]
```

| Nodo | Qué hace |
|---|---|
| Ejecutar manualmente / Webhook | Los dos disparadores. El Webhook responde con los nodos *Respond to Webhook* (`responseMode: responseNode`). |
| Configuración | `coleccion`, `modeloEmbeddings`, `dimensionVector` (2048), `modeloLlm`, `qdrantUrl`, `apiBaseUrl`, `openRouterUrl` (C8). |
| Leer cursos | `GET {apiBaseUrl}/admin/cursos` (E-07): activos e inactivos, id ascendente. |
| Validar cursos | HTTP 200, arreglo con al menos 20 cursos, ids enteros sin repetir, nombre, descripción, categoría y nivel no vacíos, duración entera > 0 y `activo` booleano. |
| ¿Existe la colección? → Decidir colección → ¿Ya existe? | `GET /collections/rutaia_cursos/exists` y bifurcación por `result.exists`. |
| Crear colección | Si no existe: `PUT /collections/rutaia_cursos` con `{"vectors":{"size":2048,"distance":"Cosine"}}`. |
| Leer configuración | Si ya existe: `GET /collections/rutaia_cursos`. |
| Comprobar colección | Si ya existía, exige un vector sin nombre de `size` 2048 y `distance` Cosine; si no coincide, falla **sin borrar nada**. Calcula `coleccionCreada`. |
| Crear índice activo | `PUT /collections/rutaia_cursos/index?wait=true` con `{"field_name":"activo","field_schema":"bool"}` (idempotente). |
| Preparar textos | Texto de C9 por curso, en 4 líneas: `{nombre}` / `Categoría: {categoria}` / `Nivel: {nivel}` / `{descripcion}`. |
| Generar embeddings | **Una sola** solicitud a OpenRouter con `input` como arreglo de todos los textos, `"input_type":"passage"` y `"encoding_format":"float"`, con la credencial «RutaIA OpenRouter». |
| Validar embeddings | HTTP 200 y sin `error`; tantos vectores como cursos; ordenados por `index` y con índices 0..n−1; cada vector con exactamente 2048 números finitos. Arma los puntos. |
| Guardar puntos | `PUT /collections/rutaia_cursos/points?wait=true` con todos los puntos. |
| Contar puntos / Armar respuesta | Comprueba que el upsert quedó `completed` y lee `points_count` de la colección. |
| Armar error / Responder error | Todos los nodos HTTP y Code tienen la salida de error conectada aquí; responde 500 con el paso que falló. |

Todo lo que no gasta cuota (leer y validar la API, preparar Qdrant y el índice) va **antes** de llamar a OpenRouter: con la API o Qdrant caídos el flujo responde 500 sin gastar solicitudes. Cada ejecución gasta **1** solicitud (de 20 por minuto y 50 por día por cuenta).

Los nodos HTTP usan *Include Response Headers and Status* y *Never Error* para que los nodos Code puedan revisar el código HTTP y dar un mensaje claro; los errores de conexión (servicio caído) salen por la salida de error del nodo.

### Cómo evita duplicados y cómo relaciona cada punto con su curso

- El ID de cada punto es el `id` del curso en PostgreSQL (entero). El upsert de Qdrant es por ID: si el punto ya existe, se reemplazan su vector y su payload; si no, se crea. Re-ejecutar el flujo no agrega puntos (con la semilla, `totalPuntos` queda en 30).
- El payload repite el identificador como `cursoId` y guarda los datos descriptivos del curso, con este formato exacto:
  `{"cursoId":1,"nombre":"…","descripcion":"…","categoria":"…","nivel":"Básico","duracionHoras":40,"activo":true}`
- La colección se crea solo si no existe; si existe, se reutiliza tras comprobar su configuración.

### Cuándo re-ejecutarlo

Después de **cargar, cambiar o desactivar cursos** (E-06, E-09, E-10 o una recarga de la semilla). El flujo no sincroniza solo:
- Un curso nuevo no aparece en las búsquedas hasta re-indexar.
- Un curso cambiado conserva el vector y el payload viejos hasta re-indexar.
- Un curso desactivado queda con `activo: true` en Qdrant hasta re-indexar; al hacerlo, el punto se actualiza a `activo: false` y el filtro `activo = true` del flujo RAG lo excluye. (El flujo RAG además descarta los cursos que no estén en `GET /api/cursos`.)
- El flujo nunca borra puntos: un curso eliminado de PostgreSQL seguiría en Qdrant.

### Cómo importarlo y publicarlo

1. Con los contenedores arriba, importa el JSON (desde la raíz del repositorio):

   ```bash
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-indexacion-cursos.json
   docker compose exec n8n n8n list:workflow
   ```

   Debe aparecer `RutaIAIndexacion|RutaIA - Indexación de cursos`.
2. Credencial: el JSON referencia «RutaIA OpenRouter» por ID (`zLvfa0tPU6IKdFZW`, el de la máquina donde se creó). En otra máquina el ID será distinto: abre el flujo en http://localhost:5678, entra al nodo **Generar embeddings**, elige tu credencial «RutaIA OpenRouter» en *Credential for Header Auth* y guarda.
3. Publica y reinicia n8n (la CLI avisa que el cambio se aplica al reiniciar):

   ```bash
   docker compose exec n8n n8n publish:workflow --id=RutaIAIndexacion
   docker compose restart n8n
   ```

   También se puede publicar desde el editor con el botón **Publish**; en ese caso no hace falta reiniciar.
4. Para guardar en el repositorio la versión publicada:

   ```bash
   docker compose exec n8n n8n export:workflow --id=RutaIAIndexacion --published --output=/home/node/flujos/rutaia-indexacion-cursos.json --pretty
   ```

   Antes de versionarlo quita del JSON el bloque `"shared"` (trae el nombre y el correo del propietario de n8n) y comprueba que no haya claves: `git grep -nE "sk-or-v1|Bearer " -- n8n/` no debe mostrar nada.

### Cómo ejecutarlo

Requisitos: contenedores arriba y la API (`bootRun`) en ejecución.

PowerShell:

```powershell
curl.exe -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos
```

bash:

```bash
curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos
```

Desde el editor: abre el flujo y pulsa *Execute workflow* (usa el Manual Trigger; la respuesta queda en el nodo **Armar respuesta** o **Armar error**).

Comprobación en Qdrant (panel en http://127.0.0.1:6333/dashboard):

```bash
curl -s http://127.0.0.1:6333/collections/rutaia_cursos
curl -s -X POST http://127.0.0.1:6333/collections/rutaia_cursos/points/scroll -H "Content-Type: application/json" -d '{"limit":2,"with_payload":true,"with_vector":false}'
```

En PowerShell, el cuerpo del scroll va en un archivo:

```powershell
curl.exe -s http://127.0.0.1:6333/collections/rutaia_cursos
curl.exe -s -X POST http://127.0.0.1:6333/collections/rutaia_cursos/points/scroll -H "Content-Type: application/json" --data-binary "@pruebas/n8n/etapa3-qdrant-scroll.json"
```

## Calibración de umbral

Flujo auxiliar **«RutaIA - Calibración de umbral»** (W-3, `n8n/rutaia-calibracion-umbral.json`, ID `RutaIACalibracion`). Vectoriza una lista de preguntas y devuelve los 10 cursos activos más parecidos a cada una, con su score y **sin umbral**. Sirve para medir la escala de los scores y elegir el umbral del flujo RAG (Etapa 4, `docs/calibracion-umbral.md`). No forma parte de la aplicación: lo ejecuta el equipo, nunca el frontend ni Spring Boot.

- Entrada: `POST http://127.0.0.1:5678/webhook/rutaia-calibrar-umbral` con `{"preguntas":["…"]}` (de 1 a 20 textos no vacíos).
- 200: `{"modelo":"nvidia/nemotron-3-embed-1b:free","coleccion":"rutaia_cursos","resultados":[{"pregunta":"…","puntos":[{"cursoId":1,"nombre":"…","categoria":"…","score":0.6421}]}]}`, con los 10 mejores puntos por pregunta (filtro `activo = true`), el score redondeado a 4 decimales y en el orden de las preguntas.
- 500: `{"error":"Falló el paso «…»: …"}`.

### Diagrama de nodos

```mermaid
flowchart LR
  WH[Webhook POST rutaia-calibrar-umbral] --> CFG[Configuración] --> VP[Validar preguntas]
  VP --> LC[Leer colección<br/>GET /collections/rutaia_cursos] --> CC[Comprobar colección<br/>existe, 2048 y Cosine, con puntos]
  CC --> EMB[Generar embeddings<br/>POST openRouterUrl/embeddings<br/>input_type query] --> VE[Validar embeddings<br/>un ítem por pregunta]
  VE --> BQ[Buscar en Qdrant<br/>POST /points/query<br/>limit 10, sin umbral] --> AR[Armar respuesta] --> R200[Responder 200]
  VP & LC & CC & EMB & VE & BQ & AR -. error .-> AE[Armar error] --> R500[Responder error 500]
```

| Nodo | Qué hace |
|---|---|
| Webhook | Disparador (`responseMode: responseNode`). No tiene Manual Trigger porque necesita el cuerpo con las preguntas. |
| Configuración | Los mismos campos que la indexación (C8); W-3 no usa `umbralSimilitud` ni `limiteResultados`. |
| Validar preguntas | `preguntas` es una lista de 1 a 20 textos no vacíos; aplica trim a cada uno (C9: sin prefijos). |
| Leer colección / Comprobar colección | `GET /collections/rutaia_cursos`: 404 → falla pidiendo ejecutar la indexación; exige `size` 2048, `distance` Cosine y al menos un punto. Arma el cuerpo de embeddings. |
| Generar embeddings | **Una sola** solicitud con todas las preguntas, `"input_type":"query"` y `"encoding_format":"float"`, con la credencial «RutaIA OpenRouter». |
| Validar embeddings | HTTP 200 y sin `error` (con la pista de 429/404/402); un vector por pregunta, ordenados por `index`, cada uno con 2048 números finitos. Emite un ítem por pregunta con el cuerpo de calibración de C9. |
| Buscar en Qdrant | Un `POST /collections/rutaia_cursos/points/query` por ítem: `{"query":[…],"filter":{"must":[{"key":"activo","match":{"value":true}}]},"limit":10,"with_payload":true,"with_vector":false}`. |
| Armar respuesta | Comprueba cada búsqueda (HTTP 200 y `result.points`), falla si llega un punto inactivo y arma la respuesta sin vectores. |
| Armar error / Responder error | Igual que en la indexación. |

Todo lo que no gasta cuota (validar la entrada y la colección) va antes de OpenRouter. Cada ejecución gasta **1** solicitud, sin importar cuántas preguntas lleve.

### Cómo importarlo, publicarlo y exportarlo

Igual que la indexación, con su archivo e ID:

```bash
docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-calibracion-umbral.json
docker compose exec n8n n8n list:workflow
docker compose exec n8n n8n publish:workflow --id=RutaIACalibracion
docker compose restart n8n
```

Debe aparecer `RutaIACalibracion|RutaIA - Calibración de umbral`. En otra máquina, elige tu credencial «RutaIA OpenRouter» en el nodo **Generar embeddings** antes de publicar. Para versionar la versión publicada:

```bash
docker compose exec n8n n8n export:workflow --id=RutaIACalibracion --published --output=/home/node/flujos/rutaia-calibracion-umbral.json --pretty
```

y quita el bloque `"shared"` como en la indexación.

### Cómo ejecutarlo

Requisitos: contenedores arriba y la colección indexada (la API no hace falta). Las 8 consultas de prueba están en `pruebas/n8n/etapa4-calibracion-preguntas.json`.

PowerShell:

```powershell
curl.exe -s -X POST http://127.0.0.1:5678/webhook/rutaia-calibrar-umbral -H "Content-Type: application/json; charset=utf-8" --data-binary "@pruebas/n8n/etapa4-calibracion-preguntas.json"
```

bash:

```bash
curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-calibrar-umbral -H "Content-Type: application/json; charset=utf-8" --data-binary "@pruebas/n8n/etapa4-calibracion-preguntas.json"
```

Re-ejecútalo (y revisa `docs/calibracion-umbral.md`) si cambian el modelo de embeddings, el texto que se indexa o las descripciones de los cursos.
