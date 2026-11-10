---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU01: Consultar catálogo de plantas"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)` *(Heredan: Responsable de Comunidad y Técnico MSP)*
* **Descripción:** El sistema deberá permitir a cualquier usuario consultar el catálogo oficial de plantas medicinales avaladas por el MSP mediante una interfaz responsiva concebida bajo el principio Mobile First, que en pantallas móviles presenta tarjetas táctiles apiladas y que en computadoras de escritorio (PC) se expande a un layout de dos columnas con barra lateral fija de filtros y cuadrícula de 3 a 4 columnas de tarjetas.
* **Dependencias:** Es extendido opcionalmente por `CP-CU02` (Búsqueda por nombre), `CP-CU03` (Filtros por altitud y características) y `CP-CU04` (Ficha técnica).

### Precondiciones
1. El sistema deberá exponer los recursos bajo la ruta pública `/api/publico/**` configurada con `.permitAll()` en `SecurityConfig.java`.
2. Existen plantas registradas en la base de datos con estado `activa = true`.

### Postcondiciones
1. El sistema no modificará el estado de la base de datos (operación de solo lectura).
2. El sistema renderizará la vista adaptada al dispositivo: columna única en móvil o cuadrícula expandida con barra lateral en escritorio.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Superficie de tarjetas: `var(--color-superficie)` con bordes `var(--color-borde)`, radio `var(--radio-md)` y sombra `var(--sombra-sm)`.
  - Distintivo térmico con `Insignia`: `CALIENTE` en tono error (`var(--color-error)`) y `FRIA` en tono éxito (`var(--color-exito)`).
  - Ancho máximo en escritorio: `var(--ancho-maximo)` (75rem / 1200px) centrado horizontalmente.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - **Cabecera de Página:** Título de la vista (*"Catálogo de Plantas"*), barra de búsqueda integrada (`CP-CU02`) y botón flotante/fijo de filtros (`CP-CU03`).
  - **Carrusel Horizontal Superior:** Chips táctiles de categorías populares con scroll horizontal (*"Todas"*, *"Digestivas"*, *"Respiratorias"*, etc.).
  - **Disposición Vertical:** Columna única con tarjetas apiladas (separación `var(--espacio-4)`).
  - **Paginación Móvil:** Botón inferior de ancho completo **`"Cargar más plantas"`** (`Boton` con `variante="secundario"`).

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Layout de 2 Columnas:**
    - **Columna Lateral Izquierda (Sidebar de Filtros, 280px fijo):** El panel de filtros de `CP-CU03` ya no es un Bottom Sheet modal, sino una **barra lateral estática fijada al scroll (*sticky sidebar*)** integrada a la izquierda con todos los filtros visibles (cualidad térmica, rangos de altitud, ciclo y categorías).
    - **Columna Principal Derecha (Área de Catálogo):**
      - Barra superior expandida que aloja el buscador (`CP-CU02`), selector de ordenamiento (alfabético, altitud) y contador de resultados (*"Mostrando 24 de 80 especies"*).
      - **Cuadrícula Responsiva (CSS Grid):** Las tarjetas de plantas se reordenan automáticamente en **3 columnas (a 1024px) o 4 columnas (a 1440px)** aprovechando todo el ancho de la pantalla de PC.
      - **Paginación de Escritorio:** En lugar de "Cargar más", al pie de la cuadrícula se despliega una barra de paginación completa con selector de elementos por página (12, 24, 48) y botones de números de página `[1] [2] [3] ... [Siguiente]` del componente `Tabla`.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Visitante / Voluntario (Modo Libre - Sin Cuenta)** accederá a la ruta pública `"/catalogo-plantas"`.
2. El **Sistema** presentará la vista dentro del área central (`<main className="layout-contenido">` del `Layout` global de `App.tsx`), adaptando la disposición automáticamente según el ancho de pantalla del dispositivo.
3. El **Sistema** frontend realizará la petición HTTP `GET /api/publico/plantas?page=0&size=12`.
4. El **Sistema** backend recuperará de la tabla `plantas` los registros con `activa = true`, ordenados por `nombre_comun`.
5. El **Sistema** backend responderá con código HTTP `200 OK` y el DTO paginado `PageResponse<PlantaResumenResponse>`.
6. El **Sistema** frontend renderizará las tarjetas: en una sola columna si es móvil, o en cuadrícula de 3-4 columnas si es PC con su barra lateral de filtros a la izquierda.
7. Al hacer clic o tocar una tarjeta o su botón **`"Ver Ficha Completa"`**, el sistema navegará a `CP-CU04`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Fallo de conectividad (HTTP 500 / Network Error):**
  - **Condición:** Si se pierde la conexión o el servidor no responde.
  - **Comportamiento:** El Sistema mostrará una tarjeta de alerta con el componente `Notificacion` (`tipo="error"` usando `var(--color-error)`): *"No se pudo cargar el catálogo de plantas. Por favor, intente nuevamente más tarde"*.
* **5a. No existen plantas registradas (HTTP 200 vacío):**
  - **Condición:** Si la base de datos no contiene plantas activas.
  - **Comportamiento:** El Sistema desplegará un mensaje centrado mediante `Notificacion` con fondo `var(--color-superficie-alt)`: *"Actualmente no hay plantas medicinales registradas en el catálogo oficial"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Envoltura Global:** La página se monta en `<main className="layout-contenido">` del `Layout` global montado en `App.tsx`.
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Boton`: Botón *"Ver Ficha Completa"* (`variante="primario"`) y controles de paginación.
  - `Insignia`: Distintivo térmico (`tono="error"` para Caliente, `tono="exito"` para Fría).
  - `Notificacion`: Alertas de error o estados vacíos.
  - `Tabla`: Paginador responsivo de escritorio.
- [x] **Nuevo Componente Común Sugerido en `frontend/src/components/`:**
  - `Tarjeta.tsx`: Contenedor base de tarjeta con clases responsivas (apilada en móvil, tarjeta de grilla en PC).
- [x] **Feature (`frontend/src/features/catalogo-plantas/`):**
  - `CatalogoPlantasGrid.tsx`: Cuadrícula responsiva (1 col móvil / 3-4 col desktop) que integra tarjetas e insignias.
  - `plantasApi.ts`: Función `obtenerPlantasPublicas(pagina: number, tamano: number)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `CatalogoPlantasPage.tsx` registrada en `App.tsx` bajo `/catalogo-plantas`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaPublicoController.java`):**
  - `GET /api/publico/plantas` -> Retorna `200 OK` con `PageResponse<PlantaResumenResponse>`.
- [x] **DTOs (`com.huertos.dto`):**
  - `PlantaResumenResponse.java` (`record`).
- [x] **Service (`com.huertos.service.PlantaService.java`):**
  - `Page<PlantaResumenResponse> listarPlantasPublicas(Pageable pageable)`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tabla `plantas`:** Mapeada en `com.huertos.model.Planta.java`.
- [x] **Repository:** `com.huertos.repository.PlantaRepository.java` con `findAllByActivaTrue(Pageable pageable)`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/PlantaServiceImplTest.java`):** Verifica ordenamiento alfabético y paginación.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaPublicoControllerTest.java`):** Código `200 OK` sin token JWT.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-plantas/CatalogoPlantasGrid.test.tsx`):** Valida renderizado en viewport móvil (1 col) y en viewport de escritorio (3-4 col con sidebar).
