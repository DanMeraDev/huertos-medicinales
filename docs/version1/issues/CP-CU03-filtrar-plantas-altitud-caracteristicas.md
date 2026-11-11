---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU03: Filtrar plantas por altitud y características"
labels: ["caso-de-uso", "back", "front", "importante"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)` *(Heredan: Responsable de Comunidad y Técnico MSP)*
* **Descripción:** El sistema deberá permitir al usuario filtrar las plantas del catálogo mediante controles adaptables: en smartphones se presentan dentro de un panel deslizable inferior (*Bottom Sheet*), mientras que en computadoras de escritorio (PC) se despliegan como una **barra lateral izquierda fija (*Sidebar de 280px*)** siempre visible junto a la cuadrícula de productos.
* **Dependencias:** Extiende a `CP-CU01` (Consultar catálogo de plantas).

### Precondiciones
1. El usuario se encuentra en el catálogo (`CP-CU01`).
2. El endpoint de filtrado es accesible públicamente.

### Postcondiciones
1. El sistema no modificará el estado de la base de datos (operación de solo lectura).
2. El sistema actualizará los resultados en la lista móvil o en la cuadrícula de escritorio según los filtros aplicados.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo de panel/sidebar: `var(--color-superficie)`.
  - Bordes y separadores: `var(--color-borde)` con radio `var(--radio-md)`.
  - Botón activo: `var(--color-primario)` con texto `var(--color-texto-inverso)`.
  - Botón inactivo: `var(--color-superficie-alt)` con texto `var(--color-texto)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Botón flotante/fijo **`"Filtros"`** con badge de conteo.
  - Al pulsar, abre un **Bottom Sheet modal** (85% altura de pantalla) con botones segmentados estáticos (`[ Todas ]` `[ Caliente ]` `[ Fría ]`), campo numérico de altitud y chips de categorías con botón anclado **`"Aplicar Filtros"`**.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Sidebar Lateral Izquierdo Fijo (280px de ancho):**
    - Se elimina el Bottom Sheet y el botón flotante.
    - Los filtros se muestran permanentemente en una **columna fija a la izquierda (*sticky sidebar*)**:
      - Título: *"Filtros"* con enlace superior táctil/clic: *"Limpiar todo"*.
      - Sección 1: *Cualidad Térmica* con botones segmentados horizontales.
      - Sección 2: *Rango de Altitud* con control deslizante (*slider*) y campo numérico de msnm.
      - Sección 3: *Ciclo de Cultivo* con botones estáticos (`[ Corto ]` `[ Largo ]`).
      - Sección 4: *Categoría de Uso* y *Región* mediante casillas de verificación o chips interactivos.
    - Los filtros en escritorio pueden aplicarse en tiempo real sin requerir cerrar ningún modal, actualizando la cuadrícula adyacente.

### 2.2. Flujo Interactivo Paso a Paso
1. En **Móvil**, el usuario toca el botón *"Filtros"* y se abre el Bottom Sheet. En **PC**, el usuario interactúa directamente con el sidebar izquierdo que se encuentra siempre visible.
2. El usuario selecciona la cualidad térmica, ajusta la altitud (ej. 2800 msnm) y selecciona la región.
3. El **Sistema** frontend emite la petición HTTP `GET /api/publico/plantas/filtrar?cualidad=CALIENTE&altitud=2800&region=SIERRA`.
4. El **Sistema** backend construye la consulta dinámica y responde con código HTTP `200 OK` y `List<PlantaResumenResponse>`.
5. El **Sistema** frontend actualiza la cuadrícula de tarjetas reflejando las plantas que cumplen las condiciones.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Altitud fuera de rango (0 - 6000 msnm):**
  - **Condición:** Si se ingresa una altitud inválida.
  - **Comportamiento:** El campo muestra un aviso preventivo: *"La altitud debe estar entre 0 y 6000 msnm"*.
* **5a. Sin resultados para la combinación de filtros:**
  - **Condición:** Si ninguna planta activa coincide.
  - **Comportamiento:** La cuadrícula muestra una tarjeta informativa con `Notificacion`: *"No se encontraron plantas para estos parámetros. Intente reajustar los filtros"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Boton`: Botones segmentados de opciones y botón de limpieza.
  - `CampoTexto`: Entrada numérica de altitud.
  - `Insignia`: Distintivo numérico de filtros activos.
  - `Notificacion`: Alerta de resultados vacíos.
- [x] **Componente Responsivo Compartido:**
  - `BottomSheet.tsx`: Se activa únicamente en pantallas móviles (< 768px).
  - En escritorio (>= 1024px), se renderiza como `<aside className="catalogo-sidebar">`.
- [x] **Feature (`frontend/src/features/catalogo-plantas/`):**
  - `PanelFiltrosPlantas.tsx`: Componente híbrido (Bottom Sheet en móvil / Sidebar en desktop).
  - `plantasApi.ts`: Función `filtrarPlantas(filtros: FiltrosPlantaRequest)`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaPublicoController.java`):**
  - `GET /api/publico/plantas/filtrar` -> Retorna `200 OK`.
- [x] **DTOs:** `FiltrosPlantaRequest.java` (`record`).
- [x] **Service:** `PlantaService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V3__crear_catalogo_plantas.sql`; ya existen, NO crear migraciones)
- [x] **Specification:** `PlantaSpecification.java`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Repositorio (`backend/src/test/java/com/huertos/repository/PlantaSpecificationTest.java`):** Valida combinación de criterios.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaPublicoControllerTest.java`):** Retorna `200 OK`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-plantas/PanelFiltrosPlantas.test.tsx`):** Valida renderizado en modo Bottom Sheet en mobile y como Sidebar en desktop.
