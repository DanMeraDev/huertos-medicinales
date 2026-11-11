---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "BO-CU03: Descontar material vegetal por siembra"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `BO-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor del Sistema / Huerto Real (Interno)`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD` (a través del flujo de siembra de huerto)
* **Visibilidad de pantalla:** `Privada (Sin pantalla independiente; servicio backend incluido en HR-CU01)`
* **Descripción:** `Descuenta automáticamente la cantidad de semillas, estacas, esquejes o plántulas del stock de bodega al registrarse una siembra en cualquier subhuerto de la comunidad, garantizando la consistencia transaccional y evitando saldos negativos de inventario.`
* **Dependencias:** `Depende de BO-CU01 (Ingreso de material) e incluido por HR-CU01 (Registrar siembra)`

### Precondiciones
1. `Existe un registro activo de material vegetal en bodega_material_vegetal con cantidad_disponible mayor a cero.`
2. `El Responsable de Comunidad ejecuta la siembra con una cantidad menor o igual al stock disponible.`

### Postcondiciones
1. `El campo cantidad_disponible en bodega_material_vegetal se decrementa en la cantidad sembrada dentro de la misma transacción.`
2. `Si la cantidad llega a cero, el registro se mantiene activo con saldo 0 o se notifica stock agotado.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El flujo de backend de **HR-CU01 (Registrar siembra)** invoca de manera interna al servicio `BodegaService.descontarStockPorSiembra(bodegaMaterialId, cantidadSembrada)`.
2. El **Sistema** recupera el registro de `BodegaMaterialVegetal` aplicando bloqueo pesimista o verificación de versión para concurrencia.
3. El **Sistema** verifica que `cantidad_disponible >= cantidadSembrada`.
4. El **Sistema** resta `cantidadSembrada` de `cantidad_disponible`.
5. El **Sistema** guarda los cambios en la base de datos dentro de la misma transacción `@Transactional`.
6. El **Sistema** confirma la operación al servicio invocador para proceder con la inserción en la tabla `siembras`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Stock insuficiente para la siembra (HTTP 400 Bad Request / Negocio):**
  - **Condición:** Si `cantidadSembrada > cantidad_disponible`.
  - **Comportamiento:** El Sistema aborta la transacción y lanza `ReglaNegocioException`: `"Stock insuficiente en bodega para realizar la siembra. Disponible: X, Solicitado: Y"`.
* **3b. Material vegetal no encontrado o inactivo (HTTP 404 Not Found):**
  - **Condición:** Si el `bodegaMaterialId` no existe o tiene `activo = false`.
  - **Comportamiento:** Lanza `RecursoNoEncontradoException`: `"El material vegetal especificado no existe en la bodega de la comunidad"`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación)`.
- [x] **Nota de Interfaz:** No requiere componente frontend propio dedicado; el selector de material e indicador de stock disponible se consumen en el modal/formulario de siembra de `frontend/src/features/huerto/FormularioSiembra.tsx`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Service (`com.huertos.service.BodegaService.java` y `com.huertos.service.impl.BodegaServiceImpl.java`):**
  - Método `@Transactional void descontarStockPorSiembra(Long bodegaMaterialId, BigDecimal cantidadSembrada)`.
- [x] **DTOs (`com.huertos.dto`):**
  - `DescuentoStockRequest.java` (`record` con: `@NotNull Long bodegaMaterialId`, `@NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal cantidadSembrada`).

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `bodega_material_vegetal` y `siembras` en `V4__crear_huertos_bodega_y_siembras.sql` (ya existen; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.BodegaMaterialVegetal.java`):**
  - Campo `cantidadDisponible` con validación de no negatividad.
- [x] **Repository (`com.huertos.repository.BodegaMaterialVegetalRepository.java`):**
  - Método con lock: `@Lock(LockModeType.PESSIMISTIC_WRITE) Optional<BodegaMaterialVegetal> findByIdAndActivoTrue(Long id);`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/BodegaServiceImplTest.java`):**
  - Prueba de descuento exitoso de existencias.
  - Prueba de lanzamiento de `ReglaNegocioException` cuando la cantidad a descontar supera el stock actual.
- [x] **Prueba de Integración transaccional (`backend/src/test/java/com/huertos/service/SiembraBodegaIntegrationTest.java`):**
  - Verifica rollback total si el descuento de bodega falla al intentar registrar una siembra.

