---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU04: Consultar ranking de comunidades"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU04`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre)`
* **Rol que ve la pantalla:** `Cualquiera (Sin login) / Público general`
* **Visibilidad de pantalla:** `Pública (Sin login)`
* **Descripción:** `Permite a cualquier persona consultar la tabla de posiciones y ranking general de comunidades de huertos medicinales en Ecuador, visualizando sus puntos acumulados por actividades oficiales MSP, su ubicación territorial (provincia y parroquia) y tipo de organización, fomentando una competencia sana e intercambio de saberes.`
* **Dependencias:** `Depende de US-CU06 (Comunidades) y GM-CU01 (Puntos comunitarios)`

### Precondiciones
1. `Acceso público al portal sin requerir credenciales de inicio de sesión.`
2. `Existen comunidades activas registradas en la tabla comunidades.`

### Postcondiciones
1. `No modifica la base de datos (operación de solo lectura).`
2. `Se presenta en pantalla el cuadro de honor y la tabla ordenada de comunidades.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El usuario accede a la ruta pública `"/ranking/comunidades"` desde la navegación principal.
2. El **Sistema** realiza una petición `GET /api/publico/ranking/comunidades`.
3. El **Sistema** consulta la base de datos trayendo las comunidades activas ordenadas de mayor a menor por `puntos_comunidad` y adicionalmente por fecha de creación ante empates.
4. El **Sistema** responde con HTTP `200 OK` y el listado `List<RankingComunidadResponse>`.
5. El **Sistema** renderiza la pantalla:
   - Podio destacado con el top 3 de comunidades líderes usando componentes `Insignia`.
   - Tabla completa de posiciones usando el componente `Tabla` con columnas: Posición, Nombre de la Comunidad, Provincia / Parroquia, Tipo de Organización, Puntos Acumulados.
6. El usuario puede filtrar dinámicamente el listado por provincia utilizando el componente `Selector` o buscar por nombre con `CampoTexto`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Sin comunidades con puntaje:**
  - **Condición:** Si ninguna comunidad ha acumulado puntos aún.
  - **Comportamiento:** Se muestran las comunidades registradas con 0 puntos y un mensaje motivacional: `"¡Sé parte de la primera comunidad en sumar puntos ancestrales!"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (podio de 3 primeros lugares organizado verticalmente o en minitarjetas apiladas para pantallas desde 360px).
  - Tabla de posiciones adaptable con columnas esenciales en móvil (posición, comunidad, puntos) evitando scroll horizontal forzado.
  - Escalado responsivo progresivo a tabla extendida con podio horizontal en tablets y escritorio.
- [x] **Rol que ve la pantalla:** `Cualquiera (Visitante, Voluntario, Responsable, Técnico)`.
- [x] **Visibilidad:** `Pública (Sin login)`.
- [x] **Feature (`frontend/src/features/ranking/`):**
  - `PodioComunidades.tsx`: Tarjetas destacadas de los tres primeros lugares con insignias de oro, plata y bronce.
  - `TablaRankingComunidades.tsx`: Tabla de posiciones con `Tabla`, `Selector` y `CampoTexto`.
  - `rankingApi.ts`: Función `obtenerRankingComunidades()` utilizando `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `RankingComunidadesPage.tsx` registrada en `App.tsx` bajo la ruta `/ranking/comunidades`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.RankingPublicoController.java`):**
  - `GET /api/publico/ranking/comunidades` -> Retorna `200 OK` con `List<RankingComunidadResponse>`. Configurado como público en `SecurityConfig`.
- [x] **DTOs (`com.huertos.dto`):**
  - `RankingComunidadResponse.java` (`record` con: `Integer posicion`, `Long comunidadId`, `String nombreComunidad`, `TipoOrganizacion tipoOrganizacion`, `String provincia`, `String parroquia`, `Integer puntosComunidad`).
- [x] **Service (`com.huertos.service.RankingService.java` y `com.huertos.service.impl.RankingServiceImpl.java`):**
  - Método `List<RankingComunidadResponse> obtenerRankingComunidades()`.
- [x] **Mapper (`com.huertos.mapper.RankingMapper.java`):**
  - Mapeo de entidades a DTOs de ranking asignando la posición correlativa (1, 2, 3...).

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `comunidades` en `V2__crear_comunidades_y_miembros.sql` (ya existe; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.Comunidad.java`):**
  - Campo `puntosComunidad`.
- [x] **Repository (`com.huertos.repository.ComunidadRepository.java`):**
  - Método `List<Comunidad> findByActivaTrueOrderByPuntosComunidadDescCreadoEnAsc();`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/RankingServiceImplTest.java`):** Comprueba ordenamiento descendente y asignación de posición ordinal consecutiva.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/RankingPublicoControllerTest.java`):** Verifica acceso público `200 OK` sin token JWT.
- [x] **Prueba de Componente Frontend (`frontend/src/features/ranking/TablaRankingComunidades.test.tsx`):** Verifica renderizado de filas ordenadas y funcionamiento de filtros.
- [x] **Criterio de Aceptación Mobile First:** El podio y el listado de ranking deben mostrarse de forma legible y atractiva en dispositivos móviles sin desbordar el ancho de pantalla.

