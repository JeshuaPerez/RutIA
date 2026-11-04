# RutaIA

Orientación académica con inteligencia artificial: el estudiante describe en lenguaje natural lo que necesita aprender y RutaIA le recomienda **cursos existentes** del catálogo institucional, explicando por qué y en qué orden tomarlos.

## Nombre del proyecto e integrantes

| Dato | Valor |
|---|---|
| Proyecto | **RutaIA**: recomendación de cursos con una arquitectura RAG |
| Integrante | Jeshua Perez (proyecto individual) |
| Repositorio | https://github.com/JeshuaPerez/RutIA |

## Descripción de la problemática y solución propuesta

**Problemática.** En un catálogo con decenas de cursos, el estudiante no siempre sabe qué nombre tiene lo que busca, qué cursos tratan su tema ni por cuál empezar según su nivel. Un buscador por palabras falla cuando la necesidad está escrita con otras palabras («proteger aplicaciones web» frente a «OWASP Top 10»), y un chatbot genérico puede inventar cursos que la institución no ofrece.

**Solución propuesta.** Una aplicación web en la que el estudiante se registra con su nivel y su área de interés, escribe su necesidad y recibe una recomendación generada con **RAG** (*Retrieval-Augmented Generation*):

1. La API guarda la consulta y la envía a un flujo de n8n.
2. El flujo convierte la pregunta en un vector (embedding) y busca en Qdrant los cursos activos más parecidos que superen un **umbral de similitud calibrado (0.34)**.
3. Si no hay cursos pertinentes, responde «Sin resultados» sin llamar al modelo de lenguaje. Si los hay, el LLM redacta la recomendación **usando solo esos cursos**, y la respuesta se valida para que no nombre cursos ajenos.
4. La API guarda la recomendación con sus fuentes y su similitud. El estudiante ve el resultado y su historial, y puede calificar cada recomendación de 1 a 5.

## Tecnologías y arquitectura

| Capa | Tecnología |
|---|---|
| Frontend | HTML, CSS y JavaScript nativos (módulos ES, Fetch, `async`/`await`), servidos con `jwebserver` (JDK 21) |
| Backend | Java 21, Spring Boot 4.1.2 (Web MVC, Data JPA, Validation), Gradle, springdoc-openapi 3.2.0 (Swagger). Es la **única puerta de entrada** |
| Base relacional | PostgreSQL 17 (contenedor), base `rutaia` |
| Base vectorial | Qdrant v1.20.0 (contenedor), colección `rutaia_cursos` (2048 dimensiones, Cosine) |
| Automatización | n8n 2.45.7 (contenedor): indexación, calibración y consulta RAG |
| IA (OpenRouter) | Embeddings `nvidia/nemotron-3-embed-1b:free` (gratuito) y LLM `google/gemma-4-31b-it` (de pago, centavos por consulta) |
| Infraestructura | Docker Compose ([docker-compose.yml](docker-compose.yml)) para PostgreSQL, Qdrant y n8n; el backend y el frontend corren en la máquina |

```text
Navegador → Frontend :5500 → API Spring Boot :8080 ─┬─ PostgreSQL :5433
                                                     └─ n8n :5678 (webhook) ─┬─ GET /api/cursos (activos)
                                                                             ├─ OpenRouter: embedding de la pregunta
                                                                             ├─ Qdrant :6333: búsqueda con umbral
                                                                             └─ OpenRouter: LLM con los cursos recuperados
```

**Backend.** Arquitectura en capas: `presentacion` (controladores y manejo global de errores) → `aplicacion` (servicios, DTO, mappers y puertos) → `dominio` y `persistencia` (repositorios Spring Data); `integracion.n8n` implementa un puerto de `aplicacion`. Aplica SOLID: interfaces de servicio separadas por uso e inyección por constructor, y la aplicación depende de `MotorRecomendacion`, nunca de n8n. El patrón **Adapter** (`MotorRecomendacionN8n`) adapta el webhook de n8n a ese puerto, así que un motor nuevo es una clase nueva.

Documentación:
- [docs/arquitectura.md](docs/arquitectura.md): diagrama de arquitectura, flujos, backend, frontend y decisiones.
- [docs/base-de-datos.md](docs/base-de-datos.md): diagrama entidad-relación, restricciones e índices.
- [docs/n8n.md](docs/n8n.md): flujos y calibración del umbral.
- [docs/pruebas.md](docs/pruebas.md): documento de pruebas.
- [docs/presentacion-final.md](docs/presentacion-final.md): guion de la presentación.

## Requisitos de instalación y variables de entorno

En la máquina solo hacen falta **Docker** (Docker Desktop con WSL2 en Windows; Docker Engine con el plugin Compose en Linux), **JDK 21** (incluye `jwebserver`) y **Git**. PostgreSQL, Qdrant y n8n no se instalan aparte.

Windows (PowerShell como administrador; después, abre Docker Desktop una vez y abre una terminal nueva):
```powershell
wsl --install --no-distribution
winget install -e --id Docker.DockerDesktop
winget install -e --id EclipseAdoptium.Temurin.21.JDK
winget install -e --id Git.Git
```
Linux, Ubuntu o Debian (sin verificar):
```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"   # cierra sesión y vuelve a entrar
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk git
```

**Variables.** Copia [.env.example](.env.example) como `.env` en la raíz y escribe tu contraseña en `RUTAIA_DB_PASSWORD`, sin comillas, espacios ni barras invertidas (lo más seguro es usar solo letras y números). El `.env` no se versiona.

```powershell
git clone https://github.com/JeshuaPerez/RutIA.git; cd RutIA; Copy-Item .env.example .env; notepad .env
```
```bash
git clone https://github.com/JeshuaPerez/RutIA.git && cd RutIA && cp .env.example .env && nano .env
```

| Variable | Valor en `.env.example` | La usan |
|---|---|---|
| `RUTAIA_DB_USER` | `rutaia_app` | Compose (usuario de PostgreSQL) y Spring Boot |
| `RUTAIA_DB_PASSWORD` | *(vacía: obligatoria, la eliges tú)* | Compose y Spring Boot. **Es secreta** |
| `RUTAIA_N8N_TIMEOUT_SEGUNDOS` | `90` | Spring Boot (tiempo máximo de espera de n8n) |
| `RUTAIA_PUERTO_POSTGRES` | `5433` | Compose (puerto publicado) y URL JDBC de Spring Boot |
| `RUTAIA_PUERTO_QDRANT` | `6333` | Compose (puerto publicado) |
| `RUTAIA_PUERTO_N8N` | `5678` | Compose (puerto publicado) y URL del webhook en Spring Boot |
| `RUTAIA_PUERTO_API` | `8080` | `server.port` de Spring Boot y `RUTAIA_API_URL` del contenedor n8n |
| `RUTAIA_PUERTO_FRONTEND` | `5500` | Orígenes CORS de Spring Boot |

Docker Compose lee el `.env` solo; Spring Boot lo importa desde `backend/` con `spring.config.import=optional:file:../.env[.properties]`. `RUTAIA_API_URL` no va en el `.env`: Compose la arma con `RUTAIA_PUERTO_API` y los flujos la leen con `{{ $env.RUTAIA_API_URL }}`. La contraseña de PostgreSQL se fija la **primera vez** que se crea el volumen `pgdata` (ver «Errores conocidos»).

### Puertos

Cada puerto se define una sola vez, en `.env`. Si uno choca con otro programa, cambia su variable y, en las filas que lo indican, también lo que dice la última columna:

| Puerto | Componente | Variable | Además |
|---|---|---|---|
| 5433 | PostgreSQL | `RUTAIA_PUERTO_POSTGRES` | `docker compose up -d` y reiniciar el backend |
| 6333 | Qdrant | `RUTAIA_PUERTO_QDRANT` | `docker compose up -d` (dentro de Docker los flujos usan `qdrant:6333`) |
| 5678 | n8n | `RUTAIA_PUERTO_N8N` | `docker compose up -d` y reiniciar el backend; usa ese puerto en las URL de n8n de este README |
| 8080 | API | `RUTAIA_PUERTO_API` | `API_BASE_URL` en [frontend/js/config.js](frontend/js/config.js); `docker compose up -d n8n` y reiniciar el backend |
| 5500 | Frontend | `RUTAIA_PUERTO_FRONTEND` | El `-p` del comando de `jwebserver`; reiniciar el backend (CORS) |

Los puertos internos de los contenedores (5432, 6333 y 5678, a la derecha de `host:contenedor`) son fijos. Con otra `RUTAIA_PUERTO_API`, «Try it out» de Swagger UI sigue llamando al 8080: prueba con `curl` en el puerto nuevo.

Ver quién ocupa un puerto:
- Windows (PowerShell). Con el `OwningProcess` del primer comando, `Get-Process -Id <PID>` dice qué programa es. El segundo comando muestra los rangos reservados por Hyper-V y WSL, que no se pueden usar:
  ```powershell
  Get-NetTCPConnection -State Listen | Where-Object LocalPort -in 5433,6333,5678,8080,5500
  netsh interface ipv4 show excludedportrange protocol=tcp
  ```
- Linux (sin verificar; con `sudo` muestra el proceso):
  ```bash
  ss -ltnp | grep -E ':(5433|6333|5678|8080|5500)\b'
  ```

Un n8n instalado con `npm` puede escuchar en `[::]:5678` y quedarse con `localhost:5678`: debe aparecer solo `com.docker.backend` (Windows) o `docker-proxy` (Linux).

### Clave de OpenRouter

1. Crea una API key en https://openrouter.ai/settings/keys (*Create API Key*). Empieza por `sk-or-v1-` y solo se muestra una vez.
2. La clave vive **solo** en la credencial «RutaIA OpenRouter» de n8n; nunca en el `.env`, el código ni el repositorio. En http://localhost:5678, crea la cuenta de propietario y luego entra a *Create → Credential → Header Auth* y completa:
   - título `RutaIA OpenRouter`, exacto;
   - Name `Authorization`;
   - Value `Bearer <clave>`, con la palabra `Bearer`, **un espacio** y la clave.
3. **Privacidad de los modelos gratuitos.** En https://openrouter.ai/settings/privacy:
   - deja apagado *Zero Data Retention*;
   - en *Data Training*, enciende «Allow free endpoints that train on request data» y «Allow free endpoints that publish prompts»;
   - pulsa **Save changes**.

   Sin esto, los modelos `:free` responden 404. Como esos proveedores pueden publicar los prompts, a OpenRouter solo viajan la pregunta, el nivel, el área y los datos de los cursos; nunca nombres ni correos.
4. **Crédito.** El LLM es de pago (centavos por consulta). Con crédito comprado, los modelos `:free` admiten 1000 solicitudes por día.
5. **No agregues una clave propia de Google AI Studio** en https://openrouter.ai/settings/integrations, y si ya la tienes, quítala. Con el LLM de pago, OpenRouter usa sus propios proveedores; con esa clave, Google respondía 500 y 504.

## Ejecución de contenedores, backend y frontend

El orden es **contenedores → backend → frontend**, cada uno en su terminal y desde la raíz del repositorio.

Windows, PowerShell (verificado):
```powershell
docker compose up -d
docker compose ps
```
```powershell
cd backend; .\gradlew.bat bootRun
```
```powershell
jwebserver -p 5500 -d "$((Resolve-Path .\frontend).Path)"
```

Linux, bash (sin verificar):
```bash
docker compose up -d
docker compose ps
```
```bash
cd backend && ./gradlew bootRun
```
```bash
jwebserver -p 5500 -d "$(pwd)/frontend"
```

`postgres` debe aparecer `healthy`, y el backend está listo cuando su registro muestra `Tomcat started on port 8080`. El primer arranque crea el esquema y la semilla: 10 estudiantes y 30 cursos, de los que 28 están activos. Para recargarlos a mano, sigue [docs/base-de-datos.md](docs/base-de-datos.md#ejecución-de-los-scripts).

| Componente | URL |
|---|---|
| Frontend | http://localhost:5500 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| n8n | http://localhost:5678 |
| Qdrant | http://127.0.0.1:6333/dashboard |

**Detener:** `docker compose stop` o `docker compose down`, sin `-v`. El backend y `jwebserver` se detienen con Ctrl+C.

## Importación de workflows

Los tres flujos están en `n8n/`, y el contenedor los ve en `/home/node/flujos`. Detalle en [docs/n8n.md](docs/n8n.md). Los comandos son iguales en PowerShell y bash.

1. **Importar:**
   ```bash
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-indexacion-cursos.json
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-calibracion-umbral.json
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-consulta-rag.json
   ```
2. **Asignar la credencial.** Las credenciales no viajan en el JSON, así que n8n pide elegirla. En http://localhost:5678, abre cada flujo y elige «RutaIA OpenRouter» en *Credential for Header Auth*:
   - en «Generar embeddings» de *Indexación de cursos* y de *Calibración de umbral*;
   - en «Embedding de la pregunta» y «Consultar LLM» de *Consulta RAG*.

   Después, guarda cada flujo.
3. **Publicar.** Solo la versión publicada atiende `/webhook/…`. Usa el botón **Publish** de cada flujo, o la CLI y un reinicio:
   ```bash
   docker compose exec n8n n8n publish:workflow --id=RutaIAIndexacion
   docker compose exec n8n n8n publish:workflow --id=RutaIACalibracion
   docker compose exec n8n n8n publish:workflow --id=RutaIAConsultaRAG
   docker compose restart n8n
   docker compose exec n8n n8n list:workflow --active=true
   ```
4. **Indexar los cursos**, con el backend en marcha. Gasta 1 solicitud; repítelo cada vez que cambies el catálogo:
   ```powershell
   curl.exe -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos
   ```
   En bash es el mismo comando con `curl`. La respuesta debe traer `"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30`.

## Endpoints principales y ejemplos de uso

Base `http://localhost:8080/api`, JSON UTF-8. Todo 4xx o 5xx devuelve `{status, error, mensaje, ruta, fecha, detalles}`. La documentación interactiva está en [Swagger UI](http://localhost:8080/swagger-ui.html) y la especificación en [docs/openapi.json](docs/openapi.json).

| ID | Método y ruta | Respuesta | Errores |
|---|---|---|---|
| E-01 | `POST /api/estudiantes` | 201 Estudiante + `Location` | 400 · 409 correo ya registrado |
| E-02 · E-03 | `GET /api/estudiantes` · `GET /api/estudiantes/{id}` | 200 | 404 |
| E-04 | `GET /api/estudiantes/{id}/historial` | 200, fecha descendente | 404 |
| E-05 | `GET /api/cursos?categoria=&nivel=` | 200 cursos activos | 400 nivel inválido |
| E-06 | `POST /api/admin/cursos` | 201 CursoAdmin + `Location` | 400 |
| E-07 · E-08 | `GET /api/admin/cursos` · `GET /api/admin/cursos/{id}` | 200 activos e inactivos | 404 |
| E-09 | `PUT /api/admin/cursos/{id}` | 200 (`activo` no cambia) | 400 · 404 |
| E-10 | `PATCH /api/admin/cursos/{id}/desactivar` | 200 (`activo` false, idempotente) | 404 |
| E-11 | `POST /api/consultas` | 201 en Respondida, Sin resultados o Error | 400 · 404, sin crear la consulta |
| E-12 | `GET /api/consultas/{id}` | 200 E-11 + `calificacion` | 404 |
| E-13 | `POST /api/recomendaciones/{id}/calificacion` | 201 (1–5, comentario opcional ≤ 500) | 400 · 404 · 409 ya calificada |
| E-14 | `GET /api/estadisticas` | 200 totales, promedio y curso más recomendado | — |

Ejemplos desde la raíz en PowerShell, con cuerpos de `pruebas/api/` (en bash, cambia `curl.exe` por `curl`). E-12 y E-13 usan el `consultaId` y el `recomendacionId` que devuelve E-11. E-06 y E-10 cambian el catálogo, así que después hay que re-indexar.
```powershell
curl.exe -s -i -X POST "http://localhost:8080/api/estudiantes" --data-binary "@pruebas/api/estudiante-valido.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/cursos?categoria=Desarrollo%20web&nivel=B%C3%A1sico"
curl.exe -s -i -X POST "http://localhost:8080/api/admin/cursos" --data-binary "@pruebas/api/curso-valido.json" -H "Content-Type: application/json"
curl.exe -s -X PATCH "http://localhost:8080/api/admin/cursos/31/desactivar"
curl.exe -s -i -X POST "http://localhost:8080/api/consultas" --data-binary "@pruebas/api/consulta-cp01-paginas-web.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/estudiantes/1/historial"
curl.exe -s "http://localhost:8080/api/consultas/1"
curl.exe -s -i -X POST "http://localhost:8080/api/recomendaciones/1/calificacion" --data-binary "@pruebas/api/calificacion-con-comentario.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/estadisticas"
```

Ejemplo de E-11 en estado Respondida, tomado de la integración (aquí se recortan la respuesta y la descripción de la fuente):
```json
{"consultaId":7,"estudianteId":1,"pregunta":"Quiero aprender a crear páginas web.","fecha":"2026-11-03T11:07:02","estado":"Respondida",
 "respuesta":"Hola. Como orientador de RutaIA, te recomiendo la siguiente ruta de aprendizaje…","mensaje":null,"recomendacionId":1,
 "fuentes":[{"cursoId":1,"nombre":"Fundamentos de HTML y CSS para páginas web","descripcion":"…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.593}]}
```
La consulta de cocina devuelve `"estado":"Sin resultados","respuesta":null,"mensaje":"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.","fuentes":[]`. Cada E-11 gasta 1 solicitud de embeddings y, si hay fuentes, 1 llamada al LLM.

## Errores conocidos

| Síntoma | Causa | Solución |
|---|---|---|
| OpenRouter **401** | La clave está mal copiada en la credencial, falta `Bearer ` con espacio, o lo pegado no es una API key (por ejemplo, una *Management key*) | Corrige el Value de «RutaIA OpenRouter»: `Bearer sk-or-v1-…` |
| OpenRouter **402** | Saldo negativo, o sin crédito para el LLM de pago | Agrega crédito en *Credits*; los `:free` exigen saldo ≥ 0 |
| OpenRouter **404** en un modelo `:free` | Privacidad de los proveedores gratuitos sin habilitar, o modelo retirado | Habilítala (sección «Clave de OpenRouter»). Si se retiró, cambia `modeloEmbeddings` en «Configuración» de los 3 flujos, re-indexa y recalibra ([docs/n8n.md](docs/n8n.md#calibración)) |
| OpenRouter **429** | Cuota diaria de los `:free` («free-models-per-day», contada por día UTC) o proveedor saturado («rate-limited upstream») | Espera al día siguiente (00:00 UTC) o reintenta más tarde; cada intento gasta solicitudes |
| Google **500** o **504** en el LLM | Hay una clave propia de Google AI Studio en OpenRouter | Quítala en https://openrouter.ai/settings/integrations |
| Las consultas terminan en Error con ECONNREFUSED o ENOTFOUND hacia OpenRouter | Falla de red pasajera | Reintenta |
| `password authentication failed for user "rutaia_app"` | Se cambió `RUTAIA_DB_PASSWORD` después de crear el volumen `pgdata`: la imagen solo la usa la primera vez | Entra con `docker compose exec postgres psql -U rutaia_app -d rutaia` y ejecuta `ALTER USER rutaia_app PASSWORD '<la de tu .env>';`. O recrea solo el volumen `pgdata` ([docs/base-de-datos.md](docs/base-de-datos.md#ejecución-de-los-scripts)), lo que borra los datos |
| `required variable RUTAIA_DB_PASSWORD is missing a value` | No existe `.env` o la contraseña está vacía | Crea el `.env` desde `.env.example` |
| Un puerto cambiado en `.env` no surte efecto | El `.env` viene del ejemplo anterior (`RUTAIA_DB_URL`, `RUTAIA_N8N_WEBHOOK_URL`, `RUTAIA_CORS_ORIGINS`), que ya no se lee | Borra esas 3 líneas y usa las `RUTAIA_PUERTO_*` |
| `port is already allocated` o `address already in use` | Otro programa usa el puerto | Cámbialo con su variable (sección «Puertos») o detén ese programa |
| Windows: `An attempt was made to access a socket in a way forbidden by its access permissions` | El puerto está dentro de un rango reservado por Hyper-V o WSL | Elige otro fuera de los rangos de `netsh interface ipv4 show excludedportrange protocol=tcp` |
| Las consultas terminan en Error con «La API (GET /cursos) no respondió» | n8n no alcanza la API: lo bloquea el firewall, o el contenedor n8n se creó con otro `RUTAIA_PUERTO_API` | Windows: permite Java en redes privadas. Linux con ufw: `sudo ufw allow from 172.16.0.0/12 to any port 8080 proto tcp`. Recrea n8n con `docker compose up -d n8n` y comprueba con `docker compose exec n8n printenv RUTAIA_API_URL` |
| Desaparecen los flujos, la credencial, la colección y los datos | `docker compose down -v` borra los volúmenes `pgdata`, `qdrant_storage` y `n8n_data` | Usa `stop` o `down` sin `-v`. Si ya pasó, recrea la credencial, importa, publica y re-indexa |
| Linux: `EACCES: permission denied` en `/home/node/flujos` | El contenedor n8n corre con UID 1000 y `./n8n` es de otro usuario | `sudo setfacl -R -m u:1000:rwX -m d:u:1000:rwX n8n` (o `chmod -R a+rwX n8n`) |
| Git Bash: `import:workflow` no encuentra `C:/Program Files/Git/home/...` | Git Bash convierte las rutas que empiezan con `/` | Usa PowerShell o antepón `MSYS_NO_PATHCONV=1` |
| PowerShell: `curl -s …` falla con «No se encuentra ningún parámetro…» | En Windows PowerShell 5.1, `curl` es un alias de `Invoke-WebRequest` | Usa `curl.exe` |
| Linux: `./gradlew: Permission denied` o `/usr/bin/env: 'sh\r'` | `gradlew` perdió el permiso de ejecución o tiene finales CRLF | `chmod +x backend/gradlew`; para el CRLF, vuelve a clonar con Git (`.gitattributes` fija `eol=lf`) o `sed -i 's/\r$//' backend/gradlew` |
| Un curso desactivado sigue con `activo: true` en Qdrant | La indexación no se sincroniza sola | No se recomienda igual (el flujo lo descarta contra la API). Re-indexa para dejar Qdrant al día |
| La recomendación tarda 30–40 s | El proveedor que asigna OpenRouter respondió lento | Es normal: el límite es 60 s en n8n y 90 s en la API (`RUTAIA_N8N_TIMEOUT_SEGUNDOS`) |
