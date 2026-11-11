---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "SA-CU01: Consultar calendario lunar"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `SA-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre)`
* **Rol que ve la pantalla:** `Cualquiera (Sin login) / Público general`
* **Visibilidad de pantalla:** `Pública (Sin login)`
* **Descripción:** `Permite a cualquier persona consultar las fases de la luna según el calendario agroecológico y cosmovisión andina (Killa), visualizando la fase actual, fechas de inicio y fin, prácticas agrícolas recomendadas (siembra, deshierbe, cosecha) y el consejo cultural de Kinti en su versión nocturna.`
* **Dependencias:** `Ninguna (Punto de entrada informativo del portal)`

### Precondiciones
1. `El usuario accede al portal sin requerir credenciales (acceso libre en SecurityConfig).`
2. `Existen períodos lunares cargados en la tabla calendario_lunar.`

### Postcondiciones
1. `No modifica la base de datos (operación de solo lectura).`
2. `Se despliega el visor del reloj lunar y la tarjeta con las recomendaciones agroecológicas actuales.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El usuario accede a la ruta pública `"/calendario-lunar"` desde el menú principal.
2. El **Sistema** despacha una petición `GET /api/publico/calendario-lunar/actual`.
3. El **Sistema** busca en la tabla `calendario_lunar` el registro cuyo rango de fechas `[fecha_inicio, fecha_fin]` contenga la fecha actual (`LocalDate.now()`).
4. El **Sistema** responde con código HTTP `200 OK` y el DTO `CalendarioLunarResponse`.
5. El **Sistema** renderiza la interfaz visual:
   - Nombre de la fase en español y kichwa (`Yana Killa`, `Wiñak Killa`, `Pura Killa`, `Wañuk Killa`).
   - Rango de fechas de vigencia.
   - Caja de prácticas recomendadas para el huerto (componente `Notificacion` / Card).
   - Frase / consejo ancestral de **Kinti** (`consejo_kinti`).
6. Si el usuario desea ver las siguientes fases o un mes específico, pulsa el selector de fecha y el Sistema invoca `GET /api/publico/calendario-lunar?fecha={fecha}`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Fecha sin fase lunar configurada (HTTP 404 Not Found):**
  - **Condición:** No se encuentra una fase configurada para la fecha consultada.
  - **Comportamiento:** Retorna HTTP 404 y muestra en pantalla: `"No hay datos lunares disponibles para la fecha seleccionada"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (tarjeta lunar principal destacada en la parte superior del viewport móvil, textos en kichwa/español legibles con contraste adecuado bajo luz solar).
  - Componentes táctiles amigables y scroll vertical natural sin desbordamiento horizontal.
  - Escalado responsivo progresivo a cuadrícula de fases en tablets y escritorio.
- [x] **Rol que ve la pantalla:** `Cualquiera (Visitante, Voluntario, Responsable, Técnico)`.
- [x] **Visibilidad:** `Pública (Sin login)`.
- [x] **Feature (`frontend/src/features/calendario-lunar/`):**
  - `VisorFaseLunar.tsx`: Muestra la ilustración de la luna, nombre kichwa/español y badge de estado.
  - `TarjetaConsejoKinti.tsx`: Despliega las recomendaciones agrícolas y el consejo de Kinti.
  - `calendarioLunarApi.ts`: Funciones `obtenerFaseLunarActual()` y `obtenerFasePorFecha(fecha: string)`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `CalendarioLunarPage.tsx` registrada en `App.tsx` en la ruta `/calendario-lunar`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.CalendarioLunarPublicoController.java`):**
  - `GET /api/publico/calendario-lunar/actual` -> Retorna `200 OK` con `CalendarioLunarResponse`. Endpoint público permitido en `SecurityConfig`.
  - `GET /api/publico/calendario-lunar` -> Retorna `200 OK` con `CalendarioLunarResponse` para parámetro `@RequestParam LocalDate fecha`.
- [x] **DTOs (`com.huertos.dto`):**
  - `CalendarioLunarResponse.java` (`record` con: `Long id`, `FaseLunar faseLunar`, `String nombreKichwa`, `String nombreEspanol`, `LocalDate fechaInicio`, `LocalDate fechaFin`, `String practicasRecomendadas`, `String consejoKinti`).
- [x] **Service (`com.huertos.service.CalendarioLunarService.java` y `com.huertos.service.impl.CalendarioLunarServiceImpl.java`):**
  - Método `CalendarioLunarResponse obtenerFaseActual(LocalDate fecha)`.
- [x] **Mapper (`com.huertos.mapper.CalendarioLunarMapper.java`):**
  - Método `toCalendarioLunarResponse(CalendarioLunar entidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `calendario_lunar` en `V6__crear_saberes_y_actividades_msp.sql` (ya existe; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.CalendarioLunar.java`, `FaseLunar.java`):**
  - Mapeo de la entidad sobre la tabla `calendario_lunar`.
- [x] **Repository (`com.huertos.repository.CalendarioLunarRepository.java`):**
  - Método `Optional<CalendarioLunar> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate f1, LocalDate f2);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/CalendarioLunarServiceImplTest.java`):** Comprueba resolución exacta de la fase según la fecha enviada.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/CalendarioLunarPublicoControllerTest.java`):** Verifica acceso público `200 OK` sin token JWT.
- [x] **Prueba de Componente Frontend (`frontend/src/features/calendario-lunar/VisorFaseLunar.test.tsx`):** Renderizado de fase lunar y textos en kichwa y español.
- [x] **Criterio de Aceptación Mobile First:** Presentación visual clara de la fase lunar y consejos agrícolas en teléfono móvil con lectura cómoda y sin scroll horizontal.

