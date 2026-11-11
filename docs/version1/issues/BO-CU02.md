---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "BO-CU02: Consultar inventario de material vegetal"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `BO-CU02`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad / Técnico MSP`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD` y `TECNICO_MSP`
* **Visibilidad de pantalla:** `Privada (Requiere autenticación)`
* **Descripción:** `Permite visualizar la lista completa del material vegetal disponible en la bodega comunitaria (semillas, estacas, esquejes, plántulas), filtrando por tipo de material o buscando por nombre de la planta medicinal.`
* **Dependencias:** `Depende de BO-CU01 y US-CU01`

### Precondiciones
1. `El usuario debe estar autenticado con rol RESPONSABLE_COMUNIDAD o TECNICO_MSP.`
2. `Existen registros de inventario en la tabla bodega_material_vegetal.`

### Postcondiciones
1. `No modifica el estado de la base de datos (operación de solo lectura).`
2. `El inventario disponible se presenta tabulado en pantalla con sus cantidades y unidades.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El usuario autenticado (**Responsable de Comunidad** o **Técnico MSP**) accede a la ruta privada `"/bodega/inventario"`.
2. El **Sistema** detecta el rol del usuario:
   - Si es **Responsable de Comunidad**, recupera la `comunidadId` asociada a su sesión.
   - Si es **Técnico MSP**, despliega adicionalmente un selector con el componente `Selector` para escoger la comunidad a supervisar.
3. El **Sistema** ejecuta una petición `GET /api/bodega/comunidades/{comunidadId}/material-vegetal`.
4. El **Sistema** retorna el código HTTP `200 OK` con un listado `List<MaterialVegetalResponse>`.
5. El **Sistema** renderiza la grilla de datos utilizando el componente `Tabla` con columnas:
   - Planta (nombre común y científico).
   - Tipo de material (`SEMILLAS`, `ESTACAS`, `ESQUEJES`, `PLANTULAS`).
   - Stock disponible y unidad (`UNIDADES`, `GRAMOS`, `MANOJOS`).
   - Lote / Origen.
   - Fecha de ingreso.
   - Acciones (botón para registrar baja).
6. El usuario puede utilizar el `CampoTexto` de filtrado rápido para filtrar por nombre de planta en tiempo real.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Comunidad sin material vegetal (Lista vacía - HTTP 200 OK):**
  - **Condición:** La comunidad no registra aún ingresos de material vegetal activo.
  - **Comportamiento:** La tabla despliega el mensaje informativo: `"No se registra material vegetal en la bodega de esta comunidad"`.
* **4a. Acceso denegado a otra comunidad (HTTP 403 Forbidden):**
  - **Condición:** Si un Responsable de Comunidad intenta consultar la `comunidadId` perteneciente a otra comunidad.
  - **Comportamiento:** El Sistema deniega la consulta y retorna error 403 con mensaje en `Notificacion`: `"No tiene permisos para consultar el inventario de otra comunidad"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Enfoque de Interfaz (PWA Mobile First):**
  - Prioridad absoluta en dispositivos móviles (visualización de inventario mediante lista o tarjetas adaptables en pantalla pequeña desde 360px, o tabla con scroll horizontal encapsulado).
  - Controles táctiles optimizados (filtros y botones con altura mínima de 44px).
  - Escalado responsivo progresivo a tabla completa en tablets y pantallas de escritorio.
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD` y `TECNICO_MSP`.
- [x] **Visibilidad:** `Privada (Requiere autenticación JWT)`.
- [x] **Feature (`frontend/src/features/bodega/`):**
  - `TablaInventarioBodega.tsx`: Utiliza los componentes `Tabla`, `CampoTexto`, `Selector` y `Boton`.
  - `bodegaApi.ts`: Función `obtenerInventarioPorComunidad(comunidadId: number)` usando `frontend/src/api/client.ts`.
- [x] **Página / Ruteo (`frontend/src/pages/`):**
  - `InventarioBodegaPage.tsx` registrada en `App.tsx` bajo la ruta `/bodega/inventario`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.BodegaController.java`):**
  - `GET /api/bodega/comunidades/{comunidadId}/material-vegetal` -> Retorna `200 OK` con `List<MaterialVegetalResponse>`. Protegido con `@PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'TECNICO_MSP')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `MaterialVegetalResponse.java` (`record` con: `Long id`, `Long comunidadId`, `Long plantaId`, `String nombreComunPlanta`, `String nombreCientificoPlanta`, `TipoMaterialVegetal tipoMaterial`, `BigDecimal cantidadDisponible`, `UnidadMedida unidadMedida`, `String loteOrigen`, `LocalDate fechaIngreso`).
- [x] **Service (`com.huertos.service.BodegaService.java` y `com.huertos.service.impl.BodegaServiceImpl.java`):**
  - Método `List<MaterialVegetalResponse> listarMaterialPorComunidad(Long comunidadId)`.
- [x] **Mapper (`com.huertos.mapper.BodegaMapper.java`):**
  - Método `toListMaterialVegetalResponse(List<BodegaMaterialVegetal> lista)`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `bodega_material_vegetal` en `V4__crear_huertos_bodega_y_siembras.sql` (ya existe; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.BodegaMaterialVegetal.java`):**
  - Mapeo de relación `@ManyToOne` con `Comunidad` y `Planta`.
- [x] **Repository (`com.huertos.repository.BodegaMaterialVegetalRepository.java`):**
  - Método `List<BodegaMaterialVegetal> findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(Long comunidadId);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/BodegaServiceImplTest.java`):** Comprueba retorno de lista filtrada por comunidad y validación de seguridad de comunidad.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/BodegaControllerTest.java`):** Verifica respuesta `200 OK` para `RESPONSABLE_COMUNIDAD` y `TECNICO_MSP`, y `401 Unauthorized` si no hay sesión.
- [x] **Prueba de Componente Frontend (`frontend/src/features/bodega/TablaInventarioBodega.test.tsx`):** Verifica renderizado de la tabla con items provistos por MSW y filtro dinámico por texto.
- [x] **Criterio de Aceptación Mobile First:** El inventario debe visualizarse con claridad en vista móvil sin romper el layout ni generar scroll horizontal global en la ventana.

