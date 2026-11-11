---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU02: Buscar planta por nombre (común, científico o kichwa)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU02`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)` *(Heredan: Responsable de Comunidad y Técnico MSP)*
* **Descripción:** El sistema deberá permitir al usuario buscar plantas medicinales ingresando una palabra clave que coincida con el nombre común, científico o kichwa, adaptando la barra de búsqueda ergonómicamente: en smartphones con teclado táctil optimizado y botón de borrado rápido, y en computadoras de escritorio (PC) expandida horizontalmente junto a atajos de teclado y autocompletado en cuadrícula.
* **Dependencias:** Extiende a `CP-CU01` (Consultar catálogo de plantas).

### Precondiciones
1. El usuario se encuentra en la vista de catálogo (`CP-CU01`).
2. El endpoint de búsqueda es accesible públicamente.

### Postcondiciones
1. El sistema no modificará el estado de la base de datos (operación de solo lectura).
2. El sistema actualizará los resultados: en lista vertical compacta en móviles o en la cuadrícula de tarjetas de escritorio.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo del buscador: `var(--color-superficie)`.
  - Bordes: `var(--color-borde)` con radio `var(--radio-full)` o `var(--radio-md)`.
  - Color de acento al enfocar: `var(--color-primario)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Barra de búsqueda de ancho completo (48px de alto) integrada en la cabecera interna de la vista, con `type="search"`, `enterKeyHint="search"` para teclado virtual y botón táctil "X" de borrado rápido.
  - Resultados desplegados en lista vertical compacta con miniaturas botánicas de 64x64px.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px):**
  - **Ubicación y Ancho Expandido:** El buscador se ubica en la barra superior de la sección principal (ancho de 400px a 600px), alineado junto al selector de ordenamiento y contador de resultados.
  - **Interacción de Teclado PC:** Soporta el atajo de teclado nativo `/` (barra oblicua) para enfocar el buscador automáticamente sin tocar el ratón.
  - **Despliegue de Resultados:** En lugar de una lista plana, los resultados filtran en tiempo real la **cuadrícula de 3-4 columnas de tarjetas** de escritorio, manteniendo el sidebar de filtros visible a la izquierda.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Visitante / Voluntario (Modo Libre - Sin Cuenta)** interactuará con el campo de búsqueda (pulsando con el dedo en móvil o presionando `/` o clic en PC).
2. El usuario ingresará al menos 2 caracteres (ej: *"chupa"* o *"matico"*).
3. El **Sistema** frontend activará el debounce (300 ms) y enviará la petición HTTP `GET /api/publico/plantas/buscar?criterio={criterio}`.
4. El **Sistema** backend ejecutará la consulta `LIKE` (MariaDB no tiene `ILIKE`; la intercalación de la tabla ya ignora mayúsculas y tildes) en `nombre_comun`, `nombre_cientifico` y `nombre_kichwa` filtrando `activa = true`.
5. El **Sistema** backend responderá con código HTTP `200 OK` y `List<PlantaResumenResponse>`.
6. El **Sistema** frontend actualizará la vista: lista vertical en smartphone o cuadrícula multicolumna en PC.
7. Al borrar el texto o pulsar el botón "X", el sistema reestablece la vista del catálogo completo.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Criterio de búsqueda menor a 2 caracteres:**
  - **Condición:** Si se ingresa una sola letra o espacios.
  - **Comportamiento:** El frontend no envía la petición y muestra un micro-texto de guía: *"Escribe al menos 2 caracteres..."*.
* **6a. Sin coincidencias:**
  - **Condición:** Si no existen plantas activas con dicho término.
  - **Comportamiento:** Se muestra el componente `Notificacion` con botón táctil: **`"Ver todas las plantas"`** (`Boton` secundario).

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Buscador configurado con `type="search"` y botón de borrado.
  - `Boton`: Botón *"Ver todas las plantas"*.
  - `Notificacion`: Alerta de estado sin coincidencias.
- [x] **Feature (`frontend/src/features/catalogo-plantas/`):**
  - `BuscadorPlantas.tsx`: Componente responsivo integrado en la cabecera del catálogo.
  - `plantasApi.ts`: Función `buscarPlantas(criterio: string)`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaPublicoController.java`):**
  - `GET /api/publico/plantas/buscar?criterio={criterio}` -> Retorna `200 OK` con `List<PlantaResumenResponse>`.
- [x] **Service (`com.huertos.service.PlantaService.java`):**
  - Método `List<PlantaResumenResponse> buscarPorNombre(String criterio)`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V3__crear_catalogo_plantas.sql`; ya existen, NO crear migraciones)
- [x] **Repository (`com.huertos.repository.PlantaRepository.java`):** Consulta `LIKE` en los 3 nombres.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Repositorio (`backend/src/test/java/com/huertos/repository/PlantaRepositoryTest.java`):** Valida coincidencia insensible a mayúsculas y acentos.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaPublicoControllerTest.java`):** Retorna `200 OK`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-plantas/BuscadorPlantas.test.tsx`):** Valida respuesta en móvil y escritorio.
