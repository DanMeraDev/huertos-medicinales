---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU05: Consultar ranking y medallero de voluntarios / miembros"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU05`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre)`
* **Rol que ve la pantalla:** `Cualquiera (Sin login) / Público general`
* **Visibilidad de pantalla:** `Pública (Sin login)`
* **Descripción:** `Permite a cualquier persona consultar la tabla de honor y medallero de los voluntarios y miembros más activos a nivel nacional, visualizando su posición, nombres, nivel alcanzado (Aprendiz, Oficial o Maestro), puntos acumulados y total de medallas/insignias obtenidas.`
* **Dependencias:** `Depende de CM-CU04 (Miembros), GM-CU01, GM-CU02 y GM-CU06`

### Precondiciones
1. `Acceso público al portal sin requerir autenticación.`
2. `Existen miembros activos registrados en la tabla miembros.`

### Postcondiciones
1. `No modifica la base de datos (operación de solo lectura).`
2. `Se despliega el ranking de voluntarios ordenado por puntaje y el medallero resumen.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El usuario accede a la ruta pública `"/ranking/voluntarios"` desde el menú principal.
2. El **Sistema** realiza una petición `GET /api/publico/ranking/miembros`.
3. El **Sistema** consulta los miembros activos ordenados de forma descendente por `puntos_totales`, calculando para cada uno el conteo de insignias ganadas en `miembro_insignias`.
4. El **Sistema** retorna HTTP `200 OK` con un listado `List<RankingMiembroResponse>`.
5. El **Sistema** renderiza la interfaz visual:
   - Podio de honor con los 3 voluntarios más destacados mostrando sus badges de rango (`Insignia`).
   - Tabla general (`Tabla`) con columnas: Posición, Nombre del Voluntario, Nivel (`APRENDIZ`, `OFICIAL`, `MAESTRO`), Puntos Totales y Cantidad de Insignias.
6. El usuario puede buscar por nombre o apellido utilizando el componente `CampoTexto` o filtrar por nivel (`Selector`).
7. El usuario puede hacer clic en un voluntario para ver su detalle en la consulta pública por cédula (CM-CU08).

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Lista de miembros vacía:**
  - **Condición:** No hay miembros registrados aún.
  - **Comportamiento:** Se muestra mensaje informativo: `"No se registran voluntarios participantes en este momento"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (tarjetas de voluntario o tabla adaptada mostrando nivel y medallas en formato compacto para pantallas pequeñas).
  - Componentes táctiles interactivos (filtro por texto y selector de nivel con altura táctil mínima de 44px).
  - Escalado responsivo progresivo a tabla completa en tablets y pantallas de escritorio.
- [x] **Rol que ve la pantalla:** `Cualquiera (Visitante, Voluntario, Responsable, Técnico)`.
- [x] **Visibilidad:** `Pública (Sin login)`.
- [x] **Feature (`frontend/src/features/ranking/`):**
  - `TablaRankingMiembros.tsx`: Componente de tabla con columnas de nivel, puntos y medallas (`Tabla`, `Insignia`, `CampoTexto`, `Selector`).
  - `rankingApi.ts`: Función `obtenerRankingMiembros()` usando `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `RankingVoluntariosPage.tsx` registrada en `App.tsx` bajo la ruta `/ranking/voluntarios`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.RankingPublicoController.java`):**
  - `GET /api/publico/ranking/miembros` -> Retorna `200 OK` con `List<RankingMiembroResponse>`. Configurado en `SecurityConfig` como público.
- [x] **DTOs (`com.huertos.dto`):**
  - `RankingMiembroResponse.java` (`record` con: `Integer posicion`, `Long miembroId`, `String nombres`, `String apellidos`, `NivelMiembro nivel`, `Integer puntosTotales`, `Long totalInsignias`).
- [x] **Service (`com.huertos.service.RankingService.java` y `com.huertos.service.impl.RankingServiceImpl.java`):**
  - Método `List<RankingMiembroResponse> obtenerRankingMiembros()`.
- [x] **Mapper (`com.huertos.mapper.RankingMapper.java`):**
  - Mapeo de miembros a DTOs con su posición numérica.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `miembros` y `miembro_insignias` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`):**
  - Campos `nombres`, `apellidos`, `nivel`, `puntosTotales`.
- [x] **Repository (`com.huertos.repository.MiembroRepository.java`):**
  - Método `List<Miembro> findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc();`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/RankingServiceImplTest.java`):** Verifica orden por puntos y conteo correcto de insignias por voluntario.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/RankingPublicoControllerTest.java`):** Acceso público sin credenciales retornando `200 OK`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/ranking/TablaRankingMiembros.test.tsx`):** Renderizado de las filas de miembros y etiquetas de nivel.
- [x] **Criterio de Aceptación Mobile First:** Listado de voluntarios y medallero totalmente legible y fluido en pantallas móviles sin scroll horizontal en la vista.

