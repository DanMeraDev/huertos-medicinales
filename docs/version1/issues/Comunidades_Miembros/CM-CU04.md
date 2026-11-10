---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU04: Registrar miembro o voluntario en la comunidad"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad registrar e integrar a un nuevo miembro voluntario dentro de su colectivo de siembra, certificando formalmente el cumplimiento de la Ley Orgánica de Protección de Datos Personales (LOPDP) del Ecuador mediante consentimiento informado del titular o tutor legal. El flujo maneja una persistencia dual relacional (tablas miembros y comunidad_miembros) para soportar que un voluntario participe en múltiples huertos: si la persona ya existe en el sistema por su número de cédula pero aún no pertenece a la comunidad actual, se asocia limpiamente creando su membresía; si ya se encuentra registrada en la misma comunidad, se bloquea la duplicidad; y si es una persona nueva, se dan de alta ambas entidades inicializando su nivel en APRENDIZ, sus puntos en 0 y la auditoría de consentimiento.`
* **Dependencias:** `Depende de #US-CU01 (sesión activa de Responsable de Comunidad) y #US-CU06 (existencia de la comunidad)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y dispone de un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario tiene asignada una comunidad activa en la tabla comunidades vinculada a su responsable_id.`
3. `Las tablas miembros y comunidad_miembros se encuentran configuradas en MariaDB según las migraciones reales del backend con las columnas de consentimiento LOPDP.`
4. `El endpoint POST /api/comunidades/mi-comunidad/miembros exige autorización con rol RESPONSABLE_COMUNIDAD en SecurityConfig.java.`

### Postcondiciones
1. `Si el miembro no existía previamente: se crea un registro en miembros con consentimiento_datos = true, consentimiento_en con Clock, puntos_totales = 0, activo = true y fecha actual de Clock.`
2. `Se crea un registro en la tabla comunidad_miembros vinculando comunidad_id y miembro_id con activo_en_comunidad = true y fecha_ingreso establecida con la fecha actual del sistema.`
3. `El sistema responde con código HTTP 201 Created y el DTO MiembroDetalleResponse.`
4. `El frontend limpia el formulario, actualiza el conteo de voluntarios y despliega una notificación de bienvenida mediante el componente Notificacion.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** ingresa al módulo de voluntarios accediendo a la ruta `"/comunidad/miembros/nuevo"` desde la barra de navegación en `Layout.tsx`.
2. El **Sistema** renderiza la vista diseñada bajo el enfoque **Mobile First para Progressive Web App (PWA)**, optimizada para que el responsable pueda inscribir voluntarios directamente en el huerto desde su teléfono móvil o desde una computadora, aplicando las variables de diseño de `frontend/src/index.css`:
   - **Contenedor Responsivo:** La vista se monta dentro de un contenedor principal (`RegistrarMiembroPage.tsx`) que en dispositivos móviles utiliza `width: 100%` con padding lateral de `var(--espacio-4)`. En pantallas de escritorio (`@media (min-width: 640px)`), se centra con `max-width: var(--ancho-lectura)` o `var(--ancho-maximo)` y márgenes automáticos (`padding: var(--espacio-8) var(--espacio-4)`).
   - **Cabecera Motivacional:** Tarjeta con fondo `var(--color-superficie)`, borde de `var(--borde-ancho) solid var(--color-borde)`, radio `var(--radio-lg)`, padding `var(--espacio-4)` y sombra `var(--sombra-sm)`, integrando el componente `<KintiGuia variante="dia"/>`:
     - Mensaje: *"¡Mejores son dos que uno, que la comunidad siga creciendo! Cada nueva mano que se suma a la siembra fortalece el saber y la salud de nuestro pueblo"*, con tipografía `var(--texto-base)`, color `var(--color-primario)` y peso `var(--peso-medio)`.
   - **Formulario Responsivo:**
     - En dispositivos móviles (< 640px), los campos se presentan en una sola columna vertical (`display: grid; grid-template-columns: 1fr; gap: var(--espacio-4)`).
     - En pantallas de escritorio (`@media (min-width: 640px)`), se organiza en grilla de dos columnas (`grid-template-columns: repeat(2, 1fr); gap: var(--espacio-4)`).
     - Campos con controles táctiles accesibles (altura mínima de 44px, padding `var(--espacio-2) var(--espacio-3)` y radio `var(--radio-md)`):
       - `Cédula de Identidad` (`cedula`: componente `CampoTexto`, numérico de 10 dígitos obligatorio con algoritmo ecuatoriano).
       - `Nombres` (`nombres`: componente `CampoTexto`, obligatorio, solo letras y espacios, máx. 100 caracteres).
       - `Apellidos` (`apellidos`: componente `CampoTexto`, obligatorio, solo letras y espacios, máx. 100 caracteres).
       - `Fecha de Nacimiento` (`fechaNacimiento`: componente `CampoTexto`, tipo `date`, obligatorio, menor a la fecha actual y edad mínima requerida de 10 años).
       - `Género` (`genero`: componente `Selector`, opciones: `FEMENINO`, `MASCULINO`, `OTRO`, obligatorio).
       - `Teléfono Celular` (`telefono`: componente `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos iniciando en `09`).
       - `Nivel Inicial` (`nivel`: componente `Selector`, opciones: `APRENDIZ`, `OFICIAL`, `MAESTRO`, valor predeterminado obligatorio: `APRENDIZ`).
     - Botón de acción **`"Registrar Voluntario"`** (componente `Boton`, variante `primary`): En smartphones se proyecta al ancho completo (`width: 100%`) con padding táctil amplio `var(--espacio-3)`; en escritorio se alinea a la derecha ocupando `grid-column: 1 / -1` con `width: auto; min-width: 14rem`.
3. El **Responsable de Comunidad** completa los datos solicitados y presiona o toca **`"Registrar Voluntario"`**.
4. El **Sistema** (frontend) valida en el cliente el cumplimiento del algoritmo de cédula ecuatoriana, que los nombres/apellidos no contengan dígitos o caracteres especiales, que el teléfono cumpla con el formato `09XXXXXXXX`, y que la persona tenga al menos 10 años de edad.
5. El **Sistema** abre una ventana emergente modal (`Modal`), diseñada bajo arquitectura Mobile First para PWA:
   - **Título:** *"Cumplimiento LOPDP - Consentimiento de Voluntario"* en tipografía `var(--texto-xl)`, color `var(--color-primario)` y peso `var(--peso-seminegrita)`.
   - **Cuerpo Informativo (LOPDP):** Texto institucional claro con tipografía `var(--texto-sm)`, interlineado `var(--interlineado-normal)` y color `var(--color-texto)`:
     > *"Antes de registrar a este voluntario/a en el sistema comunitario, confirma el cumplimiento de la Ley Orgánica de Protección de Datos Personales (LOPDP):*
     >
     > * **Consentimiento Informado:** Confirmas que la persona voluntaria (o su representante legal/tutor en caso de ser menor de edad) ha manifestado su consentimiento previo para registrar su cédula, nombres, edad y teléfono en esta plataforma.
     > * **Visibilidad Pública de Gamificación:** Se le ha comunicado al voluntario que su nombre, nivel, comunidades e insignias obtenidas serán visibles públicamente en la plataforma al ingresar su número de cédula.
     > * **Uso Exclusivo:** Los datos se destinarán con exclusividad al registro de actividades de huertos y el seguimiento de saberes comunitarios, sin fines de comercialización ni traspaso a terceros ajenos a la red."*
   - **Control Interactivo (Checkbox Obligatorio):**
     - Casilla accesible de verificación táctil:
       `[ ] Certifico como Responsable Comunitario que cuento con la autorización expresa del titular (o de su representante legal) para el registro y visualización de sus datos en la plataforma.`
   - **Botones de Acción Táctiles:**
     - En pantallas móviles se organizan verticalmente a ancho completo (`width: 100%`); en escritorio en fila horizontal:
       - Botón **`"Volver"`** (variante `secondary`, cierra el modal permitiendo corregir datos).
       - Botón **`"Confirmar Registro"`** (`Boton`, variante `primary`, **deshabilitado hasta marcar la casilla de certificación**).
6. El **Responsable de Comunidad** marca la casilla de certificación y pulsa o toca **`"Confirmar Registro"`**.
7. El **Sistema** cierra el modal, deshabilita el botón mostrando estado de carga y emite la petición `POST /api/comunidades/mi-comunidad/miembros` con el payload:
```json
{
  "cedula": "1710034065",
  "nombres": "María Tránsito",
  "apellidos": "Amaguaña Alba",
  "fechaNacimiento": "2010-03-25",
  "genero": "FEMENINO",
  "telefono": "0981234567",
  "nivel": "APRENDIZ",
  "consentimientoDatos": true
}
```
8. El **Sistema** (backend):
   - Extrae la identidad del responsable logueado y obtiene la `comunidadId`.
   - Valida que `consentimientoDatos` sea `true`.
   - Busca si ya existe un registro en `miembros` con esa `cedula`.
   - Si ya existe el miembro: verifica en `comunidad_miembros` si ya está vinculado a la `comunidadId`. Si ya pertenece a esta comunidad, rechaza la operación (HTTP 409 Conflict). Si no pertenece a esta comunidad, reutiliza la entidad `Miembro` existente y solo procede a crear la nueva vinculación en `comunidad_miembros`.
   - Si no existe el miembro: crea y guarda la nueva entidad `Miembro` en la tabla `miembros` con `puntosTotales = 0`, `activo = true`, `consentimiento_datos = true` y `consentimiento_en = LocalDateTime.now(clock)`, y a continuación crea el registro en `comunidad_miembros`.
9. El **Sistema** persiste las operaciones de manera atómica bajo `@Transactional` y responde con código HTTP `201 Created` y el DTO `MiembroDetalleResponse`:
```json
{
  "id": 15,
  "comunidadId": 3,
  "cedula": "1710034065",
  "nombreCompleto": "María Tránsito Amaguaña Alba",
  "nombres": "María Tránsito",
  "apellidos": "Amaguaña Alba",
  "fechaNacimiento": "2010-03-25",
  "edad": 16,
  "genero": "FEMENINO",
  "telefono": "0981234567",
  "nivel": "APRENDIZ",
  "puntosTotales": 0,
  "activoEnComunidad": true,
  "fechaIngreso": "2026-11-10"
}
```
10. El **Sistema** (frontend) limpia los campos, muestra una notificación visual de éxito con `Notificacion`: *"¡Voluntario/a registrado/a exitosamente en la comunidad!"* y redirige al listado interno de miembros.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **5a. Cédula inválida o menor a 10 años de edad (HTTP 400 Bad Request):**
  - **Condición:** Si la cédula no supera el dígito verificador ecuatoriano, si la fecha de nacimiento es futura, o si la edad calculada con `Clock` es menor a 10 años.
  - **Comportamiento:** Bean Validation o validación de dominio rechaza la solicitud. El frontend resalta el error con `Notificacion`: *"La persona voluntaria debe tener al menos 10 años de edad cumplidos"*.
* **5b. Nombres o apellidos con números o símbolos (HTTP 400 Bad Request):**
  - **Condición:** Si los nombres o apellidos contienen caracteres ajenos a letras del alfabeto español y espacios (`^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$`).
  - **Comportamiento:** El backend rechaza la petición con HTTP 400 ProblemDetail: *"Los nombres y apellidos solo pueden contener letras y espacios"*.
* **5c. Falta de certificación de consentimiento (HTTP 400 Bad Request):**
  - **Condición:** Si `consentimientoDatos` llega como `false` o nulo al servidor.
  - **Comportamiento:** Bean Validation rechaza la petición (`@AssertTrue`): *"Debe certificar la autorización del voluntario o su representante legal bajo la LOPDP"*.
* **7a. Voluntario ya registrado en esta misma comunidad (HTTP 409 Conflict):**
  - **Condición:** Si la cédula ya existe en la base de datos y se encuentra vinculada con `activo_en_comunidad = true` a la comunidad del responsable actual.
  - **Comportamiento:** El backend lanza `ConflictoException`: *"El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad"*. El frontend informa el conflicto mediante `Notificacion` sin borrar los datos del formulario.
* **7b. Voluntario registrado en otra comunidad (Flujo Exitoso de Vinculación Múltiple):**
  - **Condición:** Si la cédula ya existe en la tabla general `miembros` pero no tiene registro en `comunidad_miembros` para la comunidad del responsable.
  - **Comportamiento:** El sistema no lanza error; vincula exitosamente al miembro existente en `comunidad_miembros` para esta nueva comunidad, respetando su histórico de puntos y nivel alcanzado.
* **7c. Responsable sin comunidad asignada (HTTP 404 Not Found):**
  - **Condición:** Si el usuario autenticado carece de una comunidad activa en `comunidades`.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"No se encontró ninguna comunidad asociada al responsable autenticado"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros/`):**
  - `RegistrarMiembroForm.tsx`: Componente de formulario con diseño Mobile First para PWA. Contiene validación reactiva de cédula ecuatoriana, cálculo de edad en cliente (+10 años), selectores de género (`FEMENINO`, `MASCULINO`, `OTRO`) y nivel (default `APRENDIZ`), presencia de `<KintiGuia variante="dia"/>` y feedback con `Notificacion` empleando variables de `index.css`.
  - `ModalConsentimientoMiembroLopdp.tsx`: Diálogo modal responsivo previo al registro con el texto de advertencia LOPDP, control interactivo de checkbox para certificar la autorización del voluntario/tutor, botón "Volver" y botón "Confirmar Registro" habilitado solo al marcar la casilla.
  - `miembrosApi.ts`: Función `registrarMiembroEnComunidad(datos: RegistrarMiembroRequest): Promise<MiembroDetalleResponse>` consumiendo `POST /api/comunidades/mi-comunidad/miembros` a través de `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `RegistrarMiembroPage.tsx`: Vista protegida en `App.tsx` bajo la ruta `/comunidad/miembros/nuevo`, accesible solo para usuarios con rol `RESPONSABLE_COMUNIDAD`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadMiembroController.java`):**
  - `POST /api/comunidades/mi-comunidad/miembros` -> Recibe `@Valid @RequestBody RegistrarMiembroRequest request` y `Authentication authentication`, retorna HTTP `201 Created` con `MiembroDetalleResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Cuerpo de Solicitud (`RegistrarMiembroRequest`):**
    - `cedula`: `String`, obligatorio, 10 dígitos (`@NotBlank`, `@Pattern(regexp = "^[0-9]{10}$")`).
    - `nombres`: `String`, obligatorio, solo letras y espacios (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`, `@Size(max = 100)`).
    - `apellidos`: `String`, obligatorio, solo letras y espacios (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`, `@Size(max = 100)`).
    - `fechaNacimiento`: `LocalDate`, obligatorio (`@NotNull`, `@Past`).
    - `genero`: `Genero`, obligatorio (`@NotNull`, valores: `FEMENINO`, `MASCULINO`, `OTRO`).
    - `telefono`: `String`, obligatorio (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$")`).
    - `nivel`: `NivelMiembro`, obligatorio (`@NotNull`, valor por defecto: `APRENDIZ`).
    - `consentimientoDatos`: `Boolean`, obligatorio (`@NotNull`, `@AssertTrue(message = "Debe certificar la autorización del voluntario o tutor legal")`).
  - **Respuesta Exitosa (HTTP 201 Created - `MiembroDetalleResponse`):**
    - `id`: `Long` (ID del miembro).
    - `comunidadId`: `Long`.
    - `cedula`: `String`.
    - `nombreCompleto`: `String`.
    - `nombres`: `String`.
    - `apellidos`: `String`.
    - `fechaNacimiento`: `LocalDate`.
    - `edad`: `Integer`.
    - `genero`: `Genero`.
    - `telefono`: `String`.
    - `nivel`: `NivelMiembro`.
    - `puntosTotales`: `Integer`.
    - `activoEnComunidad`: `boolean`.
    - `fechaIngreso`: `LocalDate`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Error en Bean Validation, menor de 10 años, falta de consentimiento o cédula inválida.
    - `401 Unauthorized`: Token ausente o expirado.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: Comunidad del responsable no encontrada.
    - `409 Conflict`: El voluntario ya está registrado en esta comunidad.
- [x] **DTOs (`com.huertos.dto`):**
  - `RegistrarMiembroRequest.java`: `public record RegistrarMiembroRequest(@NotBlank(message = "La cédula es obligatoria") @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener 10 dígitos numéricos") String cedula, @NotBlank(message = "Los nombres son obligatorios") @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los nombres solo admiten letras y espacios") @Size(max = 100) String nombres, @NotBlank(message = "Los apellidos son obligatorios") @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los apellidos solo admiten letras y espacios") @Size(max = 100) String apellidos, @NotNull(message = "La fecha de nacimiento es obligatoria") @Past(message = "La fecha debe ser anterior a la actual") LocalDate fechaNacimiento, @NotNull(message = "El género es obligatorio") Genero genero, @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe iniciar con 09 y tener 10 dígitos") String telefono, @NotNull(message = "El nivel es obligatorio") NivelMiembro nivel, @NotNull(message = "Debe certificar el consentimiento") @AssertTrue(message = "Debe certificar la autorización del voluntario o tutor legal") Boolean consentimientoDatos) {}`.
  - `MiembroDetalleResponse.java`: `record` con los datos consolidados del voluntario y su membresía.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `@Transactional MiembroDetalleResponse registrarMiembroEnComunidad(Long responsableId, RegistrarMiembroRequest request)`.
  - Valida edad `>= 10 años` con `Clock`.
  - Guarda `consentimientoDatos = true` y `consentimientoEn = LocalDateTime.now(clock)`.
  - Verifica si `miembroRepository.findByCedula(request.cedula())` existe.
  - Verifica si `comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(...)` existe.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toMiembro(RegistrarMiembroRequest request)`.
  - Método `toDetalleResponse(Miembro miembro, ComunidadMiembro relacion, int edad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Consume las tablas `miembros` y `comunidad_miembros` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar, incluyendo campos de consentimiento LOPDP):
    - `miembros`: `id` BIGINT AUTO_INCREMENT PRIMARY KEY, `cedula` VARCHAR(13) NOT NULL UNIQUE, `nombres` VARCHAR(100) NOT NULL, `apellidos` VARCHAR(100) NOT NULL, `fecha_nacimiento` DATE NOT NULL, `genero` VARCHAR(20) NOT NULL (mapeado en JPA a enum `Genero`), `telefono` VARCHAR(20) NULL, `nivel` VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ' (mapeado en JPA a enum `NivelMiembro`), `puntos_totales` INT NOT NULL DEFAULT 0, `consentimiento_datos` BOOLEAN NOT NULL DEFAULT TRUE, `consentimiento_en` DATETIME NOT NULL, `activo` BOOLEAN NOT NULL DEFAULT TRUE, `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP.
    - `comunidad_miembros`: `id` BIGINT AUTO_INCREMENT PRIMARY KEY, `comunidad_id` BIGINT NOT NULL, `miembro_id` BIGINT NOT NULL, `activo_en_comunidad` BOOLEAN NOT NULL DEFAULT TRUE, `fecha_ingreso` DATE NOT NULL, `fecha_baja` DATE NULL, `motivo_baja` VARCHAR(255) NULL.
  - *(NO generar nuevo archivo de migración SQL sin instrucción expresa)*.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `com.huertos.model.ComunidadMiembro.java`, `Genero.java`, `NivelMiembro.java`):**
  - Mapeo de entidades con JPA y enums con `@Enumerated(EnumType.STRING)`, con atributos `consentimientoDatos` (`boolean`) y `consentimientoEn` (`LocalDateTime`).
- [x] **Repository (`com.huertos.repository.MiembroRepository.java`, `ComunidadMiembroRepository.java`):**
  - `Optional<Miembro> findByCedula(String cedula);`
  - `boolean existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(Long comunidadId, Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Registro de nuevo voluntario: verifica persistencia en `miembros` con `consentimientoDatos = true`, `consentimientoEn` e inserción en `comunidad_miembros` con Mockito.
  - Reutilización de voluntario de otra comunidad: verifica que no duplique `Miembro` y solo inserte relación en `comunidad_miembros`.
  - Duplicidad en la misma comunidad: lanza `ConflictoException`.
  - Menor a 10 años: lanza `ReglaNegocioException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadMiembroControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadMiembroController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `POST /api/comunidades/mi-comunidad/miembros` con datos válidos y consentimiento retorna HTTP `201 Created`.
    - Petición con `consentimientoDatos = false` retorna HTTP `400 Bad Request`.
    - Cédula inválida retorna HTTP `400 Bad Request`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros/RegistrarMiembroForm.test.tsx`):**
  - Renderizado responsivo de campos (1 columna en móvil), selectores de género y nivel, y presencia visual de `<KintiGuia/>`.
  - Apertura del modal `ModalConsentimientoMiembroLopdp` al pulsar "Registrar Voluntario", validando que el botón "Confirmar Registro" esté deshabilitado hasta marcar la casilla de certificación.
  - Envío exitoso con MSW (`handlers.ts`), limpieza de campos y redirección al listado.
