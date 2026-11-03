# Arquitectura de RutaIA

RutaIA recomienda cursos existentes de un catálogo institucional a partir de una necesidad escrita en lenguaje natural. Usa una arquitectura RAG (*Retrieval-Augmented Generation*):

- **Frontend** (HTML/CSS/JS servido con `jwebserver` en la máquina) — formulario de consulta y presentación de recomendaciones. Solo habla con la API.
- **API REST Spring Boot** (en la máquina) — única puerta de entrada. Valida, guarda en PostgreSQL y delega la consulta RAG a n8n por webhook.
- **n8n** (contenedor) — orquesta los flujos: genera embeddings con OpenRouter, busca en Qdrant y consulta el LLM.
- **Qdrant** (contenedor) — base vectorial con los embeddings de los cursos (2048 dimensiones).
- **PostgreSQL 17** (contenedor) — base relacional `rutaia` con el catálogo y las consultas.
- **OpenRouter** (externo, capa gratuita) — embeddings `nvidia/nemotron-3-embed-1b:free` y LLM `google/gemma-4-26b-a4b-it:free` (desde la Etapa 5; antes `google/gemma-4-31b-it:free`, ver `docs/n8n.md`). Solo n8n lo llama, con la credencial «RutaIA OpenRouter».

## Diagrama de componentes y puertos

```mermaid
flowchart LR
    U([Usuario<br/>navegador])

    subgraph MAQ["Máquina (Windows / Linux)"]
        FE["Frontend<br/>jwebserver<br/>localhost:5500"]
        API["API Spring Boot<br/>localhost:8080/api<br/>Swagger: /swagger-ui.html"]
    end

    subgraph DC["Docker Compose · proyecto rutaia (puertos solo en 127.0.0.1)"]
        PG[("PostgreSQL 17<br/>servicio postgres<br/>127.0.0.1:5433 → 5432<br/>base rutaia")]
        QD[("Qdrant v1.20.0<br/>servicio qdrant<br/>127.0.0.1:6333")]
        N8N["n8n 2.45.7<br/>servicio n8n<br/>127.0.0.1:5678"]
    end

    OR["OpenRouter<br/>openrouter.ai/api/v1<br/>embeddings + chat"]

    U -->|HTTP| FE
    FE -->|fetch JSON| API
    API -->|JDBC 127.0.0.1:5433| PG
    API -->|POST /webhook/rutaia-consulta-rag<br/>127.0.0.1:5678| N8N
    N8N -->|REST http://qdrant:6333| QD
    N8N -->|GET http://host.docker.internal:8080/api| API
    N8N -->|HTTPS Bearer · credencial<br/>«RutaIA OpenRouter»| OR
```

## Flujo de consulta (RAG)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant FE as Frontend :5500
    participant API as Spring Boot :8080
    participant PG as PostgreSQL :5433
    participant N as n8n (rutaia-consulta-rag)
    participant OR as OpenRouter
    participant Q as Qdrant :6333

    U->>FE: Escribe su necesidad, nivel y área
    FE->>API: POST /api/...
    API->>PG: Guarda la consulta
    API->>N: POST /webhook/rutaia-consulta-rag (timeout 90 s)
    N->>OR: POST /embeddings (input_type=query)
    OR-->>N: vector de 2048 números
    N->>Q: Búsqueda por similitud (con umbral calibrado)
    Q-->>N: cursos candidatos
    N->>OR: POST /chat/completions (gemma-4-26b-a4b-it:free, un mensaje user)
    OR-->>N: recomendación en texto
    N-->>API: Respuesta JSON
    API->>PG: Guarda el resultado
    API-->>FE: Recomendaciones
    FE-->>U: Muestra los cursos recomendados
```

## Flujo de indexación de cursos

```mermaid
sequenceDiagram
    autonumber
    participant N as n8n (rutaia-indexacion-cursos)
    participant API as Spring Boot (host.docker.internal:8080)
    participant OR as OpenRouter
    participant Q as Qdrant (qdrant:6333)

    N->>API: GET /api/... (catálogo de cursos)
    API-->>N: cursos
    N->>OR: POST /embeddings (input_type=passage, por lotes)
    OR-->>N: data[i].embedding (2048) + data[i].index
    N->>Q: Crea la colección si no existe y hace upsert de puntos
```

## Notas

- A OpenRouter solo viajan la pregunta, el nivel, el área y los datos de los cursos; nunca nombres ni correos.
- La capa gratuita de OpenRouter admite 20 solicitudes por minuto y 50 por día por cuenta.
- Las rutas exactas de la API y los detalles de los flujos los definen las etapas posteriores (`docs/openapi.json`, `docs/n8n.md`).
