# Estándar de commits

Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/), escritos en español. Un historial uniforme permite saber qué cambió en cada incremento sin abrir el código, y ubicar rápido el commit que introdujo un defecto.

Documentos relacionados: [Flujo de trabajo con Git](flujo-de-trabajo-git.md) · [Estándar de GitHub Issues](estandar-issues.md)

## 1. Formato

```
<tipo>(<ámbito>): <descripción>

<cuerpo opcional>

<pie opcional>
```

Ejemplo mínimo:

```
feat(back): agrega endpoint de registro de comunidad
```

Ejemplo completo:

```
fix(back): evita puntos duplicados al registrar la misma actividad

Si el usuario enviaba el formulario dos veces seguidas, se creaban dos
actividades y la comunidad sumaba el doble de puntos. Ahora se valida
que no exista otra actividad igual en el mismo huerto y fecha.

Cierra #45
```

## 2. Tipo

| Tipo | Cuándo se usa |
|---|---|
| `feat` | Funcionalidad nueva para el usuario |
| `fix` | Corrección de un defecto |
| `test` | Agrega o corrige pruebas, sin cambiar el código de producción |
| `refactor` | Cambia la estructura del código sin cambiar su comportamiento |
| `docs` | Solo documentación (README, Javadoc, Swagger, docs/) |
| `style` | Formato del código: espacios, sangría, orden de imports. No cambia lógica |
| `build` | Dependencias, `pom.xml`, `package.json`, Dockerfile |
| `ci` | Workflows de GitHub Actions |
| `chore` | Tareas de mantenimiento que no encajan en lo anterior |
| `release` | Cierre de incremento (solo en el PR de `develop` a `main`) |

## 3. Ámbito

Indica qué parte del monorepo toca el commit.

| Ámbito | Parte |
|---|---|
| `back` | `backend/` |
| `front` | `frontend/` |
| `db` | Migraciones Flyway |
| `docker` | `docker-compose.yml` y Dockerfiles |
| `ci` | `.github/` |
| `docs` | `docs/` y README |

Si un commit toca back y front a la vez, se omite el ámbito: `feat: muestra el ranking de comunidades`.

## 4. Descripción

- En **presente, tercera persona**, como si completara la frase "este commit...": *agrega*, *corrige*, *elimina*, *valida*.
- Empieza en **minúscula** y **sin punto final**.
- Máximo **72 caracteres** contando el tipo y el ámbito.
- Dice **qué** cambia, no cómo. El cómo va en el código y, si hace falta, en el cuerpo.

| Bien | Mal |
|---|---|
| `feat(front): agrega formulario de registro de miembro` | `cambios` |
| `fix(back): valida que la fecha de siembra no sea futura` | `arreglé el bug` |
| `test(back): prueba el cálculo de nivel por puntos` | `feat(back): Agregué las pruebas.` |
| `refactor(back): extrae el cálculo de fase lunar a un servicio` | `wip` |

## 5. Cuerpo y pie

- El **cuerpo** es opcional. Se usa cuando el porqué del cambio no es evidente. Va separado de la descripción por una línea en blanco.
- El **pie** enlaza issues: `Cierra #45` cierra el issue cuando el cambio llega a `develop`; `Refs #45` solo lo menciona.
- Un cambio que rompe algo que otros usan (por ejemplo, se renombra un campo de la API que el front ya consume) lleva `!` después del ámbito y lo explica en el pie:
  ```
  feat(back)!: renombra nombreComunidad a nombre en ComunidadResponse

  BREAKING CHANGE: el front debe leer el campo nombre.
  ```

## 6. Commits y TDD

El ciclo de TDD se refleja en el historial de la rama de trabajo:

```
test(back): prueba que el registro rechaza comunidades sin responsable
feat(back): registra comunidades con su responsable
refactor(back): mueve las validaciones de comunidad al DTO
```

Así queda evidencia de que la prueba se escribió antes del código. Con *Squash and merge* estos commits se combinan en uno solo al llegar a `develop`, pero se conservan en el Pull Request.

## 7. Buenas prácticas

- **Commits pequeños y frecuentes:** cada uno con un solo propósito y que compile.
- **Revisar antes de confirmar:** `git status` y `git diff --staged`, para no subir archivos de más.
- **Sin mezclar:** un commit de formato no lleva cambios de lógica.
- **El título del PR cumple este estándar,** porque es el mensaje que queda en `develop` tras el squash.
