---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "BO-CU09: Dar de baja material vegetal (con causa de fracaso)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `BO-CU09`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite al Responsable de Comunidad registrar la pérdida o desecho de material vegetal almacenado o estropeado, descontándolo del inventario y registrando obligatoriamente la causa técnica o climática del fracaso para estadísticas agroecológicas del MSP.`
* **Dependencias:** `Depende de BO-CU01 y BO-CU02`

### Precondiciones
1. `El usuario cuenta con sesión activa y rol RESPONSABLE_COMUNIDAD.`
2. `El material vegetal a dar de baja existe en bodega_material_vegetal con stock disponible mayor a cero.`

### Postcondiciones
1. `Se inserta un registro histórico en la tabla bajas_material_vegetal con la causa de fracaso.`
2. `Se descuenta la cantidad_baja del campo cantidad_disponible en bodega_material_vegetal.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** visualiza el inventario en `"/bodega/inventario"` y pulsa la acción **`"Dar de Baja"`** en la fila del material afectado.
2. El **Sistema** abre el componente `Modal` con el formulario de baja conteniendo:
   - Nombre de la planta y stock disponible actual (solo lectura).
   - `Cantidad a dar de baja` (`cantidadBaja`: entero positivo menor o igual al stock, obligatorio).
   - `Causa del Fracaso` (`causaFracaso`: componente `Selector` con opciones: `HELADA_CLIMA_EXTREMO`, `PLAGA_ENFERMEDAD`, `SEQUIA_FALTA_RIEGO`, `SUELO_INADECUADO`, `DANIO_EXTERNO`, `CADUCIDAD_PUDRICION`).
   - `Observaciones / Detalle` (`observacion`: componente `CampoTexto`, opcional, máx 300 caracteres).
   - Botón **`"Confirmar Baja"`** (componente `Boton`).
3. El **Responsable de Comunidad** ingresa los datos y confirma la operación.
4. El **Sistema** despacha una petición `POST /api/bodega/material-vegetal/{id}/bajas` con el DTO `RegistrarBajaMaterialRequest`.
5. El **Sistema** valida que la cantidad a dar de baja no supere la disponibilidad, descuenta las existencias de `bodega_material_vegetal` y registra el evento en `bajas_material_vegetal`.
6. El **Sistema** responde con código HTTP `201 Created` y el DTO `BajaMaterialResponse`.
7. El **Sistema** cierra el modal, muestra la confirmación mediante `Notificacion` y refresca la tabla de inventario.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Cantidad de baja superior al stock disponible (HTTP 400 Bad Request):**
  - **Condición:** Si `cantidadBaja > cantidad_disponible`.
  - **Comportamiento:** El Sistema deniega la baja y notifica: `"La cantidad a dar de baja supera el stock disponible en bodega"`.
* **4b. Causa de fracaso ausente o inválida (HTTP 400 Bad Request):**
  - **Condición:** No se seleccionó un valor válido del enumerado `CausaFracaso`.
  - **Comportamiento:** Muestra: `"Debe seleccionar una causa de fracaso válida para el registro de la baja"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (modal responsivo tipo bottom-sheet o pantalla completa en móvil para facilitar el ingreso de datos con teclado virtual).
  - Controles táctiles accesibles (altura mínima de 44px en selectores y botones de confirmación).
  - Escalado responsivo centrado en escritorio.
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/bodega/`):**
  - `ModalBajaMaterial.tsx`: Modal interactivo usando `Modal`, `CampoTexto`, `Selector`, `Boton` y `Notificacion`.
  - `bodegaApi.ts`: Función `registrarBajaMaterial(id: number, data: RegistrarBajaMaterialRequest)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - Integrado dentro de `InventarioBodegaPage.tsx` (`/bodega/inventario`).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.BodegaController.java`):**
  - `POST /api/bodega/material-vegetal/{id}/bajas` -> Retorna `201 Created` con `BajaMaterialResponse`. Protegido con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `RegistrarBajaMaterialRequest.java` (`record` con: `@NotNull @Positive Integer cantidadBaja`, `@NotNull CausaFracaso causaFracaso`, `String observacion`).
  - `BajaMaterialResponse.java` (`record` con: `Long id`, `Long bodegaMaterialId`, `Integer cantidadBaja`, `CausaFracaso causaFracaso`, `String observacion`, `LocalDateTime fechaBaja`).
- [x] **Service (`com.huertos.service.BodegaService.java` y `com.huertos.service.impl.BodegaServiceImpl.java`):**
  - Método `@Transactional BajaMaterialResponse registrarBajaMaterial(Long bodegaMaterialId, RegistrarBajaMaterialRequest request)`.
- [x] **Mapper (`com.huertos.mapper.BodegaMapper.java`):**
  - Método `toBajaMaterialResponse(BajaMaterialVegetal entidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `bodega_material_vegetal` y `bajas_material_vegetal` en `V4__crear_huertos_bodega_y_siembras.sql` (ya existen; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.BajaMaterialVegetal.java` y `CausaFracaso.java`):**
  - Mapeo de la entidad con clave foránea hacia `BodegaMaterialVegetal`.
- [x] **Repository (`com.huertos.repository.BajaMaterialVegetalRepository.java`):**
  - Interfaz `JpaRepository<BajaMaterialVegetal, Long>`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/BodegaServiceImplTest.java`):** Comprueba decremento exacto del stock y persistencia de la baja con causa de fracaso.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/BodegaControllerTest.java`):** Verifica respuesta `201 Created` al enviar payload válido y `400 Bad Request` ante datos erróneos.
- [x] **Prueba de Componente Frontend (`frontend/src/features/bodega/ModalBajaMaterial.test.tsx`):** Verifica apertura del modal, validaciones visuales de formulario y callback de éxito.
- [x] **Criterio de Aceptación Mobile First:** El modal o diálogo debe ser totalmente operable en pantalla móvil sin solaparse con el teclado virtual ni cortar los botones de acción.

