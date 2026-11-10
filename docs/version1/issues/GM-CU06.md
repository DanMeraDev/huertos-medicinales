---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "GM-CU06: Otorgar puntos solo a Miembro y validar talento comunitario (ej. 50 veces)"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `GM-CU06`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación (Sistema)`
* **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD` (al registrar la actividad en HR-CU08)
* **Visibilidad de pantalla:** `Privada (Sin pantalla independiente; proceso de backend transaccional)`
* **Descripción:** `Calcula y acredita automáticamente los puntos de actividades cotidianas internas (Flujo 2): suma puntos exclusivamente a la cuenta personal del miembro/voluntario (1 hora = 1 punto base), sin sumar puntos a la comunidad ni al huerto. Adicionalmente, computa las repeticiones acumuladas de la actividad y, si el miembro alcanza la meta requerida (ej. 50 repeticiones de abonaje), desbloquea el Talento Comunitario correspondiente en su vitrina personal.`
* **Dependencias:** `Invocado por HR-CU08 (Registrar actividad cotidiana a miembro); extiende a GM-CU02`

### Precondiciones
1. `El Responsable de Comunidad registra una actividad cotidiana en registro_actividades_miembro.`
2. `La actividad pertenece a catalogo_actividades_comunidad con sus puntos configurados.`

### Postcondiciones
1. `Se incrementa puntos_totales en la tabla miembros.`
2. `Los campos puntos_comunidad y puntos_huerto NO son modificados.`
3. `Si se cumple la meta de repeticiones en talentos_comunidad, se inserta el registro en miembro_insignias con tipo_origen = 'TALENTO_COMUNIDAD'.`
4. `Se invoca GM-CU02 (Ascenso de nivel).`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. En el marco del guardado de **HR-CU08**, el Sistema invoca a `GamificacionService.procesarActividadCotidiana(registroActividadId)` dentro de la transacción activa.
2. El **Sistema** recupera el registro de la actividad ejecutada, el miembro voluntario y el catálogo asociado.
3. El **Sistema** calcula los puntos a otorgar: `puntos = horas_trabajadas * actividadCatalogo.puntos_por_hora`.
4. El **Sistema** actualiza `registro_actividades_miembro.puntos_otorgados = puntos` y suma los puntos a `miembros.puntos_totales`.
5. El **Sistema** verifica si existe un talento configurado en `talentos_comunidad` asociado a esa `actividad_comunidad_id` y comunidad:
   - Si existe:
     - Cuenta el total de actividades registradas por el miembro para esa actividad específica: `COUNT(*) WHERE miembro_id = X AND actividad_comunidad_id = Y`.
     - Si el conteo es mayor o igual a `meta_repeticiones` (ej. 50) y el miembro aún no posee el talento en `miembro_insignias`:
       - Inserta el nuevo registro en `miembro_insignias` con `tipo_origen = TALENTO_COMUNIDAD`, `nombre_insignia = talento.nombre_insignia`, `icono_codigo = talento.icono_codigo` y `fecha_obtencion = LocalDate.now()`.
6. El **Sistema** invoca a **GM-CU02** para comprobar si el miembro asciende de nivel (Aprendiz -> Oficial -> Maestro).
7. La transacción finaliza de forma atómica y exitosa.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **5a. Meta de repeticiones no alcanzada:**
  - **Condición:** El voluntario aún no alcanza el número de veces requerido (ej. lleva 32 de 50).
  - **Comportamiento:** Se suman los puntos individuales pero no se desbloquea la insignia de talento comunitario.
* **5b. Talento ya desbloqueado con anterioridad:**
  - **Condición:** El miembro ya cuenta con la insignia de ese talento comunitario.
  - **Comportamiento:** Se omite la inserción para prevenir duplicidad en su vitrina.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- [x] **Rol que ve la pantalla:** `RESPONSABLE_COMUNIDAD`.
- [x] **Visibilidad:** `Privada (Requiere autenticación)`.
- [x] **Nota de Interfaz:** No requiere componente frontend propio; la acreditación se realiza al enviar el formulario en `frontend/src/features/huerto/FormularioActividadCotidiana.tsx` (HR-CU08).

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Service (`com.huertos.service.GamificacionService.java` y `com.huertos.service.impl.GamificacionServiceImpl.java`):**
  - Método `@Transactional void procesarActividadCotidiana(Long registroActividadId)`.
- [x] **Colaboradores:** Inyecta `MiembroRepository`, `RegistroActividadesMiembroRepository`, `TalentoComunidadRepository`, `MiembroInsigniaRepository`.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:** Tablas `registro_actividades_miembro`, `talentos_comunidad`, `miembro_insignias` en `V2__esquema_completo_v1.sql`.
- [x] **Entidad JPA:** `RegistroActividadesMiembro.java`, `TalentoComunidad.java`, `MiembroInsignia.java`.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio (`backend/src/test/java/com/huertos/service/GamificacionCotidianaTest.java`):**
  - Comprueba que los puntos se sumen únicamente a `miembros.puntos_totales` y que `comunidades.puntos_comunidad` permanezca inalterado.
  - Verifica adjudicación del talento comunitario al cumplir exactamente la meta de repeticiones (ej. 50).
  - Verifica que no se duplique la medalla en repeticiones posteriores (ej. ejecución 51).

