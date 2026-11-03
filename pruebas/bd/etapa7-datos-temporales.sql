-- Etapa 7: datos temporales para probar E-04 y E-14 sin gastar cuota de OpenRouter.
-- Usa el estudiante 3, que no tiene consultas. Se borran con etapa7-limpieza.sql (junto con las calificaciones
-- que se les hagan en las pruebas de concurrencia de E-13).
--  · una consulta Pendiente (resumen = MSG_PENDIENTE);
--  · dos Respondida con respuestas de exactamente 160 y 161 caracteres (límite del resumen);
--  · fuentes insertadas en orden de similitud ascendente, para ver que E-04 las ordena;
--  · los cursos 9 y 10 quedan con 2 filas cada uno en recomendacion_fuente: empate que gana el 9 (menor id).
BEGIN;

INSERT INTO consulta (estudiante_id, pregunta, fecha, estado) VALUES
    (3, 'Consulta temporal con respuesta de 161 caracteres.', '2026-11-02 23:00:01', 'Respondida'),
    (3, 'Consulta temporal con respuesta de 160 caracteres.', '2026-11-02 23:00:02', 'Respondida'),
    (3, 'Consulta temporal en Pendiente.',                    '2026-11-02 23:00:03', 'Pendiente');

INSERT INTO recomendacion (consulta_id, respuesta)
SELECT id, rpad('Respuesta temporal de 160 caracteres, con tildes (á, é, ñ) para contar caracteres y no bytes. ', 160, '.')
FROM consulta WHERE estudiante_id = 3 AND pregunta = 'Consulta temporal con respuesta de 160 caracteres.';

INSERT INTO recomendacion (consulta_id, respuesta)
SELECT id, rpad('Respuesta temporal de 161 caracteres, con tildes (á, é, ñ) para contar caracteres y no bytes. ', 160, '.') || 'Z'
FROM consulta WHERE estudiante_id = 3 AND pregunta = 'Consulta temporal con respuesta de 161 caracteres.';

INSERT INTO recomendacion_fuente (recomendacion_id, curso_id, similitud)
SELECT r.id, v.curso_id, v.similitud
FROM recomendacion r
JOIN consulta c ON c.id = r.consulta_id
JOIN (VALUES ('Consulta temporal con respuesta de 160 caracteres.', 10, 0.41),
             ('Consulta temporal con respuesta de 160 caracteres.',  9, 0.52),
             ('Consulta temporal con respuesta de 161 caracteres.', 10, 0.45),
             ('Consulta temporal con respuesta de 161 caracteres.',  9, 0.47)) AS v (pregunta, curso_id, similitud)
  ON v.pregunta = c.pregunta
WHERE c.estudiante_id = 3
ORDER BY v.similitud;

COMMIT;

SELECT c.id, c.estado, c.fecha, r.id AS recomendacion_id, char_length(r.respuesta) AS largo_respuesta
FROM consulta c LEFT JOIN recomendacion r ON r.consulta_id = c.id
WHERE c.estudiante_id = 3 ORDER BY c.id;
