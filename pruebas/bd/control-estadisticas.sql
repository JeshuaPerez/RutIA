-- Etapa 7: consultas de control de E-14 (solo lectura). Deben coincidir con GET /api/estadisticas.
\echo '== totalConsultas, consultasRespondidas, consultasSinResultados'
SELECT count(*)                                         AS total_consultas,
       count(*) FILTER (WHERE estado = 'Respondida')     AS consultas_respondidas,
       count(*) FILTER (WHERE estado = 'Sin resultados') AS consultas_sin_resultados
FROM consulta;

\echo '== promedioCalificaciones (NULL si no hay calificaciones)'
SELECT round(avg(puntuacion)::numeric, 2) AS promedio_calificaciones,
       count(*)                           AS calificaciones
FROM calificacion;

\echo '== cursoMasRecomendado: la primera fila (más filas en recomendacion_fuente; en empate, menor curso_id)'
SELECT f.curso_id, c.nombre, count(*) AS veces_recomendado
FROM recomendacion_fuente f
JOIN curso c ON c.id = f.curso_id
GROUP BY f.curso_id, c.nombre
ORDER BY veces_recomendado DESC, f.curso_id ASC
LIMIT 5;
