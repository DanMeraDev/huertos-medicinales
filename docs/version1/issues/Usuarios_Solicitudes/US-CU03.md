---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU03: Crear cuenta de usuario"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico SIS (Admin Sistema)`
* **Descripción:** `Permite al Técnico SIS registrar manualmente nuevas cuentas de usuario en la plataforma asignando cualquiera de los roles disponibles (TECNICO_SIS, TECNICO_MSP, RESPONSABLE_COMUNIDAD). Además, este caso de uso provee la lógica de servicio interna reutilizada por el backend al aprobar una solicitud comunitaria (US-CU05) para crear de forma automática la cuenta del Responsable de Comunidad. En ambos casos se valida la mayoría de edad, robustez de la contraseña, unicidad de identificación y correo, cifrando la clave con BCrypt y exigiendo cambio de credenciales en el primer acceso.`
* **Dependencias:** `Depende de #US-CU01 (para sesión de Técnico SIS) y sirve de base a #US-CU05`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol TECNICO_SIS y dispone de un token JWT válido (Authorization: Bearer <token>).`
2. `La base de datos cuenta con la tabla usuarios consolidada según las migraciones reales del backend.`
3. `El endpoint POST /api/usuarios se encuentra protegido en SecurityConfig.java exigiendo rol TECNICO_SIS.`

### Postcondiciones
1. `Se inserta un nuevo registro en la tabla usuarios con activo = true, requiere_cambio_clave = true y la contraseña cifrada con BCrypt en password_hash.`
2. `El sistema retorna código HTTP 201 Created con el DTO UsuarioDetalleResponse sin exponer el hash de contraseña.`
3. `El formulario en el frontend se limpia y muestra un mensaje de éxito mediante el componente Notificacion.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Técnico SIS** navega al módulo de usuarios accediendo a la ruta `"/usuarios/nuevo"` desde la barra de navegación en `Layout.tsx`.
2. El **Sistema** renderiza la vista de creación de usuario diseñada bajo el paradigma **Mobile First para Progressive Web App (PWA)**, optimizada para interactuar cómodamente desde dispositivos móviles de campo o estaciones de trabajo usando las variables de diseño de `frontend/src/index.css`:
   - **Contenedor Responsivo:** La vista se monta dentro de un contenedor principal (`CrearUsuarioPage.tsx`) que en dispositivos móviles utiliza `width: 100%` con padding de `var(--espacio-4)`. En tabletas y escritorio (`@media (min-width: 640px)`), se centra con `max-width: var(--ancho-lectura)` o `var(--ancho-maximo)` manteniendo un margen equilibrado (`padding: var(--espacio-8) var(--espacio-4)`).
   - **Tarjeta y Cabecera Andina:** Formulario montado en una tarjeta con fondo `var(--color-superficie)`, borde `var(--borde-ancho) solid var(--color-borde)`, radio `var(--radio-lg)` y sombra `var(--sombra-md)`. Incluye el componente `<KintiGuia variante="dia"/>` adaptado a pantallas táctiles:
     - Frase motivacional: *"El trabajo en comunidad florece cuando sumamos voluntades con confianza y compañerismo. Registra a los guardianes de nuestros saberes"*, con tipografía `var(--texto-base)`, color `var(--color-primario)` y peso `var(--peso-medio)`.
   - **Estructura de Formulario Adaptativo:**
     - En dispositivos móviles (< 640px), los campos se presentan en una sola columna vertical (`display: grid; grid-template-columns: 1fr; gap: var(--espacio-4)`), facilitando el desplazamiento vertical y la interacción táctil.
     - En pantallas de escritorio (`@media (min-width: 640px)`), se organiza en grilla de dos columnas (`grid-template-columns: repeat(2, 1fr); gap: var(--espacio-4)`), con los campos extensos ocupando las dos columnas (`grid-column: 1 / -1`).
   - **Campos del Formulario con Controles Accesibles (altura mínima 44px):**
     - `Nombre Completo` (`nombreCompleto`: `CampoTexto`, texto obligatorio, solo letras y espacios, máx. 120 caracteres, ancho completo `grid-column: 1 / -1`).
     - `Cédula de Identidad` (`identificacion`: `CampoTexto`, numérico de 10 dígitos obligatorio, validado con algoritmo de cédula ecuatoriana).
     - `Fecha de Nacimiento` (`fechaNacimiento`: `CampoTexto`, tipo `date`, obligatorio, edad mínima requerida: 18 años).
     - `Género` (`genero`: `Selector`, opciones: `FEMENINO`, `MASCULINO`, `OTRO`, obligatorio).
     - `Correo Electrónico` (`correo`: `CampoTexto`, tipo `email`, obligatorio, validación RFC).
     - `Teléfono Celular` (`telefono`: `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos iniciando en `09`).
     - `Rol del Sistema` (`rol`: `Selector`, opciones: `TECNICO_SIS`, `TECNICO_MSP`, `RESPONSABLE_COMUNIDAD`, obligatorio).
     - `Contraseña Temporal` (`password`: `CampoTexto`, tipo `password`, obligatorio, mínimo 12 caracteres, al menos 1 mayúscula, 1 minúscula y 1 número, ancho completo `grid-column: 1 / -1`).
     - Botón de envío **`"Crear Usuario"`** (`Boton`, variante `primary`): En móvil abarca el ancho total (`width: 100%`) con padding `var(--espacio-3)`; en pantallas de escritorio se alinea hacia la derecha ocupando `grid-column: 1 / -1` con `width: auto; min-width: 12rem`.
3. El **Técnico SIS** diligencia los campos y pulsa o toca **`"Crear Usuario"`**.
4. El **Sistema** (frontend) valida en tiempo real que la contraseña cumpla los criterios de seguridad, que el solicitante tenga al menos 18 años calculados respecto a la fecha actual y que la cédula sea válida.
5. El **Sistema** deshabilita el botón, muestra indicador de carga y envía la petición `POST /api/usuarios` con el payload:
```json
{
  "nombreCompleto": "Rosa Elena Chimbolema",
  "identificacion": "1710034065",
  "fechaNacimiento": "1990-05-14",
  "genero": "FEMENINO",
  "correo": "rosa.chimbolema@huertos.gob.ec",
  "telefono": "0987654321",
  "rol": "TECNICO_MSP",
  "password": "PasswordSegura2026*"
}
```
6. El **Sistema** (backend) valida la autorización JWT (`TECNICO_SIS`), verifica unicidad de `identificacion` y `correo`, valida con `Clock` del sistema que la edad sea `>= 18 años`, cifra la contraseña con `PasswordEncoder.encode(...)`, asigna `activo = true`, `requiereCambioClave = true` y almacena el registro en la tabla `usuarios`.
7. El **Sistema** retorna el código HTTP `201 Created` con el DTO `UsuarioDetalleResponse`:
```json
{
  "id": 5,
  "nombreCompleto": "Rosa Elena Chimbolema",
  "identificacion": "1710034065",
  "fechaNacimiento": "1990-05-14",
  "genero": "FEMENINO",
  "correo": "rosa.chimbolema@huertos.gob.ec",
  "telefono": "0987654321",
  "rol": "TECNICO_MSP",
  "activo": true,
  "requiereCambioClave": true,
  "creadoEn": "2026-11-10T10:30:00"
}
```
8. El **Sistema** limpia el formulario, muestra una alerta de confirmación con el componente `Notificacion` y actualiza la lista de usuarios.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Campos obligatorios vacíos o formato inválido (HTTP 400 Bad Request):**
  - **Condición:** Si alguno de los campos requeridos está ausente, el correo no cumple formato de email, o el teléfono no contiene 10 dígitos numéricos.
  - **Comportamiento:** Bean Validation rechaza la petición; `GlobalExceptionHandler` devuelve HTTP 400 ProblemDetail con el detalle del campo y el frontend resalta el error mediante `Notificacion` preservando los datos válidos.
* **4b. Contraseña débil (HTTP 400 Bad Request):**
  - **Condición:** Si `password` tiene menos de 12 caracteres o no incluye al menos una letra mayúscula, una minúscula y un dígito numérico (`@Pattern`).
  - **Comportamiento:** El backend lanza `ReglaNegocioException`: *"La contraseña debe tener al menos 12 caracteres, incluyendo al menos una mayúscula, una minúscula y un número"*.
* **4c. Menor de edad (HTTP 400 Bad Request):**
  - **Condición:** Si la diferencia entre la fecha actual del sistema (`Clock`) y `fechaNacimiento` es menor a 18 años.
  - **Comportamiento:** El backend lanza `ReglaNegocioException`: *"El usuario debe ser mayor de 18 años para ser registrado en la plataforma"*.
* **4d. Cédula o correo ya registrados (HTTP 409 Conflict):**
  - **Condición:** Si ya existe en la base de datos un usuario con la misma `identificacion` o el mismo `correo`.
  - **Comportamiento:** El backend lanza `ConflictoException` con el mensaje específico: *"Ya existe un usuario registrado con la cédula ingresada"* o *"Ya existe un usuario registrado con el correo electrónico ingresado"*.
* **4e. Acceso no autorizado o rol insuficiente (HTTP 401 / 403 Forbidden):**
  - **Condición:** Si la petición no incluye token JWT o el usuario logueado no posee el rol `TECNICO_SIS`.
  - **Comportamiento:** Spring Security bloquea la solicitud retornando HTTP 401 Unauthorized o HTTP 403 Forbidden.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/usuarios/`):**
  - `CrearUsuarioForm.tsx`: Componente con arquitectura Mobile First para PWA. Contiene formulario responsivo (1 columna en móvil, 2 en desktop), validaciones en tiempo real para cédula ecuatoriana y mayoría de edad (+18), requisitos de contraseña dinámica, selectores accesibles para género (`FEMENINO`, `MASCULINO`, `OTRO`) y rol (`TECNICO_SIS`, `TECNICO_MSP`, `RESPONSABLE_COMUNIDAD`), presencia visual de `<KintiGuia variante="dia"/>` y notificaciones con `Notificacion` empleando variables de `index.css`.
  - `usuariosApi.ts`: Exporta `crearUsuario(datos: CrearUsuarioRequest): Promise<UsuarioDetalleResponse>` consumiendo `POST /api/usuarios` mediante el cliente `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `CrearUsuarioPage.tsx`: Vista envuelta en `Layout.tsx` registrada en `App.tsx` bajo la ruta protegida `/usuarios/nuevo`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.UsuarioController.java`):**
  - `POST /api/usuarios` -> Recibe `@Valid @RequestBody CrearUsuarioRequest request`, retorna HTTP `201 Created` con `UsuarioDetalleResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('TECNICO_SIS')")`).
  - **Cuerpo de Solicitud (`CrearUsuarioRequest`):**
    - `nombreCompleto`: `String`, obligatorio (`@NotBlank`, `@Size(max = 120)`).
    - `identificacion`: `String`, obligatorio, 10 dígitos numéricos (`@NotBlank`, `@Pattern(regexp = "^[0-9]{10}$")`).
    - `fechaNacimiento`: `LocalDate`, obligatorio (`@NotNull`, `@Past`).
    - `genero`: `Genero`, obligatorio (`@NotNull`, valores: `FEMENINO`, `MASCULINO`, `OTRO`).
    - `correo`: `String`, obligatorio, formato email (`@NotBlank`, `@Email`, `@Size(max = 180)`).
    - `telefono`: `String`, obligatorio (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$")`).
    - `password`: `String`, obligatorio (`@NotBlank`, `@Size(min = 12)`, patrón seguro con mayúscula, minúscula y número).
    - `rol`: `Rol`, obligatorio (`@NotNull`, valores: `TECNICO_SIS`, `TECNICO_MSP`, `RESPONSABLE_COMUNIDAD`).
  - **Respuesta Exitosa (HTTP 201 Created - `UsuarioDetalleResponse`):**
    - `id`: `Long` (identificador generado).
    - `nombreCompleto`: `String`.
    - `identificacion`: `String`.
    - `fechaNacimiento`: `LocalDate`.
    - `genero`: `Genero`.
    - `correo`: `String`.
    - `telefono`: `String`.
    - `rol`: `Rol`.
    - `activo`: `boolean` (valor inicial `true`).
    - `requiereCambioClave`: `boolean` (valor inicial `true`).
    - `creadoEn`: `LocalDateTime`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Formato incorrecto, contraseña insegura o menor de edad.
    - `401 Unauthorized`: Token JWT ausente o inválido.
    - `403 Forbidden`: Usuario sin rol `TECNICO_SIS`.
    - `409 Conflict`: Cédula o correo duplicado.
- [x] **DTOs (`com.huertos.dto`):**
  - `CrearUsuarioRequest.java`: `record` con validaciones Bean Validation (`@NotBlank`, `@Email`, `@Pattern`, `@Size`, `@NotNull`).
  - `UsuarioDetalleResponse.java`: `record` con los datos del usuario creado.
- [x] **Service (`com.huertos.service.UsuarioService.java` y `com.huertos.service.impl.UsuarioServiceImpl.java`):**
  - Método `UsuarioDetalleResponse crearUsuario(CrearUsuarioRequest request)`.
  - Método de soporte interno: `Usuario crearUsuarioInterno(String nombre, String cedula, LocalDate nacimiento, Genero genero, String correo, String telefono, String passwordPlano, Rol rol)` reutilizable por `US-CU05`.
  - Inyección de `UsuarioRepository`, `PasswordEncoder` y `Clock`.
- [x] **Mapper (`com.huertos.mapper.UsuarioMapper.java`):**
  - Método `toUsuario(CrearUsuarioRequest request, String passwordHash)`.
  - Método `toUsuarioDetalleResponse(Usuario usuario)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Utiliza la tabla `usuarios` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con columnas tipo `VARCHAR` y `DATETIME` estándar):
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_completo` VARCHAR(120) NOT NULL
    - `identificacion` VARCHAR(13) NOT NULL UNIQUE
    - `fecha_nacimiento` DATE NOT NULL
    - `genero` VARCHAR(20) NOT NULL (mapeado en JPA como `EnumType.STRING` con valores: `'FEMENINO'`, `'MASCULINO'`, `'OTRO'`)
    - `correo` VARCHAR(180) NOT NULL UNIQUE
    - `telefono` VARCHAR(20) NULL
    - `password_hash` VARCHAR(255) NOT NULL
    - `rol` VARCHAR(30) NOT NULL (mapeado en JPA como `EnumType.STRING` con valores: `'TECNICO_SIS'`, `'TECNICO_MSP'`, `'RESPONSABLE_COMUNIDAD'`)
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
    - `requiere_cambio_clave` BOOLEAN NOT NULL DEFAULT TRUE
    - `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
  - *(NO crear nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Usuario.java`, `com.huertos.model.Rol.java`, `com.huertos.model.Genero.java`):**
  - Mapeo de la entidad `Usuario` con enums `Rol` y `Genero` anotados con `@Enumerated(EnumType.STRING)`.
- [x] **Repository (`com.huertos.repository.UsuarioRepository.java`):**
  - `Optional<Usuario> findByIdentificacion(String identificacion);`
  - `Optional<Usuario> findByCorreo(String correo);`
  - `boolean existsByIdentificacion(String identificacion);`
  - `boolean existsByCorreo(String correo);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/UsuarioServiceImplTest.java`):**
  - Registro exitoso: comprueba guardado de usuario, cifrado de contraseña con BCrypt, asignación de `activo = true` y `requiereCambioClave = true` con `Clock.fixed(...)`.
  - Validación de menor de edad: lanza `ReglaNegocioException` si la persona tiene menos de 18 años.
  - Validación de unicidad: lanza `ConflictoException` si la identificación o el correo ya están registrados.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/UsuarioControllerTest.java`):**
  - Con `@WebMvcTest(UsuarioController.class)` y `@WithMockUser(roles = "TECNICO_SIS")`:
    - `POST /api/usuarios` con datos válidos retorna HTTP `201 Created`.
    - Petición con datos incompletos o género inválido retorna HTTP `400 Bad Request`.
  - Petición con rol `TECNICO_MSP` o `RESPONSABLE_COMUNIDAD` retorna HTTP `403 Forbidden`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/usuarios/CrearUsuarioForm.test.tsx`):**
  - Renderizado responsivo de campos (1 columna en vista móvil), selectores de género (incluyendo `OTRO`) y rol, y presencia visual de `<KintiGuia/>`.
  - Validación en cliente bloqueando el envío si la cédula es inválida o el usuario es menor de 18 años.
  - Envío exitoso con MSW (`handlers.ts`), limpieza de campos y visualización de mensaje de éxito en `Notificacion`.
