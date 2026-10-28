# Estándar de GitHub Issues

Todo el trabajo del proyecto se registra como issue en GitHub y se sigue en el tablero de GitHub Projects. Si no hay issue, no hay rama ni Pull Request.

Documentos relacionados: [Flujo de trabajo con Git](flujo-de-trabajo-git.md) · [Estándar de commits](estandar-commits.md)

## 1. Tipos de issue

Cada tipo tiene su plantilla en *New issue*.

| Tipo | Plantilla | Formato del título | Ejemplo |
|---|---|---|---|
| Caso de uso | Caso de uso | `CU-XX: <nombre del caso de uso>` | `CU-01: Registro de comunidad` |
| Defecto | Defecto | `BUG: <qué falla, dónde>` | `BUG: El ranking muestra comunidades duplicadas` |
| Tarea técnica | Tarea técnica | `TAREA: <qué se hace>` | `TAREA: Configurar SonarCloud en el CI` |

- **Casos de uso:** corresponden 1:1 con los casos de uso del documento de análisis del incremento. El código `CU-XX` es el mismo del documento y no se reutiliza.
- **Defectos:** los reporta normalmente QA al probar un incremento, enlazados al caso de prueba de Qase.
- **Tareas técnicas:** trabajo que no es un caso de uso: configuración, infraestructura, documentación, diagramas.

## 2. Etiquetas

Cada issue lleva **una etiqueta de tipo**, **al menos una de área** y **una de prioridad**. Los defectos llevan además **una de severidad**.

| Grupo | Etiquetas |
|---|---|
| Tipo | `caso-de-uso`, `bug`, `tarea` |
| Área | `back`, `front`, `db`, `infra`, `docs` |
| Prioridad | `imprescindible`, `importante`, `deseable` |
| Severidad (solo defectos) | `sev:crítica`, `sev:alta`, `sev:media`, `sev:baja` |
| Estado especial | `bloqueado`, `necesita-info` |

Las prioridades son las mismas de la propuesta de alcance: si falta tiempo, se dejan primero las `deseable`.

### Severidad de defectos

| Severidad | Criterio | Ejemplo |
|---|---|---|
| `sev:crítica` | Bloquea un flujo imprescindible o pierde datos | No se puede iniciar sesión |
| `sev:alta` | Una funcionalidad no sirve, pero hay forma de seguir | No se guardan las fotos de las actividades |
| `sev:media` | Funciona con un resultado incorrecto o incómodo | El ranking no ordena bien los empates |
| `sev:baja` | Detalle visual o de texto | Falta una tilde en un botón |

Los defectos `sev:crítica` y `sev:alta` se corrigen antes de cerrar el incremento.

## 3. Hitos (milestones)

Cada issue se asigna al hito del incremento en que se trabajará:

- `Incremento 1`
- `Incremento 2`
- `Incremento 3`
- `Cierre`

Las fechas de cada hito son las del cronograma del equipo. Un issue que no se alcanzó a terminar se pasa al hito siguiente; no se cierra sin terminar.

## 4. Tablero de GitHub Projects

| Columna | Significado | Quién lo mueve |
|---|---|---|
| **Backlog** | Registrado, aún sin planificar | Coordinación |
| **Por hacer** | Planificado para el incremento en curso y listo para empezar | Coordinación |
| **En progreso** | Alguien lo está trabajando en su rama | Quien lo tiene asignado |
| **En revisión** | Tiene Pull Request abierto | Quien abrió el PR |
| **En QA** | Fusionado en `develop`, pendiente de validar en Qase | Automático al fusionar |
| **Hecho** | Validado por QA | QA |

Un issue solo pasa a *Por hacer* si cumple la definición de listo, y a *Hecho* si cumple la definición de terminado.

## 5. Definición de listo

Un caso de uso está listo para empezar cuando:

- Tiene actor, flujo principal y criterios de aceptación verificables.
- Las entidades que usa están en el diagrama de clases validado.
- Tiene etiquetas, hito y responsable asignado.
- No depende de otro issue abierto o, si depende, está enlazado.

## 6. Definición de terminado

Un caso de uso está terminado cuando:

- Cumple todos sus criterios de aceptación.
- Tiene pruebas unitarias y de integración escritas antes del código, y pasan.
- Sus endpoints están documentados en Swagger.
- El Pull Request fue aprobado y fusionado en `develop` con el CI en verde.
- QA ejecutó sus casos de prueba en Qase sin defectos `sev:crítica` ni `sev:alta` abiertos.

## 7. Cómo escribir un buen issue

- **Título:** concreto y con el formato del tipo. "CU-03: Registro de huerto real" en lugar de "huerto".
- **Criterios de aceptación:** verificables, cada uno como casilla. "Al guardar sin provincia se muestra el mensaje *La provincia es obligatoria*" en lugar de "valida bien".
- **Defectos reproducibles:** pasos numerados, resultado esperado, resultado obtenido y evidencia (captura, respuesta del API o log).
- **Una cosa por issue:** si aparecen dos problemas, son dos issues.
- **Conversación en el issue:** las dudas y decisiones sobre la tarea se comentan en el propio issue, no solo en WhatsApp, para que quede registro.

## 8. Enlace entre issue, rama, commit y PR

| Elemento | Cómo referencia al issue |
|---|---|
| Rama | `feature/CU-01-registro-comunidad` o `fix/45-ranking-duplicado` |
| Commit | Pie `Refs #12` o `Cierra #12` |
| Pull Request | `Cierra #12` en la descripción |

Con `Cierra #12` en el PR, GitHub cierra el issue al fusionarlo en `develop` (la rama por defecto del repositorio) y lo muestra enlazado en ambos lados. El tablero lo mueve entonces a *En QA*.
