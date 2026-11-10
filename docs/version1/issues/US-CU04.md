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
* **Descripción:** `Permite a cualquier líder barrial, familiar o colectivo ciudadano enviar de manera pública y sin requerir inicio de sesión una solicitud formal para constituir una nueva comunidad de huertos medicinales y su espacio de siembra. Registra la información territorial, diseño propuesto del huerto y datos personales del solicitante, quien asumirá el rol de Responsable de Comunidad una vez validada la propuesta por el Ministerio de Salud Pública.`
* **Dependencias:** `Ninguna (punto de entrada público que alimenta a #US-CU05)`

### Precondiciones
1. `El usuario accede a la plataforma en Modo Libre mediante la ruta pública /solicitar-comunidad.`
2. `El endpoint POST /api/publico/solicitudes-comunidad se encuentra configurado con acceso público (permitAll()) en SecurityConfig.java.`
3. `La tabla solicitudes_comunidad se encuentra creada en la base de datos según V2__esquema_completo_v1.sql.`

### Postcondiciones
1. `Se persiste un nuevo registro en la tabla solicitudes_comunidad con estado = 'PENDIENTE', creado_en con Clock del sistema, revisado_por_id = null y motivo_rechazo = null.`
2. `El sistema responde con código HTTP 201 Created y el DTO SolicitudComunidadResponse.`
3. `El frontend despliega un diálogo emergente (Modal) con Kinti felicitando al solicitante y notificando que la solicitud pasará a revisión del equipo técnico del MSP.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** ingresa a la opción `"Crear Comunidad"` desde la barra de navegación pública en `Layout.tsx` o desde la pantalla de bienvenida.
2. El **Sistema** despliega una página interactiva, visualmente enriquecida con motivos andinos, encabezada por `<KintiGuia variante="dia"/>` con el mensaje:
   *"¡Sembrar en comunidad sana el cuerpo y une los corazones! Da el primer paso para cultivar la salud de tu barrio completando esta solicitud"*.
3. El **Sistema** renderiza el formulario dinámico organizado en dos secciones principales con componentes del sistema:
   - **Datos de la Comunidad y Huerto:**
     - `Nombre de la Comunidad` (`nombreComunidad`: `CampoTexto`, obligatorio, solo letras y espacios, máx. 150 caracteres).
     - `Tipo de Organización` (`tipoOrganizacion`: `Selector`, opciones: `INDIVIDUAL`, `FAMILIA`, `CONDOMINIO`, `BARRIO`, `COMITE_LOCAL`, obligatorio).
     - `Provincia` (`provincia`: `CampoTexto`, obligatorio, solo letras, máx. 100 caracteres).
     - `Parroquia` (`parroquia`: `CampoTexto`, obligatorio, solo letras, máx. 100 caracteres).
     - `Dirección Exacta` (`direccion`: `CampoTexto`, obligatorio, texto libre, números y referencias, máx. 255 caracteres).
     - `Ubicación Geográfica` (`latitud`, `longitud`: inputs numéricos decimales integrados con selector visual de coordenadas de mapa).
     - `Altitud Estimada (msnm)` (`altitudMsnm`: `CampoTexto`, numérico, valor por defecto `2500`).
     - `Diseño Principal del Huerto` (`disenoPrincipal`: `Selector`, opciones: `EN_TIERRA`, `VERTICAL`, `COLGANTE`, `EN_PISO`, obligatorio).
     - `Subtipos de Cultivo Solicitados` (`subtiposSolicitados`: selector múltiple o texto que almacena hasta 3 subtipos separados por coma, ej: `"SURCOS_WACHU,TERRAZAS_ANTI,MACETAS_PISO"`).
   - **Datos del Solicitante (Futuro Responsable):**
     - `Cédula de Identidad` (`cedulaSolicitante`: `CampoTexto`, 10 dígitos numéricos, obligatorio con validación de cédula ecuatoriana).
     - `Nombre y Apellido` (`nombreSolicitante`: `CampoTexto`, obligatorio, solo letras y espacios, máx. 120 caracteres).
     - `Correo Electrónico` (`correoSolicitante`: `CampoTexto`, tipo `email`, obligatorio).
     - `Teléfono Celular` (`celularSolicitante`: `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos iniciando en `09`).
     - `Género` (`generoSolicitante`: `Selector`, opciones: `FEMENINO`, `MASCULINO`, `OTRO`, obligatorio).
   - Botón de envío **`"Enviar Solicitud de Huerto"`** (`Boton`, variante `primary`).
4. El **Visitante** ingresa la información requerida y presiona **`"Enviar Solicitud de Huerto"`**.
5. El **Sistema** (frontend) verifica localmente el cumplimiento del algoritmo de cédula ecuatoriana, formato del celular (`09XXXXXXXX`), email y la ausencia de caracteres especiales o números en el nombre de la comunidad y solicitante.
6. El **Sistema** deshabilita el botón mostrando indicador de carga y emite la petición `POST /api/publico/solicitudes-comunidad` con el payload:
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
  "correoSolicitante": "comunidad.allpa@huertos.gob.ec",
  "celularSolicitante": "0991234567",
  "generoSolicitante": "MASCULINO"
}
```
7. El **Sistema** (backend) valida la integridad de datos, verifica que no exista una solicitud idéntica en estado `PENDIENTE` o una comunidad registrada con el mismo nombre o la misma cédula, asigna `estado = 'PENDIENTE'` y fecha de registro con `Clock.systemUTC()`, y persiste el registro en `solicitudes_comunidad`.
8. El **Sistema** responde con HTTP `201 Created` y el DTO `SolicitudComunidadResponse`:
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
9. El **Sistema** (frontend) abre una ventana emergente (`Modal`):
   - **Encabezado:** `<KintiGuia variante="celebracion"/>`
   - **Título:** *"¡Felicidades por sembrar esperanza!"*
   - **Mensaje:** *"Tu solicitud para constituir el huerto **Huerto Comunitario Allpa Viva** ha sido recibida con éxito. El equipo técnico del MSP revisará la propuesta y te enviaremos una notificación al correo **comunidad.allpa@huertos.gob.ec** con los resultados y las instrucciones de acceso."*
   - **Acción:** Botón **`"Entendido"`** que cierra el modal y redirige a la página pública de inicio (`/inicio`).

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Campos obligatorios faltantes o formatos inválidos (HTTP 400 Bad Request):**
  - **Condición:** Si algún campo obligatorio está en blanco, el celular no tiene 10 dígitos o no comienza en `09`, la cédula no supera el dígito verificador, o el nombre de la comunidad contiene números o símbolos.
  - **Comportamiento:** Bean Validation rechaza la petición. El frontend muestra la notificación de advertencia con `Notificacion` indicando con precisión los campos por corregir sin borrar los datos digitados.
* **4b. Coordenadas geográficas fuera de rango ecuatoriano (HTTP 400 Bad Request):**
  - **Condición:** Si la latitud o longitud no corresponden a rangos razonables del territorio ecuatoriano (Latitud entre `-5.5` y `2.0`, Longitud entre `-92.0` y `-75.0`).
  - **Comportamiento:** El backend lanza `ReglaNegocioException`: *"Las coordenadas geográficas ingresadas no corresponden al territorio ecuatoriano"*.
* **4c. Nombre de comunidad o cédula ya en proceso (HTTP 409 Conflict):**
  - **Condición:** Si ya existe una solicitud en estado `PENDIENTE` o una comunidad activa en `comunidades` con el mismo nombre o la misma cédula del solicitante.
  - **Comportamiento:** El backend lanza `ConflictoException` con el mensaje: *"Ya existe una solicitud pendiente o una comunidad registrada con el nombre o la cédula indicada"*. Se notifica al usuario con el componente `Notificacion`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/solicitudes-publico/`):**
  - `SolicitudComunidadForm.tsx`: Formulario dinámico con campos agrupados, selector de tipo de organización y diseño, validación en tiempo real de cédula y celular, presencia de `<KintiGuia variante="dia"/>` y modal de confirmación con `<KintiGuia variante="celebracion"/>`.
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
    - `disenoPrincipal`: `DisenoHuerto`, obligatorio (`@NotNull`).
    - `subtiposSolicitados`: `String`, obligatorio (`@NotBlank`, `@Size(max = 150)`).
    - `cedulaSolicitante`: `String`, obligatorio, 10 dígitos (`@NotBlank`, `@Pattern(regexp = "^[0-9]{10}$")`).
    - `nombreSolicitante`: `String`, obligatorio (`@NotBlank`, `@Size(max = 120)`).
    - `correoSolicitante`: `String`, obligatorio (`@NotBlank`, `@Email`, `@Size(max = 180)`).
    - `celularSolicitante`: `String`, obligatorio, inicia en 09 (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$")`).
    - `generoSolicitante`: `Genero`, obligatorio (`@NotNull`).
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
    - `400 Bad Request`: Error en Bean Validation o validación de cédula/coordenadas.
    - `409 Conflict`: Nombre de comunidad o cédula ya existente en estado pendiente o activa.
- [x] **DTOs (`com.huertos.dto`):**
  - `CrearSolicitudComunidadRequest.java`: `record` con validaciones declarativas.
  - `SolicitudComunidadResponse.java`: `record` para respuesta pública.
- [x] **Service (`com.huertos.service.SolicitudComunidadService.java` y `com.huertos.service.impl.SolicitudComunidadServiceImpl.java`):**
  - Método `SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request)`.
  - Validación de regla de unicidad contra `SolicitudComunidadRepository` y `ComunidadRepository`.
- [x] **Mapper (`com.huertos.mapper.SolicitudComunidadMapper.java`):**
  - Método `toSolicitudComunidad(CrearSolicitudComunidadRequest request)`.
  - Método `toSolicitudComunidadResponse(SolicitudComunidad solicitud)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `solicitudes_comunidad` definida en `V2__esquema_completo_v1.sql`:
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_comunidad` VARCHAR(150) NOT NULL
    - `tipo_organizacion` ENUM('INDIVIDUAL', 'FAMILIA', 'CONDOMINIO', 'BARRIO', 'COMITE_LOCAL') NOT NULL
    - `provincia` VARCHAR(100) NOT NULL
    - `parroquia` VARCHAR(100) NOT NULL
    - `direccion` VARCHAR(255) NOT NULL
    - `latitud` DECIMAL(10, 7) NOT NULL
    - `longitud` DECIMAL(10, 7) NOT NULL
    - `altitud_msnm` INT NOT NULL DEFAULT 2500
    - `diseno_principal` ENUM('EN_TIERRA', 'VERTICAL', 'COLGANTE', 'EN_PISO') NOT NULL
    - `subtipos_solicitados` VARCHAR(150) NOT NULL
    - `cedula_solicitante` VARCHAR(13) NOT NULL
    - `nombre_solicitante` VARCHAR(120) NOT NULL
    - `correo_solicitante` VARCHAR(180) NOT NULL
    - `celular_solicitante` VARCHAR(20) NOT NULL
    - `genero_solicitante` ENUM('FEMENINO', 'MASCULINO', 'OTRO') NOT NULL DEFAULT 'OTRO'
    - `estado` ENUM('PENDIENTE', 'APROBADA', 'RECHAZADA') NOT NULL DEFAULT 'PENDIENTE'
    - `motivo_rechazo` VARCHAR(500) NULL
    - `revisado_por_id` BIGINT NULL
    - `fecha_revision` DATETIME(6) NULL
    - `creado_en` DATETIME(6) NOT NULL
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.SolicitudComunidad.java`, `TipoOrganizacion.java`, `DisenoHuerto.java`, `EstadoSolicitud.java`):**
  - Mapeo de `@Entity` sobre `solicitudes_comunidad`.
- [x] **Repository (`com.huertos.repository.SolicitudComunidadRepository.java`):**
  - Métodos `boolean existsByNombreComunidadAndEstado(String nombreComunidad, EstadoSolicitud estado);`
  - Métodos `boolean existsByCedulaSolicitanteAndEstado(String cedula, EstadoSolicitud estado);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SolicitudComunidadServiceImplTest.java`):**
  - Registro exitoso: comprueba estado inicial `PENDIENTE`, asignación de fecha con `Clock` y retorno de DTO.
  - Validación de conflicto: lanza `ConflictoException` si la cédula o nombre ya tiene solicitud pendiente.
  - Validación de regla de negocio: lanza `ReglaNegocioException` si las coordenadas están fuera de Ecuador.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SolicitudComunidadPublicoControllerTest.java`):**
  - Con `@WebMvcTest(SolicitudComunidadPublicoController.class)`, verificando respuesta `201 Created` en acceso público sin token JWT.
  - Validación de rechazo `400 Bad Request` ante campos vacíos, cédula inválida o teléfono con formato erróneo.
- [x] **Prueba de Componente Frontend (`frontend/src/features/solicitudes-publico/SolicitudComunidadForm.test.tsx`):**
  - Renderizado de todos los campos, selector de tipo de organización y presencia inicial de `<KintiGuia/>`.
  - Envío exitoso con mock de MSW en `handlers.ts`, verificación de apertura de `Modal` con variante de celebración de Kinti y posterior redirección.
