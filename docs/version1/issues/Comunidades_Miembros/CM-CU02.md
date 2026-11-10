---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "CM-CU02: Consultar directorio e información de comunidades"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CM-CU02`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Visitante / Voluntario (Modo Libre - Sin Cuenta)`
* **Descripción:** `Permite a cualquier ciudadano o voluntario acceder públicamente al directorio de comunidades activas registradas en la Red de Huertos Medicinales. La vista presenta una tabla con el nombre de cada comunidad, su responsable oficial, vías de contacto y número de miembros participantes. Ofrece además visualización geográfica interactiva en mapa (OpenStreetMap) con datos de ubicación (provincia, parroquia, dirección y coordenadas), e incorpora un llamado a la acción con Kinti para incentivar la postulación de nuevas comunidades enlazando al flujo público US-CU04.`
* **Dependencias:** `Depende de #US-CU06 (existencia de comunidades dadas de alta) y enlaza con #US-CU04 (creación de comunidad)`

### Precondiciones
1. `El usuario accede al portal en Modo Libre sin requerir autenticación.`
2. `El endpoint GET /api/publico/comunidades está configurado con acceso público (permitAll()) en SecurityConfig.java.`
3. `Existen comunidades activas (activa = true) en la base de datos.`

### Postcondiciones
1. `El sistema no altera el estado de la base de datos (operación de solo lectura).`
2. `Se despliega en pantalla el directorio completo de comunidades con sus estadísticas de miembros y el modal interactivo de mapa para la comunidad seleccionada.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Visitante** pulsa la opción `"Directorio de Comunidades"` en la barra de navegación pública en `Layout.tsx` o desde la página de inicio.
2. El **Sistema** emite la solicitud `GET /api/publico/comunidades`.
3. El **Sistema** (backend) consulta las comunidades con `activa = true`, obtiene el nombre del responsable desde la tabla `usuarios`, calcula el número de miembros activos desde `comunidad_miembros`, extrae las coordenadas geográficas desde `huertos` y responde con código HTTP `200 OK` y una lista de `DirectorioComunidadResponse`:
```json
[
  {
    "id": 3,
    "nombre": "Huerto Comunitario Allpa Viva",
    "nombreResponsable": "Segundo Tránsito Amaguaña",
    "telefonoContacto": "0991234567",
    "correoContacto": "comunidad.allpa@huertos.gob.ec",
    "totalMiembros": 14,
    "provincia": "Pichincha",
    "parroquia": "Calderon",
    "direccion": "Calle de las Rosas y Pasaje Los Pinos, lote 14",
    "latitud": -0.0987123,
    "longitud": -78.4321456,
    "altitudMsnm": 2650
  }
]
```
4. El **Sistema** renderiza la vista pública estructurada en tres secciones:
   - **Cabecera Motivacional:** Componente `<KintiGuia variante="dia"/>` mostrando el mensaje:
     *"¡Todas estas comunidades son nuestros guardianes de la tierra! Quienes cultivan plantas medicinales protegen la vida, la salud comunitaria y la sabiduría de nuestros mayores"*.
   - **Llamado a la Acción (CTA de Expansión):** Tarjeta destacada con `<KintiGuia variante="celebracion"/>`:
     - Mensaje: *"¿Deseas fundar un huerto en tu barrio o sumarte a este movimiento sagrado?"*
     - Botón **`"Solicitar crear mi comunidad"`** (`Boton`, variante `primary`) que redirige a la ruta `/solicitar-comunidad` (`US-CU04`).
   - **Tabla del Directorio Comunitario:** Tabla (`Tabla`) que presenta las siguientes columnas:
     1. `Nombre de la Comunidad` (`nombre`)
     2. `Responsable` (`nombreResponsable`)
     3. `Teléfono` (`telefonoContacto`)
     4. `Correo Electrónico` (`correoContacto`)
     5. `Miembros Activos` (badge con `totalMiembros` acompañado del componente `Insignia`)
     6. `Ubicación` (botón **`"Ver en el mapa"`**, `Boton`, variante `secondary`)
5. El **Visitante** pulsa el botón **`"Ver en el mapa"`** de una comunidad específica.
6. El **Sistema** despliega una ventana emergente (`Modal`):
   - **Título:** *"Ubicación Territorial - ${comunidad.nombre}"* (despliega dinámicamente el nombre de la comunidad seleccionada)
   - **Ficha de Datos:** Visualiza `Provincia`, `Parroquia`, `Dirección exacta` y `Altitud (msnm)`.
   - **Visor Geográfico:** Mapa interactivo centrado en las coordenadas `latitud` y `longitud` con marcador temático andino (vía OpenStreetMap / Leaflet).
   - **Botón:** **`"Cerrar"`** para volver al listado.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. No existen comunidades activas registradas (HTTP 200 OK - Lista vacía):**
  - **Condición:** Si la tabla `comunidades` no cuenta con ningún registro con `activa = true`.
  - **Comportamiento:** El backend retorna una lista vacía `[]`. El frontend oculta la tabla y muestra el componente `<KintiGuia variante="vacio"/>` con el mensaje: *"Aún no se han registrado comunidades en la red. ¡Sé el primero en sembrar la tuya!"* junto al botón hacia `/solicitar-comunidad`.
* **3b. Comunidad sin huerto o coordenadas pendientes:**
  - **Condición:** Si por alguna inconsistencia de migración previa una comunidad no tiene coordenadas registradas en `huertos`.
  - **Comportamiento:** En el modal de ubicación se muestran los textos de `provincia`, `parroquia` y `direccion`, y en lugar del mapa interactivo se despliega un mensaje amigable: *"Ubicación en mapa en proceso de georreferenciación"*.
* **3c. Falla en el servidor de mapas OpenStreetMap:**
  - **Condición:** Si el proveedor externo de capas de mapa no responde o no hay conexión a internet para descargar teselas.
  - **Comportamiento:** El componente de mapa captura el error de renderizado mostrando un contenedor estático con las coordenadas decimales y la dirección física sin romper la aplicación.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Feature (`frontend/src/features/comunidades-publico/`):**
  - `DirectorioComunidadesTabla.tsx`: Renderiza la grilla de comunidades con columnas de contacto, conteo de miembros y botón de acción.
  - `MapaComunidadModal.tsx`: Diálogo emergente con contenedor de mapa OpenStreetMap (vía Leaflet/tiles libres) que recibe `latitud`, `longitud`, `nombre`, `provincia`, `parroquia` y `direccion`.
  - `comunidadesPublicoApi.ts`: Función `obtenerDirectorioComunidades(): Promise<DirectorioComunidadResponse[]>` que consume `GET /api/publico/comunidades` mediante `client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `DirectorioComunidadesPage.tsx`: Vista pública registrada en `App.tsx` bajo la ruta `/comunidades`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.ComunidadPublicoController.java`):**
  - `GET /api/publico/comunidades` -> Retorna HTTP `200 OK` con `List<DirectorioComunidadResponse>`.
  - **Acceso:** Público (en `SecurityConfig.java`: `.requestMatchers("/api/publico/**").permitAll()`).
  - **Respuesta Exitosa (HTTP 200 OK - `DirectorioComunidadResponse`):**
    - `id`: `Long`.
    - `nombre`: `String`.
    - `nombreResponsable`: `String`.
    - `telefonoContacto`: `String`.
    - `correoContacto`: `String`.
    - `totalMiembros`: `Integer`.
    - `provincia`: `String`.
    - `parroquia`: `String`.
    - `direccion`: `String`.
    - `latitud`: `BigDecimal`.
    - `longitud`: `BigDecimal`.
    - `altitudMsnm`: `Integer`.
  - **Respuestas de Error:**
    - `500 Internal Server Error`: Manejado por `GlobalExceptionHandler`.
- [x] **DTOs (`com.huertos.dto`):**
  - `DirectorioComunidadResponse.java`: `public record DirectorioComunidadResponse(Long id, String nombre, String nombreResponsable, String telefonoContacto, String correoContacto, Integer totalMiembros, String provincia, String parroquia, String direccion, BigDecimal latitud, BigDecimal longitud, Integer altitudMsnm) {}`.
- [x] **Service (`com.huertos.service.ComunidadService.java` y `com.huertos.service.impl.ComunidadServiceImpl.java`):**
  - Método `List<DirectorioComunidadResponse> obtenerDirectorioPublico()`.
  - Realiza una consulta optimizada proyectando datos de `comunidades`, `usuarios`, `huertos` y agrupando el conteo de `comunidad_miembros`.
- [x] **Mapper (`com.huertos.mapper.ComunidadMapper.java`):**
  - Método `toDirectorioResponse(Comunidad comunidad, String nombreResponsable, int totalMiembros, Huerto huerto)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - Consume las tablas existentes en `V2__esquema_completo_v1.sql`: `comunidades`, `usuarios`, `huertos` y `comunidad_miembros`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA (`com.huertos.model.Comunidad.java`, `com.huertos.model.Huerto.java`):**
  - Mapeo de relaciones `@OneToOne` entre `Comunidad` y `Huerto`, y relación `@ManyToOne` con `Usuario` (`responsable`).
- [x] **Repository (`com.huertos.repository.ComunidadRepository.java`):**
  - Consulta derivada o JPQL:
    ```java
    @Query("SELECT c FROM Comunidad c WHERE c.activa = true ORDER BY c.nombre ASC")
    List<Comunidad> findAllActivasOrdenadas();
    ```
  - En `ComunidadMiembroRepository.java`:
    ```java
    @Query("SELECT COUNT(cm) FROM ComunidadMiembro cm WHERE cm.comunidad.id = :comunidadId AND cm.activoEnComunidad = true")
    int countMiembrosActivos(@Param("comunidadId") Long comunidadId);
    ```

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/ComunidadServiceImplTest.java`):**
  - Verifica que `obtenerDirectorioPublico()` retorne el listado consolidando responsable, conteo de miembros y coordenadas.
  - Verifica que solo retorne comunidades con `activa = true`.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/ComunidadPublicoControllerTest.java`):**
  - Con `@WebMvcTest(ComunidadPublicoController.class)`, validando respuesta HTTP `200 OK` en endpoint público sin necesidad de cabecera `Authorization`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/comunidades-publico/DirectorioComunidadesTabla.test.tsx`):**
  - Renderizado de la tabla con las 6 columnas y validación de presencia de `<KintiGuia/>` en cabecera y en tarjeta de CTA.
  - Verificación del enlace hacia `/solicitar-comunidad` en el botón de postulación.
  - Comprobación de apertura del modal de mapa al hacer clic en `"Ver en el mapa"`.
