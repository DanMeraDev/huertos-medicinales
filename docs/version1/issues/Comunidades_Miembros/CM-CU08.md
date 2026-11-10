---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU08: Consultar perfil público de miembro por cédula"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU08`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)`
* **Descripción:** `Permite a cualquier ciudadano o voluntario buscar de manera abierta y sin inicio de sesión el perfil público y motivacional de un miembro participante mediante su número de cédula de identidad. La consulta despliega una ventana emergente que exalta los logros del voluntario: nombre completo, rango de nivel (Aprendiz, Oficial o Maestro), comunidades en las que colabora activamente, puntos totales y vitrina de insignias o talentos cosechados. La vista incorpora acompañamiento de Kinti reconociendo el valor de los voluntarios y un llamado a la acción final que invita a sumarse o constituir una nueva comunidad enlazando al flujo US-CU04.`
* **Dependencias:** `Depende de #CM-CU04 (miembros registrados) y enlaza con #US-CU04 (creación pública de comunidad)`

### Precondiciones
1. `El usuario accede a la plataforma en Modo Libre mediante la ruta pública /consulta-miembro.`
2. `El endpoint GET /api/publico/miembros/cedula/{cedula} se encuentra habilitado con acceso público (permitAll()) en SecurityConfig.java.`
3. `Existen voluntarios activos registrados en la base de datos.`

### Postcondiciones
1. `El sistema no altera el estado de la base de datos (operación de solo lectura).`
2. `El frontend despliega una ventana emergente (Modal) con la tarjeta de reconocimiento del voluntario, sus comunidades asociadas y su medallero de insignias.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** ingresa a la opción `"Consultar Voluntario"` desde el menú público en `Layout.tsx` o desde la página de inicio.
2. El **Sistema** renderiza una vista pública limpia, viva y motivacional, estructurada en tres secciones:
   - **Cabecera de Reconocimiento:** Componente `<KintiGuia variante="dia"/>` con el mensaje:
     *"¡Nuestros voluntarios son el alma viva de los huertos! Honramos a quienes dedican sus manos y saberes ancestrales al cuidado de la tierra y la salud comunitaria"*.
   - **Buscador por Cédula:** Formulario centrado con:
     - `Cédula de Identidad` (`cedula`: componente `CampoTexto`, numérico de 10 dígitos obligatorio con validación ecuatoriana).
     - Botón de búsqueda con ícono de lupa **`"Buscar Voluntario"`** (componente `Boton`, variante `primary`).
   - **Llamado a la Acción (CTA de Participación):** En la parte inferior, tarjeta destacada con `<KintiGuia variante="celebracion"/>`:
     - Mensaje: *"¿Deseas ser parte de este listado de guardianes o quieres fundar tu propio huerto comunitario? ¡Inscríbete y siembra con nosotros!"*.
     - Botón **`"Crear mi comunidad"`** (`Boton`, variante `secondary`) que redirige a la ruta `/solicitar-comunidad` (`US-CU04`).
3. El **Visitante** digita un número de cédula válido de 10 dígitos y hace clic en **`"Buscar Voluntario"`**.
4. El **Sistema** valida el formato numérico de 10 dígitos en el cliente, deshabilita el botón mostrando estado de carga y emite la petición `GET /api/publico/miembros/cedula/{cedula}`.
5. El **Sistema** (backend) localiza al voluntario con `activo = true` por su `cedula`, recopila los nombres de las comunidades activas donde participa desde `comunidad_miembros`, extrae su medallero desde `miembro_insignias` y responde con código HTTP `200 OK` y el DTO `PerfilPublicoMiembroResponse`:
```json
{
  "id": 15,
  "cedula": "1710034065",
  "nombreCompleto": "María Tránsito Amaguaña Alba",
  "nombres": "María Tránsito",
  "apellidos": "Amaguaña Alba",
  "nivel": "OFICIAL",
  "puntosTotales": 120,
  "comunidades": [
    "Huerto Comunitario Allpa Viva",
    "Sembradores de San Antonio"
  ],
  "insignias": [
    {
      "nombre": "Maestro del Abonaje 1",
      "descripcion": "Completó 50 sesiones de preparación y distribución de compost",
      "tipo": "TALENTO_COMUNIDAD",
      "fechaObtencion": "2026-11-05"
    },
    {
      "nombre": "Guardián de la Chakra Kiwa",
      "descripcion": "Reconocimiento oficial MSP en cultivo y cosecha de plantas medicinales",
      "tipo": "OFICIAL_SABER_MSP",
      "fechaObtencion": "2026-10-20"
    }
  ]
}
```
6. El **Sistema** (frontend) despliega una ventana emergente (`Modal`):
   - **Encabezado:** Saludo festivo con `<KintiGuia variante="celebracion"/>` *"¡Sembrador/a ejemplar de nuestra red!"*.
   - **Ficha Pública:** Nombre completo destacado, badge de nivel (`Insignia`), y puntos acumulados totales.
   - **Comunidades Vinculadas:** Lista visual con las comunidades en las que participa el voluntario.
   - **Vitrina de Insignias y Talentos:** Grilla de medallas obtenidas con su fecha de obtención y descripción.
   - **Acción:** Botón **`"Cerrar"`** (`Boton`, variante `secondary`) para retornar a la búsqueda.
7. El **Visitante** pulsa **`"Cerrar"`** y el modal se oculta manteniendo la barra de búsqueda disponible.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Formato de cédula inválido (HTTP 400 Bad Request):**
  - **Condición:** Si la cédula ingresada no contiene exactamente 10 dígitos numéricos (`@Pattern(regexp = "^[0-9]{10}$")`).
  - **Comportamiento:** El frontend bloquea la solicitud y muestra en pantalla con `Notificacion`: *"La cédula debe contener exactamente 10 dígitos numéricos"*.
* **5a. Voluntario no encontrado por cédula (HTTP 404 Not Found):**
  - **Condición:** Si no existe ningún voluntario activo registrado en la tabla `miembros` con la cédula especificada.
  - **Comportamiento:** El backend emite `RecursoNoEncontradoException` (HTTP 404 ProblemDetail) y el frontend muestra el componente `Notificacion` con el mensaje: *"No se encontró ningún voluntario registrado con la cédula ingresada. Verifica el número o anímate a registrar una nueva comunidad"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/miembros-publico/`):**
  - `BuscadorPerfilCedula.tsx`: Formulario de búsqueda con `CampoTexto`, botón con ícono de lupa y mensajes motivacionales de `<KintiGuia variante="dia"/>`.
  - `PerfilMiembroModal.tsx`: Ventana emergente (`Modal`) con vitrina de logros, insignias con `Insignia`, listado de comunidades, variante de celebración de Kinti y botón de cierre.
  - `CtaRegistroComunidad.tsx`: Tarjeta inferior con mensaje motivacional y botón de enlace hacia `/solicitar-comunidad`.
  - `miembrosPublicoApi.ts`: Función `consultarPerfilPorCedula(cedula: string): Promise<PerfilPublicoMiembroResponse>` consumiendo `GET /api/publico/miembros/cedula/{cedula}` con `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `ConsultaMiembroPage.tsx`: Vista pública registrada en `App.tsx` bajo la ruta `/consulta-miembro`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.MiembroPublicoController.java`):**
  - `GET /api/publico/miembros/cedula/{cedula}` -> Recibe `@PathVariable @Pattern(regexp = "^[0-9]{10}$") String cedula`, retorna HTTP `200 OK` con `PerfilPublicoMiembroResponse`.
  - **Acceso:** Público (en `SecurityConfig.java`: `.requestMatchers("/api/publico/**").permitAll()`).
  - **Respuesta Exitosa (HTTP 200 OK - `PerfilPublicoMiembroResponse`):**
    - `id`: `Long`.
    - `cedula`: `String`.
    - `nombreCompleto`: `String`.
    - `nombres`: `String`.
    - `apellidos`: `String`.
    - `nivel`: `NivelMiembro`.
    - `puntosTotales`: `Integer`.
    - `comunidades`: `List<String>`.
    - `insignias`: `List<InsigniaObtenidaDto>`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Cédula con formato inválido.
    - `404 Not Found`: No existe voluntario con esa cédula.
- [x] **DTOs (`com.huertos.dto`):**
  - `PerfilPublicoMiembroResponse.java`: `public record PerfilPublicoMiembroResponse(Long id, String cedula, String nombreCompleto, String nombres, String apellidos, NivelMiembro nivel, Integer puntosTotales, List<String> comunidades, List<InsigniaObtenidaDto> insignias) {}`.
  - `InsigniaObtenidaDto.java`: `public record InsigniaObtenidaDto(String nombre, String descripcion, String tipo, LocalDate fechaObtencion) {}`.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `PerfilPublicoMiembroResponse obtenerPerfilPublicoPorCedula(String cedula)`.
  - Consulta `MiembroRepository`, extrae comunidades desde `ComunidadMiembroRepository` y consulta `MiembroInsigniaRepository`.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toPerfilPublicoResponse(Miembro miembro, List<String> comunidades, List<InsigniaObtenidaDto> insignias)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea las tablas `miembros`, `comunidad_miembros`, `comunidades` y `miembro_insignias` definidas en `V2__esquema_completo_v1.sql`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `com.huertos.model.MiembroInsignia.java`):**
  - Mapeo de entidades con JPA.
- [x] **Repository (`com.huertos.repository.MiembroRepository.java` y `MiembroInsigniaRepository.java`):**
  - `Optional<Miembro> findByCedulaAndActivoTrue(String cedula);`
  - `List<MiembroInsignia> findByMiembroId(Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Consulta exitosa: verifica recuperación consolidada de comunidades e insignias por cédula con Mockito.
  - Cédula no registrada: comprueba lanzamiento de `RecursoNoEncontradoException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/MiembroPublicoControllerTest.java`):**
  - Con `@WebMvcTest(MiembroPublicoController.class)`, validando respuesta HTTP `200 OK` en acceso público sin token JWT.
  - Petición con cédula no numérica o con tamaño distinto a 10 dígitos retorna HTTP `400 Bad Request`.
  - Cédula inexistente retorna HTTP `404 Not Found`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros-publico/BuscadorPerfilCedula.test.tsx`):**
  - Renderizado del campo de búsqueda, presencia de `<KintiGuia/>` y tarjeta inferior de llamado a la acción.
  - Búsqueda exitosa con MSW (`handlers.ts`), asegurando la apertura del modal con la lista de comunidades e insignias.
  - Manejo de error 404 mostrando el mensaje correspondiente con `Notificacion`.
