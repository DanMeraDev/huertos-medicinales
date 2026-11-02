# Especificación de Caso de Uso: CM-CU05 - Registrar miembro en la comunidad

## 1. Información General (PUDS)
* **Código:** CM-CU05 (Módulo: Comunidades y Miembros)
* **Incremento:** Incremento 1
* **Actor Principal:** Responsable de Comunidad
* **Descripción:** Permite al responsable de una comunidad de siembra dar de alta a un voluntario participante dentro de su comunidad, inicializando su ruta de evolución en el nivel Aprendiz.
* **Precondiciones:**
  1. El usuario ha iniciado sesión con el rol `RESPONSABLE_COMUNIDAD`.
  2. Existe una `Comunidad` registrada y asociada al responsable actual.
* **Postcondiciones:**
  1. Se persiste un nuevo registro en la tabla `miembros` vinculado a `comunidad_id`.
  2. El miembro queda registrado con `nivel = 'APRENDIZ'`, `puntos_acumulados = 0` y `activo = true`.

## 2. Escenario Básico (Flujo Principal Actor - Sistema)
1. El Responsable de Comunidad accede a la sección "Mi Comunidad > Miembros" y pulsa el botón **"Registrar Miembro"**.
2. El Sistema despliega el formulario de registro de miembro solicitando:
   - Nombres completos (`nombreCompleto`)
   - Cédula o documento de identidad (`identificacion`)
   - Fecha de nacimiento (`fechaNacimiento`)
   - Género (`genero`: `MASCULINO`, `FEMENINO`, `OTRO`)
   - Correo electrónico opcional (`email`)
3. El Responsable completa los datos requeridos y pulsa **"Guardar Miembro"**.
4. El Sistema valida el formato de los campos y verifica que la identificación no esté ya registrada dentro de la misma comunidad.
5. El Sistema asigna por defecto el nivel de expertise `APRENDIZ`, `0` puntos acumulados y la fecha de registro actual (usando el `Clock` inyectado del sistema).
6. El Sistema guarda el nuevo miembro en la base de datos y retorna el código HTTP `201 Created` con los datos del miembro creado.
7. El Sistema cierra el formulario, muestra una notificación de éxito y actualiza la tabla de miembros de la comunidad.

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)
* **4a. Campos obligatorios vacíos o fecha futura (HTTP 400 Bad Request):**
  - Si `nombreCompleto`, `identificacion`, `fechaNacimiento` o `genero` están vacíos, o si `fechaNacimiento` es posterior a la fecha actual del `Clock`:
  - El Sistema rechaza la petición (mediante Bean Validation `@NotBlank`, `@Past`, `@NotNull` en el DTO y manejo en `GlobalExceptionHandler`) e informa en pantalla el campo exacto con error sin borrar los demás datos digitados.
* **4b. Miembro duplicado en la comunidad (HTTP 409 Conflict):**
  - Si ya existe un miembro con la misma `identificacion` en esa comunidad:
  - El Sistema lanza `ConflictoException` (convertida a HTTP 409 por `GlobalExceptionHandler`) y muestra el mensaje: *"Ya existe un miembro con esa identificación registrado en esta comunidad"*.
* **4c. Comunidad no encontrada (HTTP 404 Not Found):**
  - Si el `comunidadId` enviado no existe en base de datos, el Sistema lanza `RecursoNoEncontradoException`.

## 4. Análisis de Robustez y Diseño Técnico (Para el Agente de IA)
* **Objetos Boundary (Frontend React - `frontend/src/features/miembros/`):**
  - `FormularioMiembroModal.tsx`: Formulario con inputs controlados para `nombreCompleto` (text), `identificacion` (text, máx. 13 caracteres), `fechaNacimiento` (input type="date"), `genero` (select con opciones `MASCULINO`, `FEMENINO`, `OTRO`) y `email` (input type="email"). Botones "Cancelar" y "Guardar Miembro".
  - `TablaMiembrosComunidad.tsx`: Tabla que muestra columnas: Nombre, Edad (calculada desde fecha de nacimiento), Género, Nivel (Badge visual: `APRENDIZ`), Puntos y Estado.
  - `miembrosApi.ts`: Funciones `crearMiembro(comunidadId, payload)` y `listarMiembrosPorComunidad(comunidadId)` usando `src/api/client.ts`.
* **Objetos Control (Backend Spring Boot - `com.huertos`):**
  - `MiembroController.java`:
    - `POST /api/comunidades/{comunidadId}/miembros` -> Recibe `CrearMiembroRequest` (record), retorna `201 Created` con `MiembroResponse` (record).
    - `GET /api/comunidades/{comunidadId}/miembros` -> Retorna `200 OK` con `List<MiembroResponse>`.
  - `MiembroService.java` y `MiembroServiceImpl.java`: Lógica de validación de duplicados, asignación de `NivelMiembro.APRENDIZ` y uso de `java.time.Clock`.
  - `MiembroMapper.java`: Conversión entre `Miembro` (`@Entity`) y los records `CrearMiembroRequest` / `MiembroResponse`.
* **Objetos Entity (Base de Datos y JPA - `com.huertos.model`):**
  - `Miembro.java` (Tabla `miembros`: `id`, `comunidad_id`, `usuario_id` nullable, `nombre_completo`, `identificacion`, `fecha_nacimiento`, `genero`, `email`, `nivel`, `puntos_acumulados`, `activo`, `creado_en`).
  - `Comunidad.java` (Tabla `comunidades`).

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)
- [ ] Prueba unitaria en `MiembroServiceImplTest.java` (Mockito): verifica creación con nivel `APRENDIZ` y fallo por identificación duplicada.
- [ ] Prueba de controlador en `MiembroControllerTest.java` (`@WebMvcTest`): verifica `201 Created` y `400 Bad Request` cuando faltan datos.
- [ ] Prueba de componente en `TablaMiembrosComunidad.test.tsx` (Vitest + MSW en `src/test/mocks/handlers.ts`): verifica renderizado y alta de miembro.
