---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU05: Revisar solicitud de creación de comunidad"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU05`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Descripción:** `Permite al Técnico del Ministerio de Salud Pública examinar las solicitudes ciudadanas pendientes para la creación de comunidades y huertos medicinales. El técnico puede inspeccionar la ficha completa de la solicitud en modo solo lectura y resolverla: si la aprueba, el sistema orquesta automáticamente la creación de la cuenta de usuario del responsable (US-CU03), el registro formal de la comunidad y su huerto (US-CU06) y la emisión por correo de las credenciales de acceso (US-CU07); si la rechaza, debe justificar obligatoriamente el motivo, actualizando el estado de la solicitud y notificando al solicitante vía correo electrónico (US-CU07).`
* **Dependencias:** `Depende de #US-CU01 (sesión de Técnico MSP) y #US-CU04 (solicitudes pendientes); orquesta a #US-CU03, #US-CU06 y #US-CU07`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol TECNICO_MSP y cuenta con un token JWT válido (Authorization: Bearer <token>).`
2. `Existen registros en la tabla solicitudes_comunidad con estado = 'PENDIENTE'.`
3. `Los endpoints /api/solicitudes-comunidad/** requieren autenticación y rol TECNICO_MSP en SecurityConfig.java.`

### Postcondiciones
1. `Si la solicitud es APROBADA:`
   - `El estado en solicitudes_comunidad cambia a 'APROBADA', registrando fecha_revision y revisado_por_id.`
   - `Se ejecuta US-CU03 creando el usuario para el solicitante con rol RESPONSABLE_COMUNIDAD, password temporal aleatorio y requiere_cambio_clave = true.`
   - `Se ejecuta US-CU06 creando la comunidad en comunidades y su huerto base en huertos.`
   - `Se ejecuta US-CU07 enviando un correo al solicitante con sus credenciales de ingreso temporales.`
2. `Si la solicitud es RECHAZADA:`
   - `El estado en solicitudes_comunidad cambia a 'RECHAZADA', registrando motivo_rechazo, fecha_revision y revisado_por_id.`
   - `Se ejecuta US-CU07 enviando un correo al solicitante notificando las observaciones y motivos de rechazo.`
3. `La lista de solicitudes en el frontend se actualiza reflejando la resolución.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Técnico MSP** accede a la sección `"Solicitudes"` en la barra de navegación lateral en `Layout.tsx`.
2. El **Sistema** consulta las solicitudes pendientes mediante `GET /api/solicitudes-comunidad?estado=PENDIENTE` y evalúa la presencia de datos:
   - Si no existen solicitudes pendientes: Muestra `<KintiGuia variante="vacio"/>` con el mensaje: *"No tienes solicitudes de aprobación pendientes por el momento. ¡Buen trabajo!"*.
   - Si existen una o más solicitudes: Muestra `<KintiGuia variante="dia"/>` con el mensaje: *"Varias personas quieren crear su huerto, revisa sus solicitudes aquí"*.
3. El **Sistema** despliega una tabla con el componente `Tabla` conteniendo exactamente cuatro columnas:
   - `Solicitante` (nombre completo de quien solicitó).
   - `Fecha` (fecha de envío en formato `DD/MM/AAAA`).
   - `Provincia` (provincia donde se planea el huerto).
   - `Acción` (botón **`"Ver más"`**, componente `Boton`, variante `secondary`).
4. El **Técnico MSP** pulsa **`"Ver más"`** sobre la solicitud que desea evaluar.
5. El **Sistema** obtiene el detalle completo vía `GET /api/solicitudes-comunidad/{id}` y despliega una vista o modal de inspección con todos los campos en modo de solo lectura (sin opción de edición):
   - Datos de la comunidad propuestos: nombre, tipo de organización, ubicación geográfica (provincia, parroquia, dirección, coordenadas, altitud), diseño principal y subtipos solicitados.
   - Datos del solicitante: cédula, nombre, correo, celular, género.
   - En la parte inferior se despliegan dos botones de acción: **`"Aprobar"`** (`Boton`, variante `primary`) y **`"No aprobar"`** (`Boton`, variante `danger`).
6. El **Técnico MSP** pulsa **`"Aprobar"`**.
7. El **Sistema** solicita confirmación mediante un diálogo con `Modal`: *"¿Confirmas la aprobación de esta solicitud? Se creará la comunidad, su huerto y la cuenta de acceso para el responsable"*.
8. El **Técnico MSP** confirma la acción.
9. El **Sistema** (backend) ejecuta la transacción atómica:
   - Actualiza la solicitud a estado `APROBADA`, asociando `revisado_por_id` y `fecha_revision`.
   - Llama a `UsuarioService` (`US-CU03`) para crear el usuario con rol `RESPONSABLE_COMUNIDAD`.
   - Llama a `ComunidadService` (`US-CU06`) para crear la comunidad y el huerto asociado.
   - Despacha el evento asíncrono a `EmailService` (`US-CU07`) para enviar las credenciales.
10. El **Sistema** retorna el código HTTP `200 OK` con el DTO `ResolucionSolicitudResponse`:
```json
{
  "solicitudId": 12,
  "estado": "APROBADA",
  "mensaje": "Solicitud aprobada exitosamente. Comunidad y cuenta de usuario creadas.",
  "comunidadId": 3,
  "usuarioId": 8
}
```
11. El **Sistema** (frontend) cierra la vista de inspección, muestra una alerta de confirmación con el componente `Notificacion` y refresca la tabla de solicitudes.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **6a. Flujo de Rechazo de Solicitud ("No aprobar"):**
  - **Paso 6.1:** El Técnico MSP pulsa **`"No aprobar"`**.
  - **Paso 6.2:** El Sistema abre un diálogo (`Modal`) solicitando ingresar el motivo del rechazo mediante un área de texto obligatoria (`CampoTexto` multilínea / textarea).
  - **Paso 6.3:** Si el Técnico intenta enviar el motivo en blanco, el frontend bloquea el envío y alerta que el motivo es obligatorio (mínimo 20 caracteres).
  - **Paso 6.4:** El Técnico ingresa la justificación y pulsa **`"Confirmar Rechazo"`**.
  - **Paso 6.5:** El backend procesa `POST /api/solicitudes-comunidad/{id}/rechazar` con el payload:
```json
{
  "motivoRechazo": "La ubicación indicada no cuenta con acceso seguro a fuente de agua para el riego comunitario según los lineamientos técnicos del MSP."
}
```
  - **Paso 6.6:** El Sistema actualiza el registro con `estado = 'RECHAZADA'`, guarda `motivo_rechazo`, despacha el correo informativo (`US-CU07`) y retorna HTTP `200 OK`.
* **8a. Solicitud ya resuelta previamente (HTTP 409 Conflict):**
  - **Condición:** Si otro técnico ya aprobó o rechazó la misma solicitud mientras estaba abierta en pantalla (`estado != 'PENDIENTE'`).
  - **Comportamiento:** El backend lanza `ConflictoException`: *"Esta solicitud ya ha sido resuelta previamente por otro técnico"*. El frontend actualiza la vista informando el estado actual.
* **8b. Error en la creación de usuario o comunidad (HTTP 500 / Rollback transaccional):**
  - **Condición:** Si ocurre un fallo durante la creación del usuario o la comunidad (ej. colisión inesperada de datos).
  - **Comportamiento:** La anotación `@Transactional` revierte todas las operaciones (la solicitud vuelve a quedar en `PENDIENTE` y no se persiste usuario ni comunidad a medias). Se informa al usuario mediante `Notificacion` de error.
* **8c. Acceso denegado (HTTP 403 Forbidden):**
  - **Condición:** Si el usuario autenticado no posee el rol `TECNICO_MSP`.
  - **Comportamiento:** Spring Security retorna HTTP 403 Forbidden.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/solicitudes/`):**
  - `TablaSolicitudes.tsx`: Renderiza la tabla de 4 columnas (Solicitante, Fecha, Provincia, Acción), acompaña con `<KintiGuia/>` evaluando si la lista está vacía o con elementos, y maneja estados de carga.
  - `DetalleSolicitudModal.tsx`: Visualizador en solo lectura con todos los campos de la solicitud y botones de acción ("Aprobar" y "No aprobar").
  - `RechazoSolicitudModal.tsx`: Modal secundario con campo de texto obligatorio para capturar el `motivoRechazo`.
  - `solicitudesApi.ts`: Funciones `obtenerSolicitudesPendientes()`, `obtenerDetalleSolicitud(id: number)`, `aprobarSolicitud(id: number)` y `rechazarSolicitud(id: number, motivo: string)` usando `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `SolicitudesPage.tsx`: Vista registrada en `App.tsx` bajo la ruta `/solicitudes` protegida por rol `TECNICO_MSP`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.SolicitudComunidadController.java`):**
  - `GET /api/solicitudes-comunidad?estado=PENDIENTE` -> Retorna HTTP `200 OK` con `List<SolicitudResumenResponse>`.
  - `GET /api/solicitudes-comunidad/{id}` -> Retorna HTTP `200 OK` con `SolicitudDetalleCompletoResponse`.
  - `POST /api/solicitudes-comunidad/{id}/aprobar` -> Recibe id de ruta y usuario autenticado, retorna HTTP `200 OK` con `ResolucionSolicitudResponse`.
  - `POST /api/solicitudes-comunidad/{id}/rechazar` -> Recibe `@Valid @RequestBody RechazarSolicitudRequest request`, retorna HTTP `200 OK` con `ResolucionSolicitudResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('TECNICO_MSP')")`).
  - **Cuerpo de Solicitud para Rechazo (`RechazarSolicitudRequest`):**
    - `motivoRechazo`: `String`, obligatorio (`@NotBlank`, `@Size(min = 20, max = 500)`).
  - **Respuesta Exitosa (HTTP 200 OK - `ResolucionSolicitudResponse`):**
    - `solicitudId`: `Long`.
    - `estado`: `EstadoSolicitud` (`APROBADA` o `RECHAZADA`).
    - `mensaje`: `String`.
    - `comunidadId`: `Long` (null si es rechazada).
    - `usuarioId`: `Long` (null si es rechazada).
  - **Respuestas de Error:**
    - `400 Bad Request`: Motivo de rechazo vacío o menor a 20 caracteres.
    - `401 Unauthorized`: Token no provisto o expirado.
    - `403 Forbidden`: Usuario sin rol `TECNICO_MSP`.
    - `404 Not Found`: Solicitud con el id indicado inexistente.
    - `409 Conflict`: Solicitud ya resuelta.
- [x] **DTOs (`com.huertos.dto`):**
  - `SolicitudResumenResponse.java`: `public record SolicitudResumenResponse(Long id, String solicitante, LocalDate fecha, String provincia) {}`.
  - `SolicitudDetalleCompletoResponse.java`: `record` con todos los campos de comunidad, solicitante y ubicación.
  - `RechazarSolicitudRequest.java`: `public record RechazarSolicitudRequest(@NotBlank @Size(min = 20, max = 500) String motivoRechazo) {}`.
  - `ResolucionSolicitudResponse.java`: `record` con resultado de la resolución.
- [x] **Service (`com.huertos.service.SolicitudComunidadService.java` y `com.huertos.service.impl.SolicitudComunidadServiceImpl.java`):**
  - Métodos `List<SolicitudResumenResponse> listarPorEstado(EstadoSolicitud estado)`.
  - Método `SolicitudDetalleCompletoResponse obtenerPorId(Long id)`.
  - Método `@Transactional ResolucionSolicitudResponse aprobarSolicitud(Long id, Long tecnicoMspId)`.
  - Método `@Transactional ResolucionSolicitudResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request, Long tecnicoMspId)`.
  - Inyección de `SolicitudComunidadRepository`, `UsuarioService` (US-CU03), `ComunidadService` (US-CU06) y `EmailService` (US-CU07).
- [x] **Mapper (`com.huertos.mapper.SolicitudComunidadMapper.java`):**
  - Método `toResumenResponse(SolicitudComunidad solicitud)`.
  - Método `toDetalleCompletoResponse(SolicitudComunidad solicitud)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `solicitudes_comunidad` y sus relaciones foráneas con `usuarios` definidas en `V2__esquema_completo_v1.sql`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.SolicitudComunidad.java`):**
  - Campos `estado`, `motivoRechazo`, `fechaRevision`, relación `@ManyToOne` con `revisadoPor` (`Usuario`).
- [x] **Repository (`com.huertos.repository.SolicitudComunidadRepository.java`):**
  - Método `List<SolicitudComunidad> findByEstadoOrderByCreadoEnAsc(EstadoSolicitud estado);`.
  - Método `Optional<SolicitudComunidad> findById(Long id);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SolicitudComunidadServiceImplTest.java`):**
  - Flujo de aprobación exitosa: verifica cambio de estado a `APROBADA`, invocación a creación de usuario, creación de comunidad y llamada al servicio de correo con Mockito.
  - Flujo de rechazo exitoso: comprueba asignación de `motivoRechazo`, estado `RECHAZADA` y despacho de correo explicativo.
  - Conflicto: comprueba que si `estado != PENDIENTE` lance `ConflictoException`.
  - Recurso no encontrado: lanza `RecursoNoEncontradoException` ante id no registrado.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SolicitudComunidadControllerTest.java`):**
  - Con `@WebMvcTest(SolicitudComunidadController.class)` y `@WithMockUser(roles = "TECNICO_MSP")`:
    - `GET /api/solicitudes-comunidad?estado=PENDIENTE` retorna HTTP `200 OK` y lista de solicitudes.
    - `POST /api/solicitudes-comunidad/{id}/aprobar` retorna HTTP `200 OK`.
    - `POST /api/solicitudes-comunidad/{id}/rechazar` con motivo válido retorna HTTP `200 OK`; con motivo vacío retorna HTTP `400 Bad Request`.
  - Con rol no autorizado: retorna HTTP `403 Forbidden`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/solicitudes/TablaSolicitudes.test.tsx`):**
  - Renderizado del mensaje de Kinti en estado vacío (`variante="vacio"`) cuando no hay elementos.
  - Renderizado de la tabla con 4 columnas y mensaje motivacional de Kinti cuando existen solicitudes.
  - Apertura del diálogo con datos completos al hacer clic en "Ver más".
  - Envío de aprobación y actualización de la grilla.
