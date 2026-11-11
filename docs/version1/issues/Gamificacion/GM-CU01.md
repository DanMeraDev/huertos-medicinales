---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU01: Otorgar puntos a Comunidad, Huerto y Miembros (por actividad MSP validada)"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU01`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación (Sistema)`
* **Rol que ve la pantalla:** `TECNICO_MSP` (al verificar la actividad en SA-CU06)
* **Visibilidad de pantalla:** `Privada (Sin pantalla independiente; proceso de backend transaccional)`
* **Descripción:** `Calcula y acredita automáticamente los puntos cuando el Técnico MSP aprueba una actividad ancestral mayor (Flujo 1): incrementa el puntaje de la Comunidad y del Huerto, y acredita a cada voluntario participante 1 punto por cada hora trabajada en su cuenta individual, disparando el desbloqueo de la insignia oficial (GM-CU03) y la evaluación de ascenso de nivel (GM-CU02).`
* **Dependencias:** `Invocado por SA-CU06 (Verificar evidencias MSP); extiende a GM-CU02 y GM-CU03`

### Precondiciones
1. `Una actividad en actividades_ancestrales_msp pasa a estado APROBADA.`
2. `La actividad tiene asociados registros en actividad_msp_participantes.`

### Postcondiciones
1. `Se actualizan los campos puntos_comunidad en comunidades y puntos_huerto en huertos.`
2. `Para cada participante, se incrementa puntos_totales en la tabla miembros.`
3. `Se invocan GM-CU03 (Insignia oficial) y GM-CU02 (Ascenso de nivel).`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El servicio `MspRevisionService` aprueba la actividad y llama a `GamificacionService.procesarPuntosActividadMsp(actividadId)` en la misma transacción `@Transactional`.
2. El **Sistema** recupera la actividad ancestral con su hito (`saber_ancestral_id`), comunidad, huerto y su listado de participantes.
3. El **Sistema** determina los puntos a acreditar a la comunidad según el número de intento:
   - Intento 1: 100% de `puntos_base_comunidad` (ej. 20 puntos).
   - Intento 2: 70% de `puntos_base_comunidad` (ej. 14 puntos).
4. El **Sistema** actualiza `comunidades.puntos_comunidad` y `huertos.puntos_huerto` sumando los puntos calculados.
5. Para cada participante en `actividad_msp_participantes`:
   - El **Sistema** calcula los puntos del voluntario: `puntos = horas_trabajadas * 1`.
   - El **Sistema** actualiza `actividad_msp_participantes.puntos_acreditados`.
   - El **Sistema** suma dichos puntos a `miembros.puntos_totales`.
   - El **Sistema** evalúa si el miembro asciende de rango (**GM-CU02**).
6. El **Sistema** adjudica la insignia oficial del saber a todos los participantes (**GM-CU03**).
7. La transacción finaliza de forma atómica y consistente.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **2a. Actividad no encontrada o no aprobada (Regla de Integridad):**
  - **Condición:** Si se intenta procesar puntos para una actividad cuyo estado no es `APROBADA`.
  - **Comportamiento:** Se aborta la operación lanzando `ReglaNegocioException`: `"No se pueden otorgar puntos de gamificación en actividades no aprobadas"`.
* **5a. Miembro inactivo:**
  - **Condición:** Si un participante figura como inactivo en la comunidad.
  - **Comportamiento:** El Sistema registra los puntos ganados pero no emite notificaciones activas.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `TECNICO_MSP`.
- [x] **Visibilidad:** `Privada (Requiere autenticación)`.
- [x] **Nota de Interfaz:** No requiere pantalla individual; se ejecuta como parte del dictamen del Técnico MSP en `frontend/src/features/msp-revision/DetalleAuditoriaActividad.tsx`.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Service (`com.huertos.service.GamificacionService.java` y `com.huertos.service.impl.GamificacionServiceImpl.java`):**
  - Método `@Transactional void procesarPuntosActividadMsp(Long actividadId)`.
- [x] **Colaboradores:** Inyecta `ComunidadRepository`, `HuertoRepository`, `MiembroRepository`, `ActividadAncestralMspRepository`, `MiembroInsigniaRepository`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `comunidades`, `huertos`, `miembros`, `actividades_ancestrales_msp`, `actividad_msp_participantes` en `V2__crear_comunidades_y_miembros.sql`, `V4__crear_huertos_bodega_y_siembras.sql` y `V6__crear_saberes_y_actividades_msp.sql` (ya existen; NO crear migraciones).
- [x] **Entidades JPA:** `Comunidad.java`, `Huerto.java`, `Miembro.java`, `ActividadAncestralMsp.java`, `ActividadMspParticipante.java`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/GamificacionServiceImplTest.java`):**
  - Verifica cálculo de puntos al 100% en intento 1 y penalización en intento 2.
  - Verifica acreditación de 1 punto por hora a los miembros participantes.
  - Comprueba invocación a métodos de adjudicación de insignias y ascenso de nivel.
- [x] **Prueba de Transaccionalidad (`backend/src/test/java/com/huertos/service/GamificacionTransaccionalTest.java`):** Asegura que si falla la actualización de un miembro, no se altera el puntaje de la comunidad (rollback).

