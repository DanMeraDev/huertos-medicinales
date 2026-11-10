---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU01: Iniciar sesión"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad, Técnico MSP, Técnico SIS`
* **Descripción:** `Permite a los usuarios registrados con credenciales activas identificarse en la plataforma mediante correo electrónico y contraseña. El sistema valida su estado, genera un token JWT para la sesión y verifica si requiere cambio obligatorio de credenciales en su primer acceso.`
* **Dependencias:** `Ninguna (caso de uso de entrada principal a los módulos privados).`

### Precondiciones
1. `El usuario está registrado en la tabla usuarios con activo = true.`
2. `La contraseña está cifrada con BCrypt en la columna password_hash.`
3. `El endpoint de autenticación /api/publico/auth/login está habilitado como ruta pública (permitAll()) en SecurityConfig.java.`

### Postcondiciones
1. `El Sistema genera un token JWT firmado mediante JwtService con vigencia de 24 horas y los claims: id, sub (correo), rol y requiereCambioClave.`
2. `El cliente frontend almacena el token en localStorage y configura el encabezado Authorization: Bearer <token> para las peticiones subsecuentes en frontend/src/api/client.ts.`
3. `Si el campo requiere_cambio_clave = true, el sistema redirige forzosamente a la pantalla de cambio de clave (/cambiar-clave, caso US-CU02); si es false, redirige al espacio de trabajo asignado según su rol.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Actor** ingresa a la ruta pública `"/login"` desde el encabezado o menú de navegación en `Layout.tsx`.
2. El **Sistema** renderiza la vista de inicio de sesión con identidad andina y diseño minimalista:
   - Acompañamiento visual superior con `<KintiGuia variante="celebracion"/>` mostrando el saludo: *"¡Alli shamushka! Qué alegría verte de nuevo por nuestros huertos medicinales"*.
   - Formulario de autenticación estructurado con los componentes reutilizables:
     - `Correo Electrónico` (`correo`: componente `CampoTexto`, tipo `email`, obligatorio).
     - `Contraseña` (`password`: componente `CampoTexto`, tipo `password`, obligatorio).
     - Botón de envío **`"Iniciar Sesión"`** (componente `Boton`, variante `primary`).
3. El **Actor** completa su correo, su contraseña y hace clic en **`"Iniciar Sesión"`**.
4. El **Sistema** deshabilita el botón mostrando estado de carga y emite la petición `POST /api/publico/auth/login` con el payload:
```json
{
  "correo": "responsable@huertos.gob.ec",
  "password": "Password123!"
}
```
5. El **Sistema** verifica que los campos obligatorios estén presentes, localiza al usuario por `correo`, valida que `activo = true` y comprueba la contraseña mediante `PasswordEncoder.matches(...)`.
6. El **Sistema** genera el token JWT y responde con código HTTP `200 OK` y el DTO `LoginResponse`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipoToken": "Bearer",
  "id": 1,
  "correo": "responsable@huertos.gob.ec",
  "nombreCompleto": "Taita Inti Morales",
  "rol": "RESPONSABLE_COMUNIDAD",
  "requiereCambioClave": false
}
```
7. El **Sistema** almacena las credenciales en `localStorage`, muestra un mensaje de bienvenida mediante el componente `Notificacion` y evalúa la redirección:
   - Si `requiereCambioClave == true`, redirige a la vista `"/cambiar-clave"`.
   - Si `requiereCambioClave == false`, redirige al panel correspondiente al rol.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Datos incompletos o formato inválido (HTTP 400 Bad Request):**
  - **Condición:** Si `correo` está vacío o no cumple con el formato de correo electrónico (`@NotBlank`, `@Email`), o si `password` está en blanco (`@NotBlank`).
  - **Comportamiento:** Bean Validation rechaza la solicitud en el DTO `LoginRequest`. `GlobalExceptionHandler` captura el fallo y el frontend muestra el error en el componente `Notificacion` conservando el texto ingresado en el campo de correo.
* **5a. Credenciales incorrectas o cuenta inexistente (HTTP 401 Unauthorized):**
  - **Condición:** Si el correo no existe en la base de datos o la contraseña no coincide con el registro en `password_hash`.
  - **Comportamiento:** El backend emite HTTP 401 ProblemDetail con el mensaje: *"Correo o contraseña incorrectos"*. El frontend vacía el campo `password` y muestra el aviso de error con `Notificacion`.
* **5b. Cuenta desactivada en el sistema (HTTP 403 Forbidden):**
  - **Condición:** Si el usuario existe pero tiene el campo `activo = false`.
  - **Comportamiento:** El backend lanza `ReglaNegocioException` (mapeada a HTTP 403 por `GlobalExceptionHandler`) con el mensaje: *"Esta cuenta se encuentra inactiva. Comuníquese con la administración del sistema"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/auth/`):**
  - `LoginForm.tsx`: Componente que incluye `<KintiGuia variante="celebracion"/>` en la cabecera, dos componentes `CampoTexto` controlados para correo y contraseña, un `Boton` con estado `disabled` durante el envío, y feedback visual mediante `Notificacion`.
  - `authApi.ts`: Implementa `iniciarSesion(credenciales: LoginRequest): Promise<LoginResponse>` consumiendo `POST /api/publico/auth/login` mediante la instancia base `client` de `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `LoginPage.tsx`: Vista centrada con maquetación limpia que envuelve a `LoginForm.tsx`.
  - Registro de la ruta pública `/login` en `App.tsx` accesible sin token previo.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.AuthController.java`):**
  - `POST /api/publico/auth/login` -> Recibe `@Valid @RequestBody LoginRequest request`, retorna HTTP `200 OK` con `LoginResponse`.
  - **Acceso:** Público (sin autenticación requerida en `SecurityConfig.java`, asegurar que `/api/publico/**` se mantenga con `.permitAll()`).
  - **Cuerpo de Solicitud (`LoginRequest`):**
    - `correo`: `String`, obligatorio, formato válido de correo (`@NotBlank`, `@Email`).
    - `password`: `String`, obligatorio (`@NotBlank`).
  - **Respuesta Exitosa (HTTP 200 OK - `LoginResponse`):**
    - `token`: `String` (token JWT generado).
    - `tipoToken`: `String` (valor constante `"Bearer"`).
    - `id`: `Long` (identificador único del usuario).
    - `correo`: `String` (correo del usuario autenticado).
    - `nombreCompleto`: `String` (nombre y apellido registrados).
    - `rol`: `Rol` (`TECNICO_SIS`, `TECNICO_MSP`, `RESPONSABLE_COMUNIDAD`).
    - `requiereCambioClave`: `boolean` (indica si debe actualizar su clave inicial).
  - **Respuestas de Error:**
    - `400 Bad Request`: Formato de correo incorrecto o valores en blanco.
    - `401 Unauthorized`: Contraseña errónea o correo no registrado.
    - `403 Forbidden`: Registro con `activo = false`.
- [x] **DTOs (`com.huertos.dto`):**
  - `LoginRequest.java`: `public record LoginRequest(@NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") String correo, @NotBlank(message = "La contraseña es obligatoria") String password) {}`.
  - `LoginResponse.java`: `public record LoginResponse(String token, String tipoToken, Long id, String correo, String nombreCompleto, Rol rol, boolean requiereCambioClave) {}`.
- [x] **Service (`com.huertos.service.AuthService.java` y `com.huertos.service.impl.AuthServiceImpl.java`):**
  - Método `LoginResponse iniciarSesion(LoginRequest request)`.
  - Valida coincidencia con `PasswordEncoder`, confirma estado activo, genera los claims del token con `JwtService` y emplea `Clock` inyectado (`ClockConfig.java`).
- [x] **Mapper (`com.huertos.mapper.UsuarioMapper.java`):**
  - Método `toLoginResponse(Usuario usuario, String token)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `usuarios` consolidada en `V1__init_schema.sql` y ampliada en `V2__esquema_completo_v1.sql`:
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_completo` VARCHAR(120) NOT NULL
    - `correo` VARCHAR(180) NOT NULL UNIQUE
    - `telefono` VARCHAR(20) NULL
    - `password_hash` VARCHAR(255) NOT NULL
    - `requiere_cambio_clave` BOOLEAN NOT NULL DEFAULT TRUE
    - `rol` ENUM('TECNICO_SIS', 'TECNICO_MSP', 'RESPONSABLE_COMUNIDAD') NOT NULL
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
    - `creado_en` DATETIME(6) NOT NULL
  - *(NO crear nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Usuario.java` y `com.huertos.model.Rol.java`):**
  - Mapeo de la clase `Usuario` con los atributos `requiereCambioClave`, `passwordHash` y el enum `Rol`.
- [x] **Repository (`com.huertos.repository.UsuarioRepository.java`):**
  - Método de consulta `Optional<Usuario> findByCorreo(String correo);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/AuthServiceImplTest.java`):**
  - Verificación de emisión correcta de `LoginResponse` y JWT ante credenciales válidas usando `Clock.fixed(...)`.
  - Comprobación de excepción HTTP 401 si la contraseña o el correo son erróneos.
  - Comprobación de `ReglaNegocioException` (HTTP 403) cuando el usuario tiene `activo = false`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/AuthControllerTest.java`):**
  - Con `@WebMvcTest(AuthController.class)`, validando respuesta HTTP `200 OK` en petición pública sin token previo.
  - Validación de respuesta HTTP `400 Bad Request` ante campos vacíos o estructura de correo inválida.
- [x] **Prueba de Componente Frontend (`frontend/src/features/auth/LoginForm.test.tsx`):**
  - Con Vitest y React Testing Library, verificando el renderizado de los componentes `CampoTexto`, el botón y la presencia visual de `<KintiGuia/>`.
  - Simulación de respuesta exitosa con MSW (`handlers.ts`), asegurando la persistencia del token en `localStorage` y la redirección.
  - Simulación de error HTTP 401 mostrando el mensaje descriptivo en el componente `Notificacion`.
