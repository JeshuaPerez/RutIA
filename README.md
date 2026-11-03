# RutaIA

Orientación académica con inteligencia artificial: el estudiante describe en lenguaje natural lo que necesita aprender y RutaIA le recomienda **cursos existentes** del catálogo institucional, explicando por qué y en qué orden tomarlos.

## 1. Nombre del proyecto e integrantes

| Dato | Valor |
|---|---|
| Proyecto | **RutaIA** — recomendación de cursos con una arquitectura RAG |
| Integrante | Jeshua Perez (proyecto individual: hizo todas las etapas) |
| Repositorio | https://github.com/JeshuaPerez/RutIA |

## 2. Descripción de la problemática y solución propuesta

**Problemática.** En un catálogo institucional con decenas de cursos, el estudiante no siempre sabe qué nombre tiene lo que busca, qué cursos tratan su tema ni por cuál empezar según su nivel. Un buscador por palabras falla cuando la necesidad está escrita con otras palabras («proteger aplicaciones web» frente a «OWASP Top 10»). Un chatbot genérico, en cambio, puede inventar cursos que la institución no ofrece.

**Solución propuesta.** Una aplicación web en la que el estudiante se registra con su nivel y su área de interés, escribe su necesidad y recibe una recomendación generada con **RAG** (*Retrieval-Augmented Generation*):

1. La API guarda la consulta y la envía a un flujo de n8n.
2. El flujo convierte la pregunta en un vector (embedding) y busca en Qdrant los cursos activos más parecidos que superen un **umbral de similitud calibrado (0,34)**.
3. Si no hay cursos pertinentes, responde «Sin resultados» sin llamar al modelo de lenguaje. Si los hay, el LLM redacta la recomendación **usando solo esos cursos**. La respuesta se valida para que no nombre cursos ajenos.
4. La API guarda la recomendación con sus fuentes y su similitud. El estudiante ve el resultado, su historial, y puede calificar cada recomendación de 1 a 5.

## 3. Tecnologías y arquitectura

| Capa | Tecnología |
|---|---|
| Frontend | HTML, CSS y JavaScript nativos (módulos ES, Fetch, `async`/`await`), servidos con `jwebserver` (JDK 21) en el puerto 5500 |
| Backend | Java 21, Spring Boot 4.1.2 (Web MVC, Data JPA, Validation), Gradle, springdoc-openapi 3.2.0 (Swagger). Es la **única puerta de entrada** |
| Base relacional | PostgreSQL 17 (contenedor), base `rutaia` |
| Base vectorial | Qdrant v1.20.0 (contenedor), colección `rutaia_cursos` (2048 dimensiones, Cosine) |
| Automatización | n8n 2.45.7 (contenedor): indexación, calibración y consulta RAG |
| IA (OpenRouter) | Embeddings `nvidia/nemotron-3-embed-1b:free` (capa gratuita). LLM `google/gemma-4-31b-it`, variante de pago del modelo del contrato, con crédito de OpenRouter (menos de 0,001 USD por consulta; ver «Errores conocidos») |
| Infraestructura | Docker Compose v2 ([docker-compose.yml](docker-compose.yml)): PostgreSQL, Qdrant y n8n. El backend y el frontend corren en la máquina |

```text
Navegador → Frontend :5500 → API Spring Boot :8080 ─┬─ PostgreSQL :5433
                                                     └─ n8n :5678 (webhook) ─┬─ GET /api/cursos (activos)
                                                                             ├─ OpenRouter: embedding de la pregunta
                                                                             ├─ Qdrant :6333: búsqueda con umbral
                                                                             └─ OpenRouter: LLM con los cursos recuperados
```

Documentación detallada:
- **Diagrama de arquitectura**, puertos y flujos: [docs/arquitectura.md](docs/arquitectura.md)
- **Diagrama entidad-relación**, tablas, restricciones e índices: [docs/base-de-datos.md](docs/base-de-datos.md)
- Flujos de n8n: [docs/n8n.md](docs/n8n.md) · Umbral: [docs/calibracion-umbral.md](docs/calibracion-umbral.md) · Frontend: [docs/frontend.md](docs/frontend.md)
- Contrato técnico C1–C12 y su registro de cambios: [docs/contrato.md](docs/contrato.md)
- Pruebas: [docs/pruebas-funcionales.md](docs/pruebas-funcionales.md) · Informe de integración: [docs/informe-integracion.md](docs/informe-integracion.md) · Guion de la presentación: [docs/presentacion-final.md](docs/presentacion-final.md)

## 4. Requisitos de instalación y variables de entorno

En la máquina solo hacen falta **Docker** (Docker Desktop con WSL2 en Windows; Docker Engine con el plugin Compose en Linux), **JDK 21** (incluye `jwebserver`) y **Git**. PostgreSQL, Qdrant y n8n no se instalan aparte. Instalación paso a paso: [docs/entorno-local.md](docs/entorno-local.md#1-requisitos-e-instalación).

Además hace falta una **cuenta de OpenRouter** con:
- una API key, que se guarda solo en la credencial de n8n;
- la privacidad de los proveedores gratuitos habilitada ([guía](docs/entorno-local.md#4-primera-configuración-de-n8n-y-openrouter));
- **crédito** para el LLM de pago. Con 10 créditos comprados, el tope de los modelos `:free` sube de 50 a 1000 solicitudes por día.

**Variables.** Copia [.env.example](.env.example) como `.env` en la raíz y escribe tu propia contraseña en `RUTAIA_DB_PASSWORD`. El `.env` no se versiona. Los valores van sin comillas, espacios ni barras invertidas.

PowerShell:
```powershell
git clone https://github.com/JeshuaPerez/RutIA.git; cd RutIA
Copy-Item .env.example .env
notepad .env
```
bash:
```bash
git clone https://github.com/JeshuaPerez/RutIA.git && cd RutIA
cp .env.example .env
nano .env
```

| Variable | Valor de `.env.example` | Secreta | La usan |
|---|---|---|---|
| `RUTAIA_DB_USER` | `rutaia_app` | no | Compose (`POSTGRES_USER`) y Spring Boot |
| `RUTAIA_DB_PASSWORD` | *(vacía: la eliges tú)* | **sí** | Compose (`POSTGRES_PASSWORD`) y Spring Boot |
| `RUTAIA_DB_URL` | `jdbc:postgresql://127.0.0.1:5433/rutaia` | no | Spring Boot |
| `RUTAIA_N8N_WEBHOOK_URL` | `http://127.0.0.1:5678/webhook/rutaia-consulta-rag` | no | Spring Boot |
| `RUTAIA_N8N_TIMEOUT_SEGUNDOS` | `90` | no | Spring Boot (read timeout hacia n8n) |
| `RUTAIA_CORS_ORIGINS` | `http://localhost:5500,http://127.0.0.1:5500` | no | Spring Boot (CORS de `/api/**`) |

Docker Compose lee el `.env` solo. Spring Boot lo importa con `spring.config.import=optional:file:../.env[.properties]`, porque se ejecuta desde `backend/`. La clave de OpenRouter **no** va en el `.env`.

## 5. Ejecución de contenedores, backend y frontend

Orden: **contenedores → backend → frontend**, cada uno en su terminal y desde la raíz del repositorio.

PowerShell:
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

bash:
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

`postgres` debe aparecer `healthy`. El backend termina de arrancar cuando el registro muestra `Tomcat started on port 8080`.

| Componente | URL |
|---|---|
| Frontend | http://localhost:5500 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html (especificación exportada en [docs/openapi.json](docs/openapi.json)) |
| n8n | http://localhost:5678 |
| Qdrant | http://127.0.0.1:6333/dashboard |

**Base de datos.** Con el volumen `pgdata` vacío, PostgreSQL ejecuta solo `database/01_esquema.sql` y `database/02_datos_semilla.sql`: 10 estudiantes, 30 cursos (28 activos), 7 categorías y 3 niveles. Para recargarlos a mano, el comando es igual en ambos sistemas. Borra todas las consultas; después re-indexa (sección 6).
```bash
docker compose exec postgres psql -U rutaia_app -d rutaia -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/01_esquema.sql
docker compose exec postgres psql -U rutaia_app -d rutaia -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/02_datos_semilla.sql
```

**Detener:** `docker compose stop` o `docker compose down`. **Nunca `docker compose down -v`** salvo para empezar de cero (ver «Errores conocidos»). El backend y `jwebserver` se detienen con Ctrl+C.

## 6. Importación de workflows

Los flujos están en `n8n/`, y el contenedor los ve en `/home/node/flujos`:

| Archivo | ID | Webhook | Propósito |
|---|---|---|---|
| `rutaia-indexacion-cursos.json` | `RutaIAIndexacion` | `POST /webhook/rutaia-indexar-cursos` | W-2: lee los cursos de la API, genera sus embeddings y los guarda en Qdrant |
| `rutaia-calibracion-umbral.json` | `RutaIACalibracion` | `POST /webhook/rutaia-calibrar-umbral` | W-3: herramienta auxiliar para calibrar el umbral |
| `rutaia-consulta-rag.json` | `RutaIAConsultaRAG` | `POST /webhook/rutaia-consulta-rag` | W-1: atiende las consultas de la API (solo la llama Spring Boot) |

Los comandos de los pasos 2, 4, 5 y 6 son iguales en PowerShell y en bash.

1. **Credencial.** En http://localhost:5678, crea la cuenta de propietario y luego *Create → Credential → Header Auth*:
   - título `RutaIA OpenRouter`;
   - Name `Authorization`;
   - Value `Bearer <tu clave>`.

   Detalle en [docs/entorno-local.md](docs/entorno-local.md#4-primera-configuración-de-n8n-y-openrouter).
2. **Importar** los tres flujos:
   ```bash
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-indexacion-cursos.json
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-calibracion-umbral.json
   docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-consulta-rag.json
   docker compose exec n8n n8n list:workflow
   ```
3. **Asignar la credencial.** Los JSON la referencian con el ID de la máquina donde se crearon. En tu n8n, abre cada flujo y elige tu credencial «RutaIA OpenRouter»:
   - en «Generar embeddings», en Indexación y en Calibración;
   - en «Embedding de la pregunta» y «Consultar LLM», en Consulta RAG.

   Guarda cada flujo.
4. **Publicar y reiniciar.** En n8n 2.x solo la versión publicada atiende `/webhook/…`. También puedes usar el botón **Publish** del editor; en ese caso no hace falta reiniciar.
   ```bash
   docker compose exec n8n n8n publish:workflow --id=RutaIAIndexacion
   docker compose exec n8n n8n publish:workflow --id=RutaIACalibracion
   docker compose exec n8n n8n publish:workflow --id=RutaIAConsultaRAG
   docker compose restart n8n
   docker compose exec n8n n8n list:workflow --active=true
   ```
5. **Indexar los cursos.** Usa 1 solicitud de embeddings. Repítelo cada vez que cargues, cambies o desactives cursos:
   - PowerShell: `curl.exe -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos`
   - bash: `curl -s -X POST http://127.0.0.1:5678/webhook/rutaia-indexar-cursos`

   La respuesta esperada es:
   ```json
   {"coleccion":"rutaia_cursos","coleccionCreada":true,"cursosLeidos":30,"puntosGuardados":30,"activos":28,"inactivos":2,"totalPuntos":30}
   ```
   `coleccionCreada` vale `true` la primera vez y `false` en las siguientes.
6. **Exportar (opcional).** Para guardar en el repositorio la versión publicada:
   ```bash
   docker compose exec n8n n8n export:workflow --id=<ID> --published --output=/home/node/flujos/<archivo>.json --pretty
   ```
   Antes de versionar, quita el bloque `"shared"`, que trae el correo del propietario.

## 7. Endpoints principales y ejemplos de uso

Base `http://localhost:8080/api`, JSON UTF-8, fechas ISO-8601 en hora local. Todo 4xx o 5xx devuelve `{status, error, mensaje, ruta, fecha, detalles}`. La documentación interactiva está en http://localhost:8080/swagger-ui.html.

| ID | Método y ruta | Respuesta | Errores |
|---|---|---|---|
| E-01 | `POST /api/estudiantes` | 201 Estudiante + `Location` | 400 · 409 correo ya registrado |
| E-02 | `GET /api/estudiantes` | 200 lista, id asc | — |
| E-03 | `GET /api/estudiantes/{id}` | 200 Estudiante | 404 |
| E-04 | `GET /api/estudiantes/{id}/historial` | 200 lista de HistorialItem, fecha desc (`[]` si no hay) | 404 |
| E-05 | `GET /api/cursos?categoria=&nivel=` | 200 cursos activos, id asc | 400 nivel inválido |
| E-06 | `POST /api/admin/cursos` | 201 CursoAdmin (`activo` = true) + `Location` | 400 |
| E-07 | `GET /api/admin/cursos` | 200 activos e inactivos, id asc | — |
| E-08 | `GET /api/admin/cursos/{id}` | 200 CursoAdmin | 404 |
| E-09 | `PUT /api/admin/cursos/{id}` | 200 CursoAdmin (`activo` no cambia) | 400 · 404 |
| E-10 | `PATCH /api/admin/cursos/{id}/desactivar` | 200 CursoAdmin (`activo` = false; idempotente) | 404 |
| E-11 | `POST /api/consultas` | 201 ConsultaResultado + `Location` en cualquier estado final (Respondida, Sin resultados o Error) | 400 · 404 (sin crear la consulta ni llamar a n8n) |
| E-12 | `GET /api/consultas/{id}` | 200 ConsultaDetalle (E-11 + `calificacion`) | 404 |
| E-13 | `POST /api/recomendaciones/{id}/calificacion` | 201 Calificacion + `Location` (1–5, comentario opcional ≤ 500) | 400 · 404 · 409 ya calificada |
| E-14 | `GET /api/estadisticas` | 200 totales, promedio (2 decimales o null) y curso más recomendado (o null) | — |

Ejemplos desde la raíz, con cuerpos en `pruebas/api/` (UTF-8 sin BOM). En PowerShell usa `curl.exe`, no el alias `curl`.

PowerShell:
```powershell
curl.exe -s -i -X POST "http://localhost:8080/api/estudiantes" --data-binary "@pruebas/api/etapa2-estudiante-valido.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/cursos?categoria=Desarrollo%20web&nivel=B%C3%A1sico"
curl.exe -s -i -X POST "http://localhost:8080/api/consultas" --data-binary "@pruebas/api/etapa6-01-paginas-web.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/estudiantes/1/historial"
curl.exe -s "http://localhost:8080/api/consultas/7"
curl.exe -s -i -X POST "http://localhost:8080/api/recomendaciones/1/calificacion" --data-binary "@pruebas/api/etapa7-01-calificacion-con-comentario.json" -H "Content-Type: application/json"
curl.exe -s "http://localhost:8080/api/estadisticas"
```

bash:
```bash
curl -s -i -X POST "http://localhost:8080/api/estudiantes" --data-binary "@pruebas/api/etapa2-estudiante-valido.json" -H "Content-Type: application/json"
curl -s "http://localhost:8080/api/cursos?categoria=Desarrollo%20web&nivel=B%C3%A1sico"
curl -s -i -X POST "http://localhost:8080/api/consultas" --data-binary "@pruebas/api/etapa6-01-paginas-web.json" -H "Content-Type: application/json"
curl -s "http://localhost:8080/api/estudiantes/1/historial"
curl -s "http://localhost:8080/api/consultas/7"
curl -s -i -X POST "http://localhost:8080/api/recomendaciones/1/calificacion" --data-binary "@pruebas/api/etapa7-01-calificacion-con-comentario.json" -H "Content-Type: application/json"
curl -s "http://localhost:8080/api/estadisticas"
```

Ejemplo de E-11 con estado Respondida (la descripción de la fuente se recorta aquí):
```json
{"consultaId":7,"estudianteId":1,"pregunta":"Quiero aprender a crear páginas web.","fecha":"2026-11-03T11:07:02","estado":"Respondida",
 "respuesta":"Hola. Como orientador de RutaIA, te recomiendo la siguiente ruta de aprendizaje…","mensaje":null,"recomendacionId":1,
 "fuentes":[{"cursoId":1,"nombre":"Fundamentos de HTML y CSS para páginas web","descripcion":"…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.593}]}
```
Ejemplo de Sin resultados (consulta de cocina): `"estado":"Sin resultados","respuesta":null,"mensaje":"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.","fuentes":[]`.

Cada E-11 gasta 1 solicitud de embeddings y, si hay fuentes, 1 llamada al LLM. Los cuerpos de las 10 consultas de prueba están en `pruebas/api/etapa6-01..10-*.json`.

## 8. Errores conocidos

| Error | Causa | Solución |
|---|---|---|
| OpenRouter **429** «Rate limit exceeded: free-models-per-day» | Cuota de la capa gratuita: **50 solicitudes por día y cuenta** (20 por minuto) en los modelos `:free`, contadas por día UTC. Los embeddings son `:free` | Esperar al día siguiente (00:00 UTC) o comprar 10 créditos en total para subir el tope a 1000 por día. En la integración se alcanzó con 5 créditos comprados |
| OpenRouter **429** «… is temporarily rate-limited upstream» | El proveedor gratuito del modelo está saturado para todos (no es tu cuota) | Reintentar más tarde o usar la variante de pago del modelo (ver la siguiente fila) |
| OpenRouter **502** «Provider returned error» (Google 500 INTERNAL) o **504** «The operation was aborted» en el LLM | Con `gemma-…:free`, la capacidad compartida estaba en 429 y Google AI Studio fallaba también con la clave propia (BYOK). Pasó el 2026-11-03 y bloqueó la integración | Por eso el LLM es la variante de pago `google/gemma-4-31b-it`, que exige crédito en OpenRouter |
| OpenRouter **404** en modelos `:free` | Privacidad de proveedores gratuitos no habilitada | Habilitarla en https://openrouter.ai/settings/privacy ([guía](docs/entorno-local.md#4-primera-configuración-de-n8n-y-openrouter)) |
| OpenRouter **402** | Saldo negativo, o sin saldo para el LLM de pago | Revisar *Credits* en OpenRouter y agregar crédito; los `:free` exigen saldo ≥ 0 |
| Un modelo gratuito se retira o deja de tener proveedores | OpenRouter retira variantes `:free` sin aviso | Alternativas de C10: embeddings `nvidia/nemotron-3.5-embed-1b:free` (también 2048; obliga a re-indexar y recalibrar el umbral). Para el LLM, `nvidia/nemotron-3-super-120b-a12b:free` ya no existe (2026-11-03), así que se usa la variante de pago `google/gemma-4-31b-it`. Solo cambia el nodo «Configuración»; el cambio se registra en [docs/contrato.md](docs/contrato.md) |
| Las consultas terminan en Error con «La API (GET /cursos) no respondió», o `wget` a `host.docker.internal:8080` no conecta | n8n no alcanza la API de la máquina por `host.docker.internal`: lo bloquea el firewall | Windows: permitir Java en redes privadas (o `New-NetFirewallRule -DisplayName "RutaIA Java 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private`). Linux con ufw: `sudo ufw allow from 172.16.0.0/12 to any port 8080 proto tcp`. El compose ya trae `extra_hosts: host.docker.internal:host-gateway`. Prueba en [docs/entorno-local.md](docs/entorno-local.md#conectividad-del-contenedor-n8n-hacia-la-máquina) |
| `port is already allocated` (5433, 6333, 5678, 8080 o 5500) | Puertos ocupados por otro proceso. Ojo: un n8n instalado con npm puede tomar `[::]:5678` y «robar» `localhost:5678` | Windows: `Get-NetTCPConnection -LocalPort 5678 -State Listen` y `Get-Process -Id <PID>`. Linux: `sudo ss -ltnp 'sport = :5678'`. Detén ese proceso |
| Desaparecen los flujos, la credencial, la colección y los datos | Se ejecutó `docker compose down -v`, que borra los volúmenes `pgdata`, `qdrant_storage` y `n8n_data` | Usar `docker compose stop` o `down` sin `-v`. Si ya pasó: recrear la credencial, importar y publicar los flujos, y re-indexar (sección 6) |
| Linux: `EACCES: permission denied` al exportar flujos a `/home/node/flujos` | El contenedor n8n corre con UID 1000 y la carpeta `./n8n` es de otro usuario | `sudo setfacl -R -m u:1000:rwX -m d:u:1000:rwX n8n` (o `chmod -R a+rwX n8n`) |
| PowerShell: `curl -s …` falla con «No se encuentra ningún parámetro…» | En Windows PowerShell 5.1, `curl` es un alias de `Invoke-WebRequest` | Usar `curl.exe` |
| Linux: `./gradlew: Permission denied` o `/usr/bin/env: 'sh\r': No such file or directory` | `gradlew` perdió el permiso de ejecución (por ejemplo, si se descargó como ZIP) o tiene finales de línea CRLF | `chmod +x backend/gradlew`. Para el CRLF: volver a clonar con Git (`.gitattributes` fija `eol=lf`) o `sed -i 's/\r$//' backend/gradlew` |
| El backend no arranca: falta `RUTAIA_DB_PASSWORD` o `password authentication failed` | No existe el `.env`, la contraseña está vacía o el volumen `pgdata` se creó con otra contraseña | Crear el `.env` (sección 4) o restaurar la contraseña anterior |
| Un curso desactivado sigue con `activo: true` en Qdrant | La indexación no se sincroniza sola | No se recomienda igual, porque el flujo RAG descarta los cursos que no están en `GET /api/cursos`. Re-indexa (sección 6) para dejar Qdrant al día. Para reactivar un curso no hay endpoint: `UPDATE curso SET activo = true WHERE id = …;` y re-indexar |
| La recomendación tarda 30–40 s | El proveedor de pago que asigna OpenRouter respondió lento (se vio una vez en la integración) | Es normal. El límite es 60 s en n8n y 90 s en la API (`RUTAIA_N8N_TIMEOUT_SEGUNDOS`) |

Lista completa con diagnósticos: [docs/entorno-local.md](docs/entorno-local.md#8-problemas-comunes).
