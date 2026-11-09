-- =============================================================================
-- V2__esquema_completo_v1.sql
-- Esquema unificado para la Versión 1.0 - Sistema de Huertos Medicinales MSP
-- Motor: MariaDB 11.4 | Juego de caracteres: utf8mb4
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. MÓDULO USUARIOS Y SOLICITUDES (US)
-- -----------------------------------------------------------------------------
-- Ajustamos la tabla usuarios creada en V1 para soportar TECNICO_SIS y cambio de clave en 1er login (US-CU01, US-CU02, US-CU03)
ALTER TABLE usuarios MODIFY COLUMN rol ENUM (
    'TECNICO_SIS',
    'TECNICO_MSP',
    'RESPONSABLE_COMUNIDAD'
) NOT NULL,
ADD COLUMN telefono VARCHAR(20) NULL AFTER correo,
ADD COLUMN requiere_cambio_clave BOOLEAN NOT NULL DEFAULT TRUE AFTER password_hash;

-- Solicitudes públicas para crear una comunidad y su huerto (US-CU04, US-CU05, US-CU07)
CREATE TABLE solicitudes_comunidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comunidad VARCHAR(150) NOT NULL,
    tipo_organizacion ENUM (
        'INDIVIDUAL',
        'FAMILIA',
        'CONDOMINIO',
        'BARRIO',
        'COMITE_LOCAL'
    ) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    parroquia VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    latitud DECIMAL(10, 7) NOT NULL,
    longitud DECIMAL(10, 7) NOT NULL,
    altitud_msnm INT NOT NULL DEFAULT 2500,
    diseno_principal ENUM ('EN_TIERRA', 'VERTICAL', 'COLGANTE', 'EN_PISO') NOT NULL,
    subtipos_solicitados VARCHAR(150) NOT NULL COMMENT 'Hasta 3 subtipos separados por coma: SURCOS,TERRAZAS,MACETAS',
    -- Datos del solicitante (futuro Responsable de Comunidad)
    cedula_solicitante VARCHAR(13) NOT NULL,
    nombre_solicitante VARCHAR(120) NOT NULL,
    correo_solicitante VARCHAR(180) NOT NULL,
    celular_solicitante VARCHAR(20) NOT NULL,
    genero_solicitante ENUM ('FEMENINO', 'MASCULINO', 'OTRO') NOT NULL DEFAULT 'OTRO',
    -- Estado de revisión MSP
    estado ENUM ('PENDIENTE', 'APROBADA', 'RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo VARCHAR(500) NULL,
    revisado_por_id BIGINT NULL,
    fecha_revision DATETIME (6) NULL,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_solicitud_revisor FOREIGN KEY (revisado_por_id) REFERENCES usuarios (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. MÓDULO COMUNIDADES Y MIEMBROS (CM)
-- -----------------------------------------------------------------------------
-- Comunidad de siembra registrada tras aprobar solicitud (US-CU06, CM-CU01, CM-CU02, GM-CU04)
CREATE TABLE comunidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo_organizacion ENUM (
        'INDIVIDUAL',
        'FAMILIA',
        'CONDOMINIO',
        'BARRIO',
        'COMITE_LOCAL'
    ) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    parroquia VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    telefono_contacto VARCHAR(20) NOT NULL,
    correo_contacto VARCHAR(180) NOT NULL,
    responsable_id BIGINT NOT NULL,
    puntos_comunidad INT NOT NULL DEFAULT 0 COMMENT 'Puntos ganados por actividades oficiales aprobadas por MSP',
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT uk_comunidades_nombre UNIQUE (nombre),
    CONSTRAINT fk_comunidad_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Tabla de Voluntarios / Miembros sin cuenta de login (CM-CU04, CM-CU06, CM-CU08, GM-CU02, GM-CU05)
CREATE TABLE miembros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(13) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero ENUM ('FEMENINO', 'MASCULINO', 'OTRO') NOT NULL,
    telefono VARCHAR(20) NULL,
    nivel ENUM ('APRENDIZ', 'OFICIAL', 'MAESTRO') NOT NULL DEFAULT 'APRENDIZ',
    puntos_totales INT NOT NULL DEFAULT 0 COMMENT 'Suma de horas/puntos en actividades cotidianas y ancestrales',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT uk_miembros_cedula UNIQUE (cedula)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Relación N:M porque un voluntario puede pertenecer a varios huertos/comunidades (CM-CU04, CM-CU05, CM-CU07)
CREATE TABLE comunidad_miembros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    activo_en_comunidad BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_ingreso DATE NOT NULL,
    fecha_baja DATE NULL,
    motivo_baja VARCHAR(255) NULL,
    CONSTRAINT uk_comunidad_miembro UNIQUE (comunidad_id, miembro_id),
    CONSTRAINT fk_cm_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_cm_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. MÓDULO CATÁLOGO DE PLANTAS MEDICINALES (CP)
-- -----------------------------------------------------------------------------
-- Catálogo oficial administrado por el MSP (CP-CU01 a CP-CU08)
CREATE TABLE plantas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comun VARCHAR(120) NOT NULL,
    nombre_kichwa VARCHAR(120) NULL COMMENT 'Ej: Caballo Chupa, Chakra Kiwa, Wais',
    nombre_cientifico VARCHAR(150) NOT NULL,
    familia_botanica VARCHAR(100) NOT NULL COMMENT 'Ej: Equisetaceae, Lamiaceae, Poaceae',
    cualidad_termica ENUM ('CALIENTE', 'FRIA') NOT NULL COMMENT 'Rojo = Caliente, Verde/Blanco = Fría/Fresca',
    ciclo_cultivo ENUM ('CORTO', 'LARGO') NOT NULL COMMENT 'Corto: 6-8 semanas, Largo: >8 semanas',
    categoria_uso ENUM (
        'ANTIESPASMODICA',
        'ANTIINFLAMATORIA',
        'ANTIOXIDANTE',
        'ANTIPARASITARIA',
        'ANTIPYRETICA',
        'ASTRINGENTE',
        'CICATRIZANTE',
        'DIURETICA',
        'ENERGETICA',
        'DEPURATIVA',
        'DESCONGESTIONANTE',
        'ESTIMULANTE',
        'EXPECTORANTE',
        'SEDANTE'
    ) NOT NULL,
    origen ENUM ('NATIVA', 'ENDEMICA', 'INTRODUCIDA') NOT NULL DEFAULT 'NATIVA',
    altitud_min_msnm INT NOT NULL DEFAULT 0,
    altitud_max_msnm INT NOT NULL DEFAULT 4000,
    regiones_distribucion VARCHAR(150) NOT NULL COMMENT 'Ej: SIERRA,COSTA,AMAZONIA',
    usos_medicinales TEXT NOT NULL,
    preparacion_dosificacion TEXT NOT NULL COMMENT 'Ej: Infusión, Emplasto, Cocción, Maytu',
    contraindicaciones TEXT NULL COMMENT 'Ej: No comer ají, sal ni bebidas alcohólicas',
    fotografia_url VARCHAR(500) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT uk_plantas_cientifico UNIQUE (nombre_cientifico)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. MÓDULO HUERTO REAL, SUBHUERTOS Y SIEMBRA (HR)
-- -----------------------------------------------------------------------------
-- Configuración física y territorial del Huerto de la Comunidad (HR-CU03, GM-CU07)
CREATE TABLE huertos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    nombre_huerto VARCHAR(150) NOT NULL,
    latitud DECIMAL(10, 7) NOT NULL,
    longitud DECIMAL(10, 7) NOT NULL,
    altitud_msnm INT NOT NULL DEFAULT 2500,
    orientacion VARCHAR(50) NOT NULL DEFAULT 'ESTE_OESTE',
    area_total_m2 DECIMAL(8, 2) NOT NULL DEFAULT 16.00 COMMENT 'De 16m2 a 400m2 según MSP',
    tipo_huerto ENUM ('BASICO', 'INTEGRAL') NOT NULL DEFAULT 'BASICO',
    diseno_principal ENUM ('EN_TIERRA', 'VERTICAL', 'COLGANTE', 'EN_PISO') NOT NULL DEFAULT 'EN_TIERRA',
    tipo_suelo ENUM (
        'ARCILLOSO',
        'LIMOSO',
        'ARENOSO',
        'MARGOSO',
        'GREDOSO',
        'PANTANOSO'
    ) NULL,
    fuente_agua ENUM (
        'RIO',
        'CANAL',
        'GOTEO',
        'VERTIENTE_LLUVIA',
        'GRIFO_REPOSADO'
    ) NULL,
    tipo_abono ENUM (
        'BARBECHO',
        'RESTOS_VEGETALES',
        'HOJAS_NITROGENO',
        'COMPOST',
        'PURIN_LIQUIDO',
        'ESTIERCOL_SECADO'
    ) NULL,
    tipo_cerca ENUM (
        'CERCA_VIVA',
        'MATERIAL_NATURAL',
        'MATERIAL_RECICLADO',
        'MATERIAL_METALICO',
        'CONVENCIONAL'
    ) NULL,
    detalle_cerca VARCHAR(255) NULL COMMENT 'Ej: Higuerilla y floripondio, o caña guadua',
    puntos_huerto INT NOT NULL DEFAULT 0 COMMENT 'Puntos acumulados por el huerto para el ranking normalizado',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT uk_huerto_comunidad UNIQUE (comunidad_id),
    CONSTRAINT fk_huerto_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Subhuertos o zonas de cultivo: hasta 3 subtipos por huerto (HR-CU04)
CREATE TABLE subhuertos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    huerto_id BIGINT NOT NULL,
    nombre_zona VARCHAR(100) NOT NULL COMMENT 'Ej: Wachu Norte, Terraza 1, Pared Vertical',
    subtipo ENUM (
        'SURCOS_WACHU',
        'TERRAZAS_ANTI',
        'CAMELLONES_INKA_WACHU',
        'VERTICAL_PARED',
        'COLGANTE_TECHO',
        'MACETAS_PISO',
        'LLANTAS_RECICLADAS'
    ) NOT NULL,
    area_m2 DECIMAL(8, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_subhuerto_huerto FOREIGN KEY (huerto_id) REFERENCES huertos (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. MÓDULO BODEGA DE LA COMUNIDAD (BO)
-- -----------------------------------------------------------------------------
-- Inventario de material vegetal vivo por comunidad (BO-CU01, BO-CU02, BO-CU03, BO-CU09)
CREATE TABLE bodega_material_vegetal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    planta_id BIGINT NOT NULL,
    tipo_material ENUM ('SEMILLAS', 'ESTACAS', 'ESQUEJES', 'PLANTULAS') NOT NULL,
    cantidad_disponible INT NOT NULL,
    unidad_medida ENUM ('UNIDADES', 'GRAMOS', 'MANOJOS') NOT NULL DEFAULT 'UNIDADES',
    lote_origen VARCHAR(100) NULL,
    fecha_ingreso DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_bodega_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_bodega_planta FOREIGN KEY (planta_id) REFERENCES plantas (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Historial de bajas de material vegetal o cosecha dañada con causas de fracaso (BO-CU09)
CREATE TABLE bajas_material_vegetal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bodega_material_id BIGINT NOT NULL,
    cantidad_baja INT NOT NULL,
    causa_fracaso ENUM (
        'HELADA_CLIMA_EXTREMO',
        'PLAGA_ENFERMEDAD',
        'SEQUIA_FALTA_RIEGO',
        'SUELO_INADECUADO',
        'DANIO_EXTERNO',
        'CADUCIDAD_PUDRICION'
    ) NOT NULL,
    observacion VARCHAR(300) NULL,
    fecha_baja DATETIME (6) NOT NULL,
    CONSTRAINT fk_baja_bodega FOREIGN KEY (bodega_material_id) REFERENCES bodega_material_vegetal (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Registro de siembras en un subhuerto que descuenta de bodega (HR-CU01 -> BO-CU03)
CREATE TABLE siembras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subhuerto_id BIGINT NOT NULL,
    planta_id BIGINT NOT NULL,
    bodega_material_id BIGINT NOT NULL,
    tipo_siembra ENUM ('SEMILLAS', 'ESTACAS', 'ESQUEJES', 'PLANTULAS') NOT NULL,
    cantidad_sembrada INT NOT NULL,
    fase_lunar_siembra ENUM (
        'YANA_KILLA_OSCURA',
        'WINAK_KILLA_CRECIENTE',
        'PURA_KILLA_LLENA',
        'WANUK_KILLA_MENGUANTE'
    ) NOT NULL,
    tuvo_alerta_altitud BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'True si la planta estaba fuera del rango msnm pero el responsable confirmó',
    color_senaletica ENUM ('ROJO_CALIENTE', 'VERDE_BLANCO_FRIA') NOT NULL,
    estado_cultivo ENUM (
        'SEMBRADO',
        'EN_CRECIMIENTO',
        'COSECHADO',
        'PERDIDO'
    ) NOT NULL DEFAULT 'SEMBRADO',
    fecha_siembra DATE NOT NULL,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_siembra_subhuerto FOREIGN KEY (subhuerto_id) REFERENCES subhuertos (id),
    CONSTRAINT fk_siembra_planta FOREIGN KEY (planta_id) REFERENCES plantas (id),
    CONSTRAINT fk_siembra_bodega FOREIGN KEY (bodega_material_id) REFERENCES bodega_material_vegetal (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. MÓDULO ACTIVIDADES COTIDIANAS Y TALENTOS DE COMUNIDAD (HR-CU07, HR-CU08, CM-CU03, GM-CU06)
-- (FLUJO 2: SIN APROBACIÓN MSP - Puntos solo a miembros y talentos internos)
-- -----------------------------------------------------------------------------
-- Catálogo de actividades cotidianas definido por cada Responsable de Comunidad (HR-CU07)
CREATE TABLE catalogo_actividades_comunidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL COMMENT 'Ej: Abonar camas, Deshierbar surcos, Regar con regadera',
    categoria ENUM (
        'LIMPIEZA_DESHIERBE',
        'RIEGO',
        'ABONO_COMPOST',
        'REPLANTE_APORQUE',
        'CUIDADO_GENERAL'
    ) NOT NULL,
    puntos_por_hora INT NOT NULL DEFAULT 1 COMMENT 'Regla MSP: 1 hora = 1 punto base',
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_cat_act_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Talentos o Insignias personalizados por cada Comunidad (CM-CU03: ej. Maestro del Abonaje = 50 abonos)
CREATE TABLE talentos_comunidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    actividad_comunidad_id BIGINT NOT NULL,
    nombre_insignia VARCHAR(120) NOT NULL COMMENT 'Ej: Maestro del Abonaje, Guardián del Agua',
    descripcion VARCHAR(255) NOT NULL,
    meta_repeticiones INT NOT NULL COMMENT 'Ej: 50 ejecuciones de la actividad asociada',
    icono_codigo VARCHAR(60) NOT NULL DEFAULT 'insignia-semilla',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_talento_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_talento_actividad FOREIGN KEY (actividad_comunidad_id) REFERENCES catalogo_actividades_comunidad (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Registro de actividad cotidiana ejecutada por un miembro (HR-CU08 -> GM-CU06)
CREATE TABLE registro_actividades_miembro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    actividad_comunidad_id BIGINT NOT NULL,
    fecha_actividad DATE NOT NULL,
    horas_trabajadas INT NOT NULL DEFAULT 1 COMMENT 'Máximo 24 horas por día según entrevista MSP',
    puntos_otorgados INT NOT NULL,
    observacion VARCHAR(255) NULL,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_reg_act_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_reg_act_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id),
    CONSTRAINT fk_reg_act_catalogo FOREIGN KEY (actividad_comunidad_id) REFERENCES catalogo_actividades_comunidad (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. MÓDULO SABERES ANCESTRALES, CALENDARIO LUNAR Y ACTIVIDADES MSP (SA + GM)
-- (FLUJO 1: REQUIERE APROBACIÓN MSP - Puntos a Comunidad, Huerto y Miembros)
-- -----------------------------------------------------------------------------
-- Calendario Lunar y Reloj Agrícola Andino (SA-CU01)
CREATE TABLE calendario_lunar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fase_lunar ENUM (
        'YANA_KILLA_OSCURA',
        'WINAK_KILLA_CRECIENTE',
        'PURA_KILLA_LLENA',
        'WANUK_KILLA_MENGUANTE'
    ) NOT NULL,
    nombre_kichwa VARCHAR(80) NOT NULL,
    nombre_espanol VARCHAR(80) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    practicas_recomendadas TEXT NOT NULL COMMENT 'Ej: Ritual de siembra 8AM, retiro de maleza, cosecha de raíces',
    consejo_kinti VARCHAR(300) NOT NULL COMMENT 'Mensaje que muestra Kinti en su versión nocturna'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Biblioteca de Saberes Ancestrales y Ruta Secuencial del MSP (SA-CU10)
CREATE TABLE saberes_ancestrales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    orden_paso INT NOT NULL COMMENT 'Paso secuencial N (1: Preparación tierra, 2: Abono, 3: Ritual Siembra...)',
    codigo_hito VARCHAR(40) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    titulo_kichwa VARCHAR(150) NULL,
    categoria ENUM (
        'PREPARACION_TIERRA',
        'ABONO_NATURAL',
        'RITUAL_SIEMBRA',
        'SIEMBRA_SENALETICA',
        'CUIDADO_RIEGO',
        'RITUAL_COSECHA',
        'SECADO_EMPAQUETADO'
    ) NOT NULL,
    fase_lunar_ideal ENUM (
        'YANA_KILLA_OSCURA',
        'WINAK_KILLA_CRECIENTE',
        'PURA_KILLA_LLENA',
        'WANUK_KILLA_MENGUANTE'
    ) NOT NULL,
    horario_energetico VARCHAR(100) NOT NULL COMMENT 'Ej: 05:00 a 07:00 AM (Salida del sol) o 08:00 AM',
    contenido_educativo TEXT NOT NULL,
    ingredientes_herramientas TEXT NULL,
    puntos_base_comunidad INT NOT NULL DEFAULT 20,
    nombre_insignia_oficial VARCHAR(120) NOT NULL COMMENT 'Insignia que desbloquea al aprobarse por MSP (GM-CU03)',
    CONSTRAINT uk_saberes_orden UNIQUE (orden_paso)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Actividades grandes registradas con evidencia fotográfica/video para revisión del MSP (SA-CU04, SA-CU05, SA-CU06)
CREATE TABLE actividades_ancestrales_msp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id BIGINT NOT NULL,
    huerto_id BIGINT NOT NULL,
    saber_ancestral_id BIGINT NOT NULL,
    fecha_realizacion DATE NOT NULL,
    fase_lunar_ejecucion ENUM (
        'YANA_KILLA_OSCURA',
        'WINAK_KILLA_CRECIENTE',
        'PURA_KILLA_LLENA',
        'WANUK_KILLA_MENGUANTE'
    ) NOT NULL,
    descripcion_bitacora TEXT NOT NULL,
    evidencia_foto_url VARCHAR(500) NOT NULL,
    evidencia_video_url VARCHAR(500) NULL,
    numero_intento INT NOT NULL DEFAULT 1 COMMENT '1 = Primer envío (100% pts), 2 = Corregida (menor puntaje)',
    estado_verificacion ENUM (
        'PENDIENTE',
        'OBSERVADA_CORREGIR',
        'APROBADA',
        'RECHAZADA_DEFINITIVA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    observacion_msp VARCHAR(500) NULL,
    puntos_acreditar_comunidad INT NOT NULL DEFAULT 0,
    verificado_por_id BIGINT NULL,
    fecha_verificacion DATETIME (6) NULL,
    creado_en DATETIME (6) NOT NULL,
    CONSTRAINT fk_act_msp_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_act_msp_huerto FOREIGN KEY (huerto_id) REFERENCES huertos (id),
    CONSTRAINT fk_act_msp_saber FOREIGN KEY (saber_ancestral_id) REFERENCES saberes_ancestrales (id),
    CONSTRAINT fk_act_msp_revisor FOREIGN KEY (verificado_por_id) REFERENCES usuarios (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Participantes y horas trabajadas en una actividad ancestral grande (SA-CU04 -> GM-CU01)
CREATE TABLE actividad_msp_participantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actividad_msp_id BIGINT NOT NULL,
    miembro_id BIGINT NOT NULL,
    horas_trabajadas INT NOT NULL DEFAULT 2 COMMENT '1 hora = 1 punto al aprobarse la evidencia por el MSP',
    puntos_acreditados INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_act_msp_miembro UNIQUE (actividad_msp_id, miembro_id),
    CONSTRAINT fk_part_actividad_msp FOREIGN KEY (actividad_msp_id) REFERENCES actividades_ancestrales_msp (id),
    CONSTRAINT fk_part_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. VITRINA UNIFICADA DE INSIGNIAS Y TALENTOS OBTENIDOS POR MIEMBRO (GM-CU03, GM-CU06, CM-CU08)
-- -----------------------------------------------------------------------------
CREATE TABLE miembro_insignias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    miembro_id BIGINT NOT NULL,
    comunidad_id BIGINT NOT NULL,
    tipo_origen ENUM ('OFICIAL_SABER_MSP', 'TALENTO_COMUNIDAD') NOT NULL,
    nombre_insignia VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    icono_codigo VARCHAR(60) NOT NULL DEFAULT 'medalla-kinti',
    saber_ancestral_id BIGINT NULL,
    talento_comunidad_id BIGINT NULL,
    fecha_obtencion DATE NOT NULL,
    CONSTRAINT fk_mi_ins_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id),
    CONSTRAINT fk_mi_ins_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_mi_ins_saber FOREIGN KEY (saber_ancestral_id) REFERENCES saberes_ancestrales (id),
    CONSTRAINT fk_mi_ins_talento FOREIGN KEY (talento_comunidad_id) REFERENCES talentos_comunidad (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
