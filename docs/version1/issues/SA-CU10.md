---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "SA-CU10: Consultar biblioteca de saberes ancestrales"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `SA-CU10`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre)`
* **Rol que ve la pantalla:** `Cualquiera (Sin login) / Público general`
* **Visibilidad de pantalla:** `Pública (Sin login)`
* **Descripción:** `Permite a cualquier persona consultar la biblioteca pedagógica y secuencial de saberes ancestrales aprobada por el MSP (ruta de pasos: preparación de la tierra, ritual de siembra, señalética, cosecha, etc.), explorando títulos en español y kichwa, fases lunares ideales, horarios energéticos, herramientas tradicionales e insignias oficiales que otorga cada paso.`
* **Dependencias:** `Ninguna (Módulo educativo abierto)`

### Precondiciones
1. `El usuario accede de forma pública sin requerir inicio de sesión.`
2. `Existen pasos o saberes cargados en la tabla saberes_ancestrales.`

### Postcondiciones
1. `No modifica la base de datos (operación de solo lectura).`
2. `Se despliega la lista ordenada de la ruta de saberes y la ficha detallada del saber seleccionado.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El usuario accede a la ruta pública `"/saberes-ancestrales"` desde la barra de navegación.
2. El **Sistema** realiza una petición `GET /api/publico/saberes-ancestrales`.
3. El **Sistema** recupera todos los saberes registrados ordenados ascendentemente por `orden_paso`.
4. El **Sistema** retorna el código HTTP `200 OK` con un listado `List<SaberAncestralResumenResponse>`.
5. El **Sistema** despliega visualmente la ruta secuencial de aprendizaje (timeline o tarjetas) mostrando número de paso, título en español/kichwa, categoría y fase lunar recomendada.
6. El usuario hace clic en una tarjeta específica para ver su contenido completo.
7. El **Sistema** realiza una petición `GET /api/publico/saberes-ancestrales/{id}` y despliega el modal o vista de detalle con:
   - Contenido educativo completo.
   - Horario energético ideal (ej. "05:00 a 07:00 AM - Salida del sol").
   - Ingredientes y herramientas tradicionales requeridas.
   - Puntos base otorgados a la comunidad e insignia oficial que desbloquea.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **7a. Saber ancestral no encontrado (HTTP 404 Not Found):**
  - **Condición:** Si se consulta un identificador inexistente en la tabla `saberes_ancestrales`.
  - **Comportamiento:** Lanza `RecursoNoEncontradoException` y notifica en pantalla: `"El saber ancestral solicitado no fue encontrado"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `Cualquiera (Visitante, Voluntario, Responsable, Técnico)`.
- [x] **Visibilidad:** `Pública (Sin login)`.
- [x] **Feature (`frontend/src/features/saberes-ancestrales/`):**
  - `RutaSecuencialSaberes.tsx`: Visualiza la línea de tiempo de pasos con badges de `Insignia` y botones de acción.
  - `FichaDetalleSaber.tsx`: Vista detallada utilizando el componente `Modal` para desplegar el contenido pedagógico completo.
  - `saberesAncestralesApi.ts`: Funciones `obtenerRutaSaberes()` y `obtenerDetalleSaber(id: number)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `SaberesAncestralesPage.tsx` registrada bajo `/saberes-ancestrales`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.SaberAncestralPublicoController.java`):**
  - `GET /api/publico/saberes-ancestrales` -> Retorna `200 OK` con `List<SaberAncestralResumenResponse>`. Permitido públicamente en `SecurityConfig`.
  - `GET /api/publico/saberes-ancestrales/{id}` -> Retorna `200 OK` con `SaberAncestralDetalleResponse`.
- [x] **DTOs (`com.huertos.dto`):**
  - `SaberAncestralResumenResponse.java` (`record` con: `Long id`, `Integer ordenPaso`, `String codigoHito`, `String titulo`, `String tituloKichwa`, `CategoriaSaber categoria`, `FaseLunar faseLunarIdeal`, `String nombreInsigniaOficial`).
  - `SaberAncestralDetalleResponse.java` (`record` con: `Long id`, `Integer ordenPaso`, `String codigoHito`, `String titulo`, `String tituloKichwa`, `CategoriaSaber categoria`, `FaseLunar faseLunarIdeal`, `String horarioEnergetico`, `String contenidoEducativo`, `String ingredientesHerramientas`, `Integer puntosBaseComunidad`, `String nombreInsigniaOficial`).
- [x] **Service (`com.huertos.service.SaberAncestralService.java` y `com.huertos.service.impl.SaberAncestralServiceImpl.java`):**
  - Métodos `List<SaberAncestralResumenResponse> listarRutaSaberes()` y `SaberAncestralDetalleResponse obtenerPorId(Long id)`.
- [x] **Mapper (`com.huertos.mapper.SaberAncestralMapper.java`):**
  - Mapeo a DTOs de resumen y detalle.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `saberes_ancestrales` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA (`com.huertos.model.SaberAncestral.java`, `CategoriaSaber.java`):**
  - Mapeo de la entidad sobre `saberes_ancestrales`.
- [x] **Repository (`com.huertos.repository.SaberAncestralRepository.java`):**
  - Método `List<SaberAncestral> findAllByOrderByOrdenPasoAsc();`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/SaberAncestralServiceImplTest.java`):** Verifica ordenamiento secuencial por `ordenPaso` y búsqueda por id.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/SaberAncestralPublicoControllerTest.java`):** Confirma endpoint público sin token `200 OK`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/saberes-ancestrales/RutaSecuencialSaberes.test.tsx`):** Prueba carga y renderizado de la ruta formativa de pasos ancestrales.

