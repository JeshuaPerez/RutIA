-- Etapa 1: cada caso corre en su propia transacción y termina en ROLLBACK.
-- Se ejecuta SIN ON_ERROR_STOP: cada caso debe fallar con la restricción indicada.
-- Nota: los INSERT fallidos consumen valores de las secuencias IDENTITY; tras
-- estas pruebas conviene recargar 01_esquema.sql y 02_datos_semilla.sql.
\set ECHO queries

\echo '### R01 correo duplicado -> uq_estudiante_correo'
BEGIN;
INSERT INTO estudiante (nombre_completo, correo, nivel_experiencia, area_interes)
VALUES ('Prueba Duplicado', 'valeria.gomez@rutaia.test', 'Principiante', 'Pruebas');
ROLLBACK;

\echo '### R02 correo con mayúsculas -> ck_estudiante_correo_normalizado'
BEGIN;
INSERT INTO estudiante (nombre_completo, correo, nivel_experiencia, area_interes)
VALUES ('Prueba Mayúsculas', 'Prueba.Mayusculas@RutaIA.test', 'Principiante', 'Pruebas');
ROLLBACK;

\echo '### R03 nivel Experto (estudiante) -> ck_estudiante_nivel'
BEGIN;
INSERT INTO estudiante (nombre_completo, correo, nivel_experiencia, area_interes)
VALUES ('Prueba Nivel', 'prueba.nivel@rutaia.test', 'Experto', 'Pruebas');
ROLLBACK;

\echo '### R04 nivel Experto (curso) -> ck_curso_nivel'
BEGIN;
INSERT INTO curso (nombre, descripcion, categoria, nivel, duracion_horas)
VALUES ('Curso de prueba nivel', 'Descripción de prueba', 'Programación', 'Experto', 10);
ROLLBACK;

\echo '### R05 duracion_horas 0 -> ck_curso_duracion_positiva'
BEGIN;
INSERT INTO curso (nombre, descripcion, categoria, nivel, duracion_horas)
VALUES ('Curso de prueba duración', 'Descripción de prueba', 'Programación', 'Básico', 0);
ROLLBACK;

\echo '### R06 nombre de curso solo espacios -> ck_curso_nombre_no_vacio'
BEGIN;
INSERT INTO curso (nombre, descripcion, categoria, nivel, duracion_horas)
VALUES ('  ', 'Descripción de prueba', 'Programación', 'Básico', 10);
ROLLBACK;

\echo '### R07 descripción vacía -> ck_curso_descripcion_no_vacia'
BEGIN;
INSERT INTO curso (nombre, descripcion, categoria, nivel, duracion_horas)
VALUES ('Curso de prueba descripción', '', 'Programación', 'Básico', 10);
ROLLBACK;

\echo '### R08 consulta con estudiante_id inexistente -> fk_consulta_estudiante'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta) VALUES (999999, 'Quiero aprender a crear páginas web.');
ROLLBACK;

\echo '### R09 pregunta solo espacios -> ck_consulta_pregunta_no_vacia'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta) VALUES (1, '   ');
ROLLBACK;

\echo '### R10 estado Cerrada -> ck_consulta_estado'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Cerrada');
ROLLBACK;

\echo '### R11 similitud 1.5 -> ck_fuente_similitud'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Respondida');
INSERT INTO recomendacion (consulta_id, respuesta) SELECT max(id), 'Respuesta de prueba' FROM consulta;
INSERT INTO recomendacion_fuente (recomendacion_id, curso_id, similitud) SELECT max(id), 1, 1.5 FROM recomendacion;
ROLLBACK;

\echo '### R12 fuente duplicada -> uq_fuente_recomendacion_curso'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Respondida');
INSERT INTO recomendacion (consulta_id, respuesta) SELECT max(id), 'Respuesta de prueba' FROM consulta;
INSERT INTO recomendacion_fuente (recomendacion_id, curso_id, similitud) SELECT max(id), 1, 0.82 FROM recomendacion;
INSERT INTO recomendacion_fuente (recomendacion_id, curso_id, similitud) SELECT max(id), 1, 0.75 FROM recomendacion;
ROLLBACK;

\echo '### R13 puntuación 0 -> ck_calificacion_puntuacion'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Respondida');
INSERT INTO recomendacion (consulta_id, respuesta) SELECT max(id), 'Respuesta de prueba' FROM consulta;
INSERT INTO calificacion (recomendacion_id, puntuacion) SELECT max(id), 0 FROM recomendacion;
ROLLBACK;

\echo '### R14 puntuación 6 -> ck_calificacion_puntuacion'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Respondida');
INSERT INTO recomendacion (consulta_id, respuesta) SELECT max(id), 'Respuesta de prueba' FROM consulta;
INSERT INTO calificacion (recomendacion_id, puntuacion) SELECT max(id), 6 FROM recomendacion;
ROLLBACK;

\echo '### R15 segunda calificación de la misma recomendación -> uq_calificacion_recomendacion'
BEGIN;
INSERT INTO consulta (estudiante_id, pregunta, estado) VALUES (1, 'Quiero aprender a crear páginas web.', 'Respondida');
INSERT INTO recomendacion (consulta_id, respuesta) SELECT max(id), 'Respuesta de prueba' FROM consulta;
INSERT INTO calificacion (recomendacion_id, puntuacion, comentario) SELECT max(id), 5, 'Muy útil' FROM recomendacion;
INSERT INTO calificacion (recomendacion_id, puntuacion) SELECT max(id), 4 FROM recomendacion;
ROLLBACK;

\echo '### Control: nada quedó guardado (0, 0, 0, 0 y 10, 30)'
SELECT (SELECT count(*) FROM consulta) AS consultas,
       (SELECT count(*) FROM recomendacion) AS recomendaciones,
       (SELECT count(*) FROM recomendacion_fuente) AS fuentes,
       (SELECT count(*) FROM calificacion) AS calificaciones,
       (SELECT count(*) FROM estudiante) AS estudiantes,
       (SELECT count(*) FROM curso) AS cursos;
