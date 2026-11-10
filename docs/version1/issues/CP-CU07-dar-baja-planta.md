---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU07: Dar de baja planta"
labels: ["caso-de-uso", "back", "front", "deseable"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU07`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Descripción:** El sistema deberá permitir al Técnico MSP inhabilitar (borrado lógico) una planta del catálogo mediante un diálogo de confirmación adaptable: en smartphones se presenta como un panel deslizable inferior (*Bottom Sheet*), mientras que en computadoras de escritorio (PC) se despliega como un **modal flotante clásico centrado en pantalla (`Modal.tsx`) con fondo difuminado**.

### Precondiciones
1. El usuario posee rol `TECNICO_MSP`.
2. La planta se encuentra con `activa = true`.

### Postcondiciones
1. El sistema actualizará `activa = FALSE` en la tabla `plantas`.
2. La planta dejará de aparecer en las búsquedas públicas del catálogo.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo de modal: `var(--color-superficie)` con sombra `var(--sombra-lg)` y radio `var(--radio-lg)`.
  - Botón de peligro: `var(--color-error)` con texto `var(--color-texto-inverso)`.
  - Botón de cancelación: `var(--color-superficie-alt)`, borde `var(--color-borde)`, texto `var(--color-texto)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Diálogo inferior de confirmación (*Bottom Sheet Alert*) que emerge desde la base con botones táctiles apilados verticalmente (48px alto).

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Modal Centrado en Pantalla (`Modal.tsx`):**
    - Ventana modal centrada de 480px de ancho con fondo semitransparente difuminado (*backdrop blur*).
    - Título: *"Confirmar baja de planta medicinal"* con icono de advertencia.
    - Contenido: Explicación de que las siembras históricas permanecen intactas.
    - **Botones Alineados Horizontalmente a la Derecha:**
      - Botón secundario: **`"Cancelar"`** (`Boton`).
      - Botón principal de peligro: **`"Sí, dar de baja"`** (`Boton` con `variante="peligro"`).
    - Cierre por tecla `Escape` o clic fuera del modal.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Técnico MSP** pulsará el botón **`"Dar de baja"`** en la fila o tarjeta de la planta.
2. El **Sistema** desplegará la confirmación: Bottom Sheet en smartphone o Modal centrado en PC.
3. El **Técnico MSP** confirmará la acción pulsando **`"Sí, dar de baja"`** (`Boton`).
4. El **Sistema** frontend enviará la petición HTTP `PATCH /api/msp/plantas/{id}/baja`.
5. El **Sistema** backend actualizará `activa = FALSE` en la tabla `plantas`.
6. El **Sistema** backend responderá con código HTTP `204 No Content`.
7. El **Sistema** frontend cerrará el diálogo y mostrará una notificación con `Notificacion`: *"Planta retirada del catálogo"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Cancelación voluntaria:**
  - **Condición:** Si el usuario pulsa *"Cancelar"*, presiona `Escape` o hace clic en el backdrop.
  - **Comportamiento:** El modal se cierra sin realizar modificaciones.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Boton`: Botón de peligro (`variante="peligro"`) y botón de cancelación (`variante="secundario"`).
  - `Modal`: Implementación del diálogo centrado en escritorio.
  - `Notificacion`: Alerta de confirmación de baja.
- [x] **Feature (`frontend/src/features/catalogo-admin/`):**
  - `ModalConfirmacionBaja.tsx`: Componente responsivo (Bottom Sheet en mobile / Modal centrado en desktop).
  - `plantasAdminApi.ts`: Función `darDeBajaPlanta(id: number)`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaAdminController.java`):**
  - `PATCH /api/msp/plantas/{id}/baja` con `@PreAuthorize("hasRole('TECNICO_MSP')")`. Retorna `204 No Content`.
- [x] **Service:** `PlantaAdminService.java`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tabla `plantas`:** Columna `activa`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/PlantaAdminServiceImplTest.java`):** Valida cambio a `false` de la columna `activa`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/PlantaAdminControllerTest.java`):** Retorna `204 No Content`.
