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
2. `La base de datos cuenta con la tabla usuarios consolidada según V1__init_schema.sql y adaptada en V2__esquema_completo_v1.sql.`
3. `El endpoint POST /api/usuarios se encuentra protegido en SecurityConfig.java exigiendo rol TECNICO_SIS.`

### Postcondiciones
1. `Se inserta un nuevo registro en la tabla usuarios con activo = true, requiere_cambio_clave = true y la contraseña cifrada con BCrypt en password_hash.`
2. `El sistema retorna código HTTP 201 Created con el DTO UsuarioDetalleResponse sin exponer el hash de contraseña.`
3. `El formulario en el frontend se limpia y muestra un mensaje de éxito mediante el componente Notificacion.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Técnico SIS** navega al módulo de usuarios accediendo a la ruta `"/usuarios/nuevo"` desde el menú lateral en `Layout.tsx`.
2. El **Sistema** renderiza la vista de creación de usuario acompañada por el componente `<KintiGuia variante="dia"/>` con el mensaje:
   *"El trabajo en comunidad florece cuando sumamos voluntades con confianza y compañerismo. Registra a los guardianes de nuestros saberes"*.
3. El **Sistema** presenta el formulario estructurado con los componentes reutilizables solicitando:
   - `Nombre Completo` (`nombreCompleto`: `CampoTexto`, texto obligatorio, solo letras y espacios, máx. 120 caracteres).
   - `Cédula de Identidad` (`identificacion`: `CampoTexto`, numérico de 10 dígitos obligatorio, validado con algoritmo de cédula ecuatoriana).
   - `Fecha de Nacimiento` (`fechaNacimiento`: `CampoTexto`, tipo `date`, obligatorio, edad mínima requerida: 18 años).
   - `Género` (`genero`: `Selector`, opciones: `MASCULINO`, `FEMENINO`, obligatorio).
   - `Correo Electrónico` (`correo`: `CampoTexto`, tipo `email`, obligatorio, validación de formato RFC).
   - `Teléfono Celular` (`telefono`: `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos iniciando en `09`).
   - `Rol del Sistema` (`rol`: `Selector`, opciones: `TECNICO_SIS`, `TECNICO_MSP`, `RESPONSABLE_COMUNIDAD`, obligatorio).
   - `Contraseña Temporal` (`password`: `CampoTexto`, tipo `password`, obligatorio, mínimo 12 caracteres, al menos 1 mayúscula, 1 minúscula y 1 número).
   - Botón de envío **`"Crear Usuario"`** (`Boton`, variante `primary`).
4. El **Técnico SIS** diligencia los campos y pulsa **`"Crear Usuario"`**.
5. El **Sistema** (frontend) valida en tiempo real que la contraseña cumpla los criterios de seguridad, que el solicitante tenga al menos 18 años calculados respecto a la fecha actual y que la cédula sea válida.
6. El **Sistema** deshabilita el botón, muestra indicador de carga y envía la petición `POST /api/usuarios` con el payload:
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
7. El **Sistema** (backend) valida la autorización JWT (`TECNICO_SIS`), verifica unicidad de `identificacion` y `correo`, valida con `Clock` del sistema que la edad sea `>= 18 años`, cifra la contraseña con `PasswordEncoder.encode(...)`, asigna `activo = true`, `requiereCambioClave = true` y almacena el registro en la tabla `usuarios`.
8. El **Sistema** retorna el código HTTP `201 Created` con el DTO `UsuarioDetalleResponse`:
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
9. El **Sistema** limpia el formulario, muestra una alerta de confirmación con el componente `Notificacion` y actualiza la lista de usuarios.

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
  - `CrearUsuarioForm.tsx`: Componente con formulario reactivo, validaciones en línea para cédula ecuatoriana, requisitos de contraseña dinámica, selector de género y rol, presencia visual de `<KintiGuia variante="dia"/>` y notificaciones de éxito/error.
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
    - `genero`: `Genero`, obligatorio (`@NotNull`, valores: `MASCULINO`, `FEMENINO`).
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
  - Utiliza la tabla `usuarios` definida en `V1__init_schema.sql` y ampliada en `V2__esquema_completo_v1.sql`:
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_completo` VARCHAR(120) NOT NULL
    - `identificacion` VARCHAR(13) NOT NULL UNIQUE
    - `fecha_nacimiento` DATE NOT NULL
    - `genero` VARCHAR(20) NOT NULL
    - `correo` VARCHAR(180) NOT NULL UNIQUE
    - `telefono` VARCHAR(20) NULL
    - `password_hash` VARCHAR(255) NOT NULL
    - `rol` ENUM('TECNICO_SIS', 'TECNICO_MSP', 'RESPONSABLE_COMUNIDAD') NOT NULL
    - `requiere_cambio_clave` BOOLEAN NOT NULL DEFAULT TRUE
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
    - `creado_en` DATETIME(6) NOT NULL
  - *(NO crear nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Usuario.java`, `com.huertos.model.Rol.java`, `com.huertos.model.Genero.java`):**
  - Mapeo de la entidad `Usuario` con anotaciones `@Entity`, `@Table(name = "usuarios")`, y enums asociados.
- [x] **Repository (`com.huertos.repository.UsuarioRepository.java`):**
  - Métodos `boolean existsByIdentificacion(String identificacion);`
  - Métodos `boolean existsByCorreo(String correo);`
  - Métodos `Optional<Usuario> findByIdentificacion(String identificacion);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/UsuarioServiceImplTest.java`):**
  - Creación exitosa: verifica persistencia con BCrypt, `requiereCambioClave = true`, `activo = true` y validación de mayoría de edad con `Clock.fixed(...)`.
  - Excepción por cédula duplicada: lanza `ConflictoException`.
  - Excepción por correo duplicado: lanza `ConflictoException`.
  - Excepción por menor de 18 años: lanza `ReglaNegocioException`.
  - Excepción por contraseña insegura: lanza `ReglaNegocioException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/UsuarioControllerTest.java`):**
  - Con `@WebMvcTest(UsuarioController.class)` y `@WithMockUser(roles = "TECNICO_SIS")`:
    - Respuesta HTTP `201 Created` ante payload válido.
    - Respuesta HTTP `400 Bad Request` cuando faltan campos o el formato de correo/teléfono es inválido.
    - Respuesta HTTP `409 Conflict` ante duplicidad de datos únicos.
  - Con `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")` o sin autenticación: retorna HTTP `403 Forbidden` / `401 Unauthorized`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/usuarios/CrearUsuarioForm.test.tsx`):**
  - Renderizado de todos los controles de entrada y visualización de `<KintiGuia/>`.
  - Validación dinámica en cliente de contraseña débil y bloqueo del botón de envío.
  - Simulación de respuesta exitosa con MSW (`handlers.ts`), verificación del llamado a la API, notificación de éxito y limpieza de campos.
