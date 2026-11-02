---
name: Caso de Uso
about: Especificación estándar de caso de uso para análisis, implementación técnica y pruebas automatizadas.
title: "CU-XX: [Nombre del Caso de Uso]"
labels: ["caso-de-uso"]
---

## 1. Información General

* **Código:** `MOD-UCXX`
* **Incremento / Sprint:** `[Sprint o Incremento]`
* **Actor Principal:** `[Rol de usuario que ejecuta la acción]`
* **Descripción:** `[Breve resumen de qué permite hacer y cuál es el objetivo de negocio]`
* **Dependencias:** `[Issues requeridos previamente, ej: Depende de #00]`

### Precondiciones
1. `[Estado inicial del sistema, sesión activa o datos requeridos]`

### Postcondiciones
1. `[Cambios persistentes resultantes en el sistema o estado final]`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **[Actor]** accede a `[Ruta / Pantalla / Menú]` y ejecuta la acción `[Nombre de la acción / Botón]`.
2. El **Sistema** presenta `[Formulario / Modal / Vista]` solicitando:
   - `nombreCampo1` (`tipo_dato`, `restricciones`, `obligatorio/opcional`)
   - `nombreCampo2` (`tipo_dato`, `restricciones`, `obligatorio/opcional`)
3. El **[Actor]** diligencia los datos solicitados y confirma mediante `[Botón de confirmación]`.
4. El **Sistema** valida formatos de entrada, reglas de negocio y restricciones de unicidad.
5. El **Sistema** procesa la información y aplica los valores por defecto correspondientes.
6. El **Sistema** persiste los cambios y retorna `HTTP [Código de éxito: 200 OK / 201 Created]` con el recurso resultante.
7. El **Sistema** muestra mensaje de confirmación y actualiza la vista correspondiente.

---

## 3. Escenarios Alternativos y Excepciones

<!-- Formato: [Paso][Letra]. [Condición de fallo] (Código HTTP): comportamiento del sistema -->
* **[Paso]a. Error de validación de campos obligatorios o formato (HTTP 400 Bad Request):**
  - **Condición:** Si `[campo]` no cumple con el formato o se encuentra vacío.
  - **Comportamiento:** El Sistema rechaza la petición, retorna el detalle del campo erróneo y mantiene los datos válidos en pantalla.
* **[Paso]b. Conflicto por recurso duplicado (HTTP 409 Conflict):**
  - **Condición:** Si ya existe un registro con el mismo `[campo único / identificador]`.
  - **Comportamiento:** El Sistema rechaza la operación e informa al usuario: *"Mensaje de error esperado"*.
* **[Paso]c. Recurso relacionado no encontrado (HTTP 404 Not Found):**
  - **Condición:** Si el identificador foráneo `[id_recurso]` no existe en la persistencia.
  - **Comportamiento:** El Sistema interrumpe la ejecución y retorna la excepción de recurso no encontrado.

---

## 4. Análisis y Diseño Técnico

### 4.1. Capa Frontend (Boundary)
- [ ] **Componentes / Vistas (`src/features/[modulo]/...`):**
  - `Formulario[Recurso].tsx`: Formulario con inputs controlados y validaciones del lado del cliente.
  - `Tabla[Recurso].tsx` o `Detalle[Recurso].tsx`: Renderizado de datos y manejo de estados (loading, error, éxito).
- [ ] **Cliente API (`src/features/[modulo]/api/...`):**
  - Funciones de consumo HTTP tipadas (`crear...`, `listar...`, `actualizar...`).

### 4.2. Capa Backend (Control & Lógica de Negocio)
- [ ] **Controller (`[paquete].controller`):**
  - `[METODO_HTTP] /api/[recursos]` -> Recibe `[Nombre]Request`, retorna `HTTP [Código]` con `[Nombre]Response`.
- [ ] **Service & Dominio (`[paquete].service`):**
  - Interfaz e implementación con reglas de validación, manejo de excepciones y lógica de dominio.
- [ ] **DTOs / Records / Mappers:**
  - Definición de contratos de entrada/salida y mapeo hacia/desde entidades.

### 4.3. Capa de Datos y Persistencia (Entity)
- [ ] **Migración de Base de Datos:**
  - Script de migración (ej: `VXX__[descripcion].sql`) con creación de tablas, llaves foráneas e índices.
- [ ] **Entidad (`[paquete].model` o `[paquete].entity`):**
  - Mapeo de la tabla `[nombre_tabla]` con sus atributos y relaciones.
- [ ] **Repository (`[paquete].repository`):**
  - Métodos de consulta y persistencia necesarios.

---

## 5. Criterios de Aceptación y Pruebas Requeridas

- [ ] **Prueba Unitaria de Servicio:** Cobertura de lógica de negocio, validaciones y flujos alternativos.
- [ ] **Prueba de Integración / Controlador:** Verificación de serialización, códigos HTTP de respuesta y filtros de excepción.
- [ ] **Prueba de Componente Frontend:** Comprobación de renderizado inicial, interacción del usuario y manejo de respuestas mockeadas.
- [ ] **Aceptación Funcional (E2E / Manual):** Ejecución completa del flujo principal sin anomalías de interfaz ni errores de consola.
