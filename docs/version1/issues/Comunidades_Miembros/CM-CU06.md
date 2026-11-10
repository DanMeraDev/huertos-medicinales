---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU06: Actualizar datos personales de miembro"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU06`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad modificar exclusivamente los datos de contacto y caracterización permitidos de un miembro voluntario perteneciente a su comunidad: género (Femenino, Masculino u Otro) y número de teléfono celular. Para salvaguardar la integridad de la identidad y del sistema de gamificación, datos sensibles como cédula, nombres, apellidos, fecha de nacimiento y nivel no son editables desde este flujo. Las modificaciones aplican las mismas reglas de validación y formatos exigidos durante el registro inicial.`
* **Dependencias:** `Depende de #US-CU01 (sesión de Responsable de Comunidad), #US-CU06 (comunidad activa) y #CM-CU04 (miembro registrado)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y cuenta con un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario lidera una comunidad activa en la tabla comunidades vinculada a su responsable_id.`
3. `El miembro voluntario pertenece a la comunidad del responsable a través de un registro activo en comunidad_miembros.`
4. `El endpoint PUT /api/comunidades/mi-comunidad/miembros/{miembroId} se encuentra protegido con rol RESPONSABLE_COMUNIDAD en SecurityConfig.java.`

### Postcondiciones
1. `Se actualizan los campos genero y telefono en el registro del voluntario en la tabla miembros.`
2. `El sistema responde con código HTTP 200 OK y el DTO MiembroDetalleResponse reflejando los valores modificados.`
3. `La tabla de miembros en el frontend se actualiza de manera reactiva y despliega una notificación de confirmación con el componente Notificacion.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** visualiza el listado de voluntarios de su colectivo en la vista `"/comunidad/miembros"`.
2. En la fila del voluntario que desea actualizar, el **Responsable de Comunidad** pulsa el botón **`"Editar"`** (componente `Boton`, variante `secondary`, con altura táctil accesible de 44px).
3. El **Sistema** abre el diálogo de edición rápida mediante el componente `Modal`, diseñado bajo el enfoque **Mobile First para Progressive Web App (PWA)** y adaptado a las variables de diseño de `frontend/src/index.css`:
   - **Comportamiento Móvil:** En pantallas móviles, el modal se presenta con `width: calc(100% - var(--espacio-4))`, `max-height: 90vh`, fondo `var(--color-superficie)`, bordes redondeados `var(--radio-lg)`, padding `var(--espacio-4)` y scroll vertical interno suave (`overflow-y: auto`). En pantallas de escritorio (`@media (min-width: 640px)`) escala a `max-width: var(--ancho-lectura)` con padding `var(--espacio-6)`.
   - **Campos en Solo Lectura (Datos Institucionales Protegidos):**
     - Enmarcados en un bloque superior con fondo `var(--color-superficie-alt)`, radio `var(--radio-md)`, padding `var(--espacio-3)` y tipografía `var(--texto-sm)`: Cédula de identidad, Nombres y apellidos completos, Fecha de nacimiento y Nivel actual.
   - **Campos Editables (Formulario Responsivo):**
     - En una columna vertical continua (`display: flex; flex-direction: column; gap: var(--espacio-4)`):
       - `Género` (`genero`: componente `Selector`, opciones: `FEMENINO`, `MASCULINO`, `OTRO`, obligatorio, altura mínima táctil de 44px).
       - `Teléfono Celular` (`telefono`: componente `CampoTexto`, tipo `tel`, obligatorio, 10 dígitos numéricos iniciando en `09`, padding `var(--espacio-2) var(--espacio-3)`, radio `var(--radio-md)`).
   - **Acciones Táctiles Responsivas:**
     - En pantallas móviles, los botones se organizan en pila vertical a ancho completo (`display: flex; flex-direction: column-reverse; gap: var(--espacio-2); width: 100%`).
     - En escritorio se ubican en fila alineados a la derecha (`flex-direction: row; justify-content: flex-end; gap: var(--espacio-3)`):
       - Botón **`"Cancelar"`** (variante `secondary`).
       - Botón **`"Guardar Cambios"`** (componente `Boton`, variante `primary`, fondo `var(--color-primario)`, texto blanco).
4. El **Responsable de Comunidad** modifica el teléfono celular o el género y pulsa o toca **`"Guardar Cambios"`**.
5. El **Sistema** (frontend) valida en el cliente que el teléfono cumpla con el formato `09XXXXXXXX` y que el género pertenezca a las opciones válidas (`FEMENINO`, `MASCULINO`, `OTRO`).
6. El **Sistema** deshabilita el botón mostrando estado de carga y emite la petición `PUT /api/comunidades/mi-comunidad/miembros/{miembroId}` con el payload:
```json
{
  "genero": "FEMENINO",
  "telefono": "0998877665"
}
```
7. El **Sistema** (backend) extrae el `responsableId`, recupera la `comunidadId`, verifica que el `miembroId` pertenezca a dicha comunidad en `comunidad_miembros`, actualiza los campos `genero` y `telefono` en la entidad `Miembro` y persiste los cambios.
8. El **Sistema** retorna el código HTTP `200 OK` con el DTO `MiembroDetalleResponse`:
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
  "telefono": "0998877665",
  "nivel": "APRENDIZ",
  "puntosTotales": 0,
  "activoEnComunidad": true,
  "fechaIngreso": "2026-11-10"
}
```
9. El **Sistema** (frontend) cierra el modal, muestra la alerta de éxito en `Notificacion` (*"Datos del voluntario actualizados correctamente"*) y refresca los datos en la tabla.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **5a. Teléfono inválido o campos vacíos (HTTP 400 Bad Request):**
  - **Condición:** Si `telefono` está vacío o no coincide con la expresión regular `^09[0-9]{8}$`, o si `genero` es nulo.
  - **Comportamiento:** Bean Validation rechaza la petición. El frontend muestra el error en `Notificacion` sin cerrar el modal.
* **7a. Miembro no pertenece a la comunidad del responsable (HTTP 404 Not Found):**
  - **Condición:** Si el `miembroId` enviado en la URL no pertenece a la comunidad asignada al responsable autenticado.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"El voluntario no pertenece a la comunidad administrada"*.
* **7b. Acceso no autorizado (HTTP 401 Unauthorized / HTTP 403 Forbidden):**
  - **Condición:** Si la solicitud carece de token o el usuario logueado no tiene el rol `RESPONSABLE_COMUNIDAD`.
  - **Comportamiento:** Spring Security retorna HTTP 401 o 403.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros/`):**
  - `EditarMiembroModal.tsx`: Diálogo emergente responsivo con campos de solo lectura para identificación y campos editables (`CampoTexto` para teléfono y `Selector` para género admitiendo `OTRO`), botones táctiles adaptados para móvil y feedback visual mediante `Notificacion` con variables de `index.css`.
  - `miembrosApi.ts`: Función `actualizarDatosMiembro(miembroId: number, datos: ActualizarMiembroRequest): Promise<MiembroDetalleResponse>` consumiendo `PUT /api/comunidades/mi-comunidad/miembros/{miembroId}` mediante `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - Integrado dentro de `MiembrosComunidadPage.tsx` (`/comunidad/miembros`).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadMiembroController.java`):**
  - `PUT /api/comunidades/mi-comunidad/miembros/{miembroId}` -> Recibe `@PathVariable Long miembroId`, `@Valid @RequestBody ActualizarMiembroRequest request` y `Authentication authentication`, retorna HTTP `200 OK` con `MiembroDetalleResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Cuerpo de Solicitud (`ActualizarMiembroRequest`):**
    - `genero`: `Genero`, obligatorio (`@NotNull`, valores: `FEMENINO`, `MASCULINO`, `OTRO`).
    - `telefono`: `String`, obligatorio, formato celular ecuatoriano (`@NotBlank`, `@Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe iniciar con 09 y tener 10 dígitos")`).
  - **Respuesta Exitosa (HTTP 200 OK - `MiembroDetalleResponse`):**
    - `id`: `Long`.
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
    - `400 Bad Request`: Formato de teléfono inválido o género nulo.
    - `401 Unauthorized`: Token no válido o expirado.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: Voluntario no encontrado en la comunidad.
- [x] **DTOs (`com.huertos.dto`):**
  - `ActualizarMiembroRequest.java`: `public record ActualizarMiembroRequest(@NotNull(message = "El género es obligatorio") Genero genero, @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "^09[0-9]{8}$", message = "El teléfono debe iniciar con 09 y contener 10 dígitos") String telefono) {}`.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `@Transactional MiembroDetalleResponse actualizarDatosContacto(Long responsableId, Long miembroId, ActualizarMiembroRequest request)`.
  - Verifica pertenencia en `ComunidadMiembroRepository` y actualiza la entidad `Miembro`.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `actualizarDesdeRequest(ActualizarMiembroRequest request, @MappingTarget Miembro miembro)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `miembros` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar):
    - `genero`: VARCHAR(20) NOT NULL (mapeado en JPA a enum `Genero` con valores `'FEMENINO'`, `'MASCULINO'`, `'OTRO'`).
    - `telefono`: VARCHAR(20) NULL.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `com.huertos.model.Genero.java`):**
  - Mapeo de campos `genero` y `telefono` con sus métodos accesores.
- [x] **Repository (`com.huertos.repository.MiembroRepository.java` y `ComunidadMiembroRepository.java`):**
  - `Optional<Miembro> findById(Long id);`
  - `boolean existsByComunidadIdAndMiembroId(Long comunidadId, Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Actualización exitosa: modifica únicamente `genero` (incluyendo opción `OTRO`) y `telefono` persistiendo en `MiembroRepository`.
  - No pertenencia a la comunidad: lanza `RecursoNoEncontradoException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadMiembroControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadMiembroController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `PUT /api/comunidades/mi-comunidad/miembros/{miembroId}` con teléfono válido retorna HTTP `200 OK`.
    - Petición con teléfono menor a 10 dígitos o que no inicie en `09` retorna HTTP `400 Bad Request`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros/EditarMiembroModal.test.tsx`):**
  - Apertura del modal responsivo con los datos actuales del voluntario.
  - Validación de que los campos de cédula, nombres y fecha de nacimiento estén en modo de solo lectura.
  - Envío exitoso con MSW y actualización reactiva de la grilla.
