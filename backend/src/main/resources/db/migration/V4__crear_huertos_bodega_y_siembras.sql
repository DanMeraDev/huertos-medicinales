-- El huerto se crea al aprobar la solicitud (US-CU06): nombre_huerto toma el nombre de la comunidad.
-- El área no viene en la solicitud: el Responsable la completa en HR-CU03.
CREATE TABLE huertos (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id     BIGINT         NOT NULL,
    nombre_huerto    VARCHAR(150)   NOT NULL,
    latitud          DECIMAL(10, 7) NOT NULL,
    longitud         DECIMAL(10, 7) NOT NULL,
    altitud_msnm     INT            NOT NULL DEFAULT 2500,
    orientacion      VARCHAR(30)    NULL,
    area_total_m2    DECIMAL(8, 2)  NULL,
    tipo_huerto      VARCHAR(20)    NOT NULL DEFAULT 'BASICO',
    diseno_principal VARCHAR(30)    NOT NULL,
    tipo_suelo       VARCHAR(30)    NULL,
    fuente_agua      VARCHAR(30)    NULL,
    tipo_abono       VARCHAR(30)    NULL,
    tipo_cerca       VARCHAR(30)    NULL,
    detalle_cerca    VARCHAR(255)   NULL,
    puntos_huerto    INT            NOT NULL DEFAULT 0,
    activo           BOOLEAN        NOT NULL DEFAULT TRUE,
    creado_en        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_huertos_comunidad UNIQUE (comunidad_id),
    CONSTRAINT fk_huertos_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT ck_huertos_area CHECK (area_total_m2 > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Los subhuertos se crean en HR-CU04, no al aprobar la solicitud.
-- Hasta 3 subhuertos por huerto: la regla se valida en el servicio (HR-CU04).
CREATE TABLE subhuertos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    huerto_id   BIGINT        NOT NULL,
    nombre_zona VARCHAR(100)  NOT NULL,
    subtipo     VARCHAR(30)   NOT NULL,
    area_m2     DECIMAL(8, 2) NOT NULL,
    activo      BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_en   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subhuertos_huerto FOREIGN KEY (huerto_id) REFERENCES huertos (id),
    CONSTRAINT ck_subhuertos_area CHECK (area_m2 > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cada fila es un lote de material vegetal de una comunidad (BO-CU01, BO-CU02).
CREATE TABLE bodega_material_vegetal (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id        BIGINT         NOT NULL,
    planta_id           BIGINT         NOT NULL,
    tipo_material       VARCHAR(20)    NOT NULL,
    cantidad_disponible DECIMAL(10, 2) NOT NULL,
    unidad_medida       VARCHAR(20)    NOT NULL DEFAULT 'UNIDADES',
    lote_origen         VARCHAR(100)   NULL,
    fecha_ingreso       DATE           NOT NULL,
    activo              BOOLEAN        NOT NULL DEFAULT TRUE,
    creado_en           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bodega_material_vegetal_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_bodega_material_vegetal_planta FOREIGN KEY (planta_id) REFERENCES plantas (id),
    CONSTRAINT ck_bodega_material_vegetal_cantidad CHECK (cantidad_disponible >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Historial de bajas de un lote, con cantidad y causa (BO-CU09).
CREATE TABLE bajas_material_vegetal (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    bodega_material_id BIGINT         NOT NULL,
    cantidad_baja      DECIMAL(10, 2) NOT NULL,
    causa              VARCHAR(30)    NOT NULL,
    observacion        VARCHAR(300)   NULL,
    fecha_baja         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bajas_material_vegetal_bodega FOREIGN KEY (bodega_material_id)
        REFERENCES bodega_material_vegetal (id),
    CONSTRAINT ck_bajas_material_vegetal_cantidad CHECK (cantidad_baja > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Siembra en un subhuerto a partir de un lote de bodega (HR-CU01 incluye BO-CU03).
-- La planta y el tipo de material salen del lote; la señalética sale de plantas.cualidad_termica.
CREATE TABLE siembras (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    subhuerto_id        BIGINT         NOT NULL,
    bodega_material_id  BIGINT         NOT NULL,
    cantidad_sembrada   DECIMAL(10, 2) NOT NULL,
    fecha_siembra       DATE           NOT NULL,
    fase_lunar_siembra  VARCHAR(30)    NOT NULL,
    tuvo_alerta_altitud BOOLEAN        NOT NULL DEFAULT FALSE,
    estado_cultivo      VARCHAR(20)    NOT NULL DEFAULT 'SEMBRADO',
    causa_perdida       VARCHAR(30)    NULL,
    observacion_perdida VARCHAR(300)   NULL,
    creado_en           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_siembras_subhuerto FOREIGN KEY (subhuerto_id) REFERENCES subhuertos (id),
    CONSTRAINT fk_siembras_bodega FOREIGN KEY (bodega_material_id) REFERENCES bodega_material_vegetal (id),
    CONSTRAINT ck_siembras_cantidad CHECK (cantidad_sembrada > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
