-- Actividades cotidianas: suman puntos solo al miembro, sin revisión del MSP (HR-CU07, HR-CU08, GM-CU06).
CREATE TABLE catalogo_actividades_comunidad (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    comunidad_id    BIGINT       NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    categoria       VARCHAR(30)  NOT NULL,
    puntos_por_hora INT          NOT NULL DEFAULT 1,
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_catalogo_actividades_comunidad_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insignia propia de la comunidad que se gana al repetir una actividad (CM-CU03).
CREATE TABLE talentos_comunidad (
    id                     BIGINT       AUTO_INCREMENT PRIMARY KEY,
    comunidad_id           BIGINT       NOT NULL,
    actividad_comunidad_id BIGINT       NOT NULL,
    nombre_insignia        VARCHAR(120) NOT NULL,
    descripcion            VARCHAR(255) NOT NULL,
    meta_repeticiones      INT          NOT NULL,
    icono_codigo           VARCHAR(60)  NOT NULL DEFAULT 'insignia-semilla',
    activo                 BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_talentos_comunidad_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_talentos_comunidad_actividad FOREIGN KEY (actividad_comunidad_id)
        REFERENCES catalogo_actividades_comunidad (id),
    CONSTRAINT ck_talentos_comunidad_meta CHECK (meta_repeticiones > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE registro_actividades_miembro (
    id                     BIGINT       AUTO_INCREMENT PRIMARY KEY,
    comunidad_id           BIGINT       NOT NULL,
    miembro_id             BIGINT       NOT NULL,
    actividad_comunidad_id BIGINT       NOT NULL,
    fecha_actividad        DATE         NOT NULL,
    horas_trabajadas       INT          NOT NULL,
    puntos_otorgados       INT          NOT NULL,
    observacion            VARCHAR(255) NULL,
    creado_en              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registro_actividades_miembro_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_registro_actividades_miembro_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id),
    CONSTRAINT fk_registro_actividades_miembro_actividad FOREIGN KEY (actividad_comunidad_id)
        REFERENCES catalogo_actividades_comunidad (id),
    CONSTRAINT ck_registro_actividades_miembro_horas CHECK (horas_trabajadas BETWEEN 1 AND 24)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
