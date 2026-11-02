# Pruebas funcionales de RutaIA

Cada etapa agrega su sección con la misma tabla. La columna «Solicitudes a OpenRouter» cuenta las solicitudes gastadas de la capa gratuita (20 por minuto y 50 por día por cuenta).

## Etapa 0 — Entorno, Docker Compose y repositorio

Fecha: 2026-11-01 / 2026-11-02 · Máquina: Windows 11 Home 10.0.26200

| ID | Requisito | Pasos | Esperado | Obtenido | Evidencia | Solicitudes a OpenRouter |
|---|---|---|---|---|---|---|
| E0-01 | Archivo Docker Compose | `docker compose up -d` y `docker compose ps` | 3 contenedores Up; `postgres` healthy | OK: n8n, postgres (healthy) y qdrant Up, puertos en 127.0.0.1 | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-02 | Base de datos relacional PostgreSQL | `docker compose exec postgres psql -U rutaia_app -d rutaia -c "select version();"` | Devuelve PostgreSQL 17.x | OK: PostgreSQL 17.11 (Debian 17.11-1.pgdg13+2) | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-03 | Base de datos vectorial Qdrant | `curl.exe -s http://127.0.0.1:6333/healthz` | `healthz check passed` | OK: `healthz check passed` | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-04 | Automatizaciones en n8n | `curl.exe -s http://127.0.0.1:5678/healthz` | `{"status":"ok"}` | OK: `{"status":"ok"}` | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-05 | n8n alcanza la API en la máquina | `jwebserver -b 0.0.0.0 -p 8080 -d <docs>` y `docker compose exec n8n wget -qO- http://host.docker.internal:8080/` | Listado HTML de `docs/` | OK: listado HTML recibido, sin ajustes de firewall | `docs/evidencias/etapa0-contenedores.txt` | 0 |
| E0-06 | Claves fuera del código | Crear en n8n la credencial Header Auth «RutaIA OpenRouter» | Credencial existe con ese nombre exacto | OK: «RutaIA OpenRouter», tipo `httpHeaderAuth`, Name `Authorization`, Value con prefijo `Bearer ` (comprobado en el n8n del contenedor sin mostrar la clave) | `docs/evidencias/etapa0-credencial.txt` | 0 |
| E0-07 | Embeddings mediante OpenRouter | POST /api/v1/embeddings con `pruebas/n8n/openrouter-embeddings-query.json` | `data[0].embedding` con 2048 números finitos | OK: HTTP 200, `data[0].index` 0, 2048 números finitos | `docs/evidencias/etapa0-openrouter.txt` | 1 |
| E0-08 | Modelo de lenguaje mediante OpenRouter | POST /api/v1/chat/completions con `pruebas/n8n/openrouter-chat.json` | `choices[0].message.content` con texto | **BLOQUEADO**: 7 intentos (16:11–16:23) respondieron HTTP 429 «google/gemma-4-31b-it:free is temporarily rate-limited upstream». La clave y la privacidad están bien (las embeddings respondieron 200 con la misma clave). Repetir en otro horario | `docs/evidencias/etapa0-openrouter.txt` | 7 (todas 429) |
| E0-09 | Etiquetas exactas de imagen | Revisar `docker-compose.yml` | `postgres:17`, `qdrant/qdrant:v1.20.0`, `docker.n8n.io/n8nio/n8n:2.45.7`; ninguna `latest` | OK: `docker compose config --images` lista las tres etiquetas exactas | `docker-compose.yml` | 0 |
| E0-10 | Sin secretos en el repositorio | `git check-ignore .env` y `git status` | Imprime `.env`; `git status` no lista `.env` ni claves | OK: `git check-ignore .env` imprime `.env`; `git status` no lista `.env`; ningún archivo versionable contiene `sk-or-v1-…` ni una contraseña | `git status --short --untracked-files=all` | 0 |

Total de solicitudes a OpenRouter previstas en la Etapa 0: **2** (E0-07 y E0-08).

Solicitudes realmente enviadas (detalle en `docs/evidencias/etapa0-openrouter.txt`):
- 2 con una clave que no era una API key: rechazadas por autenticación (503 y 401), no llegaron a ningún modelo.
- 1 de embeddings: OK.
- 7 de chat: todas 429 «google/gemma-4-31b-it:free is temporarily rate-limited upstream» (proveedor gratuito saturado; no es la cuota de la cuenta). E0-08 queda pendiente de repetir en otro horario; si persiste, el equipo decide si cambia el modelo del contrato (C10).
- Total con la clave válida: 8 de las 50 diarias.
