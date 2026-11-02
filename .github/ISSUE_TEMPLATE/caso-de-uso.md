---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "[MÓDULO]-CUNN: [Nombre del Caso de Uso]"
labels: ["caso-de-uso"]
---

<!--
================================================================================
GUÍA RÁPIDA PARA EL EQUIPO (Y REGLAS PARA EL AGENTE DE IA):
1. Esta Issue es el contrato funcional y técnico. La IA NO debe inventar nombres
   de clases, endpoints, tablas ni campos que no estén especificados aquí.
2. El agente debe respetar estrictamente `AGENTS.md`, `CLAUDE.md` y `docs/`.
3. Reemplaza todos los textos entre corchetes `[...]` antes de crear la Issue.
4. El código va como en el diagrama de casos de uso: `<MÓDULO>-CUNN`
   (`HR-CU01` bien, `HR-UC01` mal). Es el mismo en el título y en la rama.
5. Agrega una etiqueta de área (`back`, `front`, `db`) y una de prioridad
   (`imprescindible`, `importante`, `deseable`) según docs/reglas/estandar-issues.md.
================================================================================
-->

## 1. Información General (PUDS)

* **Código:** `[MÓDULO]-CUNN` <!-- Ej: CM-CU05 (Módulo: Comunidades y Miembros) -->
* **Incremento:** `Incremento [X]` <!-- Ej: Incremento 1 -->
* **Actor Principal:** `[Nombre del Rol]` <!-- Ej: Responsable de Comunidad -->
* **Descripción:** `[Qué permite hacer este caso de uso, para quién y con qué objetivo de negocio]` <!-- Ej: Permite al responsable de una comunidad de siembra dar de alta a un voluntario participante dentro de su comunidad, inicializando su ruta de evolución en el nivel Aprendiz. -->
* **Dependencias:** `[Ninguna / Depende de #XX]`

### Precondiciones
<!-- Estado previo requerido en el sistema antes de ejecutar el caso de uso -->
1. `[Precondición 1]` <!-- Ej: El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD. -->
2. `[Precondición 2]` <!-- Ej: Existe una Comunidad registrada y asociada al responsable actual. -->

### Postcondiciones
<!-- Qué cambia exactamente a nivel de datos/estado cuando el flujo termina con éxito -->
1. `[Postcondición 1]` <!-- Ej: Se persiste un nuevo registro en la tabla miembros vinculado a comunidad_id. -->
2. `[Postcondición 2]` <!-- Ej: El miembro queda registrado con nivel = 'APRENDIZ', puntos_acumulados = 0 y activo = true. -->

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

<!-- Describe paso a paso la interacción. Usa backticks (`) para los nombres exactos de propiedades JSON/DTO -->
1. El **[Actor Principal]** accede a la sección `"[Ruta o Menú en UI]"` y pulsa el botón **"`[Texto del Botón]`"**.
2. El **Sistema** despliega `[el formulario / modal / vista]` solicitando:
   - `[Etiqueta del campo 1]` (`nombrePropiedad1`: `[tipo de dato, restricciones, ej: texto, máx. 13 caracteres]`)
   - `[Etiqueta del campo 2]` (`nombrePropiedad2`: `[valores permitidos / enum, ej: MASCULINO, FEMENINO, OTRO]`)
   - `[Etiqueta del campo 3]` (`nombrePropiedad3`: `[obligatorio / opcional]`)
3. El **[Actor Principal]** completa los datos requeridos y pulsa **"`[Texto del Botón de Envío]`"**.
4. El **Sistema** valida el formato de los campos y verifica `[regla de negocio o unicidad, ej: que la identificación no esté ya registrada en la misma comunidad]`.
5. El **Sistema** asigna por defecto `[valores iniciales, ej: nivel APRENDIZ, 0 puntos]` y la fecha/hora actual usando el `Clock` inyectado del sistema (`ClockConfig.java`).
6. El **Sistema** guarda el registro en la base de datos y retorna el código HTTP `[201 Created / 200 OK]` con los datos del recurso.
7. El **Sistema** `[cierra el modal / limpia el formulario]`, muestra una notificación de éxito y actualiza `[la tabla / vista en pantalla]`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

<!--
El Agente de IA debe mapear estos errores usando Bean Validation (@Valid) y el
GlobalExceptionHandler.java existente en com.huertos.exception (ProblemDetail / HTTP Status).
-->
* **4a. Campos obligatorios vacíos o formato inválido (HTTP 400 Bad Request):**
  - **Condición:** Si `[campo1]`, `[campo2]` están vacíos o no cumplen `[restricción, ej: @NotBlank, @Past, @Email]`.
  - **Comportamiento:** El Sistema rechaza la petición (mediante Bean Validation en el `record` Request y manejo en `GlobalExceptionHandler`) e informa en pantalla el campo exacto con error sin borrar los demás datos digitados.
* **4b. Conflicto de negocio / Registro duplicado (HTTP 409 Conflict):**
  - **Condición:** Si `[condición de duplicidad o violación de regla de negocio]`.
  - **Comportamiento:** El Sistema lanza `ConflictoException` (`com.huertos.exception`, convertida a HTTP 409 por `GlobalExceptionHandler`; si aún no existe, este caso de uso la crea) y muestra el mensaje exacto: *"`[Mensaje de error para el usuario]`"*.
* **4c. Recurso relacionado no encontrado (HTTP 404 Not Found):**
  - **Condición:** Si el `[recursoId]` enviado no existe en la base de datos.
  - **Comportamiento:** El Sistema lanza `RecursoNoEncontradoException`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

<!--
Marca con [x] las capas que toca este Caso de Uso y especifica los nombres exactos
respetando la estructura del repositorio (docs/backend/estructura.md, docs/frontend/estructura.md, docs/db/estructura.md).
-->

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [ ] **Feature (`frontend/src/features/[nombre-modulo]/`):**
  - `[NombreFormularioModal].tsx`: `[Descripción de inputs controlados, tipos de input, selects y botones]`.
  - `[NombreTablaOVista].tsx`: `[Descripción de columnas, badges visuales, estados de carga/error]`.
  - `[modulo]Api.ts`: Funciones `[crearRecurso(...)]` y `[listarRecursos(...)]` usando el cliente base `frontend/src/api/client.ts`.
- [ ] **Página / Ruteo (`frontend/src/pages/`):**
  - Integración en `[NombrePage].tsx` o actualización en `App.tsx` (si aplica).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [ ] **Controller (`com.huertos.controller.[Nombre]Controller.java`):**
  - `POST /api/[ruta-recurso]` -> Recibe `[CrearRecursoRequest]` (`record`), retorna `201 Created` con `[RecursoResponse]` (`record`).
  - `GET /api/[ruta-recurso]` -> Retorna `200 OK` con `List<[RecursoResponse]>`.
- [ ] **DTOs (`com.huertos.dto`):**
  - `[CrearRecursoRequest].java` (`record` con anotaciones `jakarta.validation.constraints.*`).
  - `[RecursoResponse].java` (`record`).
- [ ] **Service (`com.huertos.service.[Nombre]Service.java` y `com.huertos.service.impl.[Nombre]ServiceImpl.java`):**
  - Lógica de negocio, validación de duplicados, inyección por constructor de repositorios y `java.time.Clock`.
- [ ] **Mapper (`com.huertos.mapper.[Nombre]Mapper.java`):**
  - Conversión limpia entre la entidad `[Nombre]` y los records `[CrearRecursoRequest]` / `[RecursoResponse]`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [ ] **Migración Flyway (`backend/src/main/resources/db/migration/V[X]__[descripcion_snake_case].sql`):**
  - Creación/alteración de tabla `[nombre_tabla_plural]` con columnas: `id`, `[columna_1]`, `[columna_2]`, `creado_en`, constraints (`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`).
- [ ] **Entidad JPA (`com.huertos.model.[NombreEntidad].java`):**
  - Mapeo de `@Entity` y `@Table(name = "[nombre_tabla_plural]")`, enums asociados y relaciones.
- [ ] **Repository (`com.huertos.repository.[Nombre]Repository.java`):**
  - Interfaz que extiende `JpaRepository<[NombreEntidad], [TipoId]>` y métodos de consulta derivados (ej: `existsBy...`, `findBy...`).

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

<!-- Todo PR debe pasar los workflows .github/workflows/backend.yml y frontend.yml -->
- [ ] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/[Nombre]ServiceImplTest.java`):** Con JUnit 5 y Mockito (`@ExtendWith(MockitoExtension.class)`), verificando el flujo principal (con `Clock.fixed(...)`) y las excepciones de los flujos alternativos.
- [ ] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/[Nombre]ControllerTest.java`):** Con `@WebMvcTest`, verificando respuestas `201 Created` / `200 OK` y errores `400 Bad Request` / `404 Not Found`.
- [ ] **Prueba de Integración (Opcional / Si toca consultas complejas en BD):** Con `@SpringBootTest` e `@Import(TestcontainersConfiguration.class)` contra MariaDB real.
- [ ] **Prueba de Componente Frontend (`frontend/src/features/[nombre-modulo]/[Componente].test.tsx`):** Con Vitest, Testing Library y MSW (registrando los mocks en `frontend/src/test/mocks/handlers.ts`), verificando renderizado, envío de formulario y mensajes de error.
