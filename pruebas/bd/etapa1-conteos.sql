-- Etapa 1: verificación de la semilla (solo lectura)
\echo '== Estudiantes'
SELECT count(*) AS estudiantes FROM estudiante;
SELECT nivel_experiencia, count(*) FROM estudiante GROUP BY 1 ORDER BY 1;
SELECT count(*) AS correos_no_rutaia_test FROM estudiante WHERE correo NOT LIKE '%@rutaia.test';

\echo '== Cursos activos e inactivos'
SELECT activo, count(*) FROM curso GROUP BY activo ORDER BY activo DESC;

\echo '== Categorías con cursos activos (deben ser 7, cada una >= 3)'
SELECT categoria, count(*) AS activos FROM curso WHERE activo GROUP BY categoria ORDER BY categoria;
SELECT count(DISTINCT categoria) AS categorias_con_activos FROM curso WHERE activo;

\echo '== Niveles (deben ser 3)'
SELECT nivel, count(*) FROM curso GROUP BY nivel ORDER BY nivel;
SELECT count(*) AS cursos_basico FROM curso WHERE nivel = 'Básico';

\echo '== Palabras por descripción (mín. y máx.; ninguna < 90 ni > 150)'
SELECT min(p) AS minimo, max(p) AS maximo,
       count(*) FILTER (WHERE p < 90) AS menos_de_90,
       count(*) FILTER (WHERE p > 150) AS mas_de_150
FROM (SELECT array_length(regexp_split_to_array(btrim(descripcion), '\s+'), 1) AS p FROM curso) t;

\echo '== Nombres: menos de 4 palabras (0 filas)'
SELECT id, nombre FROM curso WHERE array_length(regexp_split_to_array(btrim(nombre), '\s+'), 1) < 4;

\echo '== Nombre contenido en otro nombre, sin distinguir mayúsculas (0 filas)'
SELECT a.id, a.nombre AS contenido, b.id, b.nombre AS contenedor
FROM curso a JOIN curso b ON a.id <> b.id
WHERE strpos(lower(b.nombre), lower(a.nombre)) > 0;

\echo '== Inactivos'
SELECT id, nombre, categoria FROM curso WHERE NOT activo ORDER BY id;

\echo '== Temas ajenos (cocina/gastronomía) en cursos (0 filas)'
SELECT id, nombre FROM curso WHERE descripcion ~* '(cocina|gastronom|receta|culinari)' OR nombre ~* '(cocina|gastronom)';

\echo '== Catálogo completo'
SELECT id, nombre, categoria, nivel, duracion_horas AS horas, activo,
       array_length(regexp_split_to_array(btrim(descripcion), '\s+'), 1) AS palabras
FROM curso ORDER BY id;

\echo '== IDs consecutivos 1..N sobre tablas recién creadas'
SELECT 'estudiante' AS tabla, min(id), max(id), count(*) FROM estudiante
UNION ALL SELECT 'curso', min(id), max(id), count(*) FROM curso;

\echo '== Restricciones con nombre'
SELECT conrelid::regclass AS tabla, conname, contype FROM pg_constraint
WHERE connamespace = 'public'::regnamespace ORDER BY 1, 3, 2;

\echo '== Índices'
SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' ORDER BY 1, 2;
