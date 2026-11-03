# Presentación final de RutaIA — guion por diapositiva

Duración sugerida: 15–18 minutos (12 diapositivas + demo de 5–6 minutos). Cada diapositiva lleva **En pantalla** (lo que se muestra) y **Qué decir** (notas del expositor). Las cifras salen de `docs/pruebas-funcionales.md` y de `docs/informe-integracion.md` (pruebas del 2026-11-03).

---

## 1. Portada

**En pantalla**
- RutaIA — orientación académica con inteligencia artificial
- Jeshua Perez · proyecto individual
- https://github.com/JeshuaPerez/RutIA

**Qué decir**
- «RutaIA recomienda cursos reales del catálogo de la institución a partir de lo que el estudiante escribe con sus propias palabras».

---

## 2. Problema

**En pantalla**
- Catálogo grande; el estudiante no sabe cómo se llama lo que busca.
- El buscador por palabras falla: «proteger aplicaciones web» no coincide con «OWASP Top 10».
- Un chatbot genérico inventa cursos que no existen.

**Qué decir**
- El problema no es la falta de cursos, sino encontrarlos y saber en qué orden tomarlos según el nivel.
- La solución debe recomendar **solo** cursos existentes y explicar por qué.

---

## 3. Solución

**En pantalla**
- El estudiante se registra (nivel y área), escribe su necesidad y recibe:
  - una recomendación en texto, con orden sugerido;
  - las fuentes: los cursos del catálogo con su similitud;
  - el historial de sus consultas y la opción de calificar de 1 a 5.
- RAG: recuperar primero, generar después.

**Qué decir**
- RAG significa que el modelo de lenguaje no responde de memoria. Primero buscamos los cursos parecidos y luego le pedimos que redacte usando solo esos cursos.
- Si no hay cursos pertinentes, el sistema lo dice y no llama al modelo.

---

## 4. Arquitectura

**En pantalla**
- El diagrama de componentes de `docs/arquitectura.md`:
  - Frontend :5500 → API Spring Boot :8080 → PostgreSQL :5433 y n8n :5678;
  - n8n → Qdrant :6333 y OpenRouter;
  - n8n → API, para leer los cursos activos.
- Docker Compose: PostgreSQL 17, Qdrant v1.20.0, n8n 2.45.7 (puertos solo en 127.0.0.1). Backend y frontend en la máquina.

**Qué decir**
- La API es la **única puerta de entrada**: el navegador nunca habla con n8n, Qdrant ni OpenRouter. En la prueba, el código del frontend tiene 0 referencias a esos servicios.
- Solo n8n llama a OpenRouter, con una credencial guardada en n8n. La clave no está en el código ni en el repositorio.
- CORS solo permite el origen del frontend: un origen ajeno recibe 403.

---

## 5. Modelo de datos

**En pantalla**
- El diagrama entidad-relación de `docs/base-de-datos.md`:
  - estudiante 1–N consulta;
  - consulta 1–0..1 recomendacion;
  - recomendacion 1–N recomendacion_fuente N–1 curso;
  - recomendacion 1–0..1 calificacion.
- Restricciones: correo único y normalizado, niveles y estados exactos (CHECK), duración > 0, similitud entre 0 y 1, puntuación de 1 a 5, una calificación por recomendación.
- Índices justificados: catálogo `(activo, categoria, nivel)`, historial `(estudiante_id, fecha DESC)`, estado y curso más recomendado.

**Qué decir**
- Las reglas de negocio viven en la base, no solo en Java: aunque alguien salte la API, la base rechaza un correo duplicado o una calificación de 6.
- La consulta se guarda en «Pendiente» **antes** de pedir la recomendación y siempre termina en Respondida, Sin resultados o Error. En la integración no quedó ninguna en Pendiente.

---

## 6. Flujo RAG (n8n)

**En pantalla**
- W-1, en orden:
  1. validar la entrada;
  2. leer los cursos activos de la API;
  3. embedding de la pregunta;
  4. Qdrant (filtro `activo`, top 5, umbral);
  5. filtrar las fuentes que no estén activas en la API;
  6. si no queda ninguna: Sin resultados, sin LLM;
  7. si quedan: LLM con un solo mensaje con reglas y contexto;
  8. normalizar a texto plano;
  9. validar que no nombre cursos ajenos.
- W-2 (indexación): 30 cursos en 1 solicitud de embeddings, upsert por id.

**Qué decir**
- Todo lo que no gasta cuota va primero: una pregunta vacía nunca llega a OpenRouter.
- Hay **dos** filtros de cursos activos: el de Qdrant y la comprobación contra la API. Por eso un curso desactivado no se recomienda aunque no se haya re-indexado. Lo comprobamos desactivando el curso 1: Qdrant todavía lo devolvía y el flujo lo descartó.

---

## 7. Umbral de similitud y su justificación

**En pantalla**
- Medición con las 8 consultas de prueba (W-3, top 10 sin umbral):
  - S_cocina = **0,2515**: el mejor curso para «cocina italiana»;
  - S_pert = **0,3750**: el peor de los 3 primeros cursos pertinentes de las 7 consultas temáticas.
- Intervalo válido (0,2515; 0,3750), punto medio 0,313.
- Candidatos con margen ≥ 0,02: de 0,28 a 0,35. Elegido **0,34**: es el que deja fuera más cursos no pertinentes (solo 1 por encima) y, de los dos empatados, el que tiene más margen respecto de S_pert.

**Qué decir**
- El score es la similitud coseno; no es un porcentaje. Su escala depende del modelo, por eso el umbral se **midió**, no se adivinó.
- Con 0,34, la consulta de cocina queda en Sin resultados y cada consulta temática conserva entre 3 y 5 cursos pertinentes.
- El único caso límite conocido: «Seguridad web con OWASP» (0,3644) aparece en «páginas web». Está documentado en la calibración.

---

## 8. Demo en vivo (5–6 min)

**Antes de empezar**
- `docker compose ps`: los 3 servicios arriba y postgres `healthy`.
- API: `GET /api/estudiantes` → 200.
- Frontend abierto en http://localhost:5500.
- Saldo de OpenRouter > 0. Cada consulta temática gasta 1 embedding gratuito y 1 llamada al LLM de pago, de menos de 0,001 USD.
- Plan B si falla la red: capturas en `docs/evidencias/etapa8-1280-0*.png` y las respuestas guardadas en `docs/evidencias/integracion-consultas.txt`.

**Guion**
1. **Registro:** elegir a «Valeria Gómez Rojas · Principiante · Desarrollo web» (o registrar un perfil nuevo).
2. **Catálogo:** filtrar por categoría y nivel.
3. **Consulta temática:** «Quiero aprender a crear páginas web.» Mostrar la recomendación y las fuentes con su similitud. Señalar que solo nombra cursos de las fuentes.
4. **Cocina:** «Quiero aprender cocina italiana.» → Sin resultados, con mensaje claro y sin llamada al LLM.
5. **Historial y detalle:** abrir la consulta y calificarla con estrellas y un comentario.
6. **Swagger** (http://localhost:8080/swagger-ui.html):
   - `POST /api/consultas` con pregunta `"   "` → 400;
   - con `estudianteId` 999999 → 404;
   - `GET /api/estadisticas`.

**Qué decir**
- Durante la demo, señalar que todo pasa por la API y que los errores devuelven el mismo formato JSON.

---

## 9. Resultados de las pruebas

**En pantalla**

| Caso | Resultado |
|---|---|
| CP 01–07 (temáticas) | 7/7 Respondida, con 3 a 5 fuentes activas y ≥ 0,34, texto plano y sin cursos ajenos |
| CP 08 (cocina) | Sin resultados, sin recomendación y sin llamada al LLM |
| CP 09 (pregunta vacía) | 400, sin consulta ni ejecución en n8n (también con n8n detenido) |
| CP 10 (estudiante inexistente) | 404, sin crear la consulta |
| n8n detenido / Qdrant detenido | Error controlado (201 con mensaje), nada en Pendiente |
| Curso desactivado sin re-indexar | No aparece en las fuentes |
| Datos | 15 consultas, 5 calificaciones (promedio 3,80); estadísticas = SQL |
| Contrato C1–C12 | Verificado; única diferencia, el modelo del LLM, registrada |

**Qué decir**
- Las pruebas se hicieron de punta a punta desde la interfaz, con la base reiniciada a la semilla.
- Cada caso tiene su evidencia: petición, respuesta, filas de la base y nodos ejecutados en n8n.

---

## 10. Decisiones de diseño

**En pantalla**
- **Docker Compose** para PostgreSQL, Qdrant y n8n: mismo entorno en Windows y Linux, etiquetas exactas (nunca `latest`) y puertos solo locales.
- **Capas gratuitas** de OpenRouter para embeddings y presupuesto de solicitudes por etapa (50 por día por cuenta).
- **LLM de pago, como excepción:** el 2026-11-03 la variante gratuita de Gemma estaba saturada y Google devolvía 500/504. Se usó `google/gemma-4-31b-it` de pago, el mismo modelo del contrato. Las 8 llamadas costaron menos de 0,01 USD. Quedó registrado en el contrato.
- La API como única puerta de entrada, con transacciones separadas: Pendiente se confirma antes de llamar a n8n.
- El umbral se calibró con datos, y la salida del modelo se valida.

**Qué decir**
- Cada decisión tiene un costo: la capa gratuita es frágil, y por eso el sistema degrada bien (Error controlado, sin consultas colgadas) y la configuración del modelo se cambia en un solo nodo.

---

## 11. Errores conocidos

**En pantalla**
- 429 por cuota (50/día con menos de 10 créditos comprados; 1000/día con 10) o por proveedor saturado («rate-limited upstream»).
- 404 en `:free` por la privacidad no habilitada; 402 por saldo negativo o sin crédito para el LLM.
- Modelo gratuito retirado: la alternativa de LLM de C10 ya no existe.
- n8n no alcanza la API por `host.docker.internal` (firewall de Windows o ufw).
- Puertos ocupados (otro n8n instalado con npm en el 5678).
- `docker compose down -v` borra flujos, credenciales, colección y datos.
- Permisos de `./n8n` en Linux; alias `curl` en PowerShell; `gradlew` sin permiso o con CRLF.

**Qué decir**
- Todos tienen síntoma, causa y solución en el README («Errores conocidos»).

---

## 12. Conclusiones

**En pantalla**
- RutaIA integra 6 componentes en un único proceso y recomienda solo cursos reales, con fuentes y similitud visibles.
- El RAG con umbral calibrado separa bien lo pertinente: la cocina no recibe cursos y las 7 temáticas sí.
- La robustez importa tanto como la respuesta: validaciones antes de gastar, estados finales siempre y doble filtro de cursos activos.
- Pendientes y mejoras posibles:
  - verificación desde cero en Linux;
  - capturas de Swagger de E-04 y E-12 a E-14;
  - endpoint para reactivar cursos;
  - re-indexación automática al cambiar el catálogo.

**Qué decir**
- Cerrar con la demo de cocina frente a la de páginas web: el sistema sabe decir «no tengo un curso para eso».
