CREATE TABLE comunidades (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(150) NOT NULL,
    tipo_organizacion VARCHAR(30)  NOT NULL,
    provincia         VARCHAR(100) NOT NULL,
    parroquia         VARCHAR(100) NOT NULL,
    direccion         VARCHAR(255) NOT NULL,
    telefono_contacto VARCHAR(20)  NOT NULL,
    correo_contacto   VARCHAR(180) NOT NULL,
    responsable_id    BIGINT       NOT NULL,
    puntos_comunidad  INT          NOT NULL DEFAULT 0,
    activa            BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_comunidades_nombre UNIQUE (nombre),
    CONSTRAINT uk_comunidades_responsable UNIQUE (responsable_id),
    CONSTRAINT fk_comunidades_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE miembros (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    cedula               VARCHAR(13)  NOT NULL,
    nombres              VARCHAR(100) NOT NULL,
    apellidos            VARCHAR(100) NOT NULL,
    fecha_nacimiento     DATE         NOT NULL,
    genero               VARCHAR(20)  NOT NULL,
    telefono             VARCHAR(20)  NULL,
    nivel                VARCHAR(20)  NOT NULL DEFAULT 'APRENDIZ',
    puntos_totales       INT          NOT NULL DEFAULT 0,
    consentimiento_datos BOOLEAN      NOT NULL,
    consentimiento_en    DATETIME     NOT NULL,
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_miembros_cedula UNIQUE (cedula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comunidad_miembros (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id        BIGINT       NOT NULL,
    miembro_id          BIGINT       NOT NULL,
    activo_en_comunidad BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_ingreso       DATE         NOT NULL,
    fecha_baja          DATE         NULL,
    motivo_baja         VARCHAR(255) NULL,
    CONSTRAINT uk_comunidad_miembros_comunidad_miembro UNIQUE (comunidad_id, miembro_id),
    CONSTRAINT fk_comunidad_miembros_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_comunidad_miembros_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
