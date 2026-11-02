# Entorno local de RutaIA

Guía para levantar RutaIA en **Windows 10/11 (PowerShell)** o **Linux (bash)**. PostgreSQL, Qdrant y n8n corren en Docker Compose; el backend (Spring Boot) y el frontend (jwebserver) corren en la máquina.

> En Windows usa siempre `curl.exe` (en PowerShell 5.1, `curl` es un alias de `Invoke-WebRequest`). Los cuerpos JSON van en archivos UTF-8 sin BOM y se envían con `--data-binary "@archivo.json" -H "Content-Type: application/json"`.

## 1. Requisitos e instalación

En la máquina solo hacen falta **Docker** (con Compose v2), **JDK 21** (incluye `jwebserver`) y **Git**. PostgreSQL, Qdrant y n8n **no** se instalan aparte.

### Windows (PowerShell como administrador)

```powershell
wsl --install --no-distribution          # WSL2; puede pedir reiniciar
winget install -e --id Docker.DockerDesktop
winget install -e --id EclipseAdoptium.Temurin.21.JDK
winget install -e --id Git.Git
```

Después de instalar: reinicia si WSL lo pidió, abre Docker Desktop una vez (acepta su licencia y comprueba en *Settings → General* que está activo *Use the WSL 2 based engine*) y abre una terminal nueva para que el `PATH` se actualice.

### Linux (Ubuntu/Debian; bash)

```bash
# Docker Engine + plugin Compose (repositorio oficial de Docker)
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"     # cierra sesión y vuelve a entrar
# JDK 21 y Git
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk git
```

En otras distribuciones, instala Docker Engine según https://docs.docker.com/engine/install/ y un JDK 21 (Temurin, OpenJDK).

### Verificación de la máquina

PowerShell:

```powershell
docker version
docker compose version
java -version
jwebserver --version
git --version
wsl --status
wsl -l -v          # docker-desktop debe aparecer con VERSION 2
```

bash:

```bash
docker version
docker compose version
java -version
jwebserver --version
git --version
```

Resultado en la máquina de referencia (Windows 11 Home 10.0.26200, 2026-11-01/02). Docker, JDK y WSL no estaban instalados y se instalaron con los comandos anteriores; WSL estaba dañado y se reparó como indica «Problemas comunes».

| Comando | Resultado |
|---|---|
| `docker version` | Client 29.9.2 · Server Docker Desktop 4.97.0 (Engine 29.9.2, linux/amd64) |
| `docker compose version` | Docker Compose version v5.6.1 (plugin `docker compose`) |
| `java -version` | openjdk version "21.0.13" 2026-10-20 LTS (Temurin-21.0.13+9) |
| `jwebserver --version` | jwebserver 21.0.13 |
| `git --version` | git version 2.56.0.windows.3 |
| WSL2 (`wsl --version`, `wsl -l -v`) | WSL 2.7.14.0, kernel 6.18.33.2-2; distribución `docker-desktop` en VERSION 2 |

## 2. Obtener el repositorio y crear el `.env`

Hay un único `.env` en la raíz del repositorio. **No se versiona**; se crea a partir de `.env.example`. Valores sin comillas, sin espacios y sin `\`.

PowerShell:

```powershell
git clone <url-del-repositorio> rutaia
cd rutaia
Copy-Item .env.example .env
notepad .env       # escribe tu contraseña en RUTAIA_DB_PASSWORD=
```

bash:

```bash
git clone <url-del-repositorio> rutaia
cd rutaia
cp .env.example .env
nano .env          # escribe tu contraseña en RUTAIA_DB_PASSWORD=
```

| Variable | Valor | Secreta |
|---|---|---|
| RUTAIA_DB_USER | rutaia_app | no |
| RUTAIA_DB_PASSWORD | la eliges tú (solo letras y números es lo más seguro) | **sí** |
| RUTAIA_DB_URL | jdbc:postgresql://127.0.0.1:5433/rutaia | no |
| RUTAIA_N8N_WEBHOOK_URL | http://127.0.0.1:5678/webhook/rutaia-consulta-rag | no |
| RUTAIA_N8N_TIMEOUT_SEGUNDOS | 90 | no |
| RUTAIA_CORS_ORIGINS | http://localhost:5500,http://127.0.0.1:5500 | no |

Comprueba que Git ignora el archivo (debe imprimir `.env`):

```bash
git check-ignore .env
```

> La contraseña de PostgreSQL se fija la **primera vez** que se crea el volumen `pgdata`. Si la cambias después en `.env`, no se aplica a la base existente (ver «Empezar de cero»).

## 3. Docker Compose

`docker-compose.yml` (raíz, proyecto `rutaia`) define tres servicios con etiquetas exactas:

| Servicio | Imagen | Puerto en la máquina | Volúmenes |
|---|---|---|---|
| postgres | `postgres:17` | 127.0.0.1:5433 → 5432 | `pgdata`, `./database` → `/docker-entrypoint-initdb.d` (solo lectura) |
| qdrant | `qdrant/qdrant:v1.20.0` | 127.0.0.1:6333 | `qdrant_storage` |
| n8n | `docker.n8n.io/n8nio/n8n:2.45.7` | 127.0.0.1:5678 | `n8n_data`, `./n8n` → `/home/node/flujos` |

Etiquetas verificadas el 2026-11-01: Qdrant v1.20.0 es la última release estable de https://github.com/qdrant/qdrant/releases (22/10/2026) y n8n 2.45.7 es la release `stable` de https://github.com/n8n-io/n8n/releases (30/10/2026; coincide con la etiqueta `stable` de Docker Hub). Nunca se usa `latest`.

Los comandos son iguales en PowerShell y en bash (ejecútalos desde la raíz del repositorio):

```bash
# Levantar, ver estado, ver registros
docker compose up -d
docker compose ps
docker compose logs -f n8n

# Detener (conserva los datos)
docker compose stop
docker compose down
```

### SQL a mano

Con el volumen `pgdata` vacío, PostgreSQL ejecuta solo `database/01_esquema.sql` y `database/02_datos_semilla.sql`, en ese orden. Si el volumen ya existía (por ejemplo, lo creaste antes de que existieran esos archivos), aplícalos a mano:

```bash
docker compose exec postgres psql -U rutaia_app -d rutaia -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/01_esquema.sql
docker compose exec postgres psql -U rutaia_app -d rutaia -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/02_datos_semilla.sql
```

### CLI de n8n

```bash
docker compose exec n8n n8n import:workflow --input=/home/node/flujos/<archivo>.json
docker compose exec n8n n8n list:workflow
docker compose exec n8n n8n publish:workflow --id=<ID>
docker compose exec n8n n8n export:workflow --id=<ID> --output=/home/node/flujos/<archivo>.json --pretty
```

En n8n 2.x los flujos se **publican** (Publish); solo la versión publicada atiende la URL de producción `/webhook/…`.

### Empezar de cero

```bash
docker compose down -v
```

**Cuidado:** `-v` borra la base de datos, la colección de Qdrant y los flujos y credenciales de n8n. Úsalo solo para empezar de cero.

## 4. Primera configuración de n8n y OpenRouter

0. Comprueba que el puerto 5678 es del contenedor y no de otro n8n instalado en la máquina (ver «Problemas comunes»):
   - PowerShell: `Get-NetTCPConnection -LocalPort 5678 -State Listen` → solo `com.docker.backend`.
   - bash: `sudo ss -ltnp 'sport = :5678'` → solo `docker-proxy`.
1. Abre http://localhost:5678 y crea la **cuenta de propietario** (correo, nombre y contraseña propios; quedan solo en tu volumen `n8n_data`).
2. Crea una API key en https://openrouter.ai/settings/keys → *Create API Key* y cópiala en ese momento (solo se muestra una vez). Empieza por `sk-or-v1-` y tiene unos 73 caracteres. No sirven las *Management keys* ni tokens de sesión.
3. Crea la credencial en n8n: *Overview → Create → Credential* → busca **Header Auth**:
   - Título (arriba a la izquierda, aparece como «Header Auth account»; haz clic para renombrarlo): `RutaIA OpenRouter` (exacto, con «I» mayúscula)
   - Name: `Authorization`
   - Value: `Bearer sk-or-v1-…` (la palabra `Bearer`, un espacio y la clave)
   - Allowed HTTP Request Domains: `All`
   Guarda. La clave solo vive en esta credencial; nunca en código, flujos exportados ni documentos.
4. En OpenRouter, https://openrouter.ai/settings/privacy:
   - *Zero Data Retention*: deja todos los interruptores **apagados** (si no, se rechazan los modelos gratuitos).
   - *Data Training*: **enciende** «Allow free endpoints that train on request data» y «Allow free endpoints that publish prompts»; deja apagados «Allow paid endpoints…» y «Allow 1% data discount…». Pulsa **Save changes**.
   Sin esto, las rutas `:free` responden 404. Como los proveedores gratuitos pueden publicar los prompts, a OpenRouter solo viajan la pregunta, el nivel, el área y los datos de los cursos; nunca nombres ni correos.

Comprobación (no muestra la clave): debe listar `RutaIA OpenRouter`.

```bash
docker compose exec n8n node -e "const {DatabaseSync}=require('node:sqlite'); console.log(new DatabaseSync('/home/node/.n8n/database.sqlite',{readOnly:true}).prepare('select name, type from credentials_entity').all())"
```

## 5. Verificación de los modelos de OpenRouter (2 solicitudes)

La capa gratuita permite 20 solicitudes por minuto y 50 por día por cuenta: esta verificación gasta **2**. La clave se define **solo en la sesión** de la terminal (nunca en archivos) y se borra al terminar.

PowerShell (Windows PowerShell 5.1 o PowerShell 7):

```powershell
$s = Read-Host "Clave de OpenRouter" -AsSecureString
$env:OPENROUTER_API_KEY = [Runtime.InteropServices.Marshal]::PtrToStringBSTR([Runtime.InteropServices.Marshal]::SecureStringToBSTR($s))
"Largo: $($env:OPENROUTER_API_KEY.Length) · prefijo correcto: $($env:OPENROUTER_API_KEY.StartsWith('sk-or-v1-'))"   # debe ser ~73 y True

# 1) Embeddings (input_type=query): debe imprimir 2048
curl.exe -s https://openrouter.ai/api/v1/embeddings -H "Authorization: Bearer $env:OPENROUTER_API_KEY" -H "Content-Type: application/json" --data-binary "@pruebas/n8n/openrouter-embeddings-query.json" -o "$env:TEMP\rutaia-emb.json"
$e = Get-Content "$env:TEMP\rutaia-emb.json" -Raw | ConvertFrom-Json
if ($e.error) { $e.error } else { $e.data[0].embedding.Count }

# 2) Chat con google/gemma-4-31b-it:free: debe imprimir texto
curl.exe -s https://openrouter.ai/api/v1/chat/completions -H "Authorization: Bearer $env:OPENROUTER_API_KEY" -H "Content-Type: application/json" --data-binary "@pruebas/n8n/openrouter-chat.json" -o "$env:TEMP\rutaia-chat.json"
$c = Get-Content "$env:TEMP\rutaia-chat.json" -Raw -Encoding UTF8 | ConvertFrom-Json
if ($c.error) { $c.error } else { $c.choices[0].message.content }

Remove-Item Env:OPENROUTER_API_KEY; Remove-Variable s
```

bash (usa `python3`, presente en casi todas las distribuciones):

```bash
read -rs -p "Clave de OpenRouter: " OPENROUTER_API_KEY; echo; export OPENROUTER_API_KEY
echo "Largo: ${#OPENROUTER_API_KEY} · prefijo: ${OPENROUTER_API_KEY:0:9}"   # debe ser ~73 y sk-or-v1-

# 1) Embeddings (input_type=query): debe imprimir 2048
curl -s https://openrouter.ai/api/v1/embeddings -H "Authorization: Bearer $OPENROUTER_API_KEY" -H "Content-Type: application/json" --data-binary "@pruebas/n8n/openrouter-embeddings-query.json" \
  | python3 -c 'import sys,json,math; r=json.load(sys.stdin); v=r["data"][0]["embedding"] if "data" in r else None; print(r.get("error") if v is None else (len(v), all(math.isfinite(x) for x in v)))'

# 2) Chat con google/gemma-4-31b-it:free: debe imprimir texto
curl -s https://openrouter.ai/api/v1/chat/completions -H "Authorization: Bearer $OPENROUTER_API_KEY" -H "Content-Type: application/json" --data-binary "@pruebas/n8n/openrouter-chat.json" \
  | python3 -c 'import sys,json; r=json.load(sys.stdin); print(r.get("error") or r["choices"][0]["message"]["content"])'

unset OPENROUTER_API_KEY
```

Resultado registrado: ver la sección «Etapa 0» de `docs/pruebas-funcionales.md`.

## 6. Arranque del backend y del frontend

**Orden de arranque:** contenedores → backend → frontend.

1. Contenedores (raíz del repositorio): `docker compose up -d` y espera a que `docker compose ps` muestre `postgres` como `healthy`.
2. Backend (dentro de `backend/`; Spring Boot lee `../.env` y escucha en el puerto 8080 de todas las interfaces):

   PowerShell:
   ```powershell
   cd backend
   .\gradlew.bat bootRun
   ```
   bash:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
3. Frontend (otra terminal; `-d` exige ruta **absoluta**):

   PowerShell (desde la raíz del repositorio):
   ```powershell
   jwebserver -p 5500 -d "$((Resolve-Path .\frontend).Path)"
   ```
   bash (desde la raíz del repositorio):
   ```bash
   jwebserver -p 5500 -d "$(pwd)/frontend"
   ```
   Abre http://localhost:5500.

| Componente | Desde la máquina | Desde el contenedor n8n |
|---|---|---|
| Frontend | http://localhost:5500 | — |
| API | http://localhost:8080/api | http://host.docker.internal:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html | — |
| PostgreSQL | 127.0.0.1:5433, base `rutaia` | — |
| Qdrant | http://127.0.0.1:6333 (panel en /dashboard) | http://qdrant:6333 |
| n8n | editor http://localhost:5678 · webhooks http://127.0.0.1:5678/webhook/{ruta} | — |

## 7. Verificaciones

PowerShell:

```powershell
docker compose ps                                    # postgres (healthy), qdrant y n8n Up
docker compose exec postgres psql -U rutaia_app -d rutaia -c "select version();"
curl.exe -s http://127.0.0.1:6333/healthz            # healthz check passed
curl.exe -s http://127.0.0.1:5678/healthz            # {"status":"ok"}
git check-ignore .env                                # .env
```

bash:

```bash
docker compose ps
docker compose exec postgres psql -U rutaia_app -d rutaia -c "select version();"
curl -s http://127.0.0.1:6333/healthz
curl -s http://127.0.0.1:5678/healthz
git check-ignore .env
```

### Conectividad del contenedor n8n hacia la máquina

Simula a Spring Boot con un servidor temporal en el puerto 8080 (detenlo con Ctrl+C al terminar):

PowerShell (terminal 1, desde la raíz):
```powershell
jwebserver -b 0.0.0.0 -p 8080 -d "$((Resolve-Path .\docs).Path)"
```
bash (terminal 1, desde la raíz):
```bash
jwebserver -b 0.0.0.0 -p 8080 -d "$(pwd)/docs"
```
Terminal 2 (igual en ambos sistemas): debe imprimir el listado HTML de `docs/`.
```bash
docker compose exec n8n wget -qO- http://host.docker.internal:8080/
```

## 8. Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `Bind for 127.0.0.1:5433 failed: port is already allocated` (o 6333, 5678, 8080, 5500) | Otro proceso usa el puerto | Windows: `Get-NetTCPConnection -LocalPort 5433 \| Select-Object OwningProcess` y `Get-Process -Id <PID>`. Linux: `sudo ss -ltnp 'sport = :5433'`. Detén ese proceso o servicio. |
| El editor de `localhost:5678` no tiene tu cuenta o credencial, o `n8n list:workflow` no muestra lo que creaste en el navegador | Hay otro n8n instalado en la máquina (por ejemplo, con `npm i -g n8n`) escuchando en `[::]:5678`; el navegador resuelve `localhost` a `::1` y entra en ese, no en el contenedor | Windows: `Get-NetTCPConnection -LocalPort 5678 -State Listen` (solo debe aparecer `com.docker.backend`). Linux: `sudo ss -ltnp 'sport = :5678'`. Detén el otro n8n (Ctrl+C en su terminal) y, si no lo usas, desinstálalo con `npm uninstall -g n8n`. RutaIA solo usa el n8n del contenedor. |
| `required variable RUTAIA_DB_PASSWORD is missing a value` | No existe `.env` o la contraseña está vacía | Crea `.env` desde `.env.example` (sección 2). |
| `wget: can't connect to remote host` / timeout hacia `host.docker.internal:8080` | Firewall de la máquina | Windows: acepta el aviso de Windows Defender para Java en redes privadas, o (administrador) `New-NetFirewallRule -DisplayName "RutaIA Java 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private`. Linux con ufw: `sudo ufw allow from 172.16.0.0/12 to any port 8080 proto tcp`. Comprueba también que el servidor escucha en `0.0.0.0` y no solo en `127.0.0.1`. |
| `password authentication failed for user "rutaia_app"` | El volumen `pgdata` se creó con otra contraseña | Restaura la contraseña anterior en `.env` o empieza de cero con `docker compose down -v` (borra todo, ver sección 3). |
| Las tablas no existen tras añadir los `.sql` | El volumen `pgdata` ya existía; los scripts de init solo corren con el volumen vacío | Aplica los SQL a mano (sección 3) o `docker compose down -v` y `docker compose up -d`. |
| Linux: `EACCES: permission denied` al exportar flujos a `/home/node/flujos` | El contenedor n8n corre con UID 1000 y tu usuario tiene otro UID (`id -u`) | `sudo setfacl -R -m u:1000:rwX -m d:u:1000:rwX n8n` (o, menos restrictivo, `chmod -R a+rwX n8n`). |
| En PowerShell `curl -s ...` falla con «No se encuentra ningún parámetro...» | `curl` es un alias de `Invoke-WebRequest` | Usa `curl.exe`. |
| OpenRouter responde **401** `Missing Authentication header` o **503** `CLERK_OAUTH_ISSUER is not configured` | Lo que se pegó no es una API key (por ejemplo, un token de sesión del navegador o una *Management key*) | Crea una clave en https://openrouter.ai/settings/keys → *Create API Key* y cópiala al crearla: empieza por `sk-or-v1-` y tiene unos 73 caracteres. Corrige también el Value de la credencial de n8n (`Bearer sk-or-v1-…`). |
| OpenRouter responde **429** | Cuota agotada (20/min o 50/día por cuenta) | Espera un minuto o al día siguiente (UTC). Evita repetir pruebas innecesarias. |
| OpenRouter responde **429** `Provider returned error` · *«… is temporarily rate-limited upstream»* | El proveedor gratuito del modelo está saturado para todos los usuarios (no es tu cuota) | Reintenta cada 2 a 10 minutos; cada intento gasta 1 solicitud. Suele pasar en horas pico con `google/gemma-4-31b-it:free`. |
| OpenRouter responde **404** en un modelo `:free` | Privacidad no habilitada para proveedores gratuitos | Habilítala en https://openrouter.ai/settings/privacy (sección 4). |
| OpenRouter responde **402** | Saldo negativo en la cuenta | Revisa *Credits* en OpenRouter; los modelos `:free` exigen saldo ≥ 0. |
| OpenRouter responde **400** en embeddings | Falta `input_type` (el modelo es asimétrico) | Envía `"input_type":"query"` o `"passage"`. |
| `wsl` responde «Clase no registrada» (`Wsl/CallMsi/Install/REGDB_E_CLASSNOTREG`) | La instalación de WSL está dañada (el paquete de la tienda existe, pero no su MSI) | PowerShell como administrador: `winget install -e --id Microsoft.WSL --force` y reinicia. Si persiste: `Get-AppxPackage MicrosoftCorporationII.WindowsSubsystemForLinux \| Remove-AppxPackage`, repite el `winget install` y reinicia. Comprueba con `wsl --version`. |
| `docker: command not found` / no responde en Windows | Docker Desktop no está abierto o falta WSL2 | Abre Docker Desktop y espera a *Engine running*; revisa `wsl --status`. |
