---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU02: Cambiar contraseña"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU02`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Usuario Autenticado (Responsable de Comunidad, Técnico MSP, Técnico SIS)`
* **Descripción:** `Permite a un usuario autenticado actualizar su contraseña de acceso. Se aplica principalmente en el primer ingreso forzoso tras la aprobación y generación automática de credenciales por el sistema (solicitud aprobada por Técnico MSP), o de forma voluntaria en cualquier momento desde su configuración de cuenta. Incorpora un paso de confirmación explícito antes de procesar el cambio definitivo.`
* **Dependencias:** `Depende de #US-CU01`

### Precondiciones
1. `El usuario cuenta con una sesión iniciada y un token JWT válido almacenado en el cliente (Authorization: Bearer <token>).`
2. `El usuario existe y tiene activo = true en la tabla usuarios.`
3. `Si el usuario ingresa por primera vez con contraseña temporal, el payload de su token o su registro en base de datos presenta requiere_cambio_clave = true.`

### Postcondiciones
1. `Se actualiza el campo password_hash del usuario con el nuevo hash cifrado mediante BCrypt.`
2. `Se establece el campo requiere_cambio_clave = false en la tabla usuarios.`
3. `Se emite un nuevo token JWT actualizado reflejando requiereCambioClave = false, o se actualiza el estado de la sesión para permitir la navegación sin restricciones al menú principal del sistema.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Actor** accede a la vista `"/cambiar-clave"`, ya sea por redirección automática tras el login (`US-CU01` con `requiereCambioClave == true`) o mediante la opción de seguridad de su perfil en `Layout.tsx`.
2. El **Sistema** despliega una tarjeta de formulario estilizada y limpia con detalles andinos, sin presencia de la mascota Kinti, solicitando:
   - `Contraseña Actual` (`passwordActual`: componente `CampoTexto`, tipo `password`, obligatorio).
   - `Nueva Contraseña` (`nuevaPassword`: componente `CampoTexto`, tipo `password`, obligatorio, mínimo 8 caracteres).
   - `Confirmar Nueva Contraseña` (`confirmarPassword`: componente `CampoTexto`, tipo `password`, obligatorio).
   - Botón de acción **`"Actualizar Contraseña"`** (componente `Boton`, variante `primary`).
3. El **Actor** llena los campos y presiona **`"Actualizar Contraseña"`**.
4. El **Sistema** valida en el cliente que `nuevaPassword` cumpla con el tamaño requerido y coincida exactamente con `confirmarPassword`.
5. El **Sistema** abre el diálogo de confirmación mediante el componente `Modal`:
   - **Título:** *"Confirmar cambio de contraseña"*
   - **Mensaje:** *"¿Estás seguro de que deseas actualizar tu contraseña de acceso? A partir de tu próximo inicio de sesión deberás utilizar tu nueva clave."*
   - **Acciones:** Botón **`"Cancelar"`** (cierra el modal) y Botón **`"Sí, cambiar contraseña"`** (componente `Boton`).
6. El **Actor** pulsa **`"Sí, cambiar contraseña"`** dentro del `Modal`.
7. El **Sistema** cierra el modal, deshabilita los controles mostrando indicador de carga y emite una solicitud `PUT /api/usuarios/cambiar-clave` enviando el Bearer Token en la cabecera `Authorization` y el siguiente cuerpo JSON:
```json
{
  "passwordActual": "TemporalPass2026!",
  "nuevaPassword": "MiNuevaPasswordSegura2026*",
  "confirmarPassword": "MiNuevaPasswordSegura2026*"
}
```
8. El **Sistema** (backend) extrae el `id` o correo del usuario autenticado desde el `SecurityContextHolder`, verifica que `passwordActual` coincida con el hash registrado, valida que `nuevaPassword` sea distinta a la actual, genera el nuevo hash con `PasswordEncoder`, actualiza `requiere_cambio_clave = false` y guarda los cambios en la base de datos.
9. El **Sistema** retorna el código HTTP `200 OK` con el payload de confirmación:
```json
{
  "mensaje": "Contraseña actualizada exitosamente",
  "requiereCambioClave": false
}
```
10. El **Sistema** (frontend) despliega una notificación de éxito con el componente `Notificacion` y redirige al usuario a la pantalla de inicio o tablero principal (`/inicio`).

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Campos vacíos o contraseña débil (HTTP 400 Bad Request):**
  - **Condición:** Si alguno de los campos está en blanco o `nuevaPassword` tiene menos de 8 caracteres (`@NotBlank`, `@Size(min = 8)`).
  - **Comportamiento:** Bean Validation rechaza la petición. `GlobalExceptionHandler` captura el fallo y el frontend notifica los errores mediante el componente `Notificacion` sin borrar el formulario.
* **4b. Las contraseñas no coinciden (Error de Validación en Cliente/Servidor - HTTP 400 Bad Request):**
  - **Condición:** Si `nuevaPassword` es diferente de `confirmarPassword`.
  - **Comportamiento:** El backend lanza `ReglaNegocioException` con el mensaje: *"La nueva contraseña y su confirmación no coinciden"*. El frontend muestra el error en `Notificacion`.
* **8a. Contraseña actual incorrecta (HTTP 400 Bad Request):**
  - **Condición:** Si el valor de `passwordActual` no coincide con el `password_hash` del usuario en la base de datos.
  - **Comportamiento:** El backend lanza `ReglaNegocioException` con el mensaje: *"La contraseña actual ingresada es incorrecta"*. El frontend limpia los campos de clave y muestra el aviso de error con `Notificacion`.
* **8b. La nueva contraseña es igual a la actual (HTTP 409 Conflict):**
  - **Condición:** Si el usuario ingresa como nueva contraseña la misma que ya tenía configurada.
  - **Comportamiento:** El backend lanza `ConflictoException` con el mensaje: *"La nueva contraseña no puede ser idéntica a la contraseña actual"*. Se informa al usuario mediante `Notificacion`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/auth/` o `frontend/src/features/usuarios/`):**
  - `CambiarClaveForm.tsx`: Componente que contiene tres campos `CampoTexto` tipo `password`, botón de envío `Boton`, componente `Modal` para confirmar la acción antes del despacho HTTP, y alertas mediante `Notificacion`.
  - `usuariosApi.ts` (o función dentro de `authApi.ts`): Exporta `cambiarClave(datos: CambiarClaveRequest): Promise<CambioClaveResponse>` consumiendo `PUT /api/usuarios/cambiar-clave` a través de `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `CambiarClavePage.tsx`: Vista contenida en el layout del sistema, enfocada en la tarjeta del formulario.
  - Registrar la ruta `/cambiar-clave` en `App.tsx`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.UsuarioController.java`):**
  - `PUT /api/usuarios/cambiar-clave` -> Recibe `@Valid @RequestBody CambiarClaveRequest request` y el objeto `Authentication authentication` inyectado por Spring Security. Retorna HTTP 200 OK con `CambioClaveResponse`.
  - **Acceso:** Privado (requiere cabecera `Authorization: Bearer <token>`).
  - **Cuerpo de Solicitud (`CambiarClaveRequest`):**
    - `passwordActual`: `String`, obligatorio (`@NotBlank`).
    - `nuevaPassword`: `String`, obligatorio, mínimo 8 caracteres (`@NotBlank`, `@Size(min = 8)`).
    - `confirmarPassword`: `String`, obligatorio (`@NotBlank`).
  - **Respuesta Exitosa (HTTP 200 OK - `CambioClaveResponse`):**
    - `mensaje`: `String` (valor exacto: `"Contraseña actualizada exitosamente"`).
    - `requiereCambioClave`: `boolean` (valor `false`).
  - **Respuestas de Error:**
    - `400 Bad Request`: Formato inválido, longitud menor a 8 caracteres, o claves no coincidentes.
    - `401 Unauthorized`: Token JWT ausente, inválido o expirado.
    - `409 Conflict`: La nueva contraseña es idéntica a la anterior.
- [x] **DTOs (`com.huertos.dto`):**
  - `CambiarClaveRequest.java`: `public record CambiarClaveRequest(@NotBlank(message = "La contraseña actual es obligatoria") String passwordActual, @NotBlank(message = "La nueva contraseña es obligatoria") @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String nuevaPassword, @NotBlank(message = "La confirmación es obligatoria") String confirmarPassword) {}`.
  - `CambioClaveResponse.java`: `public record CambioClaveResponse(String mensaje, boolean requiereCambioClave) {}`.
- [x] **Service (`com.huertos.service.UsuarioService.java` y `com.huertos.service.impl.UsuarioServiceImpl.java`):**
  - Método `CambioClaveResponse cambiarClave(Long usuarioId, CambiarClaveRequest request)`.
  - Valida equivalencia de `nuevaPassword` y `confirmarPassword`.
  - Comprueba que `passwordActual` coincida con el hash mediante `PasswordEncoder.matches(...)`.
  - Comprueba que la nueva contraseña no sea idéntica a la anterior.
  - Asigna `usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()))` y `usuario.setRequiereCambioClave(false)`.
- [x] **Mapper:** No requiere conversión compleja de entidad; retorna directamente el record `CambioClaveResponse`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Utiliza la tabla `usuarios` configurada en `V1__init_schema.sql` y actualizada en `V2__esquema_completo_v1.sql`:
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `password_hash` VARCHAR(255) NOT NULL
    - `requiere_cambio_clave` BOOLEAN NOT NULL DEFAULT TRUE
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
  - *(NO generar nuevo archivo SQL; la estructura requerida ya existe en la migración V2)*.
- [x] **Entidad JPA (`com.huertos.model.Usuario.java`):**
  - Atributos `passwordHash` y `requiereCambioClave` debidamente mapeados con sus getters y setters.
- [x] **Repository (`com.huertos.repository.UsuarioRepository.java`):**
  - Métodos `Optional<Usuario> findById(Long id);` y `Optional<Usuario> findByCorreo(String correo);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/UsuarioServiceImplTest.java`):**
  - Flujo exitoso: actualiza `password_hash`, coloca `requiere_cambio_clave = false` y retorna mensaje de éxito.
  - Flujo alternativo 8a: lanza `ReglaNegocioException` si la contraseña actual no concuerda.
  - Flujo alternativo 8b: lanza `ConflictoException` si la nueva contraseña es igual a la actual.
  - Validación de coincidencia: lanza `ReglaNegocioException` si `nuevaPassword` y `confirmarPassword` difieren.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/UsuarioControllerTest.java`):**
  - Verificación con `@WebMvcTest(UsuarioController.class)` simulando usuario autenticado (`@WithMockUser`).
  - Petición con cuerpo válido retorna HTTP `200 OK`.
  - Petición sin token JWT retorna HTTP `401 Unauthorized`.
  - Petición con contraseña menor a 8 caracteres retorna HTTP `400 Bad Request`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/auth/CambiarClaveForm.test.tsx`):**
  - Renderizado de campos `passwordActual`, `nuevaPassword`, `confirmarPassword` y botón principal.
  - Verificación de apertura del `Modal` de confirmación al hacer clic en `"Actualizar Contraseña"`.
  - Verificación de que al confirmar en el modal se envíe la solicitud con MSW (`handlers.ts`), se visualice `Notificacion` de éxito y se proceda a la redirección.
  - Comprobación de mensaje de error si las contraseñas no coinciden en pantalla.
