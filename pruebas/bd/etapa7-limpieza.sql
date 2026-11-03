-- Etapa 7: borra los datos de etapa7-datos-temporales.sql (todo lo del estudiante 3) y deja los contadores de
-- identidad en el máximo id, para que la siguiente consulta real continúe la numeración.
BEGIN;

DELETE FROM calificacion
WHERE recomendacion_id IN (SELECT r.id FROM recomendacion r JOIN consulta c ON c.id = r.consulta_id
                           WHERE c.estudiante_id = 3);
DELETE FROM recomendacion_fuente
WHERE recomendacion_id IN (SELECT r.id FROM recomendacion r JOIN consulta c ON c.id = r.consulta_id
                           WHERE c.estudiante_id = 3);
DELETE FROM recomendacion WHERE consulta_id IN (SELECT id FROM consulta WHERE estudiante_id = 3);
DELETE FROM consulta WHERE estudiante_id = 3;

SELECT setval(pg_get_serial_sequence('consulta', 'id'), (SELECT coalesce(max(id), 0) + 1 FROM consulta), false);
SELECT setval(pg_get_serial_sequence('recomendacion', 'id'), (SELECT coalesce(max(id), 0) + 1 FROM recomendacion), false);
SELECT setval(pg_get_serial_sequence('recomendacion_fuente', 'id'),
              (SELECT coalesce(max(id), 0) + 1 FROM recomendacion_fuente), false);
SELECT setval(pg_get_serial_sequence('calificacion', 'id'), (SELECT coalesce(max(id), 0) + 1 FROM calificacion), false);

COMMIT;

SELECT count(*) AS consultas_estudiante_3 FROM consulta WHERE estudiante_id = 3;
