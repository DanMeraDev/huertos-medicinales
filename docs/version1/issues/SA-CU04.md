---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "SA-CU04: Registrar actividad grande con evidencias y participantes (requiere MSP)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `SA-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite al Responsable de Comunidad registrar una actividad comunitaria ancestral mayor (hito oficial de la ruta MSP), subiendo evidencia fotográfica obligatoria (y video opcional), describiendo la bitácora de ejecución, la fase lunar en la que se realizó y listando los miembros/voluntarios participantes con sus respectivas horas trabajadas para su posterior auditoría y acreditación de puntos.`
* **Dependencias:** `Depende de SA-CU10 (Saberes ancestrales), CM-CU04 (Miembros) y US-CU01`

### Precondiciones
1. `El Responsable de Comunidad está autenticado en el sistema.`
2. `La comunidad cuenta con un huerto activo y existen miembros registrados en la comunidad.`
3. `El hito/saber ancestral seleccionado está activo en la tabla saberes_ancestrales.`

### Postcondiciones
1. `Se crea un registro en actividades_ancestrales_msp con estado_verificacion = 'PENDIENTE', numero_intento = 1 y huerto_id = huerto de la comunidad del usuario.`
2. `Se insertan los participantes asociados en actividad_msp_participantes.`
3. `Queda disponible en la bandeja del Técnico MSP para su revisión (SA-CU06).`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** accede a la ruta privada `"/actividades-ancestrales/nueva"` desde el panel de gestión.
2. El **Sistema** presenta el formulario estructurado con los componentes `Selector`, `CampoTexto`, `Tabla` y `Boton`:
   - `Hito / Saber Ancestral` (`saberAncestralId`: selector con los pasos de la ruta MSP).
   - `Fecha de Realización` (`fechaRealizacion`: selector de fecha).
   - `Fase Lunar de Ejecución` (`faseLunarEjecucion`: selector con `YANA_KILLA_OSCURA`, `WINAK_KILLA_CRECIENTE`, `PURA_KILLA_LLENA`, `WANUK_KILLA_MENGUANTE`).
   - `Descripción de la Bitácora` (`descripcionBitacora`: área de texto detallando el ritual o faena).
   - `Evidencia Fotográfica` (`evidenciaFotoUrl`: URL obligatoria de la fotografía que prueba la actividad).
   - `Evidencia de Video` (`evidenciaVideoUrl`: URL opcional).
   - `Nómina de Participantes`: Selector multi-miembro con campo de `Horas Trabajadas` por cada miembro voluntario (componente `Tabla`).
3. El **Responsable de Comunidad** completa los datos y pulsa **`"Enviar a Revisión MSP"`**.
4. El **Sistema** despacha la petición `POST /api/actividades-ancestrales` enviando el DTO `RegistrarActividadAncestralRequest`.
5. El **Sistema** valida que haya al menos 1 participante con horas > 0, asocia la comunidad y huerto del usuario, y persiste la actividad con estado `PENDIENTE`.
6. El **Sistema** retorna el código HTTP `201 Created` con el DTO `ActividadAncestralResponse`.
7. El **Sistema** muestra la notificación de éxito (`Notificacion`) indicando que la actividad pasará a revisión del Técnico MSP.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Sin participantes registrados (HTTP 400 Bad Request):**
  - **Condición:** Si la lista de participantes está vacía.
  - **Comportamiento:** El Sistema rechaza el registro indicando: `"Debe registrar al menos un miembro participante en la actividad ancestral"`.
* **4b. Evidencia fotográfica faltante (HTTP 400 Bad Request):**
  - **Condición:** Si `evidenciaFotoUrl` está vacía o no tiene formato de URL válido.
  - **Comportamiento:** Notifica: `"La fotografía de evidencia es obligatoria para la revisión del MSP"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (formulario en flujo vertical optimizado para registro en campo con captura/carga de fotos desde la cámara del celular).
  - Subtabla de participantes adaptable como lista apilada de tarjetas en móvil para facilitar el ingreso de horas.
  - Botones y selectores con altura táctil mínima de 44px, adaptándose con fluidez a pantallas grandes.
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/actividades-ancestrales/`):**
  - `FormularioActividadAncestral.tsx`: Formularios con `Selector`, `CampoTexto`, `Boton`.
  - `ListaParticipantesHoras.tsx`: Subtabla dinámica para agregar voluntarios y registrar horas (`Tabla`, `Boton`).
  - `actividadesAncestralesApi.ts`: Función `registrarActividadAncestral(data: RegistrarActividadAncestralRequest)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `NuevaActividadAncestralPage.tsx` registrada bajo `/actividades-ancestrales/nueva`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ActividadAncestralController.java`):**
  - `POST /api/actividades-ancestrales` -> Retorna `201 Created` con `ActividadAncestralResponse`. Protegido con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `RegistrarActividadAncestralRequest.java` (`record` con: `@NotNull Long saberAncestralId`, `@NotNull LocalDate fechaRealizacion`, `@NotNull FaseLunar faseLunarEjecucion`, `@NotBlank String descripcionBitacora`, `@NotBlank String evidenciaFotoUrl`, `String evidenciaVideoUrl`, `@NotEmpty List<ParticipanteActividadDto> participantes`).
  - `ParticipanteActividadDto.java` (`record` con: `@NotNull Long miembroId`, `@NotNull @Min(1) @Max(24) Integer horasTrabajadas`).
  - `ActividadAncestralResponse.java` (`record` con: `Long id`, `Long comunidadId`, `String tituloSaber`, `EstadoVerificacion estado`, `Integer numeroIntento`, `LocalDate fechaRealizacion`, `List<ParticipanteActividadDto> participantes`).
- [x] **Service (`com.huertos.service.ActividadAncestralService.java` y `com.huertos.service.impl.ActividadAncestralServiceImpl.java`):**
  - Método `@Transactional ActividadAncestralResponse registrarActividadGrande(Long usuarioResponsableId, RegistrarActividadAncestralRequest request)`.
- [x] **Mapper (`com.huertos.mapper.ActividadAncestralMapper.java`):**
  - Método `toActividadAncestralResponse(ActividadAncestralMsp entidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `actividades_ancestrales_msp` y `actividad_msp_participantes` en `V6__crear_saberes_y_actividades_msp.sql` (ya existen; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.ActividadAncestralMsp.java`, `ActividadMspParticipante.java`, `EstadoVerificacion.java`):**
  - Relación `@OneToMany` entre actividad y participantes con cascade persist.
- [x] **Repository (`com.huertos.repository.ActividadAncestralMspRepository.java`):**
  - Interfaz `JpaRepository<ActividadAncestralMsp, Long>`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ActividadAncestralServiceImplTest.java`):** Comprueba guardado con participantes y estado inicial `PENDIENTE`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ActividadAncestralControllerTest.java`):** Verifica código `201 Created` ante request válido y `400 Bad Request` si la lista de participantes está vacía.
- [x] **Prueba de Componente Frontend (`frontend/src/features/actividades-ancestrales/FormularioActividadAncestral.test.tsx`):** Prueba render de campos, agregación de participantes en la lista y envío de la petición.
- [x] **Criterio de Aceptación Mobile First:** El registro de actividad y adición de voluntarios debe ser ágil y cómodo en dispositivo móvil en una sola columna sin deformación de elementos visuales.

