CREATE TABLE usuarios (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo  VARCHAR(120) NOT NULL,
    identificacion   VARCHAR(13)  NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    genero           VARCHAR(20)  NOT NULL,
    email            VARCHAR(255) NOT NULL,
    contrasena       VARCHAR(255) NOT NULL,
    rol              VARCHAR(30)  NOT NULL DEFAULT 'MIEMBRO',
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_identificacion UNIQUE (identificacion),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comunidades (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre                      VARCHAR(120) NOT NULL,
    tipo_comunidad              VARCHAR(30)  NOT NULL,
    descripcion_proposito       VARCHAR(250) NULL,
    nombre_responsable_contacto VARCHAR(120) NOT NULL,
    telefono_contacto           VARCHAR(15)  NOT NULL,
    email_contacto              VARCHAR(120) NOT NULL,
    horario_disponibilidad      VARCHAR(100) NULL,
    puntos_totales              INT          NOT NULL DEFAULT 0,
    posicion_ranking            INT          NULL,
    responsable_id              BIGINT       NULL,
    activa                      BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_comunidades_nombre UNIQUE (nombre),
    CONSTRAINT uk_comunidades_responsable UNIQUE (responsable_id),
    CONSTRAINT fk_comunidad_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE miembros (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id          BIGINT       NOT NULL,
    usuario_id            BIGINT       NULL,
    nombre_completo       VARCHAR(150) NOT NULL,
    identificacion        VARCHAR(20)  NOT NULL,
    fecha_nacimiento      DATE         NOT NULL,
    genero                VARCHAR(20)  NOT NULL,
    email                 VARCHAR(120) NULL,
    nivel                 VARCHAR(20)  NOT NULL DEFAULT 'APRENDIZ',
    puntos_acumulados     INT          NOT NULL DEFAULT 0,
    talentos_certificados VARCHAR(500) NULL,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_miembros_comunidad_identificacion UNIQUE (comunidad_id, identificacion),
    CONSTRAINT fk_miembro_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id) ON DELETE CASCADE,
    CONSTRAINT fk_miembro_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE huertos (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id       BIGINT        NOT NULL,
    nombre             VARCHAR(120)  NOT NULL,
    provincia          VARCHAR(80)   NOT NULL,
    parroquia          VARCHAR(80)   NOT NULL,
    direccion          VARCHAR(200)  NOT NULL,
    latitud            DECIMAL(10,7) NULL,
    longitud           DECIMAL(10,7) NULL,
    area_m2            DECIMAL(8,2)  NOT NULL,
    tipo_suelo         VARCHAR(30)   NOT NULL,
    tipo_agua_riego    VARCHAR(30)   NOT NULL,
    tipo_abono         VARCHAR(30)   NOT NULL,
    tipo_diseno        VARCHAR(30)   NULL,
    filas_grid         INT           NULL,
    columnas_grid      INT           NULL,
    ancho_pasillos_m   DECIMAL(5,2)  NULL,
    zona_compost       BOOLEAN       NOT NULL DEFAULT FALSE,
    zona_bodega        BOOLEAN       NOT NULL DEFAULT FALSE,
    tipo_cerca         VARCHAR(30)   NULL,
    detalle_materiales VARCHAR(250)  NULL,
    altura_m           DECIMAL(5,2)  NULL,
    perimetro_m        DECIMAL(8,2)  NULL,
    puertas_acceso     INT           NULL,
    estado_cerca       VARCHAR(30)   NULL,
    creado_en          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_huertos_comunidad UNIQUE (comunidad_id),
    CONSTRAINT fk_huerto_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plantas_medicinales (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comun              VARCHAR(100) NOT NULL,
    nombre_kichwa             VARCHAR(100) NULL,
    nombre_cientifico         VARCHAR(120) NOT NULL,
    familia_botanica          VARCHAR(80)  NOT NULL,
    region_distribucion       VARCHAR(100) NOT NULL,
    altitud_msnm              VARCHAR(60)  NULL,
    ciclo_cultivo             VARCHAR(20)  NOT NULL,
    cualidad_termica          VARCHAR(20)  NOT NULL,
    usos_medicinales          TEXT         NOT NULL,
    indicaciones_dosificacion TEXT         NULL,
    contraindicaciones        TEXT         NULL,
    foto_url                  VARCHAR(500) NOT NULL,
    creado_en                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_plantas_medicinales_nombre_cientifico UNIQUE (nombre_cientifico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lotes_material_vegetal (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id        BIGINT        NOT NULL,
    planta_id           BIGINT        NOT NULL,
    tipo_material       VARCHAR(30)   NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    unidad_medida       VARCHAR(20)   NOT NULL,
    fecha_ingreso       DATE          NOT NULL,
    observaciones       VARCHAR(250)  NULL,
    creado_en           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lote_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id) ON DELETE CASCADE,
    CONSTRAINT fk_lote_planta FOREIGN KEY (planta_id) REFERENCES plantas_medicinales (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
