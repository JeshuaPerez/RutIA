# RutaIA

Repositorio: https://github.com/JeshuaPerez/RutIA

> **Esqueleto (Etapa 0).** Las partes marcadas como *Pendiente* se completan en la etapa de integración.

## 1. Nombre del proyecto e integrantes

**RutaIA** — orientación académica con recomendación de cursos mediante IA.

| Integrante | Rol |
|---|---|
| Jeshua Perez | *Pendiente* |
| *Pendiente* | *Pendiente* |

## 2. Descripción de la problemática y solución propuesta

**Problemática.** *Pendiente (integración):* dificultad de los estudiantes para encontrar, dentro de un catálogo institucional, los cursos que responden a lo que necesitan aprender.

**Solución propuesta.** Una aplicación web en la que el estudiante describe su necesidad en lenguaje natural (con su nivel y área) y recibe recomendaciones de **cursos existentes** del catálogo, generadas con una arquitectura RAG: la necesidad se convierte en un embedding, se buscan los cursos más parecidos en una base vectorial y un modelo de lenguaje redacta la recomendación usando solo esos cursos.

## 3. Tecnologías y arquitectura

| Capa | Tecnología |
|---|---|
| Frontend | HTML, CSS y JavaScript, servido con `jwebserver` (JDK 21) |
| Backend | Java 21, Spring Boot, Gradle — única puerta de entrada (API REST) |
| Base de datos relacional | PostgreSQL 17 (contenedor) |
| Base de datos vectorial | Qdrant v1.20.0 (contenedor) |
| Automatizaciones | n8n 2.45.7 (contenedor) |
| IA | OpenRouter, capa gratuita: embeddings `nvidia/nemotron-3-embed-1b:free` (2048 dimensiones) y LLM `google/gemma-4-31b-it:free` |
| Infraestructura | Docker Compose v2 ([docker-compose.yml](docker-compose.yml)) |

Flujo: frontend → API Spring Boot → webhook de n8n → OpenRouter (embedding) → Qdrant (búsqueda) → OpenRouter (LLM) → API → frontend. PostgreSQL guarda el catálogo y las consultas.

Diagrama de arquitectura, puertos y flujos de consulta e indexación: [docs/arquitectura.md](docs/arquitectura.md).
*Pendiente (Etapa de base de datos):* diagrama entidad-relación en [docs/base-de-datos.md](docs/base-de-datos.md).

## 4. Requisitos de instalación y variables de entorno

En la máquina solo hacen falta **Docker** (Docker Desktop con WSL2 en Windows; Docker Engine con el plugin Compose en Linux), **JDK 21** y **Git**. PostgreSQL, Qdrant y n8n corren en contenedores. Instalación paso a paso para Windows y Linux: [docs/entorno-local.md](docs/entorno-local.md#1-requisitos-e-instalación).

Variables: copia [.env.example](.env.example) como `.env` en la raíz y escribe tu propia contraseña en `RUTAIA_DB_PASSWORD`. El `.env` no se versiona.

| Variable | Valor | Secreta |
|---|---|---|
| RUTAIA_DB_USER | rutaia_app | no |
| RUTAIA_DB_PASSWORD | (la elige cada integrante) | sí |
| RUTAIA_DB_URL | jdbc:postgresql://127.0.0.1:5433/rutaia | no |
| RUTAIA_N8N_WEBHOOK_URL | http://127.0.0.1:5678/webhook/rutaia-consulta-rag | no |
| RUTAIA_N8N_TIMEOUT_SEGUNDOS | 90 | no |
| RUTAIA_CORS_ORIGINS | http://localhost:5500,http://127.0.0.1:5500 | no |

La clave de OpenRouter **no** va en el `.env`: cada integrante la guarda solo en su credencial de n8n «RutaIA OpenRouter» (Header Auth, `Authorization: Bearer <clave>`). Ver [docs/entorno-local.md](docs/entorno-local.md#4-primera-configuración-de-n8n-y-openrouter).

## 5. Ejecución de contenedores, backend y frontend

Orden: contenedores → backend → frontend. Desde la raíz del repositorio:

PowerShell:

```powershell
docker compose up -d
docker compose ps                         # postgres debe aparecer healthy
cd backend; .\gradlew.bat bootRun         # en otra terminal
jwebserver -p 5500 -d "$((Resolve-Path .\frontend).Path)"   # en otra terminal, desde la raíz
```

bash:

```bash
docker compose up -d
docker compose ps
(cd backend && ./gradlew bootRun)         # en otra terminal
jwebserver -p 5500 -d "$(pwd)/frontend"   # en otra terminal, desde la raíz
```

| Componente | URL |
|---|---|
| Frontend | http://localhost:5500 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| n8n | http://localhost:5678 |
| Qdrant | http://127.0.0.1:6333/dashboard |

Detener: `docker compose stop` o `docker compose down` (sin `-v`, que borra todos los datos). Guía completa y verificaciones: [docs/entorno-local.md](docs/entorno-local.md).

## 6. Importación de workflows

Los flujos están en `n8n/` y el contenedor los ve en `/home/node/flujos`:

| Archivo | Propósito |
|---|---|
| `rutaia-indexacion-cursos.json` | Genera los embeddings de los cursos y los carga en Qdrant |
| `rutaia-calibracion-umbral.json` | Calibra el umbral de similitud |
| `rutaia-consulta-rag.json` | Atiende las consultas de la API (webhook `rutaia-consulta-rag`) |

```bash
docker compose exec n8n n8n import:workflow --input=/home/node/flujos/rutaia-consulta-rag.json
docker compose exec n8n n8n list:workflow
docker compose exec n8n n8n publish:workflow --id=<ID>
```

En n8n 2.x solo la versión **publicada** atiende la URL de producción `/webhook/…`. Antes de importar, crea la credencial «RutaIA OpenRouter».
*Pendiente (Etapa de n8n):* orden de importación, asignación de credenciales e indexación inicial ([docs/n8n.md](docs/n8n.md)).

## 7. Endpoints principales y ejemplos de uso

*Pendiente (Etapa de backend):* tabla de endpoints de `http://localhost:8080/api` y ejemplos con `curl.exe` (PowerShell) y `curl` (bash), con cuerpos en `pruebas/api/`. Especificación completa en [docs/openapi.json](docs/openapi.json) y Swagger UI.

## 8. Errores conocidos

| Error | Causa | Solución |
|---|---|---|
| OpenRouter 429 «… is temporarily rate-limited upstream» | El proveedor gratuito del modelo está saturado (afectó a `google/gemma-4-31b-it:free` el 2026-11-02) | Reintentar en otro horario; cada intento gasta 1 de las 50 solicitudes diarias |
| OpenRouter 429 por cuota | Límite de la capa gratuita: 20 solicitudes/minuto y 50/día por cuenta | Esperar; evitar pruebas repetidas |
| OpenRouter 404 en modelos `:free` | Privacidad de proveedores gratuitos no habilitada | Habilitarla en https://openrouter.ai/settings/privacy |
| OpenRouter 402 | Saldo negativo en la cuenta | Revisar *Credits* |
| El editor de `localhost:5678` no muestra la credencial | Otro n8n instalado en la máquina ocupa `[::]:5678` | Detenerlo; RutaIA usa solo el n8n del contenedor |

Lista completa con comandos de diagnóstico: [docs/entorno-local.md](docs/entorno-local.md#8-problemas-comunes).

*Pendiente (integración):* errores conocidos de la aplicación.
