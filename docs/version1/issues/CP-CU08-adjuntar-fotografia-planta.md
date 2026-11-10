---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA con enfoque Mobile First y escalabilidad responsiva a PC.
title: "CP-CU08: Adjuntar fotografía de la planta"
labels: ["caso-de-uso", "back", "front", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `CP-CU08`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Técnico MSP`
* **Descripción:** El sistema deberá permitir al Técnico MSP adjuntar la fotografía de la planta medicinal de forma adaptable: en smartphones mediante captura directa con la cámara o galería móvil, y en computadoras de escritorio (PC) mediante una **zona amplia de arrastrar y soltar (*Drag & Drop*)** con soporte para pegar imágenes directamente desde el portapapeles (`Ctrl + V`).
* **Dependencias:** Es incluido por `CP-CU05` y extiende a `CP-CU06`.

### Precondiciones
1. El usuario posee rol `TECNICO_MSP`.
2. El archivo de imagen cumple con el formato permitido (JPG, PNG o WEBP) y tamaño <= 5 MB.

### Postcondiciones
1. El sistema almacenará el archivo y generará la URL persistente `fotografia_url`.

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

### 2.1. Arquitectura y Disposición Visual Responsiva
* **Tokens de Estilo (`frontend/src/index.css`):**
  - Fondo de zona de subida: `var(--color-superficie-alt)` con borde punteado en `var(--color-borde)` y radio `var(--radio-md)`.
  - Estado activo de arrastre: Borde resaltado con `var(--color-primario)` y fondo con tinte suave.

* **A. Enfoque Móvil Base (Viewport < 768px):**
  - Recuadro táctil de 160px de alto: *"Toca para tomar foto o elegir de tu galería"*, que invoca el selector nativo móvil de cámara o archivos.

* **B. Adaptación Responsiva a Escritorio / PC (Viewport >= 1024px - Progressive Enhancement):**
  - **Zona Interactiva Drag & Drop (Altura 200px - 240px):**
    - Área amplia con icono de nube/archivo, texto principal: *"Arrastra y suelta la fotografía aquí"* y subtítulo: *"o haz clic para explorar archivos (JPG, PNG, WEBP hasta 5 MB)"*.
    - **Soporte de Portapapeles:** Permite pegar una captura de imagen directamente con el atajo de teclado `Ctrl + V` cuando el formulario está activo.
    - **Previsualización en PC:** Muestra la imagen cargada con controles contextuales para previsualizar a tamaño completo en modal, cambiar archivo o eliminarlo.

### 2.2. Flujo Interactivo Paso a Paso
1. El **Técnico MSP** interactuará con la zona de carga (tocando en smartphone o arrastrando/soltando/pegando en PC).
2. El **Sistema** frontend validará en cliente el formato y tamaño (< 5 MB).
3. El **Sistema** frontend renderizará la previsualización inmediata.
4. Al enviar el formulario, el **Sistema** frontend enviará la imagen mediante HTTP `POST /api/msp/plantas/fotografia` como `multipart/form-data`.
5. El **Sistema** backend validará la firma de bytes, almacenará la imagen y responderá con código HTTP `200 OK` y el DTO `FotografiaSubidaResponse`.
6. El **Sistema** frontend asignará la URL devuelta al campo `fotografiaUrl`.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Formato inválido o archivo corrupto (HTTP 415):**
  - **Condición:** Si no es un archivo JPEG, PNG o WEBP.
  - **Comportamiento:** Se rechaza el archivo y se muestra una notificación con `Notificacion`: *"Formato no admitido. Seleccione una imagen JPG, PNG o WEBP"*.
* **3b. Archivo mayor a 5 MB (HTTP 413):**
  - **Condición:** Si la imagen excede el límite de peso.
  - **Comportamiento:** El frontend avisa: *"La imagen supera el límite de 5 MB. Intente reducir la resolución"*.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Componentes Reutilizados de `frontend/src/components/`:**
  - `Boton`: Botones de reemplazo o eliminación de foto.
  - `Notificacion`: Alertas por tamaño o formato no admitido.
- [x] **Nuevo Componente Común Sugerido en `frontend/src/components/`:**
  - `CargadorImagen.tsx`: Componente responsivo que soporta cámara en smartphone y drag & drop en escritorio.
- [x] **Feature (`frontend/src/features/catalogo-admin/`):**
  - `plantasAdminApi.ts`: Función `subirFotografiaPlanta(archivo: File)`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller (`com.huertos.controller.PlantaAdminController.java`):**
  - `POST /api/msp/plantas/fotografia` con `@PreAuthorize("hasRole('TECNICO_MSP')")`. Retorna `200 OK`.
- [x] **DTOs:** `FotografiaSubidaResponse.java` (`record`).
- [x] **Service:** `AlmacenamientoService.java`.

### 4.3. Objetos Entity y Base de Datos (`docs/version1/V2__esquema__completo_v1.sql`)
- [x] **Tabla `plantas`:** `fotografia_url`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/AlmacenamientoServiceImplTest.java`):** Valida almacenamiento y UUID.
- [x] **Prueba de Componente Frontend (`frontend/src/features/catalogo-admin/CargadorImagen.test.tsx`):** Valida evento drag & drop en desktop y captura en móvil.
