CREATE TABLE plantas (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comun             VARCHAR(120) NOT NULL,
    nombre_kichwa            VARCHAR(120) NULL,
    nombre_cientifico        VARCHAR(150) NOT NULL,
    familia_botanica         VARCHAR(100) NOT NULL,
    cualidad_termica         VARCHAR(20)  NOT NULL,
    ciclo_cultivo            VARCHAR(20)  NULL,
    origen                   VARCHAR(20)  NULL,
    altitud_min_msnm         INT          NULL,
    altitud_max_msnm         INT          NULL,
    regiones_distribucion    VARCHAR(150) NULL,
    usos_medicinales         TEXT         NOT NULL,
    preparacion_dosificacion TEXT         NULL,
    contraindicaciones       TEXT         NULL,
    fotografia_url           VARCHAR(500) NULL,
    activa                   BOOLEAN      NOT NULL DEFAULT TRUE,
    motivo_baja              VARCHAR(300) NULL,
    fecha_baja               DATETIME     NULL,
    dado_de_baja_por_id      BIGINT       NULL,
    actualizado_por_id       BIGINT       NULL,
    creado_en                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_plantas_nombre_cientifico UNIQUE (nombre_cientifico),
    CONSTRAINT fk_plantas_dado_de_baja_por FOREIGN KEY (dado_de_baja_por_id) REFERENCES usuarios (id),
    CONSTRAINT fk_plantas_actualizado_por FOREIGN KEY (actualizado_por_id) REFERENCES usuarios (id),
    CONSTRAINT ck_plantas_rango_altitud CHECK (
        altitud_min_msnm IS NULL OR altitud_max_msnm IS NULL OR altitud_min_msnm <= altitud_max_msnm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_plantas_nombre_comun ON plantas (nombre_comun);
CREATE INDEX idx_plantas_nombre_kichwa ON plantas (nombre_kichwa);

-- Una planta puede tener varias categorías de uso (CP-CU03 filtra por ellas).
-- En Java: @ElementCollection con @Enumerated(EnumType.STRING) sobre Set<CategoriaUso>.
CREATE TABLE planta_categorias (
    planta_id BIGINT      NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    CONSTRAINT pk_planta_categorias PRIMARY KEY (planta_id, categoria),
    CONSTRAINT fk_planta_categorias_planta FOREIGN KEY (planta_id) REFERENCES plantas (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
