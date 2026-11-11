---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU04: Consultar ficha técnica de la planta"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)` *(Heredan: Responsable de Comunidad y Técnico MSP)*
* **Descripción:** El sistema deberá presentar la ficha botánica, médica y ancestral completa de una planta medicinal de forma responsiva: en smartphones mediante una vista vertical continua con imagen *edge-to-edge*, y en computadoras de escritorio (PC) en un **layout de dos columnas amplias (45% / 55%)** que coloca la fotografía y datos agronómicos a la izquierda y el contenido terapéutico, dosificación y contraindicaciones a la derecha.
* **Dependencias:** Extiende a `CP-CU01` (Consultar catálogo de plantas).

### Precondiciones
1. La planta existe en la base de datos con estado `activa = true`.
2. El endpoint de detalle individual es accesible públicamente.

### Postcondiciones
1. El sistema no modificará el estado de la base de datos (operación de solo lectura).
2. El sistema renderizará la ficha técnica adaptada a la resolución del dispositivo.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo de página: `var(--color-fondo)`.
  - Tarjetas de contenido: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Caja de contraindicaciones: Fondo `var(--color-superficie-alt)`, borde en `var(--color-advertencia)` o `var(--color-error)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Botón táctil circular `< Volver` en la esquina superior izquierda.
  - Fotografía botánica a sangre (100vw, 240px de alto) con insignia térmica superpuesta.
  - Fila deslizable horizontal de factores agronómicos (altitud, ciclo, origen, región).
  - Bloques verticales continuos de usos, preparación y contraindicaciones, con botón inferior fijo de retorno.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Barra Superior de Navegación Contextual (Breadcrumbs):**
    - Enlace de migas de pan: `Catálogo de Plantas > [Nombre de la Planta]`, junto al botón **`< Volver al Catálogo`** (`Boton` secundario) en la esquina superior izquierda.
  - **Layout de 2 Columnas (Split View 45% - 55% con `var(--ancho-maximo)`):**
    - **Columna Izquierda (Ficha Botánica y Fotografía Fija):**
      - Fotografía en alta resolución encuadrada con bordes redondeados `var(--radio-lg)`, zoom interactivo al pasar el cursor (*hover zoom*) e insignia térmica de gran tamaño.
      - Tarjeta inferior con cuadrícula de parámetros de cultivo: Rango altitudinal (msnm), ciclo de cultivo, familia botánica, origen y regiones de Ecuador.
    - **Columna Derecha (Ficha Médica y Comunitaria MSP):**
      - Título principal (`h1`) con nombre común, nombre kichwa entrecomillado y nombre científico en cursiva.
      - **Sección de Usos Terapéuticos:** Categoría médica destacada y descripción detallada de propiedades curativas.
      - **Sección de Preparación y Dosificación:** Guía paso a paso estructurada con iconos ancestrales (infusiones, cocciones, cataplasmas).
      - **Sección de Contraindicaciones y Precauciones:** Tarjeta destacada con el componente `Notificacion` para lectura clara sin scroll forzado.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Visitante / Voluntario (Modo Libre - Sin Cuenta)** seleccionará una planta en el catálogo.
2. El **Sistema** navegará a `"/catalogo-plantas/{id}"`.
3. El **Sistema** frontend realizará la petición HTTP `GET /api/publico/plantas/{id}`.
4. El **Sistema** backend responderá con código HTTP `200 OK` y el DTO `PlantaDetalleResponse`.
5. El **Sistema** frontend renderizará la información: en columna vertical en smartphones, o en dos columnas paralelas en PC.
6. El usuario consultará la información y podrá retornar al catálogo pulsando el botón `< Volver` o la miga de pan.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Identificador de planta no encontrado (HTTP 404 Not Found):**
  - **Condición:** Si el `id` no existe o la planta fue dada de baja.
  - **Comportamiento:** Se despliega una alerta con `Notificacion`: *"La ficha solicitada no está disponible"* y un botón táctil/clic para volver.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Boton`: Botón de retorno al catálogo (`variante="secundario"`).
  - `Insignia`: Distintivo de cualidad térmica (`CALIENTE`/`FRIA`).
  - `Notificacion`: Alerta de contraindicaciones y mensajes de error 404.
- [x] **Nuevo Componente Común Sugerido en `frontend/src/components/`:**
  - `Tarjeta.tsx`: Contenedor modular de las secciones botánicas y médicas.
- [x] **Feature (`frontend/src/features/catalogo-plantas/`):**
  - `FichaTecnicaView.tsx`: Componente responsivo que conmuta entre diseño apilado móvil y layout de 2 columnas en desktop.
  - `plantasApi.ts`: Función `obtenerDetallePlanta(id: number)`.
- [x] **Página:** `DetallePlantaPage.tsx` bajo `/catalogo-plantas/:id`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaPublicoController.java`):**
  - `GET /api/publico/plantas/{id}` -> Retorna `200 OK`.
- [x] **DTOs (`com.huertos.dto`):**
  - `PlantaDetalleResponse.java` (`record`): `Long id`, `String nombreComun`, `String nombreKichwa`, `String nombreCientifico`, `String familiaBotanica`, `CualidadTermica cualidadTermica`, `CicloCultivo cicloCultivo`, `OrigenPlanta origen`, `Integer altitudMinMsnm`, `Integer altitudMaxMsnm`, `String regionesDistribucion`, `String usosMedicinales`, `String preparacionDosificacion`, `String contraindicaciones`, `Set<CategoriaUso> categorias`, `String fotografiaUrl`.
- [x] **Service:** `PlantaService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V3__crear_catalogo_plantas.sql`; ya existen, NO crear migraciones)
- [x] **Repository:** `PlantaRepository.java` con `findByIdAndActivaTrue(Long id)`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/PlantaServiceImplTest.java`):** Valida retorno de campos y 404.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaPublicoControllerTest.java`):** Retorna `200 OK`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-plantas/FichaTecnicaView.test.tsx`):** Valida renderizado en 1 columna en móvil y 2 columnas en desktop.
