---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU07: Consultar ficha interna de miembro"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU07`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad`
* **Descripción:** `Permite al Responsable de Comunidad consultar en detalle y en modo estrictamente de solo lectura la ficha integral de un miembro voluntario perteneciente a su colectivo. Se despliega en una ventana emergente que agrupa sus datos de identidad, contacto, fecha de ingreso a la comunidad, edad calculada, estado de membresía, nivel alcanzado y puntos acumulados. Al no almacenarse fotografías de voluntarios, el sistema proyecta un avatar gráfico andino representativo junto a mensajes contextuales y motivacionales de la mascota Kinti adaptados dinámicamente según el rango del miembro (Aprendiz, Oficial o Maestro).`
* **Dependencias:** `Depende de #US-CU01 (sesión de Responsable de Comunidad), #US-CU06 (comunidad activa) y #CM-CU04 (miembro registrado)`

### Precondiciones
1. `El usuario ha iniciado sesión con el rol RESPONSABLE_COMUNIDAD y posee un token JWT válido (Authorization: Bearer <token>).`
2. `El usuario lidera una comunidad activa en la tabla comunidades vinculada a su responsable_id.`
3. `El miembro consultado pertenece a la comunidad del responsable a través de un registro en comunidad_miembros.`
4. `El endpoint GET /api/comunidades/mi-comunidad/miembros/{miembroId} se encuentra habilitado para RESPONSABLE_COMUNIDAD en SecurityConfig.java.`

### Postcondiciones
1. `El sistema no modifica el estado de la base de datos (operación de solo lectura).`
2. `El frontend despliega el diálogo emergente (Modal) con la ficha integral del voluntario, avatar ilustrado y el mensaje motivacional correspondiente de Kinti.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Responsable de Comunidad** visualiza la tabla de voluntarios en la vista general `"/comunidad/miembros"`.
2. En la fila del voluntario deseado, el **Responsable de Comunidad** pulsa el botón **`"Ver ficha completa"`** (componente `Boton`, variante `secondary` o de enlace).
3. El **Sistema** emite la solicitud `GET /api/comunidades/mi-comunidad/miembros/{miembroId}`.
4. El **Sistema** (backend) extrae el `responsableId`, verifica la pertenencia del miembro a la comunidad del usuario, consulta los datos personales en `miembros`, los datos de vinculación en `comunidad_miembros`, calcula la edad con `Clock` y responde con código HTTP `200 OK` y el DTO `FichaInternaMiembroResponse`:
```json
{
  "id": 15,
  "comunidadId": 3,
  "nombreComunidad": "Huerto Comunitario Allpa Viva",
  "cedula": "1710034065",
  "nombres": "María Tránsito",
  "apellidos": "Amaguaña Alba",
  "nombreCompleto": "María Tránsito Amaguaña Alba",
  "fechaNacimiento": "2010-03-25",
  "edad": 16,
  "genero": "FEMENINO",
  "telefono": "0998877665",
  "nivel": "APRENDIZ",
  "puntosTotales": 45,
  "activoEnComunidad": true,
  "fechaIngreso": "2026-11-10",
  "fechaBaja": null,
  "motivoBaja": null
}
```
5. El **Sistema** (frontend) despliega una ventana emergente (`Modal`):
   - **Identidad y Avatar:** Avatar gráfico andino predeterminado acompañado por el nombre completo y badge de nivel (`Insignia`).
   - **Mensaje Contextual de Kinti:** El componente `<KintiGuia/>` evalúa el `nivel` y muestra la frase correspondiente:
     - **APRENDIZ:** `<KintiGuia variante="dia"/>` *"¡Recién inicia su siembra en el camino de las plantas medicinales! Acompañemos sus primeros pasos con paciencia y sabiduría ancestral"*.
     - **OFICIAL:** `<KintiGuia variante="celebracion"/>` *"¡Ha demostrado constancia en las labores del huerto! Su compromiso florece y ya guía a los más jóvenes con su ejemplo y dedicación"*.
     - **MAESTRO:** `<KintiGuia variante="celebracion"/>` *"¡Un verdadero guardián de los saberes ancestrales! Su conocimiento y entrega son medicina viva para toda la comunidad"*.
   - **Ficha de Datos Detallada (Modo Solo Lectura):**
     - Cédula de Identidad: `1710034065`
     - Género: `FEMENINO`
     - Teléfono celular: `0998877665`
     - Fecha de Nacimiento: `25/03/2010` (Edad: `16 años`)
     - Fecha de ingreso a la comunidad: `10/11/2026`
     - Estado en la comunidad: `Activo`
     - Puntos acumulados: `45 puntos`
   - **Acción:** Botón **`"Cerrar"`** (`Boton`, variante `secondary`) sin permitir ninguna acción de edición desde esta vista.
6. El **Responsable de Comunidad** revisa la información y pulsa **`"Cerrar"`**.
7. El **Sistema** cierra el modal devolviendo el foco a la tabla principal.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Miembro no encontrado o ajeno a la comunidad (HTTP 404 Not Found):**
  - **Condición:** Si el `miembroId` solicitado no existe o no tiene registro en `comunidad_miembros` para la comunidad del responsable actual.
  - **Comportamiento:** El backend lanza `RecursoNoEncontradoException`: *"No se encontró la ficha del voluntario en tu comunidad"*. El frontend muestra una alerta de error con `Notificacion`.
* **4b. Miembro con estado inactivo:**
  - **Condición:** Si el voluntario tiene `activo_en_comunidad = false`.
  - **Comportamiento:** La ficha se despliega normalmente pero exhibe un badge de estado `"Suspendido / Inactivo"`, mostrando además la `fechaBaja` y el `motivoBaja` registrado.
* **4c. Acceso no autorizado (HTTP 401 Unauthorized / HTTP 403 Forbidden):**
  - **Condición:** Si la solicitud no incluye token JWT o el usuario no posee el rol `RESPONSABLE_COMUNIDAD`.
  - **Comportamiento:** Spring Security bloquea el acceso con código HTTP 401 o 403.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros/`):**
  - `FichaMiembroModal.tsx`: Modal estilizado con avatar genérico andino, visualizador de campos en solo lectura, badge de nivel (`Insignia`), y renderizado reactivo del mensaje de `<KintiGuia/>` condicionado al enum `nivel`.
  - `miembrosApi.ts`: Función `obtenerFichaMiembro(miembroId: number): Promise<FichaInternaMiembroResponse>` consumiendo `GET /api/comunidades/mi-comunidad/miembros/{miembroId}` mediante `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - Integrado dentro de `MiembrosComunidadPage.tsx` (`/comunidad/miembros`).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadMiembroController.java`):**
  - `GET /api/comunidades/mi-comunidad/miembros/{miembroId}` -> Recibe `@PathVariable Long miembroId` y `Authentication authentication`, retorna HTTP `200 OK` con `FichaInternaMiembroResponse`.
  - **Acceso:** Privado (`@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")`).
  - **Respuesta Exitosa (HTTP 200 OK - `FichaInternaMiembroResponse`):**
    - `id`: `Long`.
    - `comunidadId`: `Long`.
    - `nombreComunidad`: `String`.
    - `cedula`: `String`.
    - `nombres`: `String`.
    - `apellidos`: `String`.
    - `nombreCompleto`: `String`.
    - `fechaNacimiento`: `LocalDate`.
    - `edad`: `Integer`.
    - `genero`: `Genero`.
    - `telefono`: `String`.
    - `nivel`: `NivelMiembro`.
    - `puntosTotales`: `Integer`.
    - `activoEnComunidad`: `boolean`.
    - `fechaIngreso`: `LocalDate`.
    - `fechaBaja`: `LocalDate`.
    - `motivoBaja`: `String`.
  - **Respuestas de Error:**
    - `401 Unauthorized`: Token no válido o ausente.
    - `403 Forbidden`: Usuario sin rol `RESPONSABLE_COMUNIDAD`.
    - `404 Not Found`: Voluntario no encontrado en la comunidad.
- [x] **DTOs (`com.huertos.dto`):**
  - `FichaInternaMiembroResponse.java`: `public record FichaInternaMiembroResponse(Long id, Long comunidadId, String nombreComunidad, String cedula, String nombres, String apellidos, String nombreCompleto, LocalDate fechaNacimiento, Integer edad, Genero genero, String telefono, NivelMiembro nivel, Integer puntosTotales, boolean activoEnComunidad, LocalDate fechaIngreso, LocalDate fechaBaja, String motivoBaja) {}`.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `FichaInternaMiembroResponse obtenerFichaInterna(Long responsableId, Long miembroId)`.
  - Calcula la edad empleando `Period.between(miembro.getFechaNacimiento(), LocalDate.now(clock))`.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toFichaInternaResponse(Miembro miembro, ComunidadMiembro relacion, int edad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Consume las tablas `miembros`, `comunidades` y `comunidad_miembros` de `V2__esquema_completo_v1.sql`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `com.huertos.model.ComunidadMiembro.java`):**
  - Lectura de entidades y sus relaciones.
- [x] **Repository (`com.huertos.repository.ComunidadMiembroRepository.java`):**
  - `Optional<ComunidadMiembro> findByComunidadIdAndMiembroId(Long comunidadId, Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Consulta exitosa: retorna la ficha consolidada con cálculo correcto de edad usando `Clock.fixed(...)`.
  - Miembro no perteneciente: lanza `RecursoNoEncontradoException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadMiembroControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadMiembroController.class)` y `@WithMockUser(roles = "RESPONSABLE_COMUNIDAD")`:
    - `GET /api/comunidades/mi-comunidad/miembros/{miembroId}` retorna HTTP `200 OK`.
    - Petición con ID ajeno retorna HTTP `404 Not Found`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros/FichaMiembroModal.test.tsx`):**
  - Renderizado del avatar genérico y verificación de que ningún campo sea editable.
  - Validación de que el mensaje de `<KintiGuia/>` cambie dinámicamente según el nivel (`APRENDIZ`, `OFICIAL`, `MAESTRO`).
  - Cierre del modal al pulsar el botón correspondiente.
