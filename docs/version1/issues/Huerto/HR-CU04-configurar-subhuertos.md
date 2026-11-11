---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "HR-CU04: Configurar subhuertos (hasta 3 subtipos)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `HR-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad` *(Hereda para auditoría en sitio: Técnico MSP)*
* **Descripción:** El sistema deberá permitir al Responsable de Comunidad administrar las zonas de cultivo o subhuertos de manera responsiva: en smartphones mediante tarjetas apiladas, botón flotante (FAB) y Bottom Sheet, mientras que en computadoras de escritorio (PC) se despliega como un **dashboard de zonificación territorial con barra superior de distribución espacial, cuadrícula de 3 columnas de subhuertos y modal centrado (`Modal.tsx`) para la creación de zonas**.
* **Dependencias:** Extiende a `HR-CU03` (Completar datos operativos del huerto).

### Precondiciones
1. El huerto de la comunidad está registrado con su superficie total (`HR-CU03`).
2. El usuario autenticado posee rol `RESPONSABLE_COMUNIDAD`.

### Postcondiciones
1. El sistema insertará el subhuerto en la tabla `subhuertos` con `activo = TRUE`.
2. La zona de cultivo quedará disponible para el registro de siembras (`HR-CU01`).

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Tarjetas de subhuertos: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Barra de capacidad: `var(--color-primario)` sobre fondo `var(--color-superficie-alt)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Medidor compacto de capacidad superior.
  - Lista vertical de tarjetas de subhuertos.
  - Botón de acción flotante (FAB) circular `+` en la esquina inferior derecha.
  - Formulario de alta en Bottom Sheet modal.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Cabecera de Gestión del Huerto:**
    - Título: *"Zonificación y Subhuertos"* con resumen de área total y botón destacado **`"+ Nuevo Subhuerto"`** (`Boton` primario, 44px alto) en la esquina superior derecha (se elimina el FAB flotante).
  - **Dashboard Superior de Ocupación Territorial:**
    - Tarjeta horizontal panorámica que muestra:
      - Barra visual de distribución de superficie: porcentaje de m² ocupados vs. espacio libre remanente.
      - Cápsulas interactivas con los 3 subtipos máximos normativos del MSP: indica cuántos están en uso y cuántos cupos de subtipo quedan disponibles.
  - **Cuadrícula de Subhuertos (Grid de 3 Columnas):**
    - Cada subhuerto se muestra en una tarjeta amplia que incluye ilustración botánica del subtipo, nombre de la zona, metros cuadrados, número de siembras activas alojadas y menú de opciones de edición.
  - **Modal Centrado de Creación (`Modal.tsx`):**
    - Al pulsar `+ Nuevo Subhuerto`, se abre un modal clásico centrado de 560px de ancho con los 7 subtipos ilustrados, selector de área y validación de espacio en tiempo real.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Responsable de Comunidad** accederá a `"/huerto/subhuertos"`.
2. El **Sistema** presentará la vista: apilada en móvil o en formato dashboard de 3 columnas en PC.
3. El usuario pulsará el botón de nuevo subhuerto (FAB en móvil o botón superior en PC).
4. El **Sistema** abrirá el formulario (Bottom Sheet en móvil o Modal centrado en PC).
5. El usuario ingresará el nombre de la zona, seleccionará el subtipo (ej. `[ 🪴 Macetas en Piso ]`) y el área en m².
6. El usuario pulsará **`"Crear Subhuerto"`** (`Boton`).
7. El **Sistema** frontend enviará la petición HTTP `POST /api/comunidad/subhuertos` con `CrearSubhuertoRequest`.
8. El **Sistema** backend comprobará que no se superen 3 subtipos distintos y que `SUM(area_m2) <= area_total_m2`.
9. El **Sistema** backend persistirá el registro y responderá con código HTTP `201 Created` y `SubhuertoResponse`.
10. El **Sistema** frontend cerrará el diálogo, actualizará la barra de capacidad y mostrará una notificación con `Notificacion`: *"Subhuerto creado exitosamente"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **5a. Límite de 3 subtipos de cultivo excedido (HTTP 400 Bad Request):**
  - **Condición:** Si se intenta registrar un cuarto subtipo no utilizado.
  - **Comportamiento:** Los subtipos nuevos aparecen deshabilitados con candado visual y aviso: *"Máximo 3 subtipos por huerto según norma MSP"*.
* **5b. Área ingresada superior al espacio disponible (HTTP 400 Bad Request):**
  - **Condición:** Si `areaM2` supera los m² libres.
  - **Comportamiento:** El campo resalta en `var(--color-error)` con mensaje: *"El área ingresada supera el espacio disponible del huerto"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Nombre de zona y m² de área.
  - `Boton`: Botón FAB en móvil y botón superior de cabecera en PC.
  - `Modal`: Ventana modal centrada para PC.
  - `Notificacion`: Alertas de éxito y advertencias.
- [x] **Feature (`frontend/src/features/huerto-subhuertos/`):**
  - `GestionSubhuertosView.tsx`: Componente responsivo (tarjetas apiladas en móvil / dashboard 3 columnas en desktop).
  - `subhuertosApi.ts`: Funciones `obtenerSubhuertos()` y `crearSubhuerto(datos)`.
- [x] **Página:** `SubhuertosPage.tsx` bajo `/huerto/subhuertos`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.SubhuertoController.java`):**
  - `POST /api/comunidad/subhuertos` con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`. Retorna `201 Created`.
- [x] **DTOs (`com.huertos.dto`):**
  - `CrearSubhuertoRequest.java` (`record`):
    - `nombreZona`: `String`, obligatorio (`@NotBlank`, `@Size(max = 100)`).
    - `subtipo`: `SubtipoSubhuerto` (`SURCOS_WACHU`, `TERRAZAS_ANTI`, `CAMELLONES_INKA_WACHU`, `VERTICAL_PARED`, `COLGANTE_TECHO`, `MACETAS_PISO`, `LLANTAS_RECICLADAS`), obligatorio.
    - `areaM2`: `BigDecimal`, obligatorio (`@Positive`, `@Digits(integer = 6, fraction = 2)`).
  - `SubhuertoResponse.java` (`record`): `Long id`, `Long huertoId`, `String nombreZona`, `SubtipoSubhuerto subtipo`, `BigDecimal areaM2`, `boolean activo`.
  - Si el huerto todavía tiene `area_total_m2 = NULL` (no se hizo `HR-CU03`), lanza `ReglaNegocioException`: *"Primero completa el área del huerto"*.
- [x] **Service:** `SubhuertoService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V4__crear_huertos_bodega_y_siembras.sql`; ya existen, NO crear migraciones)
- [x] **Tabla `subhuertos`:** entidad `com.huertos.model.Subhuerto` (ya existe). Las reglas de máximo 3 subtipos y de área se validan en el servicio con los subhuertos `activo = true`; la BD solo exige `area_m2 > 0`.
- [x] **Tabla `subhuertos`:** Clave foránea `fk_subhuertos_huerto`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SubhuertoServiceImplTest.java`):** Valida restricción de 3 subtipos y sumatoria de áreas.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SubhuertoControllerTest.java`):** Retorna `201 Created`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/huerto-subhuertos/GestionSubhuertosView.test.tsx`):** Valida renderizado en mobile y cuadrícula de 3 columnas en desktop.
