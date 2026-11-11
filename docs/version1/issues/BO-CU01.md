---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "BO-CU01: Registrar ingreso de material vegetal"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `BO-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite al Responsable de Comunidad registrar la entrada o ingreso de nuevo material vegetal (semillas, estacas, esquejes o plántulas) al stock de la bodega de su comunidad, asociándolo a una planta del catálogo oficial.`
* **Dependencias:** `Depende de CP-CU01 (Catálogo de plantas) y US-CU01 (Autenticación)`

### Precondiciones
1. `El Responsable de Comunidad ha iniciado sesión y cuenta con el rol RESPONSABLE_COMUNIDAD.`
2. `La comunidad posee un huerto activo y la planta a ingresar existe en la tabla plantas.`

### Postcondiciones
1. `Se inserta o actualiza el registro correspondiente en la tabla bodega_material_vegetal.`
2. `El inventario de la comunidad queda disponible para futuras siembras.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** ingresa a la ruta privada `"/bodega/ingreso-material"` desde el menú lateral.
2. El **Sistema** despliega el formulario de ingreso utilizando los componentes `Selector`, `CampoTexto` y `Boton`:
   - `Planta del Catálogo` (`plantaId`: selector con búsqueda de plantas activas).
   - `Tipo de Material` (`tipoMaterial`: selector con valores `SEMILLAS`, `ESTACAS`, `ESQUEJES`, `PLANTULAS`).
   - `Cantidad Ingresada` (`cantidad`: numérico positivo con hasta 2 decimales, obligatorio; admite decimales porque la unidad puede ser `GRAMOS`).
   - `Unidad de Medida` (`unidadMedida`: selector con valores `UNIDADES`, `GRAMOS`, `MANOJOS`).
   - `Lote u Origen` (`loteOrigen`: texto opcional, ej. "Donación vivero local").
   - `Fecha de Ingreso` (`fechaIngreso`: selector de fecha, obligatorio).
3. El **Responsable de Comunidad** completa los datos y pulsa el botón **`"Registrar Ingreso"`**.
4. El **Sistema** realiza una petición `POST /api/bodega/material-vegetal` enviando el DTO `RegistrarMaterialVegetalRequest`.
5. El **Sistema** valida que la cantidad sea mayor a cero, asocia la comunidad del usuario autenticado y persiste el registro en `bodega_material_vegetal`.
6. El **Sistema** retorna el código HTTP `201 Created` con el DTO `MaterialVegetalResponse`.
7. El **Sistema** muestra la notificación de éxito con el componente `Notificacion` y redirige al listado de inventario.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Datos de entrada inválidos (HTTP 400 Bad Request):**
  - **Condición:** Si `cantidad` es menor o igual a 0 (`@Positive`), o los campos obligatorios están ausentes.
  - **Comportamiento:** El Sistema rechaza la petición y muestra con el componente `Notificacion`: `"La cantidad debe ser mayor a cero"`.
* **5a. Planta no encontrada (HTTP 404 Not Found):**
  - **Condición:** Si el `plantaId` no existe en la tabla `plantas`.
  - **Comportamiento:** Lanza `RecursoNoEncontradoException` y muestra: `"La planta seleccionada no existe en el catálogo"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (formulario en una sola columna vertical para viewports desde 360px).
  - Elementos táctiles optimizados (mínimo 44px de altura táctil en botones y campos, sin desbordamiento horizontal `overflow-x`).
  - Escalado responsivo progresivo a tablets y pantallas de escritorio.
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/bodega/`):**
  - `FormularioIngresoMaterial.tsx`: Usa `CampoTexto`, `Selector`, `Boton` y `Notificacion`.
  - `bodegaApi.ts`: Función `registrarMaterialVegetal(data: RegistrarMaterialVegetalRequest)` usando `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `IngresoMaterialPage.tsx` registrada en `App.tsx` bajo la ruta `/bodega/ingreso-material`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.BodegaController.java`):**
  - `POST /api/bodega/material-vegetal` -> Retorna `201 Created` con `MaterialVegetalResponse`. Endpoint asegurado con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `RegistrarMaterialVegetalRequest.java` (`record` con: `@NotNull Long plantaId`, `@NotNull TipoMaterialVegetal tipoMaterial`, `@NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal cantidad`, `@NotNull UnidadMedida unidadMedida`, `String loteOrigen`, `@NotNull LocalDate fechaIngreso`).
  - `MaterialVegetalResponse.java` (`record` con: `Long id`, `Long comunidadId`, `Long plantaId`, `String nombreComunPlanta`, `String nombreCientificoPlanta`, `TipoMaterialVegetal tipoMaterial`, `BigDecimal cantidadDisponible`, `UnidadMedida unidadMedida`, `String loteOrigen`, `LocalDate fechaIngreso`); es el mismo DTO que usa `BO-CU02`, no crear otro distinto.
- [x] **Service (`com.huertos.service.BodegaService.java` y `com.huertos.service.impl.BodegaServiceImpl.java`):**
  - Método `MaterialVegetalResponse registrarIngresoMaterial(Long comunidadId, RegistrarMaterialVegetalRequest request)`.
- [x] **Mapper (`com.huertos.mapper.BodegaMapper.java`):**
  - Método `toMaterialVegetalResponse(BodegaMaterialVegetal entidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `bodega_material_vegetal` en `V4__crear_huertos_bodega_y_siembras.sql` (ya existe; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.BodegaMaterialVegetal.java`, `TipoMaterialVegetal.java`, `UnidadMedida.java`):**
  - Mapeo de `@Entity` sobre `bodega_material_vegetal`.
- [x] **Repository (`com.huertos.repository.BodegaMaterialVegetalRepository.java`):**
  - Método `List<BodegaMaterialVegetal> findByComunidadIdAndActivoTrue(Long comunidadId);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/BodegaServiceImplTest.java`):** Verifica inserción correcta y rechazo cuando la planta no existe.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/BodegaControllerTest.java`):** Con `@WebMvcTest`, verifica `201 Created` con rol `RESPONSABLE_COMUNIDAD` y `403 Forbidden` sin autenticación.
- [x] **Prueba de Componente Frontend (`frontend/src/features/bodega/FormularioIngresoMaterial.test.tsx`):** Probando envío del formulario, validación de cantidad positiva y notificación.
- [x] **Criterio de Aceptación Mobile First:** El formulario debe ser plenamente operable desde pantalla móvil en una sola columna vertical, con campos táctiles cómodos y sin desbordamiento horizontal.

