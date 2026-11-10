---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU08: Consultar perfil público de miembro por cédula"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU08`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Sin login)`
* **Descripción:** `Permite a cualquier persona sin necesidad de cuenta buscar por número de cédula de identidad a un miembro/voluntario y visualizar su perfil público motivacional: nivel actual (Aprendiz, Oficial, Maestro), puntos acumulados, comunidades en las que participa, talentos comunitarios e insignias ancestrales obtenidas.`
* **Dependencias:** `Depende de #CM-CU04`

### Precondiciones
1. `El usuario accede al portal público sin requerir autenticación (endpoint público en SecurityConfig).`
2. `Existen miembros registrados en la tabla miembros.`

### Postcondiciones
1. `No modifica el estado de la base de datos (operación de solo lectura).`
2. `Se muestra en pantalla la tarjeta de reconocimiento del miembro con su progreso, nivel e insignias.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** accede a la ruta pública `"/consulta-miembro"` desde la barra de navegación principal.
2. El **Sistema** despliega el buscador público con el componente `CampoTexto` solicitando:
   - `Cédula de Identidad` (`cedula`: `texto numérico exacto de 10 dígitos, obligatorio`)
   y el botón **`"Consultar Progreso"`** (componente `Boton`).
3. El **Visitante** ingresa un número de cédula válido y pulsa **`"Consultar Progreso"`**.
4. El **Sistema** realiza una petición `GET /api/publico/miembros/cedula/{cedula}`.
5. El **Sistema** busca al miembro activo y recupera sus datos públicos: `nombres`, `apellidos`, `cedula`, `nivel` (`APRENDIZ`, `OFICIAL`, `MAESTRO`), `puntosTotales`, listado de `comunidades` y listado de `insignias` (tanto talentos comunitarios como insignias ancestrales).
6. El **Sistema** retorna el código HTTP `200 OK` con el DTO `PerfilPublicoMiembroResponse`.
7. El **Sistema** renderiza la tarjeta visual del miembro mostrando su rango con el componente `Insignia` y su vitrina de logros.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Formato de cédula inválido (HTTP 400 Bad Request):**
  - **Condición:** Si `cedula` no tiene exactamente 10 dígitos numéricos (`@Pattern(regexp = "^[0-9]{10}$")`).
  - **Comportamiento:** El Sistema rechaza la petición y muestra con el componente `Notificacion` el error: `"La cédula debe contener exactamente 10 dígitos numéricos"`.
* **5a. Miembro no encontrado por cédula (HTTP 404 Not Found):**
  - **Condición:** Si no existe ningún registro en la tabla `miembros` con la `cedula` indicada.
  - **Comportamiento:** El Sistema lanza `RecursoNoEncontradoException` (`com.huertos.exception`) y muestra en pantalla: `"No se encontró ningún voluntario registrado con la cédula ingresada"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros-publico/`):**
  - `BuscadorPerfilCedula.tsx`: Usa obligatoriamente `CampoTexto`, `Boton`, `Insignia` y `Notificacion` de `frontend/src/components/`.
  - `TarjetaPerfilMiembro.tsx`: Muestra nombre completo, badge de `nivel`, `puntosTotales`, lista de comunidades y grilla de medallas/talentos.
  - `miembrosPublicoApi.ts`: Función `consultarPerfilPorCedula(cedula: string)` usando `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `ConsultaMiembroPage.tsx` registrada en `App.tsx` bajo la ruta pública `/consulta-miembro`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.MiembroPublicoController.java`):**
  - `GET /api/publico/miembros/cedula/{cedula}` -> Retorna `200 OK` con `PerfilPublicoMiembroResponse` (`record`). Asegurar que `/api/publico/**` esté con `.permitAll()` en `SecurityConfig.java`.
- [x] **DTOs (`com.huertos.dto`):**
  - `PerfilPublicoMiembroResponse.java` (`record` con: `Long id`, `String cedula`, `String nombres`, `String apellidos`, `NivelMiembro nivel`, `Integer puntosTotales`, `List<String> comunidades`, `List<InsigniaObtenidaDto> insignias`).
  - `InsigniaObtenidaDto.java` (`record` con: `String nombre`, `String descripcion`, `String tipo`, `LocalDate fechaObtencion`).
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `obtenerPerfilPublicoPorCedula(String cedula)`.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toPerfilPublicoResponse(Miembro miembro, ...)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Usa las tablas `miembros` y `miembro_insignias` ya definidas en la migración maestra `V2__esquema_completo_v1.sql` (NO crear nuevo archivo SQL).
- [x] **Entidad JPA (`com.huertos.model.Miembro.java` y `com.huertos.model.NivelMiembro.java`):**
  - Mapeo de `@Entity` sobre `miembros`.
- [x] **Repository (`com.huertos.repository.MiembroRepository.java`):**
  - Método `Optional<Miembro> findByCedula(String cedula);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):** Verifica retorno exitoso por cédula y lanzamiento de `RecursoNoEncontradoException` cuando no existe.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/MiembroPublicoControllerTest.java`):** Con `@WebMvcTest`, verificando `200 OK` sin token JWT y `404 Not Found`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros-publico/BuscadorPerfilCedula.test.tsx`):** Con Vitest, Testing Library y MSW en `handlers.ts`, probando búsqueda exitosa y mensaje 404.
