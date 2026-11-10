---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU04: Solicitar crear comunidad (Formulario público)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)`
* **Descripción:** `Permite a cualquier líder barrial, familiar o colectivo ciudadano enviar de manera pública y sin requerir inicio de sesión una solicitud formal para constituir una nueva comunidad de huertos medicinales y su espacio de siembra. Registra la información territorial, diseño propuesto del huerto, datos personales completos del solicitante (con fecha de nacimiento para verificar mayoría de edad) y el consentimiento expreso para el tratamiento de datos personales conforme a la Ley Orgánica de Protección de Datos Personales (LOPDP) del Ecuador, quien asumirá el rol de Responsable de Comunidad una vez validada la propuesta por el Ministerio de Salud Pública.`
* **Dependencias:** `Ninguna (punto de entrada público que alimenta a #US-CU05)`

### Precondiciones
1. `El usuario accede a la plataforma en Modo Libre mediante la ruta pública /solicitar-comunidad.`
2. `El endpoint POST /api/publico/solicitudes-comunidad se encuentra configurado con acceso público (permitAll()) en SecurityConfig.java.`
3. `La tabla solicitudes_comunidad se encuentra creada en la base de datos según las migraciones reales del backend con las columnas de auditoría de consentimiento de datos personales.`

### Postcondiciones
1. `Se persiste un nuevo registro en la tabla solicitudes_comunidad con estado = 'PENDIENTE', fecha_nacimiento_solicitante validada (>= 18 años), consentimiento_datos = true, consentimiento_en registrado con Clock del sistema, creado_en con Clock, revisado_por_id = null y motivo_rechazo = null.`
2. `El sistema responde con código HTTP 201 Created y el DTO SolicitudComunidadResponse.`
3. `El frontend despliega un diálogo emergente responsivo (Modal) con Kinti felicitando al solicitante y notificando que la solicitud pasará a revisión del equipo técnico del MSP.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** ingresa a la opción `"Crear Comunidad"` desde la barra de navegación pública en `Layout.tsx` o desde la pantalla de bienvenida.
2. El **Sistema** despliega una página interactiva diseñada bajo el enfoque **Mobile First para Progressive Web App (PWA)**, permitiendo a líderes barriales y comunitarios completar el formulario directamente desde sus teléfonos celulares en territorio, escalando armónicamente a tabletas y pantallas de escritorio mediante las variables de diseño de `frontend/src/index.css`:
   - **Contenedor Responsivo:** La vista se aloja en un contenedor principal centrado que en pantallas móviles utiliza `width: 100%` con padding `var(--espacio-4)`, y en pantallas de escritorio (`@media (min-width: 640px)`) escala a `max-width: var(--ancho-lectura)` o `var(--ancho-maximo)` con márgenes equilibrados (`padding: var(--espacio-8) var(--espacio-4)`).
   - **Cabecera Motivacional:** Encabezada por `<KintiGuia variante="dia"/>` con mensaje adaptable:
     *"¡Sembrar en comunidad sana el cuerpo y une los corazones! Da el primer paso para cultivar la salud de tu barrio completando esta solicitud"*, con tipografía `var(--texto-base)`, color `var(--color-primario)` y peso `var(--peso-medio)`.
   - **Secciones Agrupadas en Tarjetas:** El formulario se organiza en dos tarjetas visuales diferenciadas con fondo `var(--color-superficie)`, borde `var(--borde-ancho) solid var(--color-borde)`, curvatura `var(--radio-lg)`, sombra `var(--sombra-sm)` y separación `margin-bottom: var(--espacio-6)`:
     - **Sección 1: Datos de la Comunidad y Huerto:**
       - En móvil se apila en una sola columna vertical (`display: grid; grid-template-columns: 1fr; gap: var(--espacio-4)`), escalando a dos columnas en escritorio (`@media (min-width: 640px) { grid-template-columns: repeat(2, 1fr); }`).
       - `Nombre de la Comunidad` (`nombreComunidad`: `CampoTexto`, obligatorio, solo letras y espacios, máx. 150 caracteres, ancho completo `grid-column: 1 / -1`).
       - `Tipo de Organización` (`tipoOrganizacion`: `Selector`, opciones: `INDIVIDUAL`, `FAMILIA`, `CONDOMINIO`, `BARRIO`, `COMITE_LOCAL`, obligatorio).
       - `Provincia` (`provincia`: `CampoTexto`, obligatorio, solo letras, máx. 100 caracteres).
       - `Parroquia` (`parroquia`: `CampoTexto`, obligatorio, solo letras, máx. 100 caracteres).
       - `Dirección Exacta` (`direccion`: `CampoTexto`, obligatorio, texto libre, referencias, máx. 255 caracteres, ancho completo `grid-column: 1 / -1`).
       - `Ubicación Geográfica` (`latitud`, `longitud`: inputs numéricos decimales integrados con selector visual de coordenadas adaptado a pantalla táctil).
       - `Altitud Estimada (msnm)` (`altitudMsnm`: `CampoTexto`, numérico, valor por defecto `2500`).
       - `Diseño Principal del Huerto` (`disenoPrincipal`: `Selector`, opciones del enum `DisenoPrincipal`: `EN_TIERRA`, `VERTICAL`, `COLGANTE`, `EN_PISO`, obligatorio).
       - `Subtipos de Cultivo Solicitados` (`subtiposSolicitados`: selector múltiple o texto que almacena hasta 3 subtipos separados por coma, ej: `"SURCOS_WACHU,TERRAZAS_ANTI,MACETAS_PISO"`, ancho completo `grid-column: 1 / -1`).
     - **Sección 2: Datos del Solicitante (Futuro Responsable):**
       - Estructura responsiva idéntica (1 columna en móvil, 2 columnas en escritorio).
       - `Cédula de Identidad` (`cedulaSolicitante`: `CampoTexto`, 10 dígitos numéricos, obligatorio con algoritmo verificador ecuatoriano).
       - `Nombre y Apellido` (`nombreSolicitante`: `CampoTexto`, obligatorio, solo letras y espacios, máx. 120 caracteres).
       - `Fecha de Nacimiento` (`fechaNacimiento`: `CampoTexto`, tipo `date`, obligatorio, mayoría de edad requerida: 18 años cumplidos calculados con fecha actual).
       - `Género` (`generoSolicitante`: `Selector`, opciones: `FEMENINO`, `MASCULINO`, `OTRO`, obligatorio).
       - `Correo Electrónico` (`correoSolicitante`: `CampoTexto`, tipo `email`, obligatorio, máx. 180 caracteres).
       - `Teléfono Celular` (`celularSolicitante`: `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos iniciando en `09`).
     - **Área de Acción:** Botón de envío **`"Enviar Solicitud de Huerto"`** (`Boton`, variante `primary`):
       - En dispositivos móviles ocupa el ancho completo (`width: 100%`) con padding táctil amplio `var(--espacio-3) var(--espacio-4)`.
       - En escritorios se alinea a la derecha con ancho automático (`width: auto; min-width: 14rem`).
3. El **Visitante** ingresa la información requerida y presiona o toca **`"Enviar Solicitud de Huerto"`**.
4. El **Sistema** (frontend) verifica localmente el cumplimiento del algoritmo de cédula ecuatoriana, mayoría de edad (mínimo 18 años), formato del celular (`09XXXXXXXX`), email y la ausencia de caracteres especiales o números en el nombre de la comunidad y solicitante.
5. El **Sistema** despliega inmediatamente una ventana emergente modal (`Modal`), diseñada bajo arquitectura Mobile First para PWA:
   - **Título:** *"Protección de Datos Personales (LOPDP Ecuador)"* en tipografía `var(--texto-xl)`, color `var(--color-primario)` y peso `var(--peso-seminegrita)`.
   - **Cuerpo Informativo (LOPDP):** Bloque de texto con scroll interno si es necesario, tipografía `var(--texto-sm)`, interlineado `var(--interlineado-normal)` y color `var(--color-texto)`:
     > *"En cumplimiento con la Ley Orgánica de Protección de Datos Personales (LOPDP) del Ecuador, te informamos que los datos personales y de contacto proporcionados en este formulario serán tratados exclusivamente por la Red de Huertos Comunitarios y el Ministerio de Salud Pública (MSP).*
     >
     > * **Finalidad:** Gestionar la postulación, verificación y articulación técnica de la nueva comunidad de siembra.
     > * **Uso no comercial:** Tus datos jamás serán comercializados, cedidos ni empleados para fines publicitarios o lucrativos.
     > * **Gamificación y Perfil Público:** Al formar parte de la red, tu participación comunitaria, reconocimientos, insignias y aportes podrán ser consultados de forma pública mediante tu número de cédula dentro del sistema para promover la transparencia y la participación ciudadana.
     > * **Tus derechos:** Puedes ejercer tus derechos de acceso, rectificación, actualización y eliminación de datos conforme lo estipula la ley."*
   - **Control Interactivo (Checkbox Obligatorio):**
     - Casilla de verificación con área táctil accesible de 44px:
       `[ ] Declaro que soy mayor de 18 años, que la información proporcionada es fidedigna y autorizo de forma libre, expresa e informada el tratamiento de mis datos personales bajo los términos indicados.`
   - **Botones de Acción Táctiles:**
     - En dispositivos móviles se organizan en pila vertical a ancho completo (`width: 100%`); en escritorio se disponen en fila:
       - Botón **`"Cancelar"`** (variante `secondary`, cierra el modal sin enviar la petición y permite revisar los datos).
       - Botón **`"Aceptar y Continuar"`** (`Boton`, variante `primary`, **habilitado únicamente si el checkbox está marcado**; permanece deshabilitado (`disabled`) mientras la casilla no esté seleccionada).
6. El **Visitante** marca la casilla de consentimiento y pulsa o toca **`"Aceptar y Continuar"`**.
7. El **Sistema** cierra el modal de consentimiento, deshabilita los controles mostrando indicador de carga y emite la petición `POST /api/publico/solicitudes-comunidad` con el payload:
```json
{
  "nombreComunidad": "Huerto Comunitario Allpa Viva",
  "tipoOrganizacion": "BARRIO",
  "provincia": "Pichincha",
  "parroquia": "Calderon",
  "direccion": "Calle de las Rosas y Pasaje Los Pinos, lote 14",
  "latitud": -0.0987123,
  "longitud": -78.4321456,
  "altitudMsnm": 2650,
  "disenoPrincipal": "EN_TIERRA",
  "subtiposSolicitados": "SURCOS_WACHU,TERRAZAS_ANTI",
  "cedulaSolicitante": "1710034065",
  "nombreSolicitante": "Segundo Tránsito Amaguaña",
  "fechaNacimiento": "1990-05-14",
  "generoSolicitante": "MASCULINO",
  "correoSolicitante": "comunidad.allpa@huertos.gob.ec",
  "celularSolicitante": "0991234567",
  "consentimientoDatos": true
}
```
8. El **Sistema** (backend) valida la integridad de datos, verifica que `consentimientoDatos` sea `true`, comprueba mayoría de edad con `Clock`, verifica que no exista una solicitud en estado `PENDIENTE` o una comunidad registrada con el mismo nombre o la misma cédula, asigna `estado = 'PENDIENTE'`, `consentimiento_datos = true`, `consentimiento_en` con fecha y hora actual del `Clock`, `creado_en` con `Clock.systemUTC()`, y persiste el registro en `solicitudes_comunidad`.
9. El **Sistema** responde con HTTP `201 Created` y el DTO `SolicitudComunidadResponse`:
```json
{
  "id": 12,
  "nombreComunidad": "Huerto Comunitario Allpa Viva",
  "tipoOrganizacion": "BARRIO",
  "provincia": "Pichincha",
  "parroquia": "Calderon",
  "nombreSolicitante": "Segundo Tránsito Amaguaña",
  "correoSolicitante": "comunidad.allpa@huertos.gob.ec",
  "estado": "PENDIENTE",
  "creadoEn": "2026-11-10T11:00:00"
}
```
10. El **Sistema** (frontend) abre una ventana emergente (`Modal`), con diseño adaptado a móviles (ancho completo adaptable `width: calc(100% - var(--espacio-6))`, padding `var(--espacio-5)` y radio `var(--radio-lg)`):
    - **Encabezado:** `<KintiGuia variante="celebracion"/>`
    - **Título:** *"¡Felicidades por sembrar esperanza!"*
    - **Mensaje:** *"Tu solicitud para constituir el huerto **Huerto Comunitario Allpa Viva** ha sido recibida con éxito. El equipo técnico del MSP revisará la propuesta y te enviaremos una notificación al correo **comunidad.allpa@huertos.gob.ec** con los resultados y las instrucciones de acceso."*
    - **Acción:** Botón **`"Entendido"`** (ancho completo en móvil) que cierra el modal y redirige a la página pública de inicio (`/inicio`).

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Campos obligatorios faltantes o formatos inválidos (HTTP 400 Bad Request):**
  - **Condición:** Si algún campo obligatorio está en blanco, el celular no tiene 10 dígitos o no comienza en `09`, la cédula no supera el dígito verificador, la fecha de nacimiento no corresponde a un mayor de 18 años, o el nombre de la comunidad contiene números o símbolos.
  - **Comportamiento:** Bean Validation rechaza la petición. El frontend muestra la notificación de advertencia con `Notificacion` indicando con precisión los campos por corregir sin borrar los datos digitados.
* **4b. Coordenadas geográficas fuera de rango ecuatoriano (HTTP 400 Bad Request):**
  - **Condición:** Si la latitud o longitud no corresponden a rangos razonables del territorio ecuatoriano (Latitud entre `-5.5` y `2.0`, Longitud entre `-92.0` y `-75.0`).
  - **Comportamiento:** El backend lanza `ReglaNegocioException`: *"Las coordenadas geográficas ingresadas no corresponden al territorio ecuatoriano"*.
* **4c. Solicitante menor de edad (HTTP 400 Bad Request):**
  - **Condición:** Si la fecha de nacimiento indica una edad menor a 18 años calculada con el `Clock` del sistema.
  - **Comportamiento:** El backend lanza `ReglaNegocioException`: *"El solicitante debe ser mayor de 18 años para postular una comunidad"*.
* **4d. Falta de consentimiento de datos personales (HTTP 400 Bad Request):**
  - **Condición:** Si el campo `consentimientoDatos` es `false` o nulo al recibir la petición en el backend.
  - **Comportamiento:** Bean Validation rechaza la solicitud (`@AssertTrue`): *"Debe autorizar el tratamiento de datos personales bajo los términos de la LOPDP"*. El frontend mantiene el formulario y vuelve a solicitar el consentimiento.
* **4e. Nombre de comunidad o cédula ya en proceso (HTTP 409 Conflict):**
  - **Condición:** Si ya existe una solicitud en estado `PENDIENTE` o una comunidad activa en `comunidades` con el mismo nombre o la misma cédula del solicitante.
  - **Comportamiento:** El backend lanza `ConflictoException` con el mensaje: *"Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada"*. Se notifica al usuario con el componente `Notificacion`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/solicitudes-publico/`):**
  - `SolicitudComunidadForm.tsx`: Formulario dinámico diseñado bajo enfoque Mobile First para PWA. Contiene campos agrupados en tarjetas con variables de `index.css`, selector de tipo de organización y diseño (`DisenoPrincipal`), campo de fecha de nacimiento con validación de mayoría de edad (+18), selector de género (`FEMENINO`, `MASCULINO`, `OTRO`), validación en tiempo real de cédula y celular, presencia de `<KintiGuia variante="dia"/>`.
  - `ModalConsentimientoLopdp.tsx`: Diálogo modal responsivo previo al envío con el texto legal oficial de la LOPDP, control interactivo de checkbox para aceptación libre y expresa, botón "Cancelar" y botón "Aceptar y Continuar" condicionado al estado marcado del checkbox.
  - `ModalConfirmacionSolicitud.tsx`: Diálogo emergente final con felicitación y variante de celebración de Kinti.
  - `solicitudesPublicoApi.ts`: Función `enviarSolicitudComunidad(datos: CrearSolicitudComunidadRequest): Promise<SolicitudComunidadResponse>` consumiendo `POST /api/publico/solicitudes-comunidad` mediante `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `SolicitarComunidadPage.tsx`: Vista pública registrada en `App.tsx` bajo la ruta `/solicitar-comunidad`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.SolicitudComunidadPublicoController.java`):**
  - `POST /api/publico/solicitudes-comunidad` -> Recibe `@Valid @RequestBody CrearSolicitudComunidadRequest request`, retorna HTTP `201 Created` con `SolicitudComunidadResponse`.
  - **Acceso:** Público (en `SecurityConfig.java`: `.requestMatchers("/api/publico/**").permitAll()`).
  - **Cuerpo de Solicitud (`CrearSolicitudComunidadRequest`):**
    - `nombreComunidad`: `String`, obligatorio, solo letras y espacios (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`, `@Size(max = 150)`).
    - `tipoOrganizacion`: `TipoOrganizacion`, obligatorio (`@NotNull`).
    - `provincia`: `String`, obligatorio (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`).
    - `parroquia`: `String`, obligatorio (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`).
    - `direccion`: `String`, obligatorio (`@NotBlank`, `@Size(max = 255)`).
    - `latitud`: `BigDecimal`, obligatorio (`@NotNull`, `@DecimalMin("-6.0")`, `@DecimalMax("2.5")`).
    - `longitud`: `BigDecimal`, obligatorio (`@NotNull`, `@DecimalMin("-92.0")`, `@DecimalMax("-75.0")`).
    - `altitudMsnm`: `Integer`, obligatorio (`@NotNull`, `@Min(0)`, `@Max(5000)`).
    - `disenoPrincipal`: `DisenoPrincipal`, obligatorio (`@NotNull`).
    - `subtiposSolicitados`: `String`, obligatorio (`@NotBlank`, `@Size(max = 150)`).
    - `cedulaSolicitante`: `String`, obligatorio, 10 dígitos (`@NotBlank`, `@Pattern(regexp = "^[0-9]{10}$")`).
    - `nombreSolicitante`: `String`, obligatorio (`@NotBlank`, `@Size(max = 120)`).
    - `fechaNacimiento`: `LocalDate`, obligatorio, mayor de 18 años (`@NotNull`, `@Past`).
    - `generoSolicitante`: `Genero`, obligatorio (`@NotNull`, valores: `FEMENINO`, `MASCULINO`, `OTRO`).
    - `correoSolicitante`: `String`, obligatorio (`@NotBlank`, `@Email`, `@Size(max = 180)`).
    - `celularSolicitante`: `String`, obligatorio, inicia en 09 (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$")`).
    - `consentimientoDatos`: `Boolean`, obligatorio (`@NotNull`, `@AssertTrue(message = "Debe autorizar el tratamiento de datos personales")`).
  - **Respuesta Exitosa (HTTP 201 Created - `SolicitudComunidadResponse`):**
    - `id`: `Long`.
    - `nombreComunidad`: `String`.
    - `tipoOrganizacion`: `TipoOrganizacion`.
    - `provincia`: `String`.
    - `parroquia`: `String`.
    - `nombreSolicitante`: `String`.
    - `correoSolicitante`: `String`.
    - `estado`: `EstadoSolicitud` (valor `PENDIENTE`).
    - `creadoEn`: `LocalDateTime`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Error en Bean Validation, menor de edad, falta de consentimiento o validación de cédula/coordenadas.
    - `409 Conflict`: Nombre de comunidad o cédula ya existente en estado pendiente o activa.
- [x] **DTOs (`com.huertos.dto`):**
  - `CrearSolicitudComunidadRequest.java`: `record` con validaciones declarativas completas incluyendo `consentimientoDatos`.
  - `SolicitudComunidadResponse.java`: `record` para respuesta pública.
- [x] **Service (`com.huertos.service.SolicitudComunidadService.java` y `com.huertos.service.impl.SolicitudComunidadServiceImpl.java`):**
  - Método `SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request)`.
  - Validación de regla de mayoría de edad y registro auditable de `consentimientoDatos = true` con `consentimientoEn = LocalDateTime.now(clock)`.
- [x] **Mapper (`com.huertos.mapper.SolicitudComunidadMapper.java`):**
  - Método `toSolicitudComunidad(CrearSolicitudComunidadRequest request)`.
  - Método `toSolicitudComunidadResponse(SolicitudComunidad solicitud)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `solicitudes_comunidad` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar, incluyendo campos de consentimiento LOPDP):
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_comunidad` VARCHAR(150) NOT NULL
    - `tipo_organizacion` VARCHAR(30) NOT NULL (mapeado en JPA a enum `TipoOrganizacion`)
    - `provincia` VARCHAR(100) NOT NULL
    - `parroquia` VARCHAR(100) NOT NULL
    - `direccion` VARCHAR(255) NOT NULL
    - `latitud` DECIMAL(10, 7) NOT NULL
    - `longitud` DECIMAL(10, 7) NOT NULL
    - `altitud_msnm` INT NOT NULL DEFAULT 2500
    - `diseno_principal` VARCHAR(30) NOT NULL (mapeado en JPA a enum `DisenoPrincipal`)
    - `subtipos_solicitados` VARCHAR(150) NOT NULL
    - `cedula_solicitante` VARCHAR(13) NOT NULL
    - `nombre_solicitante` VARCHAR(120) NOT NULL
    - `fecha_nacimiento_solicitante` DATE NOT NULL
    - `genero_solicitante` VARCHAR(20) NOT NULL DEFAULT 'OTRO' (mapeado en JPA a enum `Genero`)
    - `correo_solicitante` VARCHAR(180) NOT NULL
    - `celular_solicitante` VARCHAR(20) NOT NULL
    - `consentimiento_datos` BOOLEAN NOT NULL
    - `consentimiento_en` DATETIME NOT NULL
    - `estado` VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' (mapeado en JPA a enum `EstadoSolicitud`)
    - `motivo_rechazo` VARCHAR(500) NULL
    - `revisado_por_id` BIGINT NULL (FK -> `usuarios(id)`)
    - `fecha_revision` DATETIME NULL
    - `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
  - *(NO generar nuevo archivo de migración SQL sin instrucción expresa)*.
- [x] **Entidad JPA (`com.huertos.model.SolicitudComunidad.java`, `TipoOrganizacion.java`, `DisenoPrincipal.java`, `EstadoSolicitud.java`, `Genero.java`):**
  - Mapeo de `@Entity` sobre `solicitudes_comunidad` con los atributos `consentimientoDatos` (`boolean`) y `consentimientoEn` (`LocalDateTime`).
- [x] **Repository (`com.huertos.repository.SolicitudComunidadRepository.java`):**
  - Métodos `boolean existsByNombreComunidadAndEstado(String nombreComunidad, EstadoSolicitud estado);`
  - Métodos `boolean existsByCedulaSolicitanteAndEstado(String cedula, EstadoSolicitud estado);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SolicitudComunidadServiceImplTest.java`):**
  - Registro exitoso: comprueba estado inicial `PENDIENTE`, almacenamiento de `fechaNacimiento`, persistencia de `consentimientoDatos = true` y fecha/hora `consentimientoEn` con `Clock.fixed(...)`.
  - Validación de menor de edad: lanza `ReglaNegocioException` si el solicitante es menor de 18 años.
  - Validación de conflicto: lanza `ConflictoException` si la cédula o nombre ya tiene solicitud pendiente.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SolicitudComunidadPublicoControllerTest.java`):**
  - Con `@WebMvcTest(SolicitudComunidadPublicoController.class)`, verificando respuesta `201 Created` en acceso público sin token JWT.
  - Validación de rechazo `400 Bad Request` ante campos vacíos, fecha de nacimiento faltante, `consentimientoDatos = false` o teléfono con formato erróneo.
- [x] **Prueba de Componente Frontend (`frontend/src/features/solicitudes-publico/SolicitudComunidadForm.test.tsx`):**
  - Renderizado de todos los campos agrupados responsivamente en tarjetas.
  - Verificación de apertura del modal `ModalConsentimientoLopdp` al hacer clic en enviar, comprobando que el botón "Aceptar y Continuar" esté inicialmente deshabilitado hasta marcar el checkbox.
  - Envío exitoso tras aceptar el consentimiento y posterior despliegue del modal de felicitación.
