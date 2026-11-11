---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "HR-CU03: Completar datos operativos del huerto (suelo, agua, abono)"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `HR-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Responsable de Comunidad` *(Hereda para auditoría en sitio: Técnico MSP)*
* **Descripción:** El sistema deberá permitir al Responsable de Comunidad registrar y actualizar la configuración agronómica, territorial y física del huerto comunitario mediante una interfaz responsiva: en smartphones se estructura en tres pestañas táctiles deslizables (*Mobile Tabs*), mientras que en computadoras de escritorio (PC) se despliega en un **layout de pantalla dividida con mapa interactivo de ubicación a la izquierda y formulario agronómico en cuadrícula a la derecha**.
* **Dependencias:** Es extendido opcionalmente por `HR-CU04` (Configurar subhuertos).

### Precondiciones
1. El usuario autenticado posee rol `RESPONSABLE_COMUNIDAD` y su comunidad cuenta con huerto registrado.
2. Endpoint protegido para el rol de responsable y técnico MSP.

### Postcondiciones
1. El sistema actualizará las columnas operativas en la tabla `huertos`.
2. Las áreas y altitudes registradas regirán las validaciones de siembras (`HR-CU01`) y subhuertos (`HR-CU04`).

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo general: `var(--color-fondo)`.
  - Superficie de tarjetas: `var(--color-superficie)` con bordes `var(--color-borde)` y sombra `var(--sombra-sm)`.
  - Indicador de área MSP: `var(--color-exito)` dentro del rango [16.00 m² - 400.00 m²] y `var(--color-error)` fuera de norma.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Fila superior con 3 pestañas táctiles horizontales: `[ 1. Terreno y Área ]`, `[ 2. Suelo y Agua ]`, `[ 3. Cerramiento ]`.
  - Botón táctil para capturar GPS en terreno y botones inferiores *"Siguiente"* / *"Guardar Datos del Huerto"*.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Layout de Pantalla Dividida (Split View 45% - 55%):**
    - **Panel Izquierdo (Georreferenciación y Superficie):**
      - Tarjeta interactiva de mapa (visor de coordenadas) donde el usuario puede arrastrar un marcador (*pin*) para fijar la ubicación exacta del huerto, con botón de sincronización GPS.
      - Campos en paralelo: `Latitud`, `Longitud` y `Altitud (msnm)`.
      - **Superficie y Diseño:** Campo de área total con medidor gráfico de cumplimiento normativo MSP (16 a 400 m²), selector de orientación (`ESTE_OESTE`, `NORTE_SUR`) y cuadrícula 2x2 para diseño principal (`[ En Tierra ]` `[ Vertical ]` `[ Colgante ]` `[ En Piso ]`).
    - **Panel Derecho (Recursos Agronómicos y Cerramiento Perimetral):**
      - Tarjeta 1: *Suelo y Agua* (cuadrícula de chips táctiles para tipo de suelo y fuentes de riego).
      - Tarjeta 2: *Abonos Naturales* (chips de abonos orgánicos tradicionales).
      - Tarjeta 3: *Cerramiento Perimetral* (tipo de cerca protectora y descripción de especies vegetales o materiales).
      - **Barra de Acciones al Pie (Footer):** Botón **`"Guardar Configuración del Huerto"`** (`Boton` primario, 48px de alto) alineado a la derecha.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Responsable de Comunidad** accederá a `"/huerto/configuracion"`.
2. El **Sistema** precargará los datos actuales mediante `GET /api/comunidad/huerto`.
3. El usuario interactuará con los controles: mediante pestañas en smartphone o mediante el panel dividido en PC.
4. El usuario completará o ajustará los datos agronómicos y el área total (ej: 150 m²).
5. El usuario pulsará **`"Guardar Datos del Huerto"`** (`Boton`).
6. El **Sistema** frontend enviará la petición HTTP `PUT /api/comunidad/huerto` con `ActualizarHuertoRequest`.
7. El **Sistema** backend verificará que el área esté entre 16.00 y 400.00 m² y no sea menor que la suma de sus subhuertos.
8. El **Sistema** backend persistirá los cambios y retornará `200 OK` con `HuertoDetalleResponse`.
9. El **Sistema** frontend mostrará una notificación flotante con `Notificacion`: *"Configuración del huerto actualizada con éxito"*.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Superficie fuera de norma MSP (< 16 m² o > 400 m²):**
  - **Condición:** Si el área está fuera de los límites.
  - **Comportamiento:** La barra de área resalta en `var(--color-error)` y se bloquea el guardado: *"El huerto debe medir entre 16.00 m² y 400.00 m² según lineamientos MSP"*.
* **7a. Reducción de área incompatible con subhuertos existentes:**
  - **Condición:** Si el área total nueva es menor a la suma de los subhuertos registrados.
  - **Comportamiento:** El Sistema backend lanza `ReglaNegocioException` y el frontend alerta: *"No puedes reducir a [X] m² porque tus subhuertos ocupan [Y] m²"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `CampoTexto`: Nombre, coordenadas, altitud, área y cerca.
  - `Boton`: Botones de diseño y botón de guardado.
  - `Notificacion`: Alertas de validación y éxito.
- [x] **Feature (`frontend/src/features/huerto-configuracion/`):**
  - `ConfiguracionHuertoView.tsx`: Componente responsivo (tabs en móvil / split view con mapa en PC).
  - `huertoApi.ts`: Funciones `obtenerHuertoComunidad()` y `actualizarDatosOperativos(datos)`.
- [x] **Página:** `ConfiguracionHuertoPage.tsx` bajo `/huerto/configuracion`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.HuertoController.java`):**
  - `GET /api/comunidad/huerto` y `PUT /api/comunidad/huerto` con `@PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'TECNICO_MSP')")`.
- [x] **DTOs (`com.huertos.dto`):**
  - `ActualizarHuertoRequest.java` (`record`):
    - `latitud`, `longitud`: `BigDecimal`, obligatorios.
    - `altitudMsnm`: `Integer`, obligatorio (`@PositiveOrZero`).
    - `orientacion`: `String`, opcional (`ESTE_OESTE` o `NORTE_SUR`; la columna es VARCHAR, no hay enum).
    - `areaTotalM2`: `BigDecimal`, obligatorio en este caso de uso, entre 16.00 y 400.00 (`@DecimalMin("16.00")`, `@DecimalMax("400.00")`).
    - `disenoPrincipal`: `DisenoPrincipal`, obligatorio.
    - `tipoSuelo`: `TipoSuelo`, `fuenteAgua`: `FuenteAgua`, `tipoAbono`: `TipoAbono`, `tipoCerca`: `TipoCerca`, opcionales.
    - `detalleCerca`: `String`, opcional (`@Size(max = 255)`).
  - `HuertoDetalleResponse.java` (`record`): `Long id`, `Long comunidadId`, `String nombreHuerto`, los mismos campos del request, `TipoHuerto tipoHuerto`, `int puntosHuerto` y `BigDecimal areaOcupadaSubhuertosM2`.
  - No se modifican `nombre_huerto`, `comunidad_id`, `tipo_huerto` ni `puntos_huerto` en este caso de uso.
- [x] **Service:** `HuertoService.java`.

### 4.3. Objetos Entity y Base de Datos (migraciones en `backend/src/main/resources/db/migration/`: `V4__crear_huertos_bodega_y_siembras.sql`; ya existen, NO crear migraciones)
- [x] **Tabla `huertos`:** la fila ya existe (se crea al aprobar la solicitud en `US-CU06` con `area_total_m2 = NULL`); aquí solo se actualiza. Entidad `com.huertos.model.Huerto` (ya existe).
- [x] **Tabla `huertos`:** Clave única sobre `comunidad_id`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/HuertoServiceImplTest.java`):** Valida límites de área y coherencia con subhuertos.
- [x] **Prueba de Controlador (`backend/src/test/java/com/huertos/controller/HuertoControllerTest.java`):** Retorna `200 OK` para `RESPONSABLE_COMUNIDAD`.
- [x] **Prueba de Componente Frontend (`frontend/src/features/huerto-configuracion/ConfiguracionHuertoView.test.tsx`):** Valida cambio entre pestañas móviles y layout de 2 columnas en desktop.
