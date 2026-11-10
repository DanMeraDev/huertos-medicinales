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
* **Descripción:** `Permite a cualquier ciudadano o voluntario buscar de manera abierta y sin inicio de sesión el perfil público y motivacional de un miembro participante mediante su número de cédula de identidad. La consulta despliega una ventana emergente que exalta los logros comunitarios del voluntario garantizando el estricto cumplimiento de la Ley Orgánica de Protección de Datos Personales (LOPDP) del Ecuador: devuelve de forma filtrada únicamente el nombre completo, rango de nivel (Aprendiz, Oficial o Maestro), comunidades en las que colabora activamente, puntos totales y vitrina de insignias o talentos cosechados, omitiendo de forma absoluta cualquier dato de contacto privado o fecha exacta de nacimiento. Incorpora la nota legal de consentimiento en pantalla y un llamado a la acción final que invita a sumarse o constituir una nueva comunidad enlazando al flujo US-CU04.`
* **Dependencias:** `Depende de #CM-CU04 (miembros registrados con consentimiento LOPDP) y enlaza con #US-CU04 (creación pública de comunidad)`

### Precondiciones
1. `El usuario accede a la plataforma en Modo Libre mediante la ruta pública /consulta-miembro.`
2. `El endpoint GET /api/publico/miembros/cedula/{cedula} se encuentra habilitado con acceso público (permitAll()) en SecurityConfig.java.`
3. `Existen voluntarios activos registrados en la base de datos con consentimiento_datos = true.`

### Postcondiciones
1. `El sistema no altera el estado de la base de datos (operación de solo lectura).`
2. `El frontend despliega una ventana emergente responsiva (Modal) con la tarjeta de reconocimiento del voluntario, sus comunidades asociadas, medallero de insignias y la nota legal obligatoria de protección de datos (LOPDP).`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** ingresa a la opción `"Consultar Voluntario"` desde el menú público en `Layout.tsx` o desde la página de inicio.
2. El **Sistema** renderiza la vista pública diseñada bajo el enfoque **Mobile First para Progressive Web App (PWA)**, permitiendo a cualquier usuario buscar y visualizar perfiles desde smartphones o computadoras mediante las variables de diseño de `frontend/src/index.css`:
   - **Contenedor Responsivo:** La vista se aloja en un contenedor principal centrado (`ConsultaMiembroPage.tsx`) que en dispositivos móviles utiliza `width: 100%` con padding `var(--espacio-4)`. En pantallas de escritorio (`@media (min-width: 640px)`), se centra con `max-width: var(--ancho-lectura)` y márgenes automáticos (`padding: var(--espacio-12) var(--espacio-4)`).
   - **Cabecera de Reconocimiento:** Banner fluido con fondo `var(--color-superficie)`, borde `var(--borde-ancho) solid var(--color-borde)`, radio `var(--radio-lg)`, padding `var(--espacio-4)` y sombra `var(--sombra-sm)`:
     - Integración con `<KintiGuia variante="dia"/>`: *"¡Nuestros voluntarios son el alma viva de los huertos! Honramos a quienes dedican sus manos y saberes ancestrales al cuidado de la tierra y la salud comunitaria"*, con tipografía `var(--texto-base)`, color `var(--color-primario)` y peso `var(--peso-medio)`.
   - **Buscador Responsivo por Cédula:**
     - En dispositivos móviles (< 640px), los elementos se apilan verticalmente a ancho completo (`display: flex; flex-direction: column; gap: var(--espacio-3); width: 100%`).
     - En pantallas de escritorio (`@media (min-width: 640px)`), se organizan en una fila horizontal compacta (`flex-direction: row; align-items: flex-end; gap: var(--espacio-3)`):
       - `Cédula de Identidad` (`cedula`: componente `CampoTexto`, numérico de 10 dígitos obligatorio con validación ecuatoriana, altura táctil mínima de 44px, padding `var(--espacio-2) var(--espacio-3)`, radio `var(--radio-md)`).
       - Botón de búsqueda con ícono de lupa **`"Buscar Voluntario"`** (`Boton`, variante `primary`, fondo `var(--color-primario)`): En móvil ocupa el ancho total (`width: 100%`) con padding `var(--espacio-3)`; en escritorio `width: auto; min-width: 11rem`.
   - **Llamado a la Acción (CTA de Participación Comunitaria):**
     - Tarjeta destacada inferior con fondo `var(--color-superficie-alt)`, radio `var(--radio-lg)`, padding `var(--espacio-4)` y margen superior `var(--espacio-8)`:
       - Integración con `<KintiGuia variante="celebracion"/>` y el mensaje: *"¿Deseas ser parte de este listado de guardianes o quieres fundar tu propio huerto comunitario? ¡Inscríbete y siembra con nosotros!"*.
       - Botón **`"Crear mi comunidad"`** (`Boton`, variante `secondary`): Ancho completo en móvil (`width: 100%`) y ancho automático en escritorio, redirigiendo a `/solicitar-comunidad` (`US-CU04`).
3. El **Visitante** digita un número de cédula válido de 10 dígitos y hace clic o toca en **`"Buscar Voluntario"`**.
4. El **Sistema** valida el formato numérico de 10 dígitos en el cliente, deshabilita el botón mostrando estado de carga y emite la petición `GET /api/publico/miembros/cedula/{cedula}`.
5. El **Sistema** (backend) localiza al voluntario con `activo = true` por su `cedula`, recopila los nombres de las comunidades activas donde participa desde `comunidad_miembros`, extrae su medallero desde `miembro_insignias` y aplica un **filtrado estricto de privacidad**, respondiendo con código HTTP `200 OK` y el DTO `PerfilPublicoMiembroResponse` sin exponer datos privados de contacto ni fecha de nacimiento:
```json
{
  "nombreCompleto": "María Tránsito Amaguaña Alba",
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
6. El **Sistema** (frontend) despliega una ventana emergente (`Modal`), diseñada responsivamente para dispositivos móviles:
   - **Comportamiento Móvil:** Ocupa `width: calc(100% - var(--espacio-4))`, con `max-height: 90vh`, fondo `var(--color-superficie)`, curvatura `var(--radio-lg)`, padding `var(--espacio-4)` y scroll vertical interno suave (`overflow-y: auto`). En pantallas de escritorio (`@media (min-width: 640px)`) escala a `max-width: var(--ancho-lectura)` con padding `var(--espacio-6)`.
   - **Encabezado y Reconocimiento:** Saludo festivo con `<KintiGuia variante="celebracion"/>` *"¡Sembrador/a ejemplar de nuestra red!"*.
   - **Ficha Pública:** Nombre completo destacado en tipografía `var(--texto-xl)`, color `var(--color-primario)`, badge de nivel (`Insignia`), y puntos acumulados totales con etiqueta visible.
   - **Comunidades Vinculadas:** Contenedor flexible con wrap (`display: flex; flex-wrap: wrap; gap: var(--espacio-2)`) donde cada comunidad se despliega como chip o etiqueta en `var(--color-superficie-alt)` con radio `var(--radio-full)`.
   - **Vitrina de Insignias y Talentos:** Grilla adaptativa que en móvil muestra 1 columna (`grid-template-columns: 1fr; gap: var(--espacio-3)`) y en escritorio 2 columnas (`repeat(2, 1fr)`), exhibiendo cada medalla con su fecha de obtención y descripción.
   - **Nota Obligatoria LOPDP (Pie del Modal):** Bloque informativo con tipografía `var(--texto-xs)`, interlineado `var(--interlineado-apretado)`, color `var(--color-texto-secundario)`, borde superior `var(--borde-ancho) solid var(--color-borde)` y padding superior `var(--espacio-3)`:
     > *"Información comunitaria y logros visibles en conformidad con el consentimiento otorgado por el voluntario para fines de reconocimiento, motivación y trazabilidad comunitaria (LOPDP Ecuador). Prohibido su uso para fines comerciales o de contacto no autorizado."*
   - **Acción Táctil:** Botón **`"Cerrar"`** (`Boton`, variante `secondary`), con ancho completo en móvil (`width: 100%`) y padding táctil accesible `var(--espacio-3)` para retornar a la búsqueda.
7. El **Visitante** pulsa o toca **`"Cerrar"`** y el modal se oculta manteniendo la barra de búsqueda disponible.

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
  - `BuscadorPerfilCedula.tsx`: Formulario de búsqueda responsivo con `CampoTexto`, botón con ícono de lupa y mensajes motivacionales de `<KintiGuia variante="dia"/>` usando variables de `index.css`.
  - `PerfilMiembroModal.tsx`: Ventana emergente responsiva (`Modal`) con vitrina de logros, insignias con `Insignia`, listado de comunidades, variante de celebración de Kinti, **nota legal obligatoria LOPDP** al pie del diálogo y botón de cierre táctil.
  - `CtaRegistroComunidad.tsx`: Tarjeta inferior con mensaje motivacional y botón de enlace hacia `/solicitar-comunidad`.
  - `miembrosPublicoApi.ts`: Función `consultarPerfilPorCedula(cedula: string): Promise<PerfilPublicoMiembroResponse>` consumiendo `GET /api/publico/miembros/cedula/{cedula}` con `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `ConsultaMiembroPage.tsx`: Vista pública registrada en `App.tsx` bajo la ruta `/consulta-miembro`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.MiembroPublicoController.java`):**
  - `GET /api/publico/miembros/cedula/{cedula}` -> Recibe `@PathVariable @Pattern(regexp = "^[0-9]{10}$") String cedula`, retorna HTTP `200 OK` con `PerfilPublicoMiembroResponse`.
  - **Acceso:** Público (en `SecurityConfig.java`: `.requestMatchers("/api/publico/**").permitAll()`).
  - **Filtrado Estricto de Privacidad (HTTP 200 OK - `PerfilPublicoMiembroResponse`):**
    - `nombreCompleto`: `String`.
    - `nivel`: `NivelMiembro`.
    - `puntosTotales`: `Integer`.
    - `comunidades`: `List<String>`.
    - `insignias`: `List<InsigniaObtenidaDto>`.
    - *Garantía de Privacidad:* Bajo ninguna circunstancia expone `telefono`, `correo` ni `fechaNacimiento`.
  - **Respuestas de Error:**
    - `400 Bad Request`: Cédula con formato inválido.
    - `404 Not Found`: No existe voluntario con esa cédula.
- [x] **DTOs (`com.huertos.dto`):**
  - `PerfilPublicoMiembroResponse.java`: `public record PerfilPublicoMiembroResponse(String nombreCompleto, NivelMiembro nivel, Integer puntosTotales, List<String> comunidades, List<InsigniaObtenidaDto> insignias) {}`.
  - `InsigniaObtenidaDto.java`: `public record InsigniaObtenidaDto(String nombre, String descripcion, String tipo, LocalDate fechaObtencion) {}`.
- [x] **Service (`com.huertos.service.MiembroService.java` y `com.huertos.service.impl.MiembroServiceImpl.java`):**
  - Método `PerfilPublicoMiembroResponse obtenerPerfilPublicoPorCedula(String cedula)`.
  - Consulta `MiembroRepository`, extrae comunidades activas desde `ComunidadMiembroRepository` y consulta `MiembroInsigniaRepository`.
- [x] **Mapper (`com.huertos.mapper.MiembroMapper.java`):**
  - Método `toPerfilPublicoResponse(Miembro miembro, List<String> comunidades, List<InsigniaObtenidaDto> insignias)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Emplea las tablas `miembros`, `comunidad_miembros`, `comunidades` y `miembro_insignias` del esquema MariaDB en las migraciones reales del backend (`db/migration/` V1 a V4 con tipos `VARCHAR` y `DATETIME` estándar, incluyendo `consentimiento_datos` y `consentimiento_en` en `miembros`).
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `com.huertos.model.MiembroInsignia.java`):**
  - Mapeo de entidades con JPA.
- [x] **Repository (`com.huertos.repository.MiembroRepository.java` y `MiembroInsigniaRepository.java`):**
  - `Optional<Miembro> findByCedulaAndActivoTrue(String cedula);`
  - `List<MiembroInsignia> findByMiembroId(Long miembroId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/MiembroServiceImplTest.java`):**
  - Consulta exitosa: verifica recuperación consolidada de comunidades e insignias por cédula con Mockito, retornando exclusivamente los atributos del DTO `PerfilPublicoMiembroResponse`.
  - Cédula no registrada: comprueba lanzamiento de `RecursoNoEncontradoException`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/MiembroPublicoControllerTest.java`):**
  - Con `@WebMvcTest(MiembroPublicoController.class)`, validando respuesta HTTP `200 OK` en acceso público sin token JWT.
  - Verificación estricta de que el payload JSON de respuesta NO contenga `telefono`, `correo` ni `fechaNacimiento`.
  - Petición con cédula no numérica o con tamaño distinto a 10 dígitos retorna HTTP `400 Bad Request`.
  - Cédula inexistente retorna HTTP `404 Not Found`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/miembros-publico/BuscadorPerfilCedula.test.tsx`):**
  - Renderizado del campo de búsqueda responsivo, presencia de `<KintiGuia/>` y tarjeta inferior de llamado a la acción.
  - Búsqueda exitosa con MSW (`handlers.ts`), asegurando la apertura del modal responsivo con la lista de comunidades, insignias y la visualización de la nota legal de la LOPDP.
  - Manejo de error 404 mostrando el mensaje correspondiente con `Notificacion`.
