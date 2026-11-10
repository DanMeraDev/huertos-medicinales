---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU03: Definir talentos e insignias de la comunidad"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad registrar nuevos talentos o insignias personalizadas para su colectivo de siembra (ej. "Maestro del Abonaje" al completar 50 abonajes). El responsable asocia cada insignia a una actividad cotidiana de su catálogo interno (definida en HR-CU07), asigna un nombre alfanumérico motivacional, describe el logro y establece una meta de repeticiones estrictamente positiva. El sistema aplica automáticamente el código de ícono por defecto ('insignia-semilla') configurado en la base de datos, incentivando el sentido de pertenencia y la constancia de los miembros voluntarios en las labores del huerto.`
* **Dependencias:** `Depende de #US-CU01 (sesión de Responsable de Comunidad), #US-CU06 (comunidad activa) y se vincula con #HR-CU07 (catálogo de actividades cotidianas)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y cuenta con un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario lidera una comunidad activa vinculada a su responsable_id en la tabla comunidades.`
3. `La comunidad cuenta con al menos una actividad registrada y activa en catalogo_actividades_comunidad (HR-CU07).`
4. `La tabla talentos_comunidad en MariaDB tiene configurado el valor por defecto 'insignia-semilla' en la columna icono_codigo según V2__esquema_completo_v1.sql.`
5. `El endpoint POST /api/comunidades/mi-comunidad/talentos se encuentra protegido exigiendo rol RESPONSABLE_COMUNIDAD en SecurityConfig.java.`

### Postcondiciones
1. `Se inserta un nuevo registro en la tabla talentos_comunidad asociado a comunidad_id y actividad_comunidad_id con icono_codigo = 'insignia-semilla' y activo = true.`
2. `El sistema retorna el código HTTP 201 Created con el DTO TalentoComunidadResponse.`
3. `El formulario se limpia en el frontend, se muestra una notificación con el componente Notificacion y se actualiza la lista visual de talentos de la comunidad.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** ingresa a la sección `"Insignias y Talentos"` desde el menú lateral en `Layout.tsx`.
2. El **Sistema** emite en paralelo:
   - `GET /api/comunidades/mi-comunidad/actividades` para poblar el listado de actividades disponibles.
   - `GET /api/comunidades/mi-comunidad/talentos` para consultar los talentos previamente configurados.
3. El **Sistema** renderiza una interfaz viva, motivacional y festiva, acompañada por `<KintiGuia variante="dia"/>` con el mensaje:
   *"¡Fomentar la participación comunitaria es grandioso! Define medallas de talento para reconocer el esfuerzo constante de tus voluntarios en cada labor sagrada"*.
4. El **Sistema** despliega el formulario dinámico con los siguientes controles:
   - `Actividad Asociada` (`actividadComunidadId`: componente `Selector`, lista desplegable de actividades cotidianas de la comunidad, obligatorio).
   - `Nombre de la Insignia o Talento` (`nombreInsignia`: componente `CampoTexto`, obligatorio, admite solo letras, números y espacios sin símbolos especiales, ej. *"Guardián del Riego 1"*, máx. 120 caracteres).
   - `Descripción del Reconocimiento` (`descripcion`: componente `CampoTexto` multilínea / textarea, obligatorio, admite texto libre explicando el motivo del mérito, máx. 255 caracteres).
   - `Meta de Repeticiones` (`metaRepeticiones`: componente `CampoTexto`, tipo `number`, obligatorio, entero positivo mayor a 0, ej. `50`).
   - `Previsualización de Medalla`: Tarjeta visual que exhibe la previsualización del badge con el ícono temático predeterminado (`Insignia` con variante base de semilla).
   - Botón de envío **`"Registrar Medalla de Comunidad"`** (componente `Boton`, variante `primary`).
5. El **Responsable de Comunidad** selecciona la actividad (ej. *"Abonar camas de siembra"*), ingresa el nombre de la medalla, describe el propósito, define la meta (ej. `50`) y pulsa **`"Registrar Medalla de Comunidad"`**.
6. El **Sistema** (frontend) valida en el cliente que `metaRepeticiones` sea un número entero `>= 1`, que `nombreInsignia` contenga exclusivamente caracteres alfanuméricos y espacios, y que todos los campos requeridos estén completos.
7. El **Sistema** deshabilita el botón mostrando estado de carga y emite la petición `POST /api/comunidades/mi-comunidad/talentos` con el payload:
```json
{
  "actividadComunidadId": 2,
  "nombreInsignia": "Maestro del Abonaje 1",
  "descripcion": "Otorgada al miembro voluntario que complete 50 sesiones de preparación y distribución de compost en los surcos",
  "metaRepeticiones": 50
}
```
8. El **Sistema** (backend) extrae el `responsableId` de la sesión JWT, recupera la `comunidadId` del responsable, verifica que la actividad pertenezca a la misma comunidad, comprueba que no exista un talento activo con el mismo nombre en la comunidad, asigna por defecto `icono_codigo = "insignia-semilla"` y fecha actual con `Clock`, y persiste el registro en `talentos_comunidad`.
9. El **Sistema** responde con código HTTP `201 Created` y el DTO `TalentoComunidadResponse`:
```json
{
  "id": 1,
  "comunidadId": 3,
  "actividadComunidadId": 2,
  "nombreActividad": "Abonar camas de siembra",
  "nombreInsignia": "Maestro del Abonaje 1",
  "descripcion": "Otorgada al miembro voluntario que complete 50 sesiones de preparación y distribución de compost en los surcos",
  "metaRepeticiones": 50,
  "iconoCodigo": "insignia-semilla",
  "activo": true,
  "creadoEn": "2026-11-10T12:00:00"
}
```
10. El **Sistema** (frontend) limpia los campos del formulario, agrega la nueva insignia a la galería de medallas de la comunidad y muestra una notificación con el componente `Notificacion`: *"Medalla registrada con éxito. ¡Tus miembros ya pueden comenzar a acumular repeticiones para obtenerla!"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **6a. Meta de repeticiones menor o igual a cero (HTTP 400 Bad Request):**
  - **Condición:** Si `metaRepeticiones` es `<= 0` o un valor decimal/no numérico.
  - **Comportamiento:** Bean Validation rechaza la solicitud (`@Min(1)`). El frontend resalta el error en el campo con `Notificacion`: *"La meta de repeticiones debe ser un número entero mayor a 0"*.
* **6b. Nombre de insignia con caracteres especiales no permitidos (HTTP 400 Bad Request):**
  - **Condición:** Si `nombreInsignia` incluye caracteres como `@`, `#`, `$`, `%`, guiones bajos o puntuación no alfanumérica.
  - **Comportamiento:** Bean Validation rechaza la petición con mensaje: *"El nombre de la insignia solo puede contener letras, números y espacios"*.
* **8a. Nombre de insignia duplicado en la misma comunidad (HTTP 409 Conflict):**
  - **Condición:** Si ya existe en `talentos_comunidad` otro registro activo con el mismo `nombre_insignia` vinculado a la misma `comunidad_id`.
  - **Comportamiento:** El backend lanza `ConflictoException`: *"Ya existe un talento o medalla registrada con este nombre en tu comunidad"*. El frontend mantiene los datos ingresados para que el usuario pueda corregir el nombre.
* **8b. Actividad no encontrada o de otra comunidad (HTTP 404 Not Found / 400 Bad Request):**
  - **Condición:** Si el `actividadComunidadId` enviado no existe en la base de datos o pertenece a una comunidad distinta a la del responsable autenticado.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"La actividad comunitaria especificada no existe o no pertenece a tu comunidad"*.
* **8c. Acceso denegado o sesión inválida (HTTP 401 Unauthorized / HTTP 403 Forbidden):**
  - **Condición:** Si la petición carece de cabecera `Authorization` o el usuario no posee el rol `RESPONSABLE_COMUNIDAD`.
  - **Comportamiento:** Spring Security devuelve HTTP 401 o 403.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/talentos/`):**
  - `CrearTalentoForm.tsx`: Formulario con selector de actividades, campos de texto con validación de expresiones regulares, selector numérico para meta de repeticiones, previsualización de la insignia y mensaje vivo de `<KintiGuia variante="dia"/>`.
  - `GaleriaTalentosComunidad.tsx`: Grilla que exhibe las medallas activas de la comunidad empleando el componente `Insignia` con el ícono predeterminado `insignia-semilla` y la meta requerida.
  - `talentosApi.ts`: Funciones `crearTalentoComunidad(datos: CrearTalentoComunidadRequest): Promise<TalentoComunidadResponse>` y `obtenerTalentosComunidad(): Promise<TalentoComunidadResponse[]>` usando `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `TalentosComunidadPage.tsx`: Vista registrada en `App.tsx` bajo la ruta `/comunidad/talentos`, protegida para `RESPONSABLE_COMUNIDAD`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.TalentoComunidadController.java`):**
  - `GET /api/comunidades/mi-comunidad/talentos` -> Retorna HTTP `200 OK` con `List<TalentoComunidadResponse>`.
  - `POST /api/comunidades/mi-comunidad/talentos` -> Recibe `@Valid @RequestBody CrearTalentoComunidadRequest request`, retorna HTTP `201 Created` con `TalentoComunidadResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Cuerpo de Solicitud (`CrearTalentoComunidadRequest`):**
    - `actividadComunidadId`: `Long`, obligatorio (`@NotNull`).
    - `nombreInsignia`: `String`, obligatorio, solo letras, números y espacios (`@NotBlank`, `@Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$")`, `@Size(max = 120)`).
    - `descripcion`: `String`, obligatorio (`@NotBlank`, `@Size(max = 255)`).
    - `metaRepeticiones`: `Integer`, obligatorio, mínimo 1 (`@NotNull`, `@Min(value = 1, message = "La meta de repeticiones debe ser al menos 1")`).
  - **Respuesta Exitosa (HTTP 201 Created - `TalentoComunidadResponse`):**
    - `id`: `Long`.
    - `comunidadId`: `Long`.
    - `actividadComunidadId`: `Long`.
    - `nombreActividad`: `String`.
    - `nombreInsignia`: `String`.
    - `descripcion`: `String`.
    - `metaRepeticiones`: `Integer`.
    - `iconoCodigo`: `String` (valor constante predeterminado `"insignia-semilla"`).
    - `activo`: `boolean`.
    - `creadoEn`: `LocalDateTime`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Parámetros inválidos, meta <= 0 o caracteres especiales en el nombre.
    - `401 Unauthorized`: Token ausente o inválido.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: Actividad no encontrada.
    - `409 Conflict`: Nombre de insignia duplicado en la misma comunidad.
- [x] **DTOs (`com.huertos.dto`):**
  - `CrearTalentoComunidadRequest.java`: `public record CrearTalentoComunidadRequest(@NotNull(message = "La actividad es obligatoria") Long actividadComunidadId, @NotBlank(message = "El nombre de la insignia es obligatorio") @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo admite letras, números y espacios") @Size(max = 120) String nombreInsignia, @NotBlank(message = "La descripción es obligatoria") @Size(max = 255) String descripcion, @NotNull(message = "La meta es obligatoria") @Min(value = 1, message = "La meta debe ser mayor a cero") Integer metaRepeticiones) {}`.
  - `TalentoComunidadResponse.java`: `record` completo para retorno de la medalla registrada.
- [x] **Service (`com.huertos.service.TalentoComunidadService.java` y `com.huertos.service.impl.TalentoComunidadServiceImpl.java`):**
  - Método `List<TalentoComunidadResponse> listarPorResponsable(Long responsableId)`.
  - Método `@Transactional TalentoComunidadResponse registrarTalento(Long responsableId, CrearTalentoComunidadRequest request)`.
  - Asigna `iconoCodigo = "insignia-semilla"` de manera automática.
  - Inyección de `TalentoComunidadRepository`, `ComunidadRepository`, `ActividadComunidadRepository` y `Clock`.
- [x] **Mapper (`com.huertos.mapper.TalentoComunidadMapper.java`):**
  - Método `toEntity(CrearTalentoComunidadRequest request, Comunidad comunidad, ActividadComunidad actividad)`.
  - Método `toResponse(TalentoComunidad entity)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea la tabla `talentos_comunidad` definida en `V2__esquema_completo_v1.sql`:
    - `id` BIGINT AUTO_INCREMENT PRIMARY KEY
    - `comunidad_id` BIGINT NOT NULL (FK -> `comunidades(id)`)
    - `actividad_comunidad_id` BIGINT NOT NULL (FK -> `catalogo_actividades_comunidad(id)`)
    - `nombre_insignia` VARCHAR(120) NOT NULL
    - `descripcion` VARCHAR(255) NOT NULL
    - `meta_repeticiones` INT NOT NULL
    - `icono_codigo` VARCHAR(60) NOT NULL DEFAULT 'insignia-semilla'
    - `activo` BOOLEAN NOT NULL DEFAULT TRUE
    - `creado_en` DATETIME(6) NOT NULL
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.TalentoComunidad.java`):**
  - Mapeo de la entidad con campos `nombreInsignia`, `descripcion`, `metaRepeticiones`, `iconoCodigo`, `activo`, relaciones `@ManyToOne` con `Comunidad` y `ActividadComunidad`.
- [x] **Repository (`com.huertos.repository.TalentoComunidadRepository.java`):**
  - `boolean existsByComunidadIdAndNombreInsigniaAndActivoTrue(Long comunidadId, String nombreInsignia);`
  - `List<TalentoComunidad> findByComunidadIdAndActivoTrue(Long comunidadId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/TalentoComunidadServiceImplTest.java`):**
  - Creación exitosa: valida que se persista la entidad con `iconoCodigo = "insignia-semilla"`, `activo = true` y fecha de `Clock`.
  - Excepción por duplicidad: lanza `ConflictoException` si el nombre de medalla ya existe en la misma comunidad.
  - Excepción por meta inválida: lanza `ReglaNegocioException` si `metaRepeticiones <= 0`.
  - Excepción por actividad ajena: lanza `RecursoNoEncontradoException` si la actividad no pertenece a la comunidad del responsable.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/TalentoComunidadControllerTest.java`):**
  - Con `@WebMvcTest(TalentoComunidadController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `POST /api/comunidades/mi-comunidad/talentos` con datos válidos retorna HTTP `201 Created`.
    - Petición con nombre que contiene caracteres especiales retorna HTTP `400 Bad Request`.
    - Petición con meta `<= 0` retorna HTTP `400 Bad Request`.
  - Con rol distinto o anónimo: retorna HTTP `403 Forbidden` o `401 Unauthorized`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/talentos/CrearTalentoForm.test.tsx`):**
  - Renderizado de controles y presencia de `<KintiGuia/>` con la frase de motivación.
  - Validación en cliente de bloqueo de caracteres especiales en el nombre de la medalla.
  - Envío exitoso con MSW (`handlers.ts`), alerta de confirmación con `Notificacion` e inserción en la galería de talentos.
