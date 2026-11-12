-- Catálogo inicial de plantas medicinales (CP-CU01 a CP-CU04).
--
-- Fuente principal: MSP (2017). Implementación de huertos medicinales en los establecimientos de salud del
-- primer nivel de atención. Lineamientos. Se tomaron de ahí:
--   * las plantas recomendadas para la Sierra (sección 1.6) y las de la tabla del Anexo 2.1;
--   * la cualidad caliente o fría (Anexo 5, columnas PLANTAS CALIENTES / PLANTAS FRESCAS);
--   * el ciclo corto o largo cuando el documento lo nombra (pág. 48: menta, ortiga, manzanilla / cedrón, romero, marco);
--   * los nombres kichwa, usos y contraindicaciones del Anexo 2.1 cuando la planta aparece ahí;
--   * las categorías de uso según la clasificación del Anexo 5 (por su utilidad).
-- Los datos que el documento no trae (rango de altitud, origen en Ecuador, preparación y algunas
-- contraindicaciones) se completaron con referencias botánicas generales; son orientativos y el Técnico MSP
-- puede corregirlos desde CP-CU06.
--
-- fotografia_url guarda la ruta relativa dentro de la carpeta de fotos del backend (igual que Planta.java y las
-- evidencias de actividades): plantas/<archivo>.jpg. Los archivos los copia y sirve el AlmacenamientoService de
-- CP-CU08; mientras no existan, el frontend muestra un marcador de imagen.

INSERT INTO plantas (nombre_comun, nombre_kichwa, nombre_cientifico, familia_botanica, cualidad_termica,
                     ciclo_cultivo, origen, altitud_min_msnm, altitud_max_msnm, regiones_distribucion,
                     usos_medicinales, preparacion_dosificacion, contraindicaciones, fotografia_url)
VALUES
-- ===================== PLANTAS CALIENTES (Anexo 5) =====================
('Manzanilla', NULL, 'Matricaria chamomilla', 'Asteraceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 1500, 3500, 'Sierra',
 'Calma los cólicos y espasmos estomacales, ayuda a la digestión y tiene un efecto relajante suave. Se usa en lavados para irritaciones leves de la piel y los ojos.',
 'Infusión: 1 cucharadita de flores secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día.',
 'Evitar en personas alérgicas a plantas de la familia Asteraceae (margarita, ambrosía). En embarazo, consultar al personal de salud.',
 'plantas/manzanilla.jpg'),

('Menta', NULL, 'Mentha x piperita', 'Lamiaceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 1000, 3200, 'Sierra',
 'Alivia gases, cólicos y digestión pesada. En vaporizaciones ayuda a despejar la nariz en resfriados.',
 'Infusión: 1 cucharadita de hojas secas o 5 hojas frescas en 1 taza de agua hirviendo, reposar 5 a 10 minutos. Hasta 3 tazas al día, después de las comidas.',
 'No usar en personas con reflujo gastroesofágico o cálculos en la vesícula. No aplicar el aceite ni vapores concentrados en la cara de niños menores de 2 años.',
 'plantas/menta.jpg'),

('Toronjil', NULL, 'Melissa officinalis', 'Lamiaceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 1500, 3200, 'Sierra',
 'Calma los nervios, la ansiedad leve y ayuda a conciliar el sueño. Alivia cólicos y digestión nerviosa.',
 'Infusión: 1 cucharadita de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día; una de ellas antes de dormir.',
 'Precaución en personas con hipotiroidismo. Puede aumentar el efecto de sedantes. En embarazo, consultar al personal de salud.',
 'plantas/toronjil.jpg'),

('Orégano', NULL, 'Origanum vulgare', 'Lamiaceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 1500, 3200, 'Sierra',
 'Ayuda a la digestión y alivia gases y cólicos. Se usa en infusión para la tos y el resfriado. Planta antioxidante (Anexo 5).',
 'Infusión: 1 cucharadita de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 2 tazas al día.',
 'No usar en dosis medicinales durante el embarazo. Evitar el aceite esencial sin diluir.',
 'plantas/oregano.jpg'),

('Romero', NULL, 'Rosmarinus officinalis', 'Lamiaceae', 'CALIENTE',
 'LARGO', 'INTRODUCIDA', 1500, 3200, 'Sierra',
 'Tónico que ayuda en el cansancio y la digestión. En fricciones externas alivia dolores musculares y reumáticos. Planta astringente (Anexo 5).',
 'Infusión: 1 cucharadita de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 2 tazas al día, en la mañana. Uso externo: alcohol o aceite de romero en fricciones.',
 'No usar en embarazo, epilepsia ni en personas con presión arterial alta. No dar a niños pequeños.',
 'plantas/romero.jpg'),

('Matico', NULL, 'Aristeguietia glutinosa', 'Asteraceae', 'CALIENTE',
 'LARGO', 'ENDEMICA', 2200, 3500, 'Sierra',
 'Cicatrizante y desinflamante: se usa en lavados y emplastos para heridas, golpes e inflamaciones. En infusión se usa para molestias del estómago.',
 'Uso externo: cocinar un manojo de hojas en 1 litro de agua por 10 minutos y lavar la zona afectada 2 veces al día. Infusión: 1 cucharadita de hojas secas en 1 taza, hasta 2 tazas al día.',
 'No tomar durante el embarazo ni la lactancia. No aplicar sobre heridas profundas o infectadas sin valoración del personal de salud.',
 'plantas/matico.jpg'),

('Marco', NULL, 'Ambrosia arborescens', 'Asteraceae', 'CALIENTE',
 'LARGO', 'NATIVA', 2500, 3800, 'Sierra',
 'Planta protectora del huerto. Se usa en limpias energéticas y en baños y frotaciones para dolores del cuerpo y reumatismo. Ingrediente del abono líquido natural (Tabla 3).',
 'Uso externo: cocinar un manojo de ramas en 2 litros de agua por 15 minutos y usar en baños tibios o compresas. Las limpias las realiza un hombre o mujer de sabiduría.',
 'Solo uso externo. No tomar durante el embarazo. Puede causar alergia en personas sensibles a la familia Asteraceae.',
 'plantas/marco.jpg'),

('Ortiga', 'Tsini', 'Urtica dioica', 'Urticaceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 2200, 3800, 'Sierra',
 'Antiinflamatoria y diurética (Anexo 5). Se usa para el reumatismo, para limpiar la sangre y en limpias energéticas. Protege a las plantas cercanas de hongos (Anexo 3).',
 'Infusión: 1 cucharadita de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día. Manipular la planta fresca con guantes.',
 'Precaución en personas con insuficiencia renal o cardíaca, o que tomen diuréticos o anticoagulantes. No usar en embarazo.',
 'plantas/ortiga.jpg'),

('Paico', 'Paiku', 'Dysphania ambrosioides', 'Amaranthaceae', 'CALIENTE',
 'CORTO', 'NATIVA', 500, 3500, 'Sierra, Costa',
 'Antiparasitario y vermífugo: ayuda a expulsar lombrices intestinales (Anexo 5). También alivia gases y cólicos.',
 'Solo con indicación del personal de salud. Infusión suave: 1 cucharadita de hojas frescas en 1 taza de agua hirviendo, una vez al día en ayunas, máximo 3 días.',
 'Tóxico en dosis altas. No usar en embarazo, lactancia, niños menores de 5 años ni personas con enfermedad del hígado o del riñón. Nunca usar el aceite esencial.',
 'plantas/paico.jpg'),

('Chilca', 'Chillka', 'Baccharis latifolia', 'Asteraceae', 'CALIENTE',
 'LARGO', 'NATIVA', 2000, 3800, 'Sierra',
 'Desinflamante en emplastos y baños para golpes, torceduras y dolores reumáticos. Planta energética usada en limpias y como protectora de cercas vivas (Anexo 3).',
 'Uso externo: calentar hojas frescas y aplicarlas como emplasto sobre la zona afectada 2 veces al día, o cocinar un manojo en 2 litros de agua para baños.',
 'Preferir solo uso externo. No tomar durante el embarazo.',
 'plantas/chilca.jpg'),

('Cedrón', NULL, 'Aloysia citrodora', 'Verbenaceae', 'CALIENTE',
 'LARGO', 'INTRODUCIDA', 1500, 3000, 'Sierra',
 'Alivia la digestión pesada, los gases y los cólicos. Tiene un efecto calmante suave para los nervios y el insomnio.',
 'Infusión: 1 cucharadita de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día, después de las comidas.',
 'No usar en dosis altas ni por tiempo prolongado. Precaución en personas con enfermedad del riñón.',
 'plantas/cedron.jpg'),

('Albahaca', 'Chakra kiwa', 'Ocimum basilicum', 'Lamiaceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 0, 2800, 'Costa, Sierra',
 'Alivia los gases estomacales (Anexo 2.1). Se usa en la menstruación irregular. Atrae abejas y repele la mosca blanca en el huerto (Anexo 3).',
 'Infusión: un manojo de hojas y tallos hervidos en agua por 5 a 10 minutos; tomar 3 a 4 vasos al día hasta que pase la molestia (Anexo 2.1).',
 'No usar en dosis medicinales durante el embarazo. Durante el tratamiento no comer ají, sal ni carne de chancho (Anexo 2.1).',
 'plantas/albahaca.jpg'),

('Ruda', NULL, 'Ruta graveolens', 'Rutaceae', 'CALIENTE',
 'LARGO', 'INTRODUCIDA', 1500, 3200, 'Sierra, Costa',
 'Planta energética usada en limpias de mal aire y espanto. En uso externo alivia dolores musculares y cólicos.',
 'Preferir uso externo: frotaciones con hojas machacadas o baños con un manojo cocinado en 2 litros de agua. Las limpias las realiza un hombre o mujer de sabiduría.',
 'No tomar durante el embarazo: puede provocar aborto. El contacto con la planta fresca y luego el sol puede causar manchas y quemaduras en la piel. Tóxica en dosis altas.',
 'plantas/ruda.jpg'),

('Achicoria', NULL, 'Cichorium intybus', 'Asteraceae', 'CALIENTE',
 'CORTO', 'INTRODUCIDA', 1500, 3200, 'Sierra',
 'La infusión de las flores y las hojas provoca la producción de bilis y ayuda a expulsar cálculos de la vesícula (Anexo 2.1). Depurativa.',
 'Infusión: 1 cucharadita de flores y hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 2 tazas al día.',
 'No consumir alcohol durante el tratamiento porque la planta es cálida (Anexo 2.1). No usar si hay obstrucción de las vías biliares sin valoración médica.',
 'plantas/achicoria.jpg'),

-- ===================== PLANTAS FRÍAS O FRESCAS (Anexo 5) =====================
('Diente de león', 'Tani', 'Taraxacum officinale', 'Asteraceae', 'FRIA',
 'CORTO', 'INTRODUCIDA', 2000, 3800, 'Sierra',
 'Diurético y depurativo (Anexo 5). Ayuda a la digestión y al funcionamiento del hígado y la vesícula. Ligeramente laxante.',
 'Infusión: 1 cucharadita de hojas o raíz seca en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día.',
 'No usar si hay obstrucción de las vías biliares o úlcera de estómago. Precaución en personas que toman diuréticos o litio. Alergia a la familia Asteraceae.',
 'plantas/diente-de-leon.jpg'),

('Llantén', NULL, 'Plantago major', 'Plantaginaceae', 'FRIA',
 'CORTO', 'INTRODUCIDA', 1500, 3800, 'Sierra',
 'Astringente y diurético (Anexo 5). Se usa para la diarrea, la tos y la irritación de garganta, y en lavados y emplastos para heridas e inflamaciones de la piel.',
 'Infusión: 1 cucharada de hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día. Uso externo: hojas frescas machacadas como emplasto.',
 'Tomar separado de otros medicamentos (al menos 1 hora), porque puede disminuir su absorción. En embarazo, consultar al personal de salud.',
 'plantas/llanten.jpg'),

('Cola de caballo', 'Caballo chupa', 'Equisetum giganteum', 'Equisetaceae', 'FRIA',
 'LARGO', 'NATIVA', 0, 3500, 'Sierra, Amazonía',
 'Para problemas de riñón, infección de vías urinarias, reumatismo y dolores del cuerpo (Anexo 2.1). Diurética (Anexo 5).',
 'Hervir el tallo de una planta madura por 15 minutos en 1 litro de agua; tomar 1 vaso al día durante 4 días (Anexo 2.1).',
 'No usar por más de 4 semanas seguidas. No usar en embarazo, lactancia, ni en personas con enfermedad del corazón o del riñón. Durante el tratamiento no comer ají ni tomar bebidas alcohólicas o heladas (Anexo 2.1).',
 'plantas/cola-de-caballo.jpg'),

('Valeriana', NULL, 'Valeriana officinalis', 'Caprifoliaceae', 'FRIA',
 'LARGO', 'INTRODUCIDA', 2000, 3200, 'Sierra',
 'Sedante y antiespasmódica (Anexo 5): ayuda en el insomnio, la ansiedad y el nerviosismo, y calma los calambres.',
 'Infusión de la raíz: 1 cucharadita de raíz seca en 1 taza de agua hirviendo, reposar 10 a 15 minutos. Tomar 1 taza 30 minutos antes de dormir.',
 'No combinar con alcohol, pastillas para dormir ni otros sedantes. Puede causar somnolencia: no conducir después de tomarla. No usar en embarazo, lactancia ni en niños menores de 3 años.',
 'plantas/valeriana.jpg'),

('Sábila', NULL, 'Aloe vera', 'Asphodelaceae', 'FRIA',
 'LARGO', 'INTRODUCIDA', 0, 2800, 'Costa, Sierra',
 'Cicatrizante (Anexo 5): el gel de las hojas alivia quemaduras leves, heridas superficiales e irritaciones de la piel. Desinflamante.',
 'Uso externo: cortar una hoja, lavarla, retirar la cáscara y el líquido amarillo, y aplicar el gel transparente sobre la piel 2 o 3 veces al día.',
 'No tomar el líquido amarillo (látex) de la hoja: es un laxante fuerte. No usar por vía oral en embarazo, lactancia ni en niños. No aplicar en heridas profundas.',
 'plantas/sabila.jpg'),

('Violeta', NULL, 'Viola odorata', 'Violaceae', 'FRIA',
 'LARGO', 'INTRODUCIDA', 1800, 3200, 'Sierra',
 'Expectorante: ayuda a aflojar la flema en la tos y el resfriado. Calmante suave para la garganta.',
 'Infusión: 1 cucharadita de flores y hojas secas en 1 taza de agua hirviendo, reposar 10 minutos. Hasta 3 tazas al día.',
 'No usar la raíz ni las semillas: en dosis altas provocan vómito. En embarazo, consultar al personal de salud.',
 'plantas/violeta.jpg');

-- Categorías de uso (Anexo 5: clasificación por su utilidad).
INSERT INTO planta_categorias (planta_id, categoria)
SELECT p.id, c.categoria
FROM plantas p
JOIN (
    SELECT 'Matricaria chamomilla' AS cientifico, 'ANTIESPASMODICA' AS categoria
    UNION ALL SELECT 'Matricaria chamomilla', 'SEDANTE'
    UNION ALL SELECT 'Matricaria chamomilla', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Mentha x piperita', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Mentha x piperita', 'DESCONGESTIONANTE'
    UNION ALL SELECT 'Melissa officinalis', 'SEDANTE'
    UNION ALL SELECT 'Melissa officinalis', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Origanum vulgare', 'ANTIOXIDANTE'
    UNION ALL SELECT 'Origanum vulgare', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Rosmarinus officinalis', 'ASTRINGENTE'
    UNION ALL SELECT 'Rosmarinus officinalis', 'ESTIMULANTE'
    UNION ALL SELECT 'Rosmarinus officinalis', 'ANTIOXIDANTE'
    UNION ALL SELECT 'Aristeguietia glutinosa', 'CICATRIZANTE'
    UNION ALL SELECT 'Aristeguietia glutinosa', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Ambrosia arborescens', 'ENERGETICA'
    UNION ALL SELECT 'Ambrosia arborescens', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Urtica dioica', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Urtica dioica', 'DIURETICA'
    UNION ALL SELECT 'Urtica dioica', 'ENERGETICA'
    UNION ALL SELECT 'Dysphania ambrosioides', 'ANTIPARASITARIA'
    UNION ALL SELECT 'Baccharis latifolia', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Baccharis latifolia', 'ENERGETICA'
    UNION ALL SELECT 'Aloysia citrodora', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Aloysia citrodora', 'SEDANTE'
    UNION ALL SELECT 'Ocimum basilicum', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Ruta graveolens', 'ENERGETICA'
    UNION ALL SELECT 'Ruta graveolens', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Cichorium intybus', 'DEPURATIVA'
    UNION ALL SELECT 'Taraxacum officinale', 'DIURETICA'
    UNION ALL SELECT 'Taraxacum officinale', 'DEPURATIVA'
    UNION ALL SELECT 'Plantago major', 'ASTRINGENTE'
    UNION ALL SELECT 'Plantago major', 'DIURETICA'
    UNION ALL SELECT 'Plantago major', 'CICATRIZANTE'
    UNION ALL SELECT 'Equisetum giganteum', 'DIURETICA'
    UNION ALL SELECT 'Valeriana officinalis', 'SEDANTE'
    UNION ALL SELECT 'Valeriana officinalis', 'ANTIESPASMODICA'
    UNION ALL SELECT 'Aloe vera', 'CICATRIZANTE'
    UNION ALL SELECT 'Aloe vera', 'ANTIINFLAMATORIA'
    UNION ALL SELECT 'Viola odorata', 'EXPECTORANTE'
) c ON c.cientifico = p.nombre_cientifico;
