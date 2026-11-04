# Presentación final de RutaIA — guion

Duración: 15–18 minutos, con 12 diapositivas y una demo de 5–6 minutos. Cada diapositiva lleva lo que se muestra y, en *Decir*, la idea principal. Las cifras salen de [pruebas.md](pruebas.md).

## 1. Portada
- RutaIA: orientación académica con inteligencia artificial.
- Jeshua Perez · proyecto individual · https://github.com/JeshuaPerez/RutIA

*Decir:* RutaIA recomienda cursos reales del catálogo a partir de lo que el estudiante escribe con sus propias palabras.

## 2. Problema
- El estudiante no sabe cómo se llama lo que busca en un catálogo grande.
- El buscador por palabras falla: «proteger aplicaciones web» no coincide con «OWASP Top 10».
- Un chatbot genérico inventa cursos que no existen.

*Decir:* hay cursos de sobra; el problema es encontrarlos y saber en qué orden tomarlos. La solución debe recomendar **solo** cursos existentes.

## 3. Solución
- Registro con nivel y área → necesidad en texto libre → recomendación con orden sugerido y las fuentes del catálogo con su similitud.
- Historial de consultas y calificación de 1 a 5.
- RAG: recuperar primero, generar después.

*Decir:* el modelo no responde de memoria: primero buscamos los cursos parecidos y luego redacta usando solo esos. Si no hay cursos pertinentes, el sistema lo dice y no llama al modelo.

## 4. Arquitectura
- Diagrama de componentes de [arquitectura.md](arquitectura.md): Frontend :5500 → API :8080 → PostgreSQL :5433 y n8n :5678 → Qdrant :6333 y OpenRouter.
- Docker Compose para PostgreSQL 17, Qdrant v1.20.0 y n8n 2.45.7; el backend y el frontend corren en la máquina.
- Backend en capas (presentación, aplicación, dominio, persistencia, integración), SOLID y patrón Adapter para el motor de recomendación.

*Decir:* la API es la única puerta de entrada; el frontend tiene 0 referencias a n8n, Qdrant u OpenRouter. La clave de OpenRouter vive solo en una credencial de n8n.

## 5. Modelo de datos
- Diagrama ER de [base-de-datos.md](base-de-datos.md): estudiante → consulta → recomendación → fuentes (curso) y calificación.
- Restricciones: correo único y normalizado, niveles y estados exactos, duración > 0, similitud entre 0 y 1, puntuación de 1 a 5, una calificación por recomendación.
- Índices justificados: catálogo, historial, estado y curso más recomendado.

*Decir:* las reglas viven también en la base. La consulta se guarda en Pendiente antes de pedir la recomendación y siempre termina en Respondida, Sin resultados o Error.

## 6. Flujo RAG en n8n
- Validar entrada → cursos activos de la API → embedding → Qdrant (activo, top 5, umbral) → filtrar contra la API → sin fuentes: Sin resultados, sin LLM → con fuentes: LLM → texto plano → validar que no nombre cursos ajenos.
- Indexación: 30 cursos en 1 solicitud de embeddings, upsert por id.

*Decir:* lo que no gasta cuota va primero. Hay dos filtros de cursos activos: un curso desactivado no se recomienda aunque no se haya re-indexado (se probó con el curso 1).

## 7. Umbral de similitud
- 8 consultas de prueba, top 10 sin umbral: S_cocina = 0,2515 y S_pert = 0,3750.
- Candidatos con margen ≥ 0,02: de 0,28 a 0,35. Elegido **0,34**: deja fuera más cursos no pertinentes y tiene más margen.

*Decir:* el score es similitud coseno, no un porcentaje; por eso el umbral se midió. Caso límite: «OWASP Top 10» (0,3644) aparece en «páginas web».

## 8. Demo en vivo (5–6 min)
Antes:
- `docker compose ps` con los 3 servicios arriba;
- `GET /api/estudiantes` → 200;
- el frontend abierto en http://localhost:5500;
- saldo de OpenRouter > 0.

Plan B: las capturas de [evidencias/](evidencias/).

1. Registro: elegir a «Valeria Gómez Rojas · Principiante · Desarrollo web».
2. Catálogo: filtrar por categoría y nivel.
3. «Quiero aprender a crear páginas web.» → recomendación y fuentes con su similitud.
4. «Quiero aprender cocina italiana.» → Sin resultados, sin llamar al LLM.
5. Historial → Ver detalle → calificar con estrellas y comentario.
6. Swagger: `POST /api/consultas` con `"   "` → 400; con `estudianteId` 999999 → 404; `GET /api/estadisticas`.

## 9. Resultados de las pruebas

| Caso | Resultado |
|---|---|
| CP 01–07 | 7/7 Respondida, con 3 a 5 fuentes activas ≥ 0,34 y sin cursos ajenos |
| CP 08 cocina | Sin resultados, sin llamada al LLM |
| CP 09 y CP 10 | 400 y 404, sin crear la consulta ni llamar a n8n |
| n8n o Qdrant detenidos | Error controlado, nada en Pendiente |
| Requisitos | 62 de 63 cumplidos y 1 parcial; estadísticas iguales a SQL |

## 10. Decisiones de diseño
- Docker Compose con etiquetas exactas y puertos definidos una sola vez en `.env`.
- Embeddings gratuitos; LLM de pago como excepción, porque la variante gratuita de Gemma estaba saturada (8 llamadas por menos de 0,01 USD).
- Transacciones separadas: Pendiente se confirma antes de llamar a n8n.
- Umbral calibrado con datos y salida del modelo validada.

*Decir:* la capa gratuita es frágil; por eso el sistema degrada bien y el modelo se cambia en un solo nodo.

## 11. Errores conocidos
- OpenRouter: 401 (credencial mal copiada), 402 (saldo), 404 (privacidad o modelo retirado) y 429 (cuota o proveedor saturado).
- Puertos ocupados: se cambia la variable en `.env`.
- n8n no alcanza la API por el firewall. `docker compose down -v` borra flujos y credenciales.

*Decir:* cada uno tiene síntoma, causa y solución en el README.

## 12. Conclusiones
- Seis componentes en un único proceso; solo se recomiendan cursos reales, con fuentes y similitud visibles.
- El umbral calibrado separa lo pertinente: la cocina no recibe cursos y las 7 temáticas sí.
- Mejoras posibles: verificación en Linux, capturas de Swagger de E-04 y E-12 a E-14, un endpoint para reactivar cursos y re-indexación automática.

*Decir:* cerrar con cocina frente a páginas web: el sistema sabe decir «no tengo un curso para eso».
