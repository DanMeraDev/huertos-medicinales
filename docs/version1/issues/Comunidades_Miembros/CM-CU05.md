---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU05: Gestionar estado de miembro (Alta / Baja)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU05`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad activar ("Dar de alta") o suspender temporalmente ("Dar de baja") la participación de un miembro voluntario dentro de su comunidad específica. La operación se ejecuta sobre la tabla relacional comunidad_miembros sin eliminar ni alterar la ficha global del voluntario en la tabla miembros, garantizando la trazabilidad histórica de su participación. Si se da de baja, el sistema registra la fecha y opcionalmente el motivo; si se da de alta, reactiva su condición de participante activo.`
* **Dependencias:** `Depende de #US-CU01 (sesión de Responsable de Comunidad), #US-CU06 (comunidad activa) y #CM-CU04 (miembro previamente vinculado)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y cuenta con un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario lidera una comunidad activa en comunidades asociada a su responsable_id.`
3. `El voluntario seleccionado cuenta con un registro en la tabla comunidad_miembros correspondiente a la comunidad del responsable.`
4. `El endpoint PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado se encuentra protegido con rol RESPONSABLE_COMUNIDAD en SecurityConfig.java.`

### Postcondiciones
1. `Se actualiza el registro en comunidad_miembros:`
   - `Si es Dar de baja: activo_en_comunidad = false, fecha_baja = fecha actual con Clock, y se persiste motivo_baja.`
   - `Si es Dar de alta: activo_en_comunidad = true, fecha_baja = null, motivo_baja = null.`
2. `El sistema responde con código HTTP 200 OK y el DTO MiembroEstadoResponse.`
3. `La tabla en el frontend actualiza el estado del voluntario y conmuta el botón de acción correspondiente ("Dar de alta" o "Dar de baja").`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** ingresa a la vista general de miembros en `"/comunidad/miembros"` desde `Layout.tsx`.
2. El **Sistema** renderiza la vista diseñada bajo el enfoque **Mobile First para Progressive Web App (PWA)**, estructurada con las variables de diseño de `frontend/src/index.css`:
   - **Contenedor Responsivo:** La página utiliza `width: 100%` con padding lateral de `var(--espacio-4)` en dispositivos móviles, extendiéndose hasta `var(--ancho-maximo)` en computadoras de escritorio con espaciado de `padding: var(--espacio-8) var(--espacio-6)`.
   - **Cabecera Motivacional:** Banner con fondo `var(--color-superficie)`, borde de `var(--borde-ancho) solid var(--color-borde)`, radio `var(--radio-lg)`, padding `var(--espacio-4)` y sombra `var(--sombra-sm)`, integrando el componente `<KintiGuia variante="dia"/>`:
     - Mensaje: *"La unión hace la fuerza: estos son los miembros y guardianes que hacen que nuestro huerto florezca día a día"*, con tipografía `var(--texto-base)`, color `var(--color-primario)` y peso `var(--peso-medio)`.
   - **Tabla Responsiva de Voluntarios:**
     - La tabla (`Tabla`) se aloja en un contenedor con desplazamiento horizontal suave (`overflow-x: auto; -webkit-overflow-scrolling: touch;`), permitiendo lectura fluida en móviles sin desbordar el viewport:
       - Columnas: `Cédula`, `Nombre Completo`, `Género`, `Teléfono`, `Nivel`, `Estado` (badge con `Insignia`) y `Acciones`.
       - En la columna de acciones, los botones ofrecen áreas táctiles mínimas de 44px de altura con padding `var(--espacio-2) var(--espacio-3)` para interacción cómoda con el pulgar.
3. En la fila del voluntario que presenta `activo_en_comunidad == true`, el **Responsable de Comunidad** pulsa el botón **`"Dar de baja"`** (componente `Boton`, variante `danger` / outline).
4. El **Sistema** despliega un diálogo de confirmación (`Modal`), diseñado responsivamente para PWA:
   - **Comportamiento Móvil:** Ocupa `width: calc(100% - var(--espacio-4))`, con `max-height: 90vh`, fondo `var(--color-superficie)`, radio `var(--radio-lg)` y padding `var(--espacio-4)`.
   - **Título:** *"Confirmar baja de voluntario en la comunidad"* en tipografía `var(--texto-xl)`, color `var(--color-error)` y peso `var(--peso-seminegrita)`.
   - **Mensaje:** *"¿Estás seguro de suspender la participación de este voluntario en esta comunidad? Podrás reactivar su estado en cualquier momento."*, en `var(--texto-sm)` y color `var(--color-texto)`.
   - **Campo Opcional:** `Motivo de la baja` (`CampoTexto`, multilínea con controles accesibles, padding `var(--espacio-2) var(--espacio-3)`, radio `var(--radio-md)`, máx. 255 caracteres).
   - **Acciones Táctiles Responsivas:** En pantallas móviles, los botones se apilan verticalmente a ancho completo (`display: flex; flex-direction: column-reverse; gap: var(--espacio-2); width: 100%`); en escritorio se orientan en fila (`flex-direction: row; justify-content: flex-end; gap: var(--espacio-3)`):
     - Botón **`"Cancelar"`** (variante `secondary`).
     - Botón **`"Confirmar Baja"`** (`Boton`, variante `danger`, fondo `var(--color-error)`, texto blanco).
5. El **Responsable de Comunidad** ingresa el motivo (ej. *"Cambio temporal de domicilio"*) y pulsa o toca **`"Confirmar Baja"`**.
6. El **Sistema** deshabilita el modal mostrando indicador de carga y emite la petición `PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado` con el payload:
```json
{
  "activo": false,
  "motivoBaja": "Cambio temporal de domicilio"
}
```
7. El **Sistema** (backend) extrae el `responsableId`, recupera la `comunidadId`, localiza la relación en `comunidad_miembros`, asigna `activo_en_comunidad = false`, registra `fecha_baja` con `Clock.systemUTC()` y persiste `motivo_baja`.
8. El **Sistema** retorna el código HTTP `200 OK` con el DTO `MiembroEstadoResponse`:
```json
{
  "miembroId": 15,
  "comunidadId": 3,
  "nombreMiembro": "María Tránsito Amaguaña Alba",
  "activoEnComunidad": false,
  "fechaBaja": "2026-11-10",
  "motivoBaja": "Cambio temporal de domicilio",
  "mensaje": "Estado de participación actualizado exitosamente"
}
```
9. El **Sistema** (frontend) cierra el modal, muestra una notificación con el componente `Notificacion` y actualiza la fila de la tabla mostrando el badge inactivo y alternando el botón hacia **`"Dar de alta"`**.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Flujo alternativo: Dar de alta a un miembro inactivo:**
  - **Paso 4.1:** Si el miembro tiene `activo_en_comunidad == false`, en la tabla se visualiza el botón **`"Dar de alta"`** (`Boton`, variante `primary`).
  - **Paso 4.2:** El responsable pulsa **`"Dar de alta"`**.
  - **Paso 4.3:** El Sistema abre el `Modal` de confirmación responsivo: *"¿Deseas reactivar la membresía de este voluntario en la comunidad?"*.
  - **Paso 4.4:** Al confirmar, se emite `PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado` con `{"activo": true, "motivoBaja": null}`.
  - **Paso 4.5:** El backend actualiza `activo_en_comunidad = true`, limpia `fecha_baja` y `motivo_baja`, y retorna HTTP `200 OK`.
* **8a. Miembro no pertenece a la comunidad del responsable (HTTP 404 Not Found):**
  - **Condición:** Si el `miembroId` proporcionado en la ruta no tiene una relación registrada con la comunidad del responsable autenticado.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"El miembro especificado no pertenece a tu comunidad"*.
* **8b. Estado idéntico al actual (HTTP 409 Conflict):**
  - **Condición:** Si se intenta dar de baja a un miembro que ya tiene `activo_en_comunidad = false` o dar de alta a uno que ya está activo.
  - **Comportamiento:** El backend lanza `ConflictoException`: *"El voluntario ya se encuentra en el estado solicitado"*.
* **8c. Acceso no autorizado (HTTP 401 Unauthorized / HTTP 403 Forbidden):**
  - **Condición:** Si la petición no cuenta con token JWT o el usuario no posee el rol `RESPONSABLE_COMUNIDAD`.
  - **Comportamiento:** Spring Security devuelve HTTP 401 o 403.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros/`):**
  - `TablaMiembrosComunidad.tsx`: Grilla responsiva adaptada para PWA con contenedor de scroll táctil para móvil. Contiene las acciones de estado ("Dar de baja" / "Dar de alta"), botón de edición (`CM-CU06`) y botón de ficha completa (`CM-CU07`).
  - `ConfirmarBajaModal.tsx`: Diálogo emergente responsivo para capturar opcionalmente el motivo y confirmar la suspensión.
  - `miembrosApi.ts`: Función `cambiarEstadoMiembro(miembroId: number, datos: CambiarEstadoMiembroRequest): Promise<MiembroEstadoResponse>` consumiendo `PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado` con `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `MiembrosComunidadPage.tsx`: Vista registrada en `App.tsx` bajo la ruta `/comunidad/miembros`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadMiembroController.java`):**
  - `PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado` -> Recibe `@PathVariable Long miembroId`, `@Valid @RequestBody CambiarEstadoMiembroRequest request` y `Authentication authentication`, retorna HTTP `200 OK` con `MiembroEstadoResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Cuerpo de Solicitud (`CambiarEstadoMiembroRequest`):**
    - `activo`: `boolean`, obligatorio (`@NotNull`).
    - `motivoBaja`: `String`, opcional (`@Size(max = 255)`).
  - **Respuesta Exitosa (HTTP 200 OK - `MiembroEstadoResponse`):**
    - `miembroId`: `Long`.
    - `comunidadId`: `Long`.
    - `nombreMiembro`: `String`.
    - `activoEnComunidad`: `boolean`.
    - `fechaBaja`: `LocalDate` (null si es alta).
    - `motivoBaja`: `String` (null si es alta).
    - `mensaje`: `String`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Payload malformado.
    - `401 Unauthorized`: Token JWT ausente o inválido.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: Miembro o relación comunitaria inexistente.
    - `409 Conflict`: El miembro ya se encuentra en el estado indicado.
- [x] **DTOs (`com.huertos.dto`):**
  - `CambiarEstadoMiembroRequest.java`: `public record CambiarEstadoMiembroRequest(@NotNull Boolean activo, @Size(max = 255) String motivoBaja) {}`.
  - `MiembroEstadoResponse.java`: `record` con el resultado de la actualización de estado.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `@Transactional MiembroEstadoResponse cambiarEstadoEnComunidad(Long responsableId, Long miembroId, CambiarEstadoMiembroRequest request)`.
  - Utiliza `Clock` para fijar la `fechaBaja` al suspender.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toEstadoResponse(ComunidadMiembro relacion, String mensaje)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `comunidad_miembros` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar):
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `comunidad_id` BIGINT NOT NULL (FK -> `comunidades(id)`)
    - `miembro_id` BIGINT NOT NULL (FK -> `miembros(id)`)
    - `activo_en_comunidad` BOOLEAN NOT NULL DEFAULT TRUE
    - `fecha_ingreso` DATE NOT NULL
    - `fecha_baja` DATE NULL
    - `motivo_baja` VARCHAR(255) NULL
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.ComunidadMiembro.java`):**
  - Mapeo de columnas `activoEnComunidad`, `fechaBaja`, `motivoBaja`.
- [x] **Repository (`com.huertos.repository.ComunidadMiembroRepository.java`):**
  - `Optional<ComunidadMiembro> findByComunidadIdAndMiembroId(Long comunidadId, Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Flujo de baja exitoso: asigna `activoEnComunidad = false`, fija `fechaBaja` con `Clock.fixed(...)` y persiste `motivoBaja`.
  - Flujo de alta exitoso: asigna `activoEnComunidad = true`, limpia `fechaBaja` y `motivoBaja`.
  - Excepción por no pertenencia: lanza `RecursoNoEncontradoException`.
  - Excepción por estado duplicado: lanza `ConflictoException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadMiembroControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadMiembroController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `PATCH /api/comunidades/mi-comunidad/miembros/{miembroId}/estado` retorna HTTP `200 OK`.
    - Petición a miembro ajeno retorna HTTP `404 Not Found`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros/TablaMiembrosComunidad.test.tsx`):**
  - Renderizado responsivo del botón dinámico ("Dar de baja" / "Dar de alta") según el estado del miembro.
  - Apertura del modal responsivo de confirmación y captura opcional de motivo.
  - Simulación de respuesta con MSW y actualización del badge visual.
