# Flujo de trabajo con Git

Reglas para que los siete integrantes trabajen sobre el mismo repositorio sin pisarse. Se aplican desde el Incremento 1.

Documentos relacionados: [Estándar de commits](estandar-commits.md) · [Estándar de GitHub Issues](estandar-issues.md)

## 1. Ramas

| Rama | Para qué | Quién escribe en ella |
|---|---|---|
| `main` | Versión entregada. Cada cierre de incremento queda etiquetado. | Solo merges desde `develop`, al cerrar un incremento |
| `develop` | Integración del incremento en curso. Siempre debe compilar y pasar el CI. | Solo Pull Requests aprobados |
| Ramas de trabajo | Un issue por rama. Se crean desde `develop` y se borran al fusionarse. | Quien tiene asignado el issue |

### Nombre de las ramas de trabajo

```
<tipo>/<referencia>-<descripcion-corta>
```

| Tipo | Cuándo | Ejemplo |
|---|---|---|
| `feature/` | Un caso de uso | `feature/CU-01-registro-comunidad` |
| `fix/` | Un defecto reportado | `fix/45-ranking-duplicado` |
| `test/` | Solo pruebas, sin cambiar funcionalidad | `test/52-pruebas-e2e-registro` |
| `docs/` | Solo documentación | `docs/30-diagrama-clases-inc1` |
| `chore/` | Configuración, dependencias, CI | `chore/18-configurar-sonarcloud` |
| `hotfix/` | Defecto urgente sobre lo ya entregado en `main` | `hotfix/60-login-caido` |

Reglas del nombre:

- La referencia es el código del caso de uso (`CU-01`) o, si no es un caso de uso, el número del issue (`45`).
- Todo en minúsculas salvo el código `CU`, palabras separadas con guion, sin tildes ni ñ.
- Corta: entre dos y cinco palabras.

## 2. Ciclo de trabajo de un issue

```
develop ──┬─────────────────────────────────●── develop
          │                                 │ (squash merge)
          └── feature/CU-01-registro-comunidad
                 test → código → refactor → PR
```

1. **Tomar el issue.** Asígnatelo en GitHub y muévelo a *En progreso* en el tablero.
2. **Crear la rama desde un `develop` actualizado.**
   ```bash
   git switch develop
   git pull
   git switch -c feature/CU-01-registro-comunidad
   ```
3. **Trabajar con TDD.** Primero la prueba que falla, luego el código que la hace pasar y al final el refactor. Haz commits pequeños siguiendo el [estándar de commits](estandar-commits.md).
4. **Mantener la rama al día.** Si `develop` avanzó mientras trabajabas, trae sus cambios a tu rama y resuelve los conflictos en tu rama, nunca en `develop`.
   ```bash
   git fetch origin
   git merge origin/develop
   ```
5. **Verificar en local antes de subir.**
   ```bash
   cd backend && ./mvnw verify
   cd frontend && npm run lint && npm run test:run && npm run build
   ```
6. **Subir la rama y abrir el Pull Request hacia `develop`.**
   ```bash
   git push -u origin feature/CU-01-registro-comunidad
   ```
7. **Revisión.** Al menos una aprobación de otro integrante y el CI en verde. Los comentarios de revisión se atienden con nuevos commits en la misma rama.
8. **Fusión.** Quien abrió el PR lo fusiona con *Squash and merge* una vez aprobado. La rama se borra después de fusionar.

## 3. Pull Requests

- **Destino:** siempre `develop`. Solo los cierres de incremento y los `hotfix/` van a `main`.
- **Título:** sigue el [estándar de commits](estandar-commits.md), porque con *Squash and merge* el título del PR se convierte en el commit que queda en `develop`.
  ```
  feat(back): registra comunidades con su responsable (CU-01)
  ```
- **Descripción:** se llena con la plantilla e incluye `Cierra #<número>` para que el issue se cierre solo al fusionar.
- **Tamaño:** un PR resuelve un solo issue. Si pasa de unas 400 líneas cambiadas, conviene dividirlo.
- **Borrador:** si quieres comentarios antes de terminar, ábrelo como *Draft*.

### Qué revisa quien aprueba

- El código hace lo que pide el issue y cumple sus criterios de aceptación.
- Hay pruebas para lo nuevo y pasan.
- Respeta la arquitectura en capas del README (el controller no accede al repositorio, las entidades no se exponen en la API).
- Si cambia el esquema, hay una migración Flyway nueva y no se editó ninguna existente.
- No hay credenciales, claves ni datos personales reales.
- Los endpoints nuevos están documentados en Swagger.

Quien revisa aprueba, o pide cambios explicando qué y por qué. No se aprueba el PR propio.

## 4. Cierre de un incremento

Al terminar cada incremento, quien coordina:

1. Confirma que `develop` está en verde y que QA validó los casos de uso del incremento.
2. Abre un PR de `develop` hacia `main` titulado `release: incremento 1` y lo fusiona con *Create a merge commit* (sin squash, para conservar la historia).
3. Etiqueta la versión en `main`:
   ```bash
   git switch main && git pull
   git tag -a v0.1 -m "Incremento 1"
   git push origin v0.1
   ```

| Incremento | Etiqueta |
|---|---|
| Incremento 1 | `v0.1` |
| Incremento 2 | `v0.2` |
| Incremento 3 | `v0.3` |
| Entrega final | `v1.0` |

## 5. Hotfix

Solo para un defecto grave en lo ya entregado en `main`:

1. Rama `hotfix/<número>-<descripcion>` desde `main`.
2. PR hacia `main`, con una aprobación y el CI en verde.
3. Se etiqueta con la versión siguiente de parche (`v0.1.1`).
4. Se fusiona también `main` en `develop` para no perder la corrección.

## 6. Prohibido

- Hacer push directo a `main` o a `develop`.
- Usar `git push --force` en ramas compartidas. En tu propia rama solo con `--force-with-lease`.
- Editar una migración Flyway que ya está en `develop`.
- Subir el archivo `.env`, carpetas `target/`, `node_modules/` o `dist/`.
- Fusionar con el CI en rojo, aunque "solo falte una prueba".

## 7. Configuración del repositorio (una sola vez, el dueño)

En GitHub, *Settings → Rules → Rulesets → New branch ruleset*, uno para `main` y otro para `develop`, con:

- **Restrict deletions**
- **Require a pull request before merging**, con 1 aprobación y *Dismiss stale approvals* activado
- **Require status checks to pass**: `Backend / test` y `Frontend / test`
- **Block force pushes**

En *Settings → General*:

- **Default branch:** `develop`. Es necesario para que `Cierra #<número>` cierre el issue al fusionar el PR en `develop`, y para que los PR se abran hacia `develop` por defecto.

En *Settings → General → Pull Requests*:

- Activar **Allow squash merging** y **Allow merge commits**; desactivar *Allow rebase merging*.
- Activar **Automatically delete head branches**.

En el tablero de GitHub Projects, *Workflows → Item closed*: mover a la columna **En QA**.
