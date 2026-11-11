---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "HR-CU07: Definir catálogo de actividades cotidianas de comunidad (sin aval MSP)"
labels: ["caso-de-uso", "back", "front", "importante"]
---

## 1. Información General (PUDS)

* **Código:** `HR-CU07`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** El sistema deberá permitir al Responsable de Comunidad parametrizar las tareas cotidianas de cuidado del huerto de forma responsiva: en smartphones mediante tarjetas apiladas y botón flotante (FAB), mientras que en computadoras de escritorio (PC) se despliega como una **tabla de datos completa (`Tabla.tsx`) con columnas ordenables, filtros por categoría y modal centrado (`Modal.tsx`) para la creación de tareas**.
* **Dependencias:** Es incluido por `CM-CU03` y habilita el registro de labores en `HR-CU08`.

### Precondiciones
1. El usuario posee rol `RESPONSABLE_COMUNIDAD`.
2. La comunidad se encuentra activa en el sistema.

### Postcondiciones
1. El sistema insertará la actividad en `catalogo_actividades_comunidad` con `puntos_por_hora = 1` y `activa = TRUE`.
2. La labor quedará habilitada para ser seleccionada en la bitácora de labores (`HR-CU08`).

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Tabla y tarjetas: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Distintivo de puntos: `Insignia` en tono neutro o éxito.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Lista vertical de tareas en tarjetas compactas.
  - Botón circular flotante (FAB) `+` en la esquina inferior derecha.
  - Formulario de alta en Bottom Sheet modal con botones estáticos de categoría.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Cabecera de Página:** Título *"Catálogo de Labores Comunitarias"* con botón de acción destacado **`"+ Nueva Actividad Cotidiana"`** (`Boton` primario) en la esquina superior derecha (se elimina el FAB flotante).
  - **Tabla de Gestión Completa (`Tabla.tsx`):**
    - En pantallas amplias las tarjetas se sustituyen por una tabla estructurada con las siguientes columnas:
      - `Labor`: Icono descriptivo y nombre de la actividad.
      - `Categoría`: Badge con color según la categoría (`LIMPIEZA_DESHIERBE`, `RIEGO`, `ABONO_COMPOST`, `REPLANTE_APORQUE`, `CUIDADO_GENERAL`).
      - `Puntaje Base`: Etiqueta destacada *"1 pt / hora"*.
      - `Estado`: Insignia *"Activa"*.
      - `Acciones`: Botones de edición rápida o desactivación.
  - **Modal Centrado de Creación (`Modal.tsx`):**
    - Ventana emergente centrada de 500px de ancho con los 5 botones estáticos de categorías, campo de nombre y confirmación al pie.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Responsable de Comunidad** accederá a `"/comunidad/actividades-catalogo"`.
2. El **Sistema** presentará la vista: tarjetas en móvil o tabla tabular completa en PC.
3. El usuario pulsará el botón de nueva actividad (FAB en móvil o botón superior en PC).
4. El **Sistema** abrirá el formulario (Bottom Sheet en móvil o Modal centrado en PC).
5. El usuario ingresará el nombre de la labor y seleccionará la categoría.
6. El usuario pulsará **`"Guardar Actividad"`** (`Boton`).
7. El **Sistema** frontend enviará la petición HTTP `POST /api/comunidad/actividades-catalogo` con `CrearActividadCatalogoRequest`.
8. El **Sistema** backend comprobará que no exista duplicado en la comunidad y persistirá la fila con `puntos_por_hora = 1`.
9. El **Sistema** backend responderá con código HTTP `201 Created` y `ActividadCatalogoResponse`.
10. El **Sistema** frontend cerrará el diálogo y actualizará la lista/tabla con notificación de éxito.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **7a. Nombre duplicado en la comunidad (HTTP 409 Conflict):**
  - **Condición:** Si la actividad ya existe.
  - **Comportamiento:** El diálogo permanece abierto y el campo resalta en `var(--color-error)` con mensaje: *"Ya existe una actividad con este nombre en tu comunidad"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Nombre de la actividad.
  - `Boton`: Botón superior en PC y botón de guardado.
  - `Insignia`: Badge de puntos y categoría.
  - `Tabla`: Tabla estructurada de escritorio.
  - `Modal`: Ventana modal centrada para PC.
  - `Notificacion`: Alertas de confirmación o error.
- [x] **Feature (`frontend/src/features/comunidad-actividades/`):**
  - `CatalogoActividadesView.tsx`: Vista responsiva que muestra tarjetas en móvil y `Tabla` en PC.
  - `actividadesCatalogoApi.ts`: Funciones `listarActividades()` y `crearActividad(datos)`.
- [x] **Página:** `CatalogoActividadesPage.tsx` bajo `/comunidad/actividades-catalogo`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ActividadCatalogoController.java`):**
  - `POST /api/comunidad/actividades-catalogo` con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`. Retorna `201 Created`.
- [x] **DTOs:** `CrearActividadCatalogoRequest.java`, `ActividadCatalogoResponse.java`.
- [x] **Service:** `ActividadCatalogoService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V5__crear_actividades_y_talentos_comunidad.sql`; ya existen, NO crear migraciones)
- [x] **Tabla `catalogo_actividades_comunidad`:** Clave foránea `fk_catalogo_actividades_comunidad_comunidad`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ActividadCatalogoServiceImplTest.java`):** Valida `puntos_por_hora = 1` y unicidad en la comunidad.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ActividadCatalogoControllerTest.java`):** Retorna `201 Created` para `RESPONSABLE_COMUNIDAD`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/comunidad-actividades/CatalogoActividadesView.test.tsx`):** Valida tarjetas en mobile y componente `Tabla` en desktop.
