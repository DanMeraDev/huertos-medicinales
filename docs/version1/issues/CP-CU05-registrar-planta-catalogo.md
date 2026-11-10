---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU05: Registrar planta en el catálogo"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU05`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Descripción:** El sistema deberá permitir al Técnico MSP incorporar una nueva planta medicinal al catálogo mediante un formulario responsivo: en smartphones adopta un asistente paso a paso (*Wizard de 3 fases*) para evitar la sobrecarga vertical, mientras que en computadoras de escritorio (PC) se presenta como un **formulario integral de 2 columnas agrupado en tarjetas temáticas**, con soporte de arrastrar y soltar (*drag & drop*) para fotografías y botones de acción al pie.
* **Dependencias:** Incluye obligatoriamente a `CP-CU08` (Adjuntar fotografía de la planta).

### Precondiciones
1. El usuario ha iniciado sesión con el rol `TECNICO_MSP`.
2. El endpoint de creación está protegido mediante `@PreAuthorize("hasRole('TECNICO_MSP')")`.

### Postcondiciones
1. El sistema insertará un nuevo registro en la tabla `plantas` con `activa = TRUE`.
2. La nueva planta estará inmediatamente disponible para consultas públicas (`CP-CU01`) y siembras comunitarias (`HR-CU01`).

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Tarjetas de formulario: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Botones principales: `var(--color-primario)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Asistente Wizard en 3 pasos con barra de progreso superior fija:
    - *Paso 1:* Identificación y fotografía.
    - *Paso 2:* Parámetros agronómicos (botones estáticos de cualidad térmica, ciclo y campos de altitud).
    - *Paso 3:* Usos medicinales y dosificación MSP.
  - Sticky bottom bar con botones *"Atrás"* y *"Siguiente"* / *"Guardar Planta"*.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Formulario Integral en 2 Columnas (Grid de 12 Columnas):**
    - En pantallas amplias se elimina la división por pasos para permitir una visión global y rápida captura de datos:
      - **Columna Izquierda (Datos Botánicos, Agronómicos y Multimedia):**
        - Tarjeta 1: *Taxonomía y Nombres* (campos alineados en pares: Nombre común y Nombre científico; Nombre kichwa y Familia botánica).
        - Tarjeta 2: *Zona de Fotografía (`CP-CU08`)* con soporte de arrastrar y soltar archivos (*drag & drop*) desde el explorador del sistema operativo.
        - Tarjeta 3: *Condiciones de Cultivo* (Cualidad térmica en botones segmentados, ciclo de cultivo, altitud mínima y máxima en paralelo, y chips de regiones).
      - **Columna Derecha (Ficha Médica y Dosificación):**
        - Tarjeta 4: *Usos Terapéuticos MSP* (Selector de categoría y editor de afecciones).
        - Tarjeta 5: *Modos de Preparación y Dosificación* (recetas tradicionales de infusión, cocción, etc.).
        - Tarjeta 6: *Contraindicaciones* con caja de estilo preventivo.
    - **Barra de Acciones al Pie (Footer):** Alineada a la derecha al final del formulario: Botón **`"Cancelar"`** (`Boton` secundario) y botón destacado **`"Guardar Planta en Catálogo"`** (`Boton` primario, 48px alto).

### 2.2. Flujo Interactivo Paso a Paso
1. El **Técnico MSP** accederá a la ruta privada `"/admin/plantas/nueva"`.
2. El **Sistema** presentará la vista adecuada: asistente de 3 pasos en móvil o formulario completo en 2 columnas en PC.
3. El **Técnico MSP** completará los campos requeridos y cargará la fotografía correspondiente.
4. El **Técnico MSP** pulsará el botón de guardado.
5. El **Sistema** frontend enviará la petición HTTP `POST /api/msp/plantas` con `CrearPlantaRequest` y token JWT.
6. El **Sistema** backend comprobará rol `TECNICO_MSP`, unicidad de nombre científico y persistirá la planta.
7. El **Sistema** backend responderá con código HTTP `201 Created`.
8. El **Sistema** frontend mostrará una notificación flotante con `Notificacion`: *"Planta registrada exitosamente"* y redirigirá al catálogo.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Validación de campos obligatorios:**
  - **Condición:** Si faltan datos obligatorios.
  - **Comportamiento:** Los campos vacíos adquieren borde en `var(--color-error)` y se muestra el mensaje de error.
* **6a. Nombre científico duplicado (HTTP 409 Conflict):**
  - **Condición:** Si la especie ya está registrada.
  - **Comportamiento:** El campo de nombre científico se resalta con mensaje de error: *"El nombre científico ya existe en el catálogo"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Nombres botánicos, familia, altitud y textos médicos.
  - `Selector`: Categoría médica y origen.
  - `Boton`: Botones de navegación y guardado.
  - `Notificacion`: Alertas de confirmación o error.
- [x] **Feature (`frontend/src/features/catalogo-admin/`):**
  - `FormularioCrearPlanta.tsx`: Componente responsivo que renderiza Wizard en móvil o cuadrícula de 2 columnas en PC.
  - `plantasAdminApi.ts`: Función `registrarPlanta(datos: CrearPlantaRequest)`.
- [x] **Página:** `AdminCrearPlantaPage.tsx` bajo `/admin/plantas/nueva`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaAdminController.java`):**
  - `POST /api/msp/plantas` con `@PreAuthorize("hasRole('TECNICO_MSP')")`. Retorna `201 Created`.
- [x] **DTOs:** `CrearPlantaRequest.java` (`record`).
- [x] **Service:** `PlantaAdminService.java`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tabla `plantas`:** Clave única `uk_plantas_cientifico`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/PlantaAdminServiceImplTest.java`):** Valida persistencia y unicidad.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaAdminControllerTest.java`):** Retorna `201 Created` para `TECNICO_MSP`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-admin/FormularioCrearPlanta.test.tsx`):** Valida modo wizard en móvil y formulario 2 columnas en desktop.
