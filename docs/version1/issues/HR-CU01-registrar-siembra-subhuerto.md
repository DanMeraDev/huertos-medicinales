---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "HR-CU01: Registrar siembra en subhuerto (con alerta de altitud)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `HR-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Actor Secundario / Motor:** `Motor de Gamificación y Sistema <<Sistema>>`
* **Descripción:** El sistema deberá permitir al Responsable de Comunidad registrar una siembra de material vegetal en un subhuerto mediante una interfaz responsiva: en smartphones presenta tarjetas táctiles apiladas con botones de fases lunares en kichwa, mientras que en computadoras de escritorio (PC) despliega un **layout de dos columnas con panel de validación agronómica en tiempo real**, descontando de bodega (`BO-CU03`) y advirtiendo sobre discrepancias de altitud geográfica.
* **Dependencias:** Incluye obligatoriamente a `BO-CU03` (Descontar material vegetal por siembra).

### Precondiciones
1. El usuario posee rol `RESPONSABLE_COMUNIDAD` y su comunidad cuenta con huerto (`HR-CU03`) y subhuertos activos (`HR-CU04`).
2. Existe inventario disponible en la tabla `bodega_material_vegetal`.

### Postcondiciones
1. El sistema insertará un nuevo registro en la tabla `siembras` con estado `SEMBRADO`.
2. El sistema descontará el material sembrado en `bodega_material_vegetal`.
3. Si la altitud del huerto no coincidió con el rango de la planta y el usuario confirmó, se registrará `tuvo_alerta_altitud = TRUE`.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Tarjetas: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Botón lunar activo: `var(--color-primario)` con texto `var(--color-texto-inverso)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Cabecera fija con altitud del huerto.
  - Tarjetas apiladas: 1. Subhuerto, 2. Bodega, 3. Cantidad y fecha, 4. Fase lunar en kichwa (`[ 🌑 Yana ]` `[ 🌓 Wiñak ]` `[ 🌕 Pura ]` `[ 🌘 Wañuk ]`).
  - Alerta de altitud en Bottom Sheet emergente y botón inferior anclado **`"Confirmar Siembra"`**.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Layout Dividido en 2 Columnas (Split View 60% / 40%):**
    - **Columna Izquierda (Formulario de Parámetros de Siembra):**
      - Selector de subhuerto en tarjeta amplia.
      - Selector visual de lote en bodega con saldo disponible y señalética térmica.
      - Campos en paralelo: `Cantidad sembrada` y `Fecha de siembra`.
      - Fila horizontal de los 4 botones estáticos de fases lunares tradicionales en kichwa con iconos ilustrativos.
    - **Columna Derecha (Panel de Validación Agronómica en Vivo - Sticky Card):**
      - Tarjeta fija que compara en tiempo real:
        - Altitud del huerto (`[X] msnm`) vs. Rango óptimo de la especie seleccionada (`[Min] - [Max] msnm`) con un gráfico comparativo visual (barra de compatibilidad).
        - Desglose del stock en bodega: Existencia actual, descuento por siembra y saldo remanente resultante.
        - Ficha de señalética térmica (`ROJO_CALIENTE` o `VERDE_BLANCO_FRIA`) que se aplicará al cultivo.
      - Botón destacado al pie del panel derecho: **`"Confirmar Siembra"`** (`Boton` primario, 48px alto).
  - **Alerta de Altitud en PC:** En caso de incompatibilidad, se despliega un **Modal centrado clásico (`Modal.tsx`)** de 500px de ancho con gráfico de altitud y botones de confirmación o cancelación.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Responsable de Comunidad** accederá a la ruta `"/huerto/siembras/nueva"`.
2. El **Sistema** presentará el formulario adaptado a la pantalla (apilado en móvil o 2 columnas en PC).
3. El usuario seleccionará el subhuerto, la planta de bodega, la cantidad y la fase lunar.
4. En PC, el panel derecho actualizará en tiempo real el medidor de altitud y el remanente de bodega.
5. El usuario pulsará **`"Confirmar Siembra"`** (`Boton`).
6. El **Sistema** frontend enviará la petición HTTP `POST /api/comunidad/siembras` con `RegistrarSiembraRequest`.
7. El **Sistema** backend descontará el stock en bodega, asignará la señalética térmica y creará el registro en `siembras`.
8. El **Sistema** backend responderá con código HTTP `201 Created` y `SiembraDetalleResponse`.
9. El **Sistema** frontend desplegará una notificación con `Notificacion`: *"Siembra registrada con éxito. Stock de bodega actualizado"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Alerta de Incompatibilidad de Altitud:**
  - **Condición:** Si la altitud del huerto está fuera del rango de la planta.
  - **Comportamiento:** Se abre un diálogo (Bottom Sheet en móvil o Modal centrado en PC) alertando el riesgo de heladas o bajo desarrollo. Si el usuario pulsa **`"Sembrar de todos modos (Aceptar riesgo)"`**, se guarda `tuvo_alerta_altitud = TRUE`.
* **6a. Stock insuficiente en bodega (HTTP 400 Bad Request):**
  - **Condición:** Si la cantidad sembrada supera el inventario disponible.
  - **Comportamiento:** El campo de cantidad resalta en `var(--color-error)` con mensaje: *"Stock insuficiente en bodega"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Selector`: Selección de subhuerto y material vegetal.
  - `CampoTexto`: Entrada numérica de cantidad sembrada.
  - `Boton`: Botones de fases lunares y botón *"Confirmar Siembra"*.
  - `Insignia`: Distintivos térmicos de la planta.
  - `Modal`: Diálogo centrado de advertencia de altitud en PC.
  - `Notificacion`: Alertas de éxito o error.
- [x] **Feature (`frontend/src/features/huerto-siembras/`):**
  - `FormularioSiembra.tsx`: Componente responsivo (apilado vertical en móvil / 2 columnas con panel de validación en PC).
  - `siembrasApi.ts`: Función `registrarSiembra(datos: RegistrarSiembraRequest)`.
- [x] **Página:** `RegistroSiembraPage.tsx` bajo `/huerto/siembras/nueva`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.SiembraController.java`):**
  - `POST /api/comunidad/siembras` con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`. Retorna `201 Created`.
- [x] **DTOs:** `RegistrarSiembraRequest.java`, `SiembraDetalleResponse.java`.
- [x] **Service:** `SiembraService.java`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tablas:** `siembras`, `bodega_material_vegetal`, `subhuertos`, `huertos`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SiembraServiceImplTest.java`):** Valida descuento de bodega y persistencia de `tuvo_alerta_altitud`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SiembraControllerTest.java`):** Retorna `201 Created` para `RESPONSABLE_COMUNIDAD`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/huerto-siembras/FormularioSiembra.test.tsx`):** Valida layout móvil y split view con panel de validación en desktop.
