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
2. El **Sistema** renderiza la vista de inicio de sesión diseñada bajo el paradigma **Mobile First para Progressive Web App (PWA)**, garantizando adaptabilidad fluida desde dispositivos móviles pequeños (320px) hasta pantallas de escritorio amplias mediante las variables de diseño de `frontend/src/index.css`:
   - **Estructura y Contenedor Responsivo:** La vista se monta dentro de un contenedor principal (`LoginPage.tsx`) que en dispositivos móviles utiliza `width: 100%` con padding lateral de `var(--espacio-4)`. En pantallas de tablet y escritorio (`@media (min-width: 640px)`), se centra vertical y horizontalmente limitando el ancho máximo a `var(--ancho-lectura)` (40rem) con márgenes automáticos (`margin: 0 auto; padding: var(--espacio-12) var(--espacio-4)`).
   - **Tarjeta de Autenticación:** Se presenta en una tarjeta con fondo `var(--color-superficie)`, borde de `var(--borde-ancho) solid var(--color-borde)`, radio de curvatura `var(--radio-lg)` y elevación suave `var(--sombra-md)`, ofreciendo aislamiento visual y contraste sobre el fondo general `var(--color-fondo)`.
   - **Cabecera y Acompañamiento Andino:** Integración superior responsiva con el componente `<KintiGuia variante="celebracion"/>`, optimizado para no saturar la pantalla móvil:
     - Avatar e ilustración escalables que reducen sus dimensiones en pantallas estrechas sin perder nitidez.
     - Saludo motivacional: *"¡Alli shamushka! Qué alegría verte de nuevo por nuestros huertos medicinales"*, tipografiado con `font-size: var(--texto-lg)` en móvil y `var(--texto-xl)` en pantallas mayores, color `var(--color-primario)` y peso `var(--peso-seminegrita)`.
   - **Formulario Responsivo:** Dispuesto en un flujo vertical limpio (`display: flex; flex-direction: column; gap: var(--espacio-4)`):
     - `Correo Electrónico` (`correo`: componente `CampoTexto`, tipo `email`, obligatorio). Control con altura mínima táctil accesible de 44px, tipografía `var(--texto-base)`, relleno `var(--espacio-2) var(--espacio-3)`, borde `var(--borde-ancho) solid var(--color-borde)` y radio `var(--radio-md)`. Al foco activa `border-color: var(--color-primario)` y outline accesible.
     - `Contraseña` (`password`: componente `CampoTexto`, tipo `password`, obligatorio). Cumple con las mismas proporciones táctiles y estilos del campo de correo.
     - Botón de envío **`"Iniciar Sesión"`** (componente `Boton`, variante `primary`): En resoluciones móviles se proyecta al ancho completo (`width: 100%`) con padding de `var(--espacio-3) var(--espacio-4)` para facilitar la pulsación con una sola mano, mientras que en pantallas de escritorio mantiene coherencia visual adaptada al formulario. Emplea fondo `var(--color-primario)`, texto `var(--color-texto-inverso)` y transición suave hacia `var(--color-primario-interaccion)`.
     - Área de alertas y feedback mediante el componente `Notificacion`, ubicado de forma flotante o en cabecera del formulario sin provocar saltos bruscos de maquetación (layout shift).
3. El **Actor** completa su correo, su contraseña y hace clic o toca en **`"Iniciar Sesión"`**.
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
  - `LoginForm.tsx`: Componente con enfoque Mobile First para PWA. Incluye `<KintiGuia variante="celebracion"/>` en la cabecera, dos componentes `CampoTexto` controlados para correo y contraseña con áreas táctiles adaptadas, un `Boton` de ancho completo en móvil (`width: 100%`) con estado `disabled` durante el envío, y feedback visual mediante `Notificacion` utilizando variables de `index.css`.
  - `authApi.ts`: Implementa `iniciarSesion(credenciales: LoginRequest): Promise<LoginResponse>` consumiendo `POST /api/publico/auth/login` mediante la instancia base `client` de `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `LoginPage.tsx`: Vista centrada responsiva (`max-width: var(--ancho-lectura)`) con maquetación limpia que envuelve a `LoginForm.tsx`.
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
  - Emplea la tabla `usuarios` del esquema MariaDB consolidada en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar):
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `nombre_completo` VARCHAR(120) NOT NULL
    - `identificacion` VARCHAR(13) NOT NULL UNIQUE
    - `fecha_nacimiento` DATE NOT NULL
    - `genero` VARCHAR(20) NOT NULL
    - `correo` VARCHAR(180) NOT NULL UNIQUE
    - `telefono` VARCHAR(20) NULL
    - `password_hash` VARCHAR(255) NOT NULL
    - `requiere_cambio_clave` BOOLEAN NOT NULL DEFAULT TRUE
    - `rol` VARCHAR(30) NOT NULL (mapeado en JPA como `EnumType.STRING` con valores: `'TECNICO_SIS'`, `'TECNICO_MSP'`, `'RESPONSABLE_COMUNIDAD'`)
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
    - `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
  - *(NO crear nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Usuario.java` y `com.huertos.model.Rol.java`):**
  - Mapeo de la clase `Usuario` con los atributos `requiereCambioClave`, `passwordHash` y el enum `Rol` (`@Enumerated(EnumType.STRING)`).
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
  - Con Vitest y React Testing Library, verificando el renderizado responsivo de los componentes `CampoTexto`, el botón a ancho completo y la presencia visual de `<KintiGuia/>`.
  - Simulación de respuesta exitosa con MSW (`handlers.ts`), asegurando la persistencia del token en `localStorage` y la redirección.
  - Simulación de error HTTP 401 mostrando el mensaje descriptivo en el componente `Notificacion`.
