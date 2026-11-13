-- Ruta secuencial de saberes ancestrales del huerto medicinal (SA-CU10, SA-CU04, GM-CU03).
--
-- Los pasos siguen la ruta de la definición del proyecto (preparación de la tierra, surcos, ritual de siembra,
-- siembra, señalética, cuidados y riego, ritual de cosecha, cosecha, limpieza y secado, empaquetado) y el orden de
-- las fases II y III de: MSP (2017). Implementación de huertos medicinales en los establecimientos de salud del
-- primer nivel de atención. Lineamientos. De ese documento se tomaron:
--   * el contenido de cada paso (págs. 31-52: abono natural, preparación de la tierra, técnicas y tipos de siembra,
--     rituales de siembra y de cosecha, señalética, cuidados, limpieza, secado, etiquetado y empacado);
--   * las fases lunares ideales (Anexo 4, conteo del tiempo lunar, y págs. 33, 42, 45 y 49);
--   * los horarios de los rituales, la siembra y la cosecha (págs. 42, 46, 48 y 49);
--   * las herramientas e ingredientes (tablas 3, 4, 7, 8 y 9).
-- Los textos visibles no citan tablas ni anexos del documento (mismo criterio que la V10).
--
-- Valores orientativos (el documento no los fija y las columnas son NOT NULL):
--   * fase lunar de SURCOS-CAMELLONES, SENALETICA y LIMPIEZA-SECADO, y el horario de ABONO-NATURAL,
--     SURCOS-CAMELLONES y CUIDADO-RIEGO;
--   * los nombres de las insignias oficiales, que son una propuesta de gamificación (GM-CU03).
-- titulo_kichwa solo se llena cuando el documento da el término (wachu, anti, inka wachu); el resto queda NULL a
-- propósito para que lo completen la clienta o el Técnico MSP con una fuente.
-- puntos_base_comunidad = 20 en todos los pasos (valor por defecto; el intento 2 acredita 14 según GM-CU01).

INSERT INTO saberes_ancestrales (orden_paso, codigo_hito, titulo, titulo_kichwa, categoria, fase_lunar_ideal,
                                 horario_energetico, contenido_educativo, ingredientes_herramientas,
                                 puntos_base_comunidad, nombre_insignia_oficial)
VALUES
(1, 'ABONO-NATURAL', 'Elaboración de abono natural', NULL, 'ABONO_NATURAL', 'YANA_KILLA_OSCURA',
 'En la mañana de un día soleado',
 'La luna oscura es el tiempo de la transformación: lo que se descompone se convierte en abono. Se pueden usar los restos de la siembra anterior, el barbecho y las hojas de plantas que aportan nitrógeno (chochos, fréjol, habas, alfalfa), que se mezclan con la tierra y reposan al menos 15 días. También se prepara abono líquido macerando ortiga, marco e higuerilla en agua sin cloro durante 8 a 20 días, removiéndolo cada día, o abono sólido enterrando rastrojos o estiércol en un hoyo tapado por 15 días. Otra práctica de los pueblos andinos es mover el corral de los animales por turnos para abonar el suelo.',
 'Machetes, palas, un hoyo o espacio para compost junto al huerto, restos vegetales, estiércol seco, ortiga, marco, higuerilla y agua sin cloro.',
 20, 'Guardián del abono'),

(2, 'PREP-TIERRA', 'Preparación de la tierra', NULL, 'PREPARACION_TIERRA', 'YANA_KILLA_OSCURA',
 'Día de mucho sol',
 'La tierra se prepara en luna oscura, para que la maleza se transforme en abono. Se retira la maleza con machete, azadón o pala y se esparce desmenuzada sobre el terreno, que es mejor que quemarla. Si el suelo es pobre se añade abono orgánico ya secado al sol. La tierra se remueve a mano con azadón, pala o yunta; si se usa tractor, sin profundizar. Después se deja reposar 15 días para que el abono se desintegre y se convierta en tierra fértil.',
 'Machetes, azadones, palas de mano y rastrillos.',
 20, 'Guardián de la Allpa'),

(3, 'SURCOS-CAMELLONES', 'Elaboración de surcos, terrazas o camellones', 'Wachu, anti e inka wachu',
 'PREPARACION_TIERRA', 'WINAK_KILLA_CRECIENTE',
 'En la mañana, después del reposo de la tierra',
 'Pasados los 15 días de reposo se da forma al huerto según el terreno. En terreno plano se abren surcos (wachu); en ladera, siempre de forma transversal y nunca vertical, o se construyen terrazas o andenes (anti) desde la parte baja hacia la alta, guardando la capa de suelo fértil para esparcirla al final. En suelos pantanosos se levantan camellones (inka wachu): se marcan con postes, se acordonan, se remueve la tierra y se apila en línea. Los camellones permiten cultivar en el mismo espacio plantas que necesitan mucha y poca agua.',
 'Azadón, pala de mano, estacas de madera, piola o cuerda, cal para marcar y piedras o ramas para afirmar los bordes de las terrazas.',
 20, 'Constructor de wachus'),

(4, 'RITUAL-SIEMBRA', 'Ritual de siembra: pago a la tierra', NULL, 'RITUAL_SIEMBRA', 'WINAK_KILLA_CRECIENTE',
 'Con la salida del sol',
 'Antes de sembrar se hace una ceremonia de pago a la tierra en luna creciente o luna llena, para despertar, alimentar y fortalecer a la madre tierra, bendecir las semillas, estacas y plántulas, y pedir que las plantas nazcan con el don de sanar. La guían los hombres y mujeres de sabiduría del lugar con la participación de todos. Los participantes forman un círculo alrededor de una manta con las semillas y un plato de barro; cada uno toma una porción de tierra con sus manos, dice su plegaria o palabras de gratitud y la deposita en el plato, que pasa a la persona de su derecha. Junto a la manta, hacia la salida del sol, se sahúma con palo santo. Cada pueblo puede usar sus propios símbolos, colores y rezos.',
 'Plato de barro, una porción de tierra por participante, flores del lugar o chagrillo, carbón, palo santo, recipiente para sahumar, fósforos y una manta blanca o de colores vivos de 1 m por 1 m.',
 20, 'Sembrador de la Wiñak Killa'),

(5, 'SIEMBRA', 'Siembra de plantas medicinales', NULL, 'SIEMBRA_SENALETICA', 'WINAK_KILLA_CRECIENTE',
 'Desde las 08:00 o 09:00, después del ritual',
 'Las fases de cuarto creciente y luna llena son las más recomendadas para sembrar. Se puede sembrar por semillas (mejor si son de la región, porque ya están adaptadas), por estacas (un trozo de tallo con yema en un hoyo de unos 10 cm, regado de inmediato con abundante agua), por esquejes (una rama con yemas que se pone en agua hasta que eche raíces) o por plántulas (semillas germinadas en bolsa con tierra y abono que luego se trasplantan). Se riega con agua de vertiente, de lluvia o de grifo reposada una noche para que pierda el cloro.',
 'Semillas, estacas, esquejes o plántulas, barretón de metal, tijera podadora, bolsas negras con tierra y abono, recipiente con agua y regadera.',
 20, 'Mano que siembra'),

(6, 'SENALETICA', 'Colocación de señalética', NULL, 'SIEMBRA_SENALETICA', 'WINAK_KILLA_CRECIENTE',
 'Después de la siembra',
 'Cada espacio o clase de planta del huerto lleva un letrero con su nombre en el idioma indígena local, su nombre común, su nombre científico, su familia y su uso general. El color indica su cualidad: en el mundo kichwa andino el rojo equivale a fuego y calor (plantas calientes) y el verde o el blanco a frío o fresco (plantas frías). Conviene investigar con los propios participantes del huerto los nombres y colores que usa cada pueblo.',
 'Tablas triplex, tirantes de madera de 1 m, clavos de 2 pulgadas, pinturas de caucho amarilla, azul y roja, brocha y pincel.',
 20, 'Guardián de los nombres'),

(7, 'CUIDADO-RIEGO', 'Cuidados de crecimiento y riego', NULL, 'CUIDADO_RIEGO', 'YANA_KILLA_OSCURA',
 'En la mañana fresca',
 'Los brotes se cuidan con deshierbe, replante de las plantas que se perdieron y aporque. En la Sierra se coloca ceniza de fogón o cal alrededor de cada planta para alejar plagas y pulgones. Las plantas medicinales no se fumigan con ningún producto, ni natural ni industrial: se protegen sembrando juntas las especies que se ayudan entre sí. El riego se hace por los surcos o dejando la manguera en el suelo para que el agua moje la tierra hasta las raíces; no se usa aspersor porque el chorro en forma de lluvia violenta a las plantas. El huerto debe estar cercado y alejado del ruido.',
 'Regadera de aluminio, manguera de media pulgada, ceniza de fogón o cal, azadón y pala de mano.',
 20, 'Cuidador del huerto'),

(8, 'RITUAL-COSECHA', 'Baño de las plantas y ritual de gratitud', NULL, 'RITUAL_COSECHA', 'PURA_KILLA_LLENA',
 '05:00 a 07:00, al amanecer',
 'Uno o dos días antes de cosechar se riega o se baña a las plantas con regadera para limpiarlas, conversando con ellas y pidiéndoles que ayuden a sanar. El día de la cosecha, al amanecer, se hace un ritual de gratitud a la madre tierra con el personal de salud y la comunidad. Se cava un hoyo pequeño; cada participante recoge una ramita, flor, fruto o semilla sin dañar a la planta. Con el fuego encendido se sahúma el huerto agradeciendo por las plantas; luego cada uno besa lo que recogió y lo deposita en el hoyo, se pone un poco de agua y se cubre. También se pueden ofrecer frutas o pan.',
 'Pala para el hoyo, palo santo o incienso, carbón, recipiente para quemar carbón, agua, fósforos, manta blanca o de colores vivos, pétalos de flores o chagrillo y un altar ceremonial.',
 20, 'Corazón agradecido'),

(9, 'COSECHA', 'Cosecha ritual de las plantas medicinales', NULL, 'RITUAL_COSECHA', 'PURA_KILLA_LLENA',
 'Desde las 08:00, cuando el sol ya secó el rocío',
 'Las plantas medicinales no se cosechan con humedad: se espera a que el sol caliente las hojas y seque el rocío. Si se usarán hojas, flores o frutos se cosecha en cuarto creciente o luna llena; si se usarán semillas o raíces, en cuarto menguante. Las de ciclo corto (menta, manzanilla, ortiga, salvia) están listas en 6 a 8 semanas; las de ciclo largo (romero, cedrón, marco) tardan más. Se cosecha a mano, tomando solo las partes que se necesitan y pidiendo a la planta que sea efectiva; la planta entera se arranca solo cuando se necesita la raíz.',
 'Podadora de plantas, hoz y lavacaras o cajones de plástico.',
 20, 'Cosechador de la Pura Killa'),

(10, 'LIMPIEZA-SECADO', 'Limpieza y secado a la sombra', NULL, 'SECADO_EMPAQUETADO', 'WANUK_KILLA_MENGUANTE',
 'Durante el día, en un lugar con techo',
 'Las plantas cosechadas se limpian a mano escogiendo las partes que se usarán. No se lavan después de la cosecha, porque se pudren y pierden su espíritu sanador; por eso se bañan antes, durante el riego. Las hojas, raíces y flores se secan a la sombra sobre papel periódico en un estante; las plantas completas se cuelgan con la raíz hacia arriba en alambres inoxidables o piolas de algodón. El lugar debe tener techo, circulación de aire y estar lejos de animales y corrales. El secado dura de 10 a 15 días.',
 'Lavacaras o cajones de plástico, papel periódico, estante o mesa de madera, piola de algodón o alambre inoxidable delgado.',
 20, 'Guardián de la sombra'),

(11, 'EMPAQUETADO', 'Empaquetado, etiquetado y almacenaje', NULL, 'SECADO_EMPAQUETADO', 'WANUK_KILLA_MENGUANTE',
 'Durante el día, en un lugar fresco y seco',
 'Las hojas secas se empacan en fundas plásticas transparentes cerradas herméticamente, de 100 gramos por paquete. Cada paquete lleva una etiqueta con el nombre en idioma de la nacionalidad, el nombre popular, el nombre científico, la familia, el peso, las indicaciones de uso, la dosificación, las contraindicaciones, la fecha de empaquetado, la fecha de caducidad y la cualidad caliente o fría. En la Sierra se pueden usar hasta 12 meses después de empacadas; en la Costa y la Amazonía, hasta 6 meses. Se guardan en un lugar fresco y seco, separadas por especie y por cualidad.',
 'Bolsas plásticas transparentes, balanza digital, selladora eléctrica de plásticos, papel y un estante de madera con divisiones.',
 20, 'Maestro de la farmacia viva');
