---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "HR-CU08: Registrar actividad cotidiana a miembro (puntos solo a miembro)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `HR-CU08`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Actor Secundario / Motor:** `Motor de Gamificación y Sistema <<Sistema>>`
* **Descripción:** El sistema deberá permitir al Responsable de Comunidad registrar la jornada y labor desempeñada por un voluntario mediante una interfaz responsiva: en smartphones mediante tarjetas táctiles apiladas, y en computadoras de escritorio (PC) en un **layout de dos columnas con panel motivacional del voluntario en vivo**, acreditando puntos de gamificación de forma exclusiva al voluntario (`GM-CU06`), evaluando su ascenso de nivel (`GM-CU02`) y celebrando el progreso.
* **Dependencias:** Incluye obligatoriamente a `GM-CU06` y es extendido por `GM-CU02` (Ascenso de nivel).

### Precondiciones
1. El usuario posee rol `RESPONSABLE_COMUNIDAD`.
2. El miembro pertenece a la comunidad y tiene estado `activo = TRUE`.
3. La actividad comunitaria está registrada en `catalogo_actividades_comunidad` (`HR-CU07`).

### Postcondiciones
1. El sistema insertará un nuevo registro en la tabla `registro_actividades_miembro`.
2. El sistema sumará los puntos exclusivamente a `miembros.puntos_totales` (`horas * 1`).
3. El sistema evaluará el ascenso de nivel del voluntario (`APRENDIZ` -> `OFICIAL` -> `MAESTRO`).
4. **Regla de integridad:** Los puntos de la comunidad y del huerto permanecen inalterados.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Tarjetas: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Calculadora animada: Fondo `var(--color-superficie-alt)`, texto en `var(--color-primario)` y borde `var(--color-exito)`.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Flujo vertical en tarjetas apiladas: 1. Voluntario (Bottom Sheet), 2. Labor, 3. Fecha y horas con botones estáticos (`[ 1h ]` `[ 2h ]` `[ 4h ]` `[ 8h ]`), 4. Observaciones.
  - Sticky bottom bar con botón ancho **`"Acreditar Puntos al Miembro"`**.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Layout Dividido en 2 Columnas (Split View 60% / 40%):**
    - **Columna Izquierda (Formulario de Asignación):**
      - Campo de búsqueda y selección de voluntario con desplegable interactivo.
      - Selector de actividad comunitaria con tarjeta resumen de la labor.
      - Campos en paralelo: Fecha y selector de horas trabajadas (con botones estáticos rápidos y control manual de 1 a 24 horas).
      - Campo de observaciones.
      - Botón de envío al pie del formulario: **`"Acreditar Puntos al Miembro"`** (`Boton` primario, 48px alto).
    - **Columna Derecha (Ficha Motivacional del Voluntario en Vivo - Sticky Card):**
      - Al seleccionar a un miembro, este panel lateral despliega su tarjeta de gamificación en tiempo real:
        - Avatar grande y datos de identificación (nombres completos, cédula).
        - Rango actual (`Insignia` en grande: `APRENDIZ`, `OFICIAL` o `MAESTRO`).
        - Vitrina con los talentos comunitarios e insignias ancestrales que ya posee.
        - **Proyección Dinámica de Puntaje:** Muestra barra de avance interactiva: *"Puntos actuales: 80 pts → Tras esta labor: 84 pts (+4 pts)"*.
        - Indicador de proximidad al siguiente rango.
  - **Celebración en PC:** Al guardar, se abre un **Modal centrado animado (`Modal.tsx`)** de reconocimiento al voluntario.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Responsable de Comunidad** accederá a `"/comunidad/actividades-miembros/nueva"`.
2. El **Sistema** presentará la vista: apilada en móvil o en dos columnas con ficha motivacional en PC.
3. El usuario seleccionará al voluntario (en PC se carga de inmediato su ficha en el panel derecho).
4. El usuario seleccionará la labor e indicará las horas trabajadas.
5. El usuario pulsará **`"Acreditar Puntos al Miembro"`** (`Boton`).
6. El **Sistema** frontend validará los datos y enviará la petición HTTP `POST /api/comunidad/actividades-miembros` con `RegistrarLaborMiembroRequest`.
7. El **Sistema** backend insertará el registro en `registro_actividades_miembro` y acreditará los puntos a `miembros.puntos_totales`.
8. El **Motor de Gamificación y Sistema <<Sistema>>** evaluará el ascenso de nivel (`GM-CU02`) y metas de talentos comunitarios (`GM-CU06`).
9. El **Sistema** backend responderá con código HTTP `201 Created` y `ResultadoAcreditacionMiembroResponse`.
10. El **Sistema** frontend abrirá el diálogo modal de celebración felicitando al miembro y mostrando sus insignias actualizadas.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **8a. Jornada mayor a 24 horas (HTTP 400 Bad Request):**
  - **Condición:** Si se ingresan más de 24 horas en un día.
  - **Comportamiento:** El campo de horas resalta en `var(--color-error)` y se bloquea el botón: *"La jornada máxima permitida es de 24 horas por día según norma MSP"*.
* **8b. Fecha futura:**
  - **Condición:** Si la fecha es posterior a la actual.
  - **Comportamiento:** El selector bloquea fechas futuras y alerta: *"No se pueden registrar labores futuras"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Búsqueda de miembro y observaciones.
  - `Selector`: Selección de labor comunitaria.
  - `Boton`: Botones de horas y botón de acreditación.
  - `Insignia`: Distintivo de rango del voluntario y medallas de talentos.
  - `Modal`: Diálogo centrado de celebración en PC.
  - `Notificacion`: Alertas informativas.
- [x] **Feature (`frontend/src/features/comunidad-actividades/`):**
  - `FormularioRegistroLabor.tsx`: Componente responsivo (tarjetas apiladas en móvil / split view con ficha motivacional en PC).
  - `ModalCelebracionPuntos.tsx`: Pantalla modal con insignias de ascenso.
  - `registroLaboresApi.ts`: Función `registrarLaborMiembro(datos: RegistrarLaborMiembroRequest)`.
- [x] **Página:** `RegistroLaborMiembroPage.tsx` bajo `/comunidad/actividades-miembros/nueva`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.RegistroActividadMiembroController.java`):**
  - `POST /api/comunidad/actividades-miembros` con `@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`. Retorna `201 Created`.
- [x] **DTOs:** `RegistrarLaborMiembroRequest.java`, `ResultadoAcreditacionMiembroResponse.java`.
- [x] **Service:** `RegistroActividadMiembroService.java`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tablas:** `registro_actividades_miembro`, `miembros`, `catalogo_actividades_comunidad`, `talentos_comunidad`, `miembro_insignias`.
- [x] **Regla Estricta:** No se modifican los puntos en las tablas `comunidades` ni `huertos`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/RegistroActividadMiembroServiceImplTest.java`):** Valida acreditación de puntos a miembro, inalterabilidad de puntos comunitarios y ascenso de nivel.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/RegistroActividadMiembroControllerTest.java`):** Retorna `201 Created` para `RESPONSABLE_COMUNIDAD`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/comunidad-actividades/FormularioRegistroLabor.test.tsx`):** Valida layout móvil y split view con ficha motivacional en desktop.
