---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "SA-CU05: Corregir y reenviar evidencias"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `SA-CU05`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite al Responsable de Comunidad consultar las observaciones emitidas por el Técnico MSP sobre una actividad ancestral observada, corregir la descripción o evidencias fotográficas/video y reenviar la postulación incrementando el número de intento (segundo intento) para una nueva evaluación.`
* **Dependencias:** `Extiende de SA-CU04 y depende de SA-CU06 (Verificación previa MSP)`

### Precondiciones
1. `El Responsable de Comunidad está autenticado.`
2. `La actividad ancestral se encuentra en estado OBSERVADA_CORREGIR.`

### Postcondiciones
1. `Se actualizan los enlaces de evidencia y/o bitácora en actividades_ancestrales_msp.`
2. `El estado_verificacion cambia nuevamente a 'PENDIENTE'.`
3. `Se incrementa el contador numero_intento (por ejemplo, a intento 2).`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** ingresa al panel de actividades en `"/actividades-ancestrales"` y localiza la actividad con badge `Insignia` en estado `"OBSERVADA"`.
2. El **Responsable de Comunidad** pulsa el botón **`"Subsanar Evidencias"`**.
3. El **Sistema** abre la pantalla de corrección mostrando de forma destacada con el componente `Notificacion`:
   - El mensaje/observación del Técnico MSP (`observacion_msp`).
   - El número de intento actual (`numero_intento`).
4. El **Responsable de Comunidad** actualiza la URL de la evidencia fotográfica (`evidenciaFotoUrl`), el enlace de video y/o complementa la bitácora (`CampoTexto`).
5. El **Responsable de Comunidad** pulsa **`"Reenviar Evidencias Corregidas"`** (`Boton`).
6. El **Sistema** envía la petición `PUT /api/actividades-ancestrales/{id}/reenviar` con el DTO `ReenviarEvidenciasRequest`.
7. El **Sistema** verifica que la actividad esté en estado `OBSERVADA_CORREGIR`, incrementa `numero_intento`, asigna estado `PENDIENTE` y limpia o archiva la observación previa.
8. El **Sistema** retorna el código HTTP `200 OK` con el DTO `ActividadAncestralResponse`.
9. El **Sistema** notifica el reenvío exitoso y redirige al listado.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. La actividad no está en estado observado (HTTP 409 Conflict):**
  - **Condición:** Si la actividad está en estado `APROBADA` o `RECHAZADA_DEFINITIVA`.
  - **Comportamiento:** El Sistema rechaza la modificación: `"Solo es posible reenviar evidencias para actividades en estado OBSERVADA_CORREGIR"`.
* **4a. Límite de intentos superado (HTTP 400 Bad Request):**
  - **Condición:** Si el hito ya cumplió los intentos máximos permitidos por la normativa técnica MSP (intento > 2).
  - **Comportamiento:** Notifica: `"Se ha alcanzado el número máximo de reenvíos permitidos para esta actividad"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (panel de observaciones en bloque superior legible en pantalla móvil seguido del formulario de reenvío en columna única).
  - Carga y reemplazo de foto optimizado para uso táctil directo desde la galería o cámara del dispositivo.
  - Escalado responsivo progresivo a pantallas de mayor tamaño.
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/actividades-ancestrales/`):**
  - `FormularioSubsanarEvidencias.tsx`: Muestra la observación previa en un panel de alerta y permite modificar evidencias con `CampoTexto`, `Boton` y `Notificacion`.
  - `actividadesAncestralesApi.ts`: Función `reenviarEvidencias(id: number, data: ReenviarEvidenciasRequest)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `SubsanarEvidenciaPage.tsx` registrada bajo `/actividades-ancestrales/:id/subsanar`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ActividadAncestralController.java`):**
  - `PUT /api/actividades-ancestrales/{id}/reenviar` -> Retorna `200 OK` con `ActividadAncestralResponse`. Protegido con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `ReenviarEvidenciasRequest.java` (`record` con: `@NotBlank String descripcionBitacora`, `@NotBlank String evidenciaFotoUrl`, `String evidenciaVideoUrl`).
- [x] **Service (`com.huertos.service.ActividadAncestralService.java` y `com.huertos.service.impl.ActividadAncestralServiceImpl.java`):**
  - Método `@Transactional ActividadAncestralResponse reenviarEvidencias(Long id, ReenviarEvidenciasRequest request)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `actividades_ancestrales_msp` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA (`com.huertos.model.ActividadAncestralMsp.java`):**
  - Actualización de campos `numeroIntento`, `estadoVerificacion`, `evidenciaFotoUrl` y `descripcionBitacora`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ActividadAncestralServiceImplTest.java`):** Verifica incremento de `numero_intento` a 2 y cambio de estado a `PENDIENTE`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ActividadAncestralControllerTest.java`):** Verifica respuesta `200 OK` ante reenvío válido y `409 Conflict` si la actividad no estaba observada.
- [x] **Prueba de Componente Frontend (`frontend/src/features/actividades-ancestrales/FormularioSubsanarEvidencias.test.tsx`):** Verifica visualización de observaciones del técnico y reenvío de formulario.
- [x] **Criterio de Aceptación Mobile First:** Lectura clara de la observación y sustitución de evidencias en pantalla móvil sin solapamientos ni scroll horizontal.

