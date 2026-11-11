---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU03: Desbloquear insignia oficial por saber ancestral"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU03`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación (Sistema)`
* **Rol que ve la pantalla:** `Cualquiera (Visualizado en el perfil público CM-CU08)`
* **Visibilidad de pantalla:** `Privada (Sin pantalla independiente; adjudicación interna de backend)`
* **Descripción:** `Adjudica automáticamente la medalla o insignia oficial del Ministerio de Salud Pública a la vitrina digital de cada voluntario participante cuando una actividad ancestral mayor es validada con éxito por el Técnico MSP, evitando duplicidad de la misma insignia por el mismo hito.`
* **Dependencias:** `Invocado por GM-CU01 tras la aprobación de SA-CU06`

### Precondiciones
1. `Una actividad ancestral fue dictaminada con estado APROBADA.`
2. `El saber ancestral cuenta con un nombre_insignia_oficial configurado en saberes_ancestrales.`

### Postcondiciones
1. `Se inserta un registro en la tabla miembro_insignias por cada participante de la actividad que no la poseía previamente.`
2. `La insignia queda ligada a saber_ancestral_id con tipo_origen = 'OFICIAL_SABER_MSP'.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. En el marco de la ejecución de **GM-CU01**, el Sistema invoca a `GamificacionService.otorgarInsigniaOficial(actividadId)`.
2. El **Sistema** recupera la lista de `miembro_id` de los participantes de la actividad y los datos del saber ancestral asociado (`nombre_insignia_oficial`, `titulo`).
3. Para cada miembro participante:
   - El **Sistema** consulta en `miembro_insignias` si el voluntario ya tiene una insignia con `saber_ancestral_id` igual al del hito.
   - Si no la tiene:
     - Crea la entidad `MiembroInsignia` con `tipo_origen = OFICIAL_SABER_MSP`.
     - Asigna `nombre_insignia = saber.nombre_insignia_oficial`.
     - Asigna `descripcion = "Insignia oficial obtenida por completar el hito: " + saber.titulo`.
     - Asigna `icono_codigo = "medalla-kinti"`.
     - Asigna `fecha_obtencion = LocalDate.now()`.
     - Guarda el registro en `miembro_insignias`.
4. El **Sistema** finaliza el proceso dejando la vitrina de medallas actualizada.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Insignia ya otorgada previamente al miembro:**
  - **Condición:** El miembro ya había obtenido esta misma insignia en otra actividad o período.
  - **Comportamiento:** El Sistema omite la inserción duplicada para ese voluntario específico, manteniendo la fecha de obtención original.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `Público general / Voluntarios / Responsables`.
- [x] **Visibilidad:** Las insignias otorgadas se visualizan mediante el componente `Insignia` en la vitrina pública de `frontend/src/features/miembros-publico/TarjetaPerfilMiembro.tsx` (CM-CU08).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Service (`com.huertos.service.GamificacionService.java` y `com.huertos.service.impl.GamificacionServiceImpl.java`):**
  - Método `@Transactional void otorgarInsigniaOficial(Long actividadId)`.
- [x] **Repository (`com.huertos.repository.MiembroInsigniaRepository.java`):**
  - Método `boolean existsByMiembroIdAndSaberAncestralId(Long miembroId, Long saberAncestralId);`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `miembro_insignias` en `V7__crear_insignias_de_miembros.sql` (ya existe; NO crear migraciones).
- [x] **Entidad JPA (`com.huertos.model.MiembroInsignia.java`, `TipoOrigenInsignia.java`):**
  - Relaciones con `Miembro`, `Comunidad` y `SaberAncestral`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/InsigniasOficialesTest.java`):**
  - Verifica creación exitosa de `MiembroInsignia` con origen `OFICIAL_SABER_MSP`.
  - Verifica que no se creen registros duplicados si el miembro ya poseía la insignia.

