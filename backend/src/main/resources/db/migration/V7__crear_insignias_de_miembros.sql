-- Vitrina de cada miembro: insignias oficiales del MSP y talentos de su comunidad (GM-CU03, GM-CU06, CM-CU08).
-- Las UNIQUE impiden ganar dos veces la misma insignia; MariaDB permite varios NULL en ellas.
CREATE TABLE miembro_insignias (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    miembro_id           BIGINT       NOT NULL,
    comunidad_id         BIGINT       NOT NULL,
    tipo_origen          VARCHAR(30)  NOT NULL,
    nombre_insignia      VARCHAR(120) NOT NULL,
    descripcion          VARCHAR(255) NOT NULL,
    icono_codigo         VARCHAR(60)  NOT NULL DEFAULT 'medalla-kinti',
    saber_ancestral_id   BIGINT       NULL,
    talento_comunidad_id BIGINT       NULL,
    fecha_obtencion      DATE         NOT NULL,
    CONSTRAINT uk_miembro_insignias_miembro_saber UNIQUE (miembro_id, saber_ancestral_id),
    CONSTRAINT uk_miembro_insignias_miembro_talento UNIQUE (miembro_id, talento_comunidad_id),
    CONSTRAINT fk_miembro_insignias_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id),
    CONSTRAINT fk_miembro_insignias_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_miembro_insignias_saber FOREIGN KEY (saber_ancestral_id) REFERENCES saberes_ancestrales (id),
    CONSTRAINT fk_miembro_insignias_talento FOREIGN KEY (talento_comunidad_id) REFERENCES talentos_comunidad (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
