SET client_encoding = 'UTF8';

-- =====================================================================
-- RutaIA - Esquema relacional (contrato C4)
-- PostgreSQL 17, esquema public. Este script es la única fuente del
-- esquema: Spring Boot solo lo valida (ddl-auto=validate).
-- Es repetible: borra y recrea las tablas (se pierden los datos; después
-- hay que recargar 02_datos_semilla.sql y re-ejecutar la indexación).
-- =====================================================================

-- Borrado en orden inverso de dependencias
DROP TABLE IF EXISTS calificacion CASCADE;
DROP TABLE IF EXISTS recomendacion_fuente CASCADE;
DROP TABLE IF EXISTS recomendacion CASCADE;
DROP TABLE IF EXISTS consulta CASCADE;
DROP TABLE IF EXISTS curso CASCADE;
DROP TABLE IF EXISTS estudiante CASCADE;

-- ---------------------------------------------------------------------
-- estudiante (RF 01)
-- ---------------------------------------------------------------------
CREATE TABLE estudiante (
    id                BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre_completo   VARCHAR(120) NOT NULL,
    correo            VARCHAR(254) NOT NULL,
    nivel_experiencia VARCHAR(20)  NOT NULL,
    area_interes      VARCHAR(100) NOT NULL,
    fecha_registro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_estudiante PRIMARY KEY (id),
    CONSTRAINT ck_estudiante_nombre_no_vacio CHECK (btrim(nombre_completo) <> ''),
    CONSTRAINT ck_estudiante_correo_normalizado CHECK (correo = lower(btrim(correo))),
    CONSTRAINT ck_estudiante_nivel CHECK (nivel_experiencia IN ('Principiante', 'Intermedio', 'Avanzado')),
    CONSTRAINT ck_estudiante_area_no_vacia CHECK (btrim(area_interes) <> ''),
    -- Índice único uq_estudiante_correo (RF 01): impide correos duplicados y
    -- sirve la búsqueda del estudiante por correo. Junto con
    -- ck_estudiante_correo_normalizado, «Ana@x» y «ana@x» no pueden coexistir.
    CONSTRAINT uq_estudiante_correo UNIQUE (correo)
);

-- ---------------------------------------------------------------------
-- curso (RF 03)
-- ---------------------------------------------------------------------
CREATE TABLE curso (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre         VARCHAR(150) NOT NULL,
    descripcion    TEXT         NOT NULL,
    categoria      VARCHAR(80)  NOT NULL,
    nivel          VARCHAR(20)  NOT NULL,
    duracion_horas INTEGER      NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_curso PRIMARY KEY (id),
    CONSTRAINT ck_curso_nombre_no_vacio CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_curso_descripcion_no_vacia CHECK (btrim(descripcion) <> ''),
    CONSTRAINT ck_curso_categoria_no_vacia CHECK (btrim(categoria) <> ''),
    CONSTRAINT ck_curso_nivel CHECK (nivel IN ('Básico', 'Intermedio', 'Avanzado')),
    CONSTRAINT ck_curso_duracion_positiva CHECK (duracion_horas > 0)
);

-- idx_curso_catalogo (RF 04): el catálogo se filtra casi siempre por cursos
-- activos y, opcionalmente, por categoría y nivel. El orden (activo,
-- categoria, nivel) cubre los filtros «activo», «activo+categoría» y
-- «activo+categoría+nivel» con un solo índice.
CREATE INDEX idx_curso_catalogo ON curso (activo, categoria, nivel);

-- ---------------------------------------------------------------------
-- consulta (RF 07)
-- ---------------------------------------------------------------------
CREATE TABLE consulta (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    estudiante_id BIGINT       NOT NULL,
    pregunta      VARCHAR(500) NOT NULL,
    fecha         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'Pendiente',
    detalle_error TEXT         NULL,
    CONSTRAINT pk_consulta PRIMARY KEY (id),
    CONSTRAINT fk_consulta_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiante (id),
    CONSTRAINT ck_consulta_pregunta_no_vacia CHECK (btrim(pregunta) <> ''),
    CONSTRAINT ck_consulta_estado CHECK (estado IN ('Pendiente', 'Respondida', 'Sin resultados', 'Error'))
);

-- idx_consulta_estudiante_fecha (historial): el historial de un estudiante se
-- pide por estudiante_id y se ordena de la más reciente a la más antigua;
-- el índice devuelve las filas ya ordenadas, sin ordenar en memoria. También
-- sirve para la FK fk_consulta_estudiante (PostgreSQL no indexa las FK solo).
CREATE INDEX idx_consulta_estudiante_fecha ON consulta (estudiante_id, fecha DESC);

-- idx_consulta_estado (estadísticas): las estadísticas cuentan consultas por
-- estado (Respondida, Sin resultados, Error) y localizan las Pendiente.
CREATE INDEX idx_consulta_estado ON consulta (estado);

-- ---------------------------------------------------------------------
-- recomendacion (RF 14)
-- ---------------------------------------------------------------------
CREATE TABLE recomendacion (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    consulta_id BIGINT    NOT NULL,
    respuesta   TEXT      NOT NULL,
    fecha       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_recomendacion PRIMARY KEY (id),
    CONSTRAINT fk_recomendacion_consulta FOREIGN KEY (consulta_id) REFERENCES consulta (id),
    CONSTRAINT ck_recomendacion_respuesta_no_vacia CHECK (btrim(respuesta) <> ''),
    -- Índice único uq_recomendacion_consulta (relación 1:1): una consulta
    -- tiene como máximo una recomendación; además indexa la FK para pasar
    -- de la consulta a su recomendación.
    CONSTRAINT uq_recomendacion_consulta UNIQUE (consulta_id)
);

-- ---------------------------------------------------------------------
-- recomendacion_fuente (RF 14: varios cursos como fuentes)
-- ---------------------------------------------------------------------
CREATE TABLE recomendacion_fuente (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    recomendacion_id BIGINT           NOT NULL,
    curso_id         BIGINT           NOT NULL,
    similitud        DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_recomendacion_fuente PRIMARY KEY (id),
    CONSTRAINT fk_fuente_recomendacion FOREIGN KEY (recomendacion_id) REFERENCES recomendacion (id),
    CONSTRAINT fk_fuente_curso FOREIGN KEY (curso_id) REFERENCES curso (id),
    CONSTRAINT ck_fuente_similitud CHECK (similitud BETWEEN 0 AND 1),
    -- Índice único uq_fuente_recomendacion_curso: un curso aparece una sola
    -- vez como fuente de la misma recomendación; como empieza por
    -- recomendacion_id, también sirve para leer las fuentes de una
    -- recomendación (y cubre la FK fk_fuente_recomendacion).
    CONSTRAINT uq_fuente_recomendacion_curso UNIQUE (recomendacion_id, curso_id)
);

-- idx_fuente_curso (estadística «curso más recomendado»): agrupa y cuenta las
-- fuentes por curso_id; el índice único anterior no sirve porque curso_id es
-- su segunda columna. También cubre la FK fk_fuente_curso.
CREATE INDEX idx_fuente_curso ON recomendacion_fuente (curso_id);

-- ---------------------------------------------------------------------
-- calificacion (RF 17)
-- ---------------------------------------------------------------------
CREATE TABLE calificacion (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    recomendacion_id BIGINT       NOT NULL,
    puntuacion       INTEGER      NOT NULL,
    comentario       VARCHAR(500) NULL,
    fecha            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_calificacion PRIMARY KEY (id),
    CONSTRAINT fk_calificacion_recomendacion FOREIGN KEY (recomendacion_id) REFERENCES recomendacion (id),
    CONSTRAINT ck_calificacion_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    -- Índice único uq_calificacion_recomendacion (RF 17): una sola
    -- calificación por recomendación; además indexa la FK.
    CONSTRAINT uq_calificacion_recomendacion UNIQUE (recomendacion_id)
);
