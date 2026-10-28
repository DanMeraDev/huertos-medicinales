# Errores comunes de la base de datos

Errores frecuentes con MariaDB y Flyway en Windows, y cómo resolverlos. La mayoría aparecen en los logs del back al arrancar.

Relacionados: [Ejecución de la base](ejecucion.md) · [Estructura de la base](estructura.md) · [Errores comunes del back](../backend/errores-comunes.md)

## Conexión

### Puerto 3306 ocupado

Al ejecutar `docker compose up -d db` aparece:

```
Ports are not available: exposing port TCP 0.0.0.0:3306 ...
```

o `port is already allocated`.

Otro programa ya usa el puerto 3306. En Windows casi siempre es **MySQL instalado como servicio** o **XAMPP**. Para confirmarlo:

```powershell
netstat -ano | findstr :3306
# La última columna es el PID
Get-Process -Id <PID>
```

Tienes dos opciones:

1. **Detener ese programa.** En XAMPP, pulsa *Stop* en MySQL. Si es un servicio, en PowerShell como administrador:
   ```powershell
   Get-Service *mysql*, *maria*
   Stop-Service MySQL80        # usa el nombre que mostró el comando anterior
   ```
   Para que no vuelva a arrancar con Windows: `Set-Service MySQL80 -StartupType Manual`.
2. **Usar otro puerto para la base del proyecto.** Pon `DB_PORT=3307` en el `.env` y vuelve a ejecutar `docker compose up -d db`. El back en local también tomará el 3307 automáticamente.

### `Socket fail to connect to localhost:3306. Connection refused`

El back no encuentra la base. Revisa su estado:

```powershell
docker compose ps
```

- **Si `db` no aparece:** `docker compose up -d db`.
- **Si aparece como `starting`:** espera unos segundos a que pase a `healthy`.
- **Si cambiaste `DB_PORT` en el `.env`:** comprueba que el back se esté ejecutando con el perfil `dev`, que es el que lee el `.env`.

### `Unknown database 'huertos'` o `Access denied for user 'huertos'@'%' to database 'huertos'`

El back se conectó a **otra** base que no tiene `huertos`, o el volumen de Docker se creó con otro `DB_NAME`.

1. Revisa quién usa el puerto con `netstat -ano | findstr :3306` y `Get-Process -Id <PID>`. Si no es Docker (`com.docker.backend` o `wslrelay`), es un MySQL o XAMPP local: sigue [Puerto 3306 ocupado](#puerto-3306-ocupado).
2. Si sí es Docker, [recrea la base](ejecucion.md#4-recrear-la-base).

### `Access denied for user 'huertos'@'...' (using password: YES)`

La clave del `.env` no coincide con la de la base. Hay dos causas habituales:

- **Cambiaste `DB_USER` o `DB_PASSWORD` después de crear el volumen.** MariaDB solo los lee la primera vez. [Recrea la base](ejecucion.md#4-recrear-la-base).
- **Respondió el MySQL de XAMPP** en lugar del contenedor. Sigue [Puerto 3306 ocupado](#puerto-3306-ocupado).

## Flyway

### `Validate failed: Migrations have failed validation` ... `Migration checksum mismatch for migration version N`

Alguien modificó una migración que tu base ya tenía aplicada. Las migraciones aplicadas no se editan: se crea una nueva.

- **En tu máquina:** [recrea la base](ejecucion.md#4-recrear-la-base) para alinearla con el repo.
- **Si el cambio vino de un Pull Request:** avisa en el issue, porque no debió fusionarse. La corrección es revertir la edición y crear una migración nueva con el cambio.

### `Found more than one migration with version N`

Dos archivos tienen el mismo número de versión, casi siempre porque dos personas crearon una migración en paralelo. Quien fusiona después renombra su archivo al siguiente número libre, por ejemplo de `V5__...` a `V6__...`.

### `Detected resolved migration not applied to database: N`

En tu base ya se aplicaron migraciones con número mayor que una que acaba de llegar de `develop`. Pasa cuando probaste en local una migración tuya y después se fusionó otra con un número menor. [Recrea la base](ejecucion.md#4-recrear-la-base).

### `Migration V<N>__....sql failed` con un error de SQL

La migración tiene un error de sintaxis o choca con datos existentes. El mensaje incluye la línea y el error de MariaDB. Corrige el archivo, que todavía no está en `develop`, y [recrea la base](ejecucion.md#4-recrear-la-base): Flyway la marcó como fallida en `flyway_schema_history` y no volverá a intentarla sola.

## Hibernate

### `Schema-validation: missing table [...]` o `missing column [...]`

Hibernate compara las entidades con las tablas y encontró una diferencia:

- **Hay una entidad en `model` sin su migración:** créala en `db/migration`.
- **El nombre no coincide:** por ejemplo, el campo `fechaNacimiento` espera la columna `fecha_nacimiento`. Revisa las [convenciones de nombres](estructura.md#4-convenciones-de-tablas-y-columnas).
- **Mayúsculas:** la tabla se creó como `Comunidad` y la entidad busca `comunidad`. Los nombres de tabla van siempre en minúsculas.

### `Schema-validation: wrong column type encountered in column [...]`

El tipo de la columna no coincide con el del campo en Java. Por ejemplo, una columna `INT` para un campo `Long`, que espera `BIGINT`. Revisa la [tabla de tipos](estructura.md#4-convenciones-de-tablas-y-columnas) y corrige con una migración nueva.
