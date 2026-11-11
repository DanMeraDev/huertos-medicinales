CREATE TABLE calendario_lunar (
    id                     BIGINT       AUTO_INCREMENT PRIMARY KEY,
    fase_lunar             VARCHAR(30)  NOT NULL,
    nombre_kichwa          VARCHAR(80)  NOT NULL,
    nombre_espanol         VARCHAR(80)  NOT NULL,
    fecha_inicio           DATE         NOT NULL,
    fecha_fin              DATE         NOT NULL,
    practicas_recomendadas TEXT         NOT NULL,
    consejo_kinti          VARCHAR(300) NOT NULL,
    CONSTRAINT ck_calendario_lunar_fechas CHECK (fecha_inicio <= fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ruta secuencial de saberes del MSP; cada paso otorga una insignia oficial (SA-CU10, GM-CU03).
CREATE TABLE saberes_ancestrales (
    id                        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    orden_paso                INT          NOT NULL,
    codigo_hito               VARCHAR(40)  NOT NULL,
    titulo                    VARCHAR(150) NOT NULL,
    titulo_kichwa             VARCHAR(150) NULL,
    categoria                 VARCHAR(30)  NOT NULL,
    fase_lunar_ideal          VARCHAR(30)  NOT NULL,
    horario_energetico        VARCHAR(100) NOT NULL,
    contenido_educativo       TEXT         NOT NULL,
    ingredientes_herramientas TEXT         NULL,
    puntos_base_comunidad     INT          NOT NULL DEFAULT 20,
    nombre_insignia_oficial   VARCHAR(120) NOT NULL,
    CONSTRAINT uk_saberes_ancestrales_orden_paso UNIQUE (orden_paso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actividad grande con evidencia que revisa el MSP; al aprobarse suma a comunidad, huerto y miembros (SA-CU04 a SA-CU06, GM-CU01).
CREATE TABLE actividades_ancestrales_msp (
    id                         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    comunidad_id               BIGINT       NOT NULL,
    huerto_id                  BIGINT       NOT NULL,
    saber_ancestral_id         BIGINT       NOT NULL,
    fecha_realizacion          DATE         NOT NULL,
    fase_lunar_ejecucion       VARCHAR(30)  NOT NULL,
    descripcion_bitacora       TEXT         NOT NULL,
    evidencia_foto_url         VARCHAR(500) NOT NULL,
    evidencia_video_url        VARCHAR(500) NULL,
    numero_intento             INT          NOT NULL DEFAULT 1,
    estado_verificacion        VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    observacion_msp            VARCHAR(500) NULL,
    puntos_acreditar_comunidad INT          NOT NULL DEFAULT 0,
    verificado_por_id          BIGINT       NULL,
    fecha_verificacion         DATETIME     NULL,
    creado_en                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_actividades_ancestrales_msp_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_actividades_ancestrales_msp_huerto FOREIGN KEY (huerto_id) REFERENCES huertos (id),
    CONSTRAINT fk_actividades_ancestrales_msp_saber FOREIGN KEY (saber_ancestral_id)
        REFERENCES saberes_ancestrales (id),
    CONSTRAINT fk_actividades_ancestrales_msp_verificador FOREIGN KEY (verificado_por_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE actividad_msp_participantes (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    actividad_msp_id   BIGINT NOT NULL,
    miembro_id         BIGINT NOT NULL,
    horas_trabajadas   INT    NOT NULL,
    puntos_acreditados INT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_actividad_msp_participantes_actividad_miembro UNIQUE (actividad_msp_id, miembro_id),
    CONSTRAINT fk_actividad_msp_participantes_actividad FOREIGN KEY (actividad_msp_id)
        REFERENCES actividades_ancestrales_msp (id),
    CONSTRAINT fk_actividad_msp_participantes_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id),
    CONSTRAINT ck_actividad_msp_participantes_horas CHECK (horas_trabajadas BETWEEN 1 AND 24)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
