---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "SA-CU06: Verificar evidencias de actividad ancestral"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `SA-CU06`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Rol que ve la pantalla:** `TECNICO_MSP`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite al Técnico del MSP revisar la bitácora y evidencias fotográficas/audiovisuales de una actividad ancestral enviada por una comunidad, y dictaminar su verificación aprobándola (lo que dispara la acreditación de puntos e insignias vía GM-CU01), observándola para corrección (SA-CU05) o rechazándola definitivamente.`
* **Dependencias:** `Depende de SA-CU04 y desencadena GM-CU01 al ser aprobada`

### Precondiciones
1. `El usuario está autenticado con rol TECNICO_MSP.`
2. `La actividad ancestral se encuentra en estado 'PENDIENTE'.`

### Postcondiciones
1. `Se actualiza el registro en actividades_ancestrales_msp con el nuevo estado, id del revisor y fecha de verificación.`
2. `Si es APROBADA, dispara de inmediato el caso de uso GM-CU01 (Otorgar puntos a comunidad, huerto y miembros) y GM-CU03 (Desbloquear insignia oficial).`
3. `Si es OBSERVADA_CORREGIR o RECHAZADA_DEFINITIVA, se persiste la justificación en observacion_msp.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Técnico MSP** accede a la bandeja de auditoría en `"/msp/revision-actividades"`.
2. El **Sistema** lista las actividades con estado `PENDIENTE` utilizando el componente `Tabla` con columnas: Comunidad, Saber Ancestral, Fecha, Intento y botón de revisión.
3. El **Técnico MSP** selecciona una actividad y pulsa **`"Auditar Evidencia"`**.
4. El **Sistema** abre la vista de auditoría detallada mostrando la descripción de la bitácora, vista previa de la fotografía, enlace al video y la lista de voluntarios participantes.
5. El **Técnico MSP** selecciona la decisión:
   - **Aprobar** (opción por defecto cuando la evidencia cumple los criterios agroecológicos).
   - **Observar para corrección** (requiere redactar la observación en `CampoTexto`).
   - **Rechazar definitivamente** (requiere justificación técnica).
6. El **Técnico MSP** pulsa el botón **`"Emitir Dictamen"`** (`Boton`).
7. El **Sistema** realiza la petición `POST /api/msp/actividades-ancestrales/{id}/verificar` con el DTO `VerificarActividadMspRequest`.
8. El **Sistema** actualiza la actividad. Al ser aprobada, calcula los puntos de comunidad según el intento (intento 1 = 100% de puntos base; intento 2 = porcentaje proporcional), registra el `verificado_por_id` y delega al servicio de gamificación la acreditación.
9. El **Sistema** responde con HTTP `200 OK` y el DTO `ResultadoVerificacionResponse`.
10. El **Sistema** despliega una notificación de confirmación (`Notificacion`) y actualiza la bandeja de pendientes.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **5a. Observación requerida no ingresada (HTTP 400 Bad Request):**
  - **Condición:** Si la decisión es `OBSERVADA_CORREGIR` o `RECHAZADA_DEFINITIVA` y el campo `observacion` está en blanco.
  - **Comportamiento:** El Sistema bloquea la acción notificando: `"Debe ingresar una observación detallada para justificar la observación o rechazo"`.
* **7a. Actividad ya dictaminada (HTTP 409 Conflict):**
  - **Condición:** Si otro técnico ya auditó la actividad previamente.
  - **Comportamiento:** Retorna error indicando que la actividad ya no se encuentra en estado pendiente.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (bandeja de pendientes como tarjetas resumidas en móvil con estado visual en badge, visor de auditoría apilando fotos, bitácora y dictamen en orden vertical).
  - Botones de acción (Aprobar, Observar, Rechazar) con tamaño táctil accesible (mínimo 44px de altura) dispuestos ergonómicamente en móvil.
  - Escalado responsivo progresivo a panel dividido (master-detail) en pantallas de escritorio.
- [x] **Rol que ve la pantalla:** `TECNICO_MSP`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/msp-revision/`):**
  - `BandejaRevisionActividades.tsx`: Tabla de pendientes con `Tabla`, `Insignia`, `Boton`.
  - `DetalleAuditoriaActividad.tsx`: Panel con visor de foto, bitácora, nómina de participantes y formulario de veredicto (`CampoTexto`, `Selector`, `Boton`).
  - `mspRevisionApi.ts`: Funciones `listarPendientes()` y `verificarActividad(id: number, data: VerificarActividadMspRequest)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `RevisionActividadesPage.tsx` registrada bajo `/msp/revision-actividades`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.MspRevisionController.java`):**
  - `POST /api/msp/actividades-ancestrales/{id}/verificar` -> Retorna `200 OK` con `ResultadoVerificacionResponse`. Protegido con `@PreAuthorize("hasRole('TECNICO_MSP')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `VerificarActividadMspRequest.java` (`record` con: `@NotNull EstadoVerificacion nuevoEstado`, `String observacion`).
  - `ResultadoVerificacionResponse.java` (`record` con: `Long actividadId`, `EstadoVerificacion estado`, `Integer puntosComunidadAcreditados`, `LocalDateTime fechaVerificacion`).
- [x] **Service (`com.huertos.service.MspRevisionService.java` y `com.huertos.service.impl.MspRevisionServiceImpl.java`):**
  - Método `@Transactional ResultadoVerificacionResponse dictaminarActividad(Long tecnicoId, Long actividadId, VerificarActividadMspRequest request)`.
- [x] **Invocación interna:** Inyecta `GamificacionService.procesarPuntosActividadMsp(actividadId)` cuando el estado es `APROBADA`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `actividades_ancestrales_msp` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA (`com.huertos.model.ActividadAncestralMsp.java`):**
  - Campos `estadoVerificacion`, `observacionMsp`, `puntosAcreditarComunidad`, `verificadoPorId`, `fechaVerificacion`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MspRevisionServiceImplTest.java`):** Comprueba cambio de estado a `APROBADA` y disparo del método de gamificación; prueba que exige observación ante rechazo.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/MspRevisionControllerTest.java`):** Verifica acceso exclusivo para `TECNICO_MSP` y denegación con `403 Forbidden` a otros roles.
- [x] **Prueba de Componente Frontend (`frontend/src/features/msp-revision/DetalleAuditoriaActividad.test.tsx`):** Prueba renderizado de imágenes, selección de veredicto y envío exitoso.
- [x] **Criterio de Aceptación Mobile First:** El técnico debe poder auditar fotografías, bitácoras y emitir su veredicto fluidamente desde un dispositivo móvil con excelente legibilidad y sin desbordamiento.

