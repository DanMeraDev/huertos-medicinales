---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU06: Registrar comunidad"
labels: ["caso-de-uso", "back", "db", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU06`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación y Sistema <<Sistema>>`
* **Descripción:** `Caso de uso de backend que opera en segundo plano (sin interfaz de usuario dedicada). Se ejecuta automáticamente como parte de la orquestación transaccional cuando el Técnico MSP aprueba una solicitud comunitaria (US-CU05). Utiliza la información territorial y de siembra validada en la solicitud junto con el identificador del usuario responsable recién creado (US-CU03) para persistir la nueva entidad en la tabla comunidades, dar de alta la entidad física en huertos con configuración inicial básica (área base de 16m2 y diseño solicitado) y crear las zonas iniciales en subhuertos según los subtipos solicitados.`
* **Dependencias:** `Depende de #US-CU05 (evento desencadenante) y #US-CU03 (cuenta del responsable generada)`

### Precondiciones
1. `El caso de uso US-CU05 se encuentra en ejecución bajo una transacción activa (@Transactional) tras la aprobación del Técnico MSP.`
2. `El usuario responsable ha sido previamente persistido en la tabla usuarios (US-CU03) y posee un identificador único (id) válido.`
3. `La solicitud comunitaria de origen cuenta con estado = 'APROBADA'.`
4. `Las tablas comunidades, huertos y subhuertos existen en el esquema MariaDB según V2__esquema_completo_v1.sql.`

### Postcondiciones
1. `Se inserta un registro en la tabla comunidades asignando responsable_id, puntos_comunidad = 0, activa = true y fecha actual con Clock.`
2. `Se inserta un registro vinculado en la tabla huertos con nombre_huerto derivado de la comunidad, coordenadas, altitud, diseno_principal, area_total_m2 = 16.00, puntos_huerto = 0 y activo = true.`
3. `Se crean los registros iniciales en subhuertos correspondientes a los subtipos especificados en subtipos_solicitados.`
4. `Se retorna la entidad persistida ComunidadResponse al servicio orquestador de US-CU05.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Sistema** (servicio `SolicitudComunidadServiceImpl`) invoca al componente `ComunidadService.registrarComunidadDesdeSolicitud(solicitud, usuarioResponsable)` tras la validación técnica en `US-CU05`.
2. El **Sistema** extrae los datos de la solicitud aprobada:
   - Nombre de la comunidad: `nombreComunidad`.
   - Tipo de organización: `tipoOrganizacion`.
   - Ubicación: `provincia`, `parroquia`, `direccion`.
   - Contacto: `celularSolicitante` (como `telefono_contacto`) y `correoSolicitante` (como `correo_contacto`).
   - Identificador del responsable: `usuarioResponsable.getId()`.
3. El **Sistema** persiste la nueva entidad `Comunidad` en la base de datos con `puntos_comunidad = 0` y `activa = true`.
4. El **Sistema** instancia y persiste la entidad `Huerto` asociada a la comunidad:
   - `nombre_huerto`: `"Huerto " + comunidad.getNombre()`.
   - `latitud` y `longitud`: coordenadas validadas de la solicitud.
   - `altitud_msnm`: altitud registrada en la solicitud.
   - `diseno_principal`: valor enum de la solicitud (`EN_TIERRA`, `VERTICAL`, `COLGANTE`, `EN_PISO`).
   - `area_total_m2`: `16.00` (área mínima estándar según lineamientos MSP).
   - `tipo_huerto`: `'BASICO'`.
   - `puntos_huerto`: `0`.
   - `activo`: `true`.
5. El **Sistema** procesa la cadena `subtipos_solicitados` (ej. `"SURCOS_WACHU,TERRAZAS_ANTI"`), crea cada entidad `Subhuerto` asociada al huerto con su correspondiente valor enum `subtipo`, asignando nombre de zona descriptivo y área equitativa, guardándolos en la tabla `subhuertos`.
6. El **Sistema** retorna el objeto `ComunidadResponse` consolidado al flujo de `US-CU05` para continuar con el despacho del correo de notificación (`US-CU07`).

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Nombre de comunidad duplicado al persistir (HTTP 409 Conflict):**
  - **Condición:** Si por concurrencia ya existe otra comunidad con el mismo `nombre` en la tabla `comunidades`.
  - **Comportamiento:** El Sistema lanza `ConflictoException`: *"Ya existe una comunidad activa registrada con este nombre"*. La transacción activa hace rollback general impidiendo registros huérfanos.
* **3b. Usuario responsable no encontrado o inactivo (HTTP 404 Not Found / 400 Bad Request):**
  - **Condición:** Si el `usuarioResponsable` pasado como parámetro no existe en base de datos o tiene `activo = false`.
  - **Comportamiento:** El Sistema lanza `ReglaNegocioException`: *"El usuario asignado como responsable no es válido para liderar la comunidad"*.
* **3c. Error en la creación del huerto o subhuertos (Rollback):**
  - **Condición:** Si falla la persistencia de la entidad `Huerto` o alguno de sus `Subhuerto`.
  - **Comportamiento:** Debido a la demarcación `@Transactional`, la inserción de la comunidad se revierte completamente garantizando consistencia relacional 1:1 entre comunidad y huerto.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- *Nota arquitectural:* Este caso de uso **no posee interfaz gráfica directa ni formulario dedicado**. Es un proceso interno de servidor.
- La confirmación visual y los datos de la comunidad creada (`comunidadId`, nombre) son presentados al Técnico MSP dentro del flujo de resolución de `US-CU05` en `frontend/src/features/solicitudes/DetalleSolicitudModal.tsx`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller / Endpoints:**
  - *No expone endpoint REST directo.* Su lógica es invocada a nivel de capa de servicio desde `SolicitudComunidadController.java` (`POST /api/solicitudes-comunidad/{id}/aprobar`).
- [x] **DTOs (`com.huertos.dto`):**
  - `RegistrarComunidadInternoDto.java`: `public record RegistrarComunidadInternoDto(String nombre, TipoOrganizacion tipoOrganizacion, String provincia, String parroquia, String direccion, String telefonoContacto, String correoContacto, Long responsableId, BigDecimal latitud, BigDecimal longitud, Integer altitudMsnm, DisenoHuerto disenoPrincipal, String subtiposSolicitados) {}`.
  - `ComunidadResponse.java`: `public record ComunidadResponse(Long id, String nombre, TipoOrganizacion tipoOrganizacion, String provincia, String parroquia, String direccion, String telefonoContacto, String correoContacto, Long responsableId, Integer puntosComunidad, boolean activa, LocalDateTime creadoEn) {}`.
- [x] **Service (`com.huertos.service.ComunidadService.java` y `com.huertos.service.impl.ComunidadServiceImpl.java`):**
  - Método `@Transactional ComunidadResponse registrarComunidadDesdeSolicitud(SolicitudComunidad solicitud, Usuario responsable)`.
  - Inyección de repositorios: `ComunidadRepository`, `HuertoRepository`, `SubhuertoRepository` y componente `Clock`.
- [x] **Mapper (`com.huertos.mapper.ComunidadMapper.java`):**
  - Método `toComunidad(RegistrarComunidadInternoDto dto, Usuario responsable)`.
  - Método `toComunidadResponse(Comunidad comunidad)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Utiliza las tablas `comunidades`, `huertos` y `subhuertos` definidas en `V2__esquema_completo_v1.sql`:
    - `comunidades`: `id`, `nombre`, `tipo_organizacion`, `provincia`, `parroquia`, `direccion`, `telefono_contacto`, `correo_contacto`, `responsable_id`, `puntos_comunidad`, `activa`, `creado_en`.
    - `huertos`: `id`, `comunidad_id`, `nombre_huerto`, `latitud`, `longitud`, `altitud_msnm`, `orientacion`, `area_total_m2`, `tipo_huerto`, `diseno_principal`, `puntos_huerto`, `activo`, `creado_en`.
    - `subhuertos`: `id`, `huerto_id`, `nombre_zona`, `subtipo`, `area_m2`, `activo`, `creado_en`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Comunidad.java`, `com.huertos.model.Huerto.java`, `com.huertos.model.Subhuerto.java`):**
  - Mapeo de entidades con `@Entity`, relaciones `@OneToOne` entre `Comunidad` y `Huerto`, y `@OneToMany` entre `Huerto` y `Subhuerto`.
- [x] **Repository (`com.huertos.repository.ComunidadRepository.java`, `HuertoRepository.java`, `SubhuertoRepository.java`):**
  - `boolean existsByNombre(String nombre);`
  - `Optional<Comunidad> findByResponsableId(Long responsableId);`

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ComunidadServiceImplTest.java`):**
  - Verificación de inserción atómica de `Comunidad`, `Huerto` y desglose de `Subhuerto` ante una solicitud válida aprobada con Mockito.
  - Comprobación de que `puntosComunidad` y `puntosHuerto` inicien en `0`.
  - Comprobación de excepción `ConflictoException` si el nombre de la comunidad colisiona con un registro existente.
  - Validación del rollback automático cuando la persistencia del huerto falla.
- [x] **Prueba de Integración con Base de Datos (`backend/src/test/java/com/huertos/service/ComunidadIntegrationTest.java`):**
  - Con `@SpringBootTest` y MariaDB (Testcontainers), validando que las llaves foráneas `fk_comunidad_responsable`, `fk_huerto_comunidad` y `fk_subhuerto_huerto` se inserten íntegramente.
