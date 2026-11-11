---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU06: Actualizar ficha de la planta"
labels: ["caso-de-uso", "back", "front", "importante"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU06`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Descripción:** El sistema deberá permitir al Técnico MSP modificar los datos de una planta medicinal mediante una interfaz responsiva: en smartphones utiliza acordeones colapsables táctiles para optimizar el espacio vertical, mientras que en computadoras de escritorio (PC) despliega **todas las secciones abiertas simultáneamente en un layout de 2 columnas amplias** para edición ágil y visión panorámica de la ficha botánica.
* **Dependencias:** Extiende opcionalmente a `CP-CU08` (actualización de fotografía).

### Precondiciones
1. El usuario posee rol `TECNICO_MSP`.
2. La planta existe en la base de datos.

### Postcondiciones
1. El sistema actualizará las columnas correspondientes en la tabla `plantas`.
2. Las consultas posteriores reflejarán los cambios.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Superficie de secciones: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Botones de acción: `var(--color-primario)` para guardar y `var(--color-secundario)` para descartar.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - 3 acordeones colapsables táctiles (Botánica, Agronomía, Medicina/Dosificación).
  - Sticky bottom bar con botones *"Descartar"* y *"Guardar Cambios"*.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Todas las Secciones Abiertas en Paralelo:**
    - Se elimina el colapso obligatorio de acordeones; las 3 áreas temáticas se organizan en **dos columnas lado a lado**:
      - Columna izquierda: Identificación botánica, reemplazo de fotografía (`CP-CU08`) y parámetros agronómicos.
      - Columna derecha: Usos medicinales, modo de preparación y contraindicaciones.
    - **Barra de Acciones al Pie (Footer):** Botones situados en la parte inferior derecha del formulario: **`"Descartar Cambios"`** (`Boton` secundario) y **`"Guardar Cambios"`** (`Boton` primario).

### 2.2. Flujo Interactivo Paso a Paso
1. El **Técnico MSP** accederá a la edición de una planta (`"/admin/plantas/{id}/editar"`).
2. El **Sistema** precargará los datos mediante `GET /api/publico/plantas/{id}`.
3. El **Técnico MSP** modificará los campos necesarios: desplegando acordeones en smartphone o navegando directamente entre columnas en PC.
4. El **Técnico MSP** pulsará el botón **`"Guardar Cambios"`** (`Boton`).
5. El **Sistema** frontend enviará la petición HTTP `PUT /api/msp/plantas/{id}` con `ActualizarPlantaRequest`.
6. El **Sistema** backend comprobará que el `nombreCientifico`, si fue cambiado, no pertenezca a otra planta con distinto ID.
7. El **Sistema** backend persistirá la actualización y responderá con código HTTP `200 OK`.
8. El **Sistema** frontend mostrará una notificación con `Notificacion`: *"Ficha técnica actualizada exitosamente"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **6a. Conflicto por nombre científico ya registrado:**
  - **Condición:** Si el nuevo nombre pertenece a otra planta.
  - **Comportamiento:** El campo resalta con `var(--color-error)` y se muestra alerta mediante `Notificacion`: *"Nombre científico ya registrado en otra planta"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Edición de textos y números.
  - `Selector`: Categoría terapéutica.
  - `Boton`: Botones de acción *"Descartar"* y *"Guardar Cambios"*.
  - `Notificacion`: Mensajes de confirmación o alerta.
- [x] **Feature (`frontend/src/features/catalogo-admin/`):**
  - `FormularioEditarPlanta.tsx`: Componente responsivo (acordeones en mobile / 2 columnas abiertas en desktop).
  - `plantasAdminApi.ts`: Función `actualizarPlanta(id: number, datos: ActualizarPlantaRequest)`.
- [x] **Página:** `AdminEditarPlantaPage.tsx`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaAdminController.java`):**
  - `PUT /api/msp/plantas/{id}` con `@PreAuthorize("hasRole('TECNICO_MSP')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `ActualizarPlantaRequest.java` (`record`) con los mismos campos y validaciones que `CrearPlantaRequest` (`CP-CU05`):
    - `nombreComun`: `String`, obligatorio (`@NotBlank`, `@Size(max = 120)`).
    - `nombreKichwa`: `String`, opcional (`@Size(max = 120)`).
    - `nombreCientifico`: `String`, obligatorio y único (`@NotBlank`, `@Size(max = 150)`).
    - `familiaBotanica`: `String`, obligatorio (`@NotBlank`, `@Size(max = 100)`).
    - `cualidadTermica`: `CualidadTermica` (`CALIENTE`, `FRIA`), obligatorio (`@NotNull`).
    - `cicloCultivo`: `CicloCultivo` (`CORTO`, `LARGO`), opcional.
    - `origen`: `OrigenPlanta` (`NATIVA`, `ENDEMICA`, `INTRODUCIDA`), opcional.
    - `altitudMinMsnm`: `Integer`, opcional (`@PositiveOrZero`).
    - `altitudMaxMsnm`: `Integer`, opcional (`@PositiveOrZero`); si vienen las dos, `altitudMinMsnm <= altitudMaxMsnm` (si no, HTTP 400).
    - `regionesDistribucion`: `String`, opcional (`@Size(max = 150)`).
    - `usosMedicinales`: `String`, obligatorio (`@NotBlank`).
    - `preparacionDosificacion`: `String`, opcional.
    - `contraindicaciones`: `String`, opcional.
    - `categorias`: `Set<CategoriaUso>`, al menos una (`@NotEmpty`); se guarda en la tabla `planta_categorias`.
    - `fotografiaUrl`: `String`, opcional (`@Size(max = 500)`); es la URL que devuelve `CP-CU08`.
  - Responde `200 OK` con `PlantaDetalleResponse` (definido en `CP-CU04`).
- [x] **Service:** `PlantaAdminService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V3__crear_catalogo_plantas.sql`; ya existen, NO crear migraciones)
- [x] **Repository:** `PlantaRepository.java` con `existsByNombreCientificoAndIdNot(String nombreCientifico, Long id)`.
- [x] **Tabla `plantas`:** al actualizar se guarda `actualizado_por_id` = id del Técnico MSP autenticado; `actualizado_en` se actualiza solo. Las categorías se reemplazan en `planta_categorias`.
- [x] **Entidad JPA:** `com.huertos.model.Planta` (ya existe; NO crear `PlantaMedicinal`). Los nombres de los campos del DTO son los mismos que los de la entidad.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/PlantaAdminServiceImplTest.java`):** Valida actualización y duplicidad.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaAdminControllerTest.java`):** Retorna `200 OK` para `TECNICO_MSP`.
