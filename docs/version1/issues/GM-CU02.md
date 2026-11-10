---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU02: Ascender automáticamente nivel de miembro (Aprendiz -> Oficial -> Maestro)"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU02`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación (Sistema)`
* **Rol que ve la pantalla:** `Cualquiera (Reflejado en perfil público CM-CU08 y ranking GM-CU05)`
* **Visibilidad de pantalla:** `Privada (Sin pantalla independiente; lógica automática de negocio en backend)`
* **Descripción:** `Evalúa el puntaje acumulado total de un miembro/voluntario tras cualquier acreditación de puntos (sea por actividad MSP o cotidiana) y asciende su rango de forma jerárquica: de APRENDIZ a OFICIAL al alcanzar 50 puntos, y de OFICIAL a MAESTRO al alcanzar 150 puntos.`
* **Dependencias:** `Invocado por GM-CU01 y GM-CU06`

### Precondiciones
1. `El miembro ha recibido nuevos puntos y su campo puntos_totales ha sido actualizado en la tabla miembros.`

### Postcondiciones
1. `Si el puntaje supera el umbral de su nivel actual, el campo nivel en la tabla miembros se actualiza.`
2. `El nuevo rango queda visible de inmediato en la vitrina del miembro y rankings comunitarios.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El motor de gamificación finaliza la suma de puntos a un voluntario e invoca a `GamificacionService.evaluarAscensoNivel(miembroId)`.
2. El **Sistema** consulta el registro del voluntario en la tabla `miembros`: `nivel` actual y `puntos_totales`.
3. El **Sistema** evalúa las reglas de negocio de los umbrales de rango:
   - Si `puntos_totales >= 150` y `nivel != MAESTRO`: el nuevo nivel es `MAESTRO`.
   - Si `puntos_totales >= 50` y `puntos_totales < 150` y `nivel == APRENDIZ`: el nuevo nivel es `OFICIAL`.
   - Si no cumple umbral superior, mantiene su nivel actual.
4. Si hay cambio de nivel:
   - El **Sistema** actualiza `miembros.nivel = nuevoNivel`.
   - El **Sistema** registra en el log del sistema el evento de ascenso honorífico.
5. El **Sistema** retorna el estado del voluntario actualizado.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **3a. Miembro ya posee el rango máximo (MAESTRO):**
  - **Condición:** Si `nivel == MAESTRO`.
  - **Comportamiento:** El Sistema conserva el nivel `MAESTRO` y continúa sumando sus puntos sin degradar ni alterar el registro.
* **3b. El miembro no alcanza el siguiente umbral:**
  - **Condición:** Los puntos totales no superan el valor requerido para la siguiente categoría.
  - **Comportamiento:** No se realiza ninguna actualización sobre el campo `nivel`.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `Público general / Voluntarios / Responsables`.
- [x] **Visibilidad:** El nuevo nivel se refleja en `frontend/src/features/miembros-publico/TarjetaPerfilMiembro.tsx` (CM-CU08) y en la tabla de ranking (GM-CU05).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Service (`com.huertos.service.GamificacionService.java` y `com.huertos.service.impl.GamificacionServiceImpl.java`):**
  - Método `NivelMiembro evaluarAscensoNivel(Long miembroId)`.
- [x] **Constantes de Negocio (`com.huertos.config.ConstantesGamificacion.java` o enum `NivelMiembro.java`):**
  - `UMBRAL_OFICIAL = 50`.
  - `UMBRAL_MAESTRO = 150`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tabla `miembros` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA (`com.huertos.model.Miembro.java`, `NivelMiembro.java`):**
  - Enum con valores `APRENDIZ`, `OFICIAL`, `MAESTRO`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/AscensoNivelTest.java`):**
  - Miembro con 49 puntos permanece en `APRENDIZ`.
  - Miembro que llega a 50 puntos asciende a `OFICIAL`.
  - Miembro que llega a 150 puntos asciende a `MAESTRO`.
  - Miembro con 200 puntos que ya es `MAESTRO` se mantiene en `MAESTRO`.

