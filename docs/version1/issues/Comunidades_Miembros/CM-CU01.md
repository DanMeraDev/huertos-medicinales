---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU01: Actualizar información de comunidad y contacto"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad autenticado consultar la ficha de su propia comunidad y actualizar exclusivamente los datos editables permitidos por el modelo de negocio: el nombre de la comunidad y los canales oficiales de contacto (teléfono celular y correo electrónico). La comunidad se recupera automáticamente a partir de la identidad del usuario logueado en la sesión, garantizando que cada responsable gestione de forma estricta y única la comunidad que lidera.`
* **Dependencias:** `Depende de #US-CU01 (inicio de sesión previo con rol RESPONSABLE_COMUNIDAD) y de #US-CU06 (existencia de la comunidad registrada)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y posee un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario cuenta con una comunidad asignada y activa en la tabla comunidades vinculada mediante responsable_id.`
3. `Los endpoints /api/comunidades/mi-comunidad están configurados con seguridad en SecurityConfig.java exigiendo rol RESPONSABLE_COMUNIDAD.`

### Postcondiciones
1. `Se actualizan los campos nombre, telefono_contacto y correo_contacto en el registro correspondiente de la tabla comunidades.`
2. `El sistema retorna el código HTTP 200 OK con el DTO ComunidadDetalleResponse reflejando los valores modificados.`
3. `El frontend despliega una notificación de confirmación con el componente Notificacion y mantiene los datos sincronizados en pantalla.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** accede a la sección `"Mi Comunidad"` desde el menú principal de navegación en `Layout.tsx`.
2. El **Sistema** emite la petición `GET /api/comunidades/mi-comunidad` enviando el token JWT.
3. El **Sistema** (backend) extrae el `id` del usuario autenticado desde el `SecurityContextHolder`, localiza la comunidad asociada mediante `responsable_id` y responde con código HTTP `200 OK` y el DTO `ComunidadDetalleResponse`:
```json
{
  "id": 3,
  "nombre": "Huerto Comunitario Allpa Viva",
  "tipoOrganizacion": "BARRIO",
  "provincia": "Pichincha",
  "parroquia": "Calderon",
  "direccion": "Calle de las Rosas y Pasaje Los Pinos, lote 14",
  "telefonoContacto": "0991234567",
  "correoContacto": "comunidad.allpa@huertos.gob.ec",
  "responsableId": 8,
  "nombreResponsable": "Segundo Tránsito Amaguaña",
  "puntosComunidad": 150,
  "activa": true,
  "creadoEn": "2026-11-10T11:00:00"
}
```
4. El **Sistema** renderiza una interfaz sobria, profesional y vívida (con colores tierra y maíz de identidad andina, sin presencia del personaje Kinti), organizada en:
   - **Datos Informativos (Solo lectura):** Tipo de organización, provincia, parroquia, dirección geográfica, nombre del responsable asignado y puntos acumulados de la comunidad.
   - **Campos Editables:**
     - `Nombre de la Comunidad` (`nombre`: componente `CampoTexto`, obligatorio, solo letras y espacios, máx. 150 caracteres).
     - `Teléfono Celular de Contacto` (`telefonoContacto`: componente `CampoTexto`, tipo `tel`, obligatorio, exactamente 10 dígitos iniciando en `09`).
     - `Correo Electrónico de Contacto` (`correoContacto`: componente `CampoTexto`, tipo `email`, obligatorio, máx. 180 caracteres).
   - Botón de acción **`"Guardar Cambios"`** (componente `Boton`, variante `primary`).
5. El **Responsable de Comunidad** modifica los campos deseados (ej. actualiza el teléfono y el correo) y pulsa **`"Guardar Cambios"`**.
6. El **Sistema** valida en el cliente que el nombre no contenga caracteres numéricos ni símbolos, que el teléfono cumpla con el formato `09XXXXXXXX` y que el correo tenga estructura válida.
7. El **Sistema** deshabilita el botón mostrando estado de carga y emite la solicitud `PUT /api/comunidades/mi-comunidad` con el payload:
```json
{
  "nombre": "Huerto Comunitario Allpa Sumak",
  "telefonoContacto": "0998765432",
  "correoContacto": "contacto.allpa@huertos.gob.ec"
}
```
8. El **Sistema** (backend) extrae el `usuarioId` de la sesión, obtiene la comunidad asociada, valida que el nuevo nombre no esté registrado por otra comunidad distinta (`id != comunidad.getId()`), actualiza los campos permitidos y persiste los cambios.
9. El **Sistema** retorna el código HTTP `200 OK` con el DTO `ComunidadDetalleResponse` actualizado.
10. El **Sistema** (frontend) muestra una notificación visual de éxito mediante el componente `Notificacion`: *"La información de tu comunidad ha sido actualizada correctamente"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **6a. Campos obligatorios vacíos o formatos inválidos (HTTP 400 Bad Request):**
  - **Condición:** Si `nombre`, `telefonoContacto` o `correoContacto` están vacíos, si el nombre contiene números/símbolos, si el teléfono no tiene 10 dígitos comenzando con `09`, o el correo no tiene formato válido.
  - **Comportamiento:** Bean Validation rechaza la petición. `GlobalExceptionHandler` captura los fallos y el frontend despliega el detalle de errores con `Notificacion` sin limpiar el formulario.
* **8a. Nombre de comunidad duplicado (HTTP 409 Conflict):**
  - **Condición:** Si el responsable ingresa un nombre que ya pertenece a otra comunidad registrada en la base de datos.
  - **Comportamiento:** El backend lanza `ConflictoException`: *"Ya existe otra comunidad registrada con el nombre ingresado"*. El frontend informa el conflicto mediante `Notificacion` preservando los cambios editados.
* **8b. Responsable sin comunidad asignada (HTTP 404 Not Found):**
  - **Condición:** Si el usuario autenticado tiene el rol `RESPONSABLE_COMUNIDAD` pero no existe ningún registro en `comunidades` donde `responsable_id == usuarioId`.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"No se encontró ninguna comunidad asociada al responsable autenticado"*. El frontend muestra una pantalla de aviso indicando que contacte al soporte del MSP.
* **8c. Acceso denegado o sesión expirada (HTTP 401 Unauthorized / HTTP 403 Forbidden):**
  - **Condición:** Si el token JWT está ausente, vencido o el usuario logueado no posee el rol `RESPONSABLE_COMUNIDAD`.
  - **Comportamiento:** Spring Security retorna HTTP 401 Unauthorized o HTTP 403 Forbidden impidiendo cualquier visualización o modificación.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/comunidades/`):**
  - `MiComunidadForm.tsx`: Componente con diseño limpio y vívido en tarjeta estilizada con bordes andinos (sin la mascota Kinti). Presenta bloques informativos no editables y campos controlados con `CampoTexto` para nombre, teléfono y correo, botón `Boton` con estado `disabled` en envío, y retroalimentación mediante `Notificacion`.
  - `comunidadesApi.ts`: Exporta las funciones:
    - `obtenerMiComunidad(): Promise<ComunidadDetalleResponse>` consumiendo `GET /api/comunidades/mi-comunidad`.
    - `actualizarMiComunidad(datos: ActualizarComunidadRequest): Promise<ComunidadDetalleResponse>` consumiendo `PUT /api/comunidades/mi-comunidad`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `MiComunidadPage.tsx`: Vista protegida en `App.tsx` bajo la ruta `/mi-comunidad`, accesible únicamente para usuarios con rol `RESPONSABLE_COMUNIDAD`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadController.java`):**
  - `GET /api/comunidades/mi-comunidad` -> Obtiene la comunidad del usuario autenticado vía `Authentication`, retorna HTTP `200 OK` con `ComunidadDetalleResponse`.
  - `PUT /api/comunidades/mi-comunidad` -> Recibe `@Valid @RequestBody ActualizarComunidadRequest request` y `Authentication authentication`, retorna HTTP `200 OK` con `ComunidadDetalleResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Cuerpo de Solicitud (`ActualizarComunidadRequest`):**
    - `nombre`: `String`, obligatorio, solo letras y espacios (`@NotBlank`, `@Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")`, `@Size(max = 150)`).
    - `telefonoContacto`: `String`, obligatorio, formato celular ecuatoriano (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$")`).
    - `correoContacto`: `String`, obligatorio, formato email (`@NotBlank`, `@Email`, `@Size(max = 180)`).
  - **Respuesta Exitosa (HTTP 200 OK - `ComunidadDetalleResponse`):**
    - `id`: `Long`.
    - `nombre`: `String`.
    - `tipoOrganizacion`: `TipoOrganizacion`.
    - `provincia`: `String`.
    - `parroquia`: `String`.
    - `direccion`: `String`.
    - `telefonoContacto`: `String`.
    - `correoContacto`: `String`.
    - `responsableId`: `Long`.
    - `nombreResponsable`: `String`.
    - `puntosComunidad`: `Integer`.
    - `activa`: `boolean`.
    - `creadoEn`: `LocalDateTime`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Datos incompletos o formatos inválidos.
    - `401 Unauthorized`: Token no provisto o expirado.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: No existe comunidad asociada al usuario logueado.
    - `409 Conflict`: Nombre de comunidad ya registrado por otra entidad.
- [x] **DTOs (`com.huertos.dto`):**
  - `ActualizarComunidadRequest.java`: `public record ActualizarComunidadRequest(@NotBlank(message = "El nombre es obligatorio") @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios") @Size(max = 150) String nombre, @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe tener 10 dígitos y empezar con 09") String telefonoContacto, @NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") @Size(max = 180) String correoContacto) {}`.
  - `ComunidadDetalleResponse.java`: `record` completo con todos los datos informativos y de contacto.
- [x] **Service (`com.huertos.service.ComunidadService.java` y `com.huertos.service.impl.ComunidadServiceImpl.java`):**
  - Método `ComunidadDetalleResponse obtenerPorResponsableId(Long responsableId)`.
  - Método `@Transactional ComunidadDetalleResponse actualizarInformacionContacto(Long responsableId, ActualizarComunidadRequest request)`.
  - Validación de unicidad de nombre excluyendo el propio ID: `comunidadRepository.existsByNombreAndIdNot(request.nombre(), comunidad.getId())`.
- [x] **Mapper (`com.huertos.mapper.ComunidadMapper.java`):**
  - Método `toDetalleResponse(Comunidad comunidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `comunidades` definida en `V2__esquema_completo_v1.sql`:
    - Columnas actualizadas: `nombre`, `telefono_contacto`, `correo_contacto`.
    - Columna relacional: `responsable_id` (FK hacia `usuarios(id)`).
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Comunidad.java`):**
  - Mapeo de la entidad `Comunidad` con campos `nombre`, `telefonoContacto`, `correoContacto`, y relación `@OneToOne` o `@ManyToOne` con `Usuario` (`responsable`).
- [x] **Repository (`com.huertos.repository.ComunidadRepository.java`):**
  - `Optional<Comunidad> findByResponsableId(Long responsableId);`
  - `boolean existsByNombreAndIdNot(String nombre, Long id);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ComunidadServiceImplTest.java`):**
  - Consulta exitosa: retorna el DTO correcto para el responsable logueado.
  - Actualización exitosa: actualiza únicamente nombre, teléfono y correo en base de datos.
  - Conflicto por nombre duplicado: lanza `ConflictoException` si otra comunidad ya tiene el mismo nombre.
  - Responsable sin comunidad: lanza `RecursoNoEncontradoException` si el ID del responsable no tiene comunidad asociada.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `GET /api/comunidades/mi-comunidad` retorna HTTP `200 OK`.
    - `PUT /api/comunidades/mi-comunidad` con payload válido retorna HTTP `200 OK`.
    - `PUT /api/comunidades/mi-comunidad` con teléfono o correo inválido retorna HTTP `400 Bad Request`.
  - Con rol distinto a `RESPONSABLE_COMUNIDAD` (ej. `TECNICO_MSP` o anónimo): retorna HTTP `403 Forbidden` o `401 Unauthorized`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/comunidades/MiComunidadForm.test.tsx`):**
  - Carga y renderizado inicial de los datos de la comunidad en modo lectura y campos editables.
  - Validación de que los campos no editables (provincia, tipo de organización, puntos) estén protegidos contra modificación.
  - Envío del formulario con MSW (`handlers.ts`), visualización de la alerta de éxito en `Notificacion` y comprobación de ausencia del componente Kinti.
