# Ejecución de la base de datos

Cómo levantar MariaDB, conectarse para ver los datos y recrear la base cuando haga falta.

Antes, completa [Primeros pasos](../primeros-pasos.md): Docker Desktop y el `.env`.

Relacionados: [Estructura de la base](estructura.md) · [Errores comunes de la base](errores-comunes.md)

## 1. Levantar y apagar

Desde la raíz del repo:

```powershell
docker compose up -d db      # levantar
docker compose ps            # comprobar: db debe estar "healthy"
docker compose stop db       # apagar, conservando los datos
```

La primera vez, MariaDB crea la base, el usuario y la clave con los valores del `.env` (`DB_NAME`, `DB_USER`, `DB_PASSWORD`). Los datos se guardan en el volumen de Docker `db-data`, así que no se pierden al apagar ni al reiniciar el equipo.

Las tablas no se crean aquí: las crea Flyway la primera vez que arranca el back.

## 2. Conectarse a la base

### Datos de conexión

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `3306` (o el `DB_PORT` de tu `.env`) |
| Base | `huertos` |
| Usuario | `huertos` |
| Clave | `huertos` |
| Usuario administrador | `root`, con la clave `root` |

### Con un cliente gráfico

Cualquiera de estos sirve. Crea una conexión nueva de tipo **MariaDB** con los datos de la tabla anterior:

- **IntelliJ IDEA Ultimate:** panel *Database* → *+* → *Data Source* → *MariaDB*. La primera vez pide descargar el driver.
- **DBeaver:** `winget install dbeaver.dbeaver`, luego *Nueva conexión* → *MariaDB*.
- **HeidiSQL:** `winget install HeidiSQL.HeidiSQL`, luego *Nueva* → tipo de red *MariaDB or MySQL (TCP/IP)*.

### Desde la terminal

Abre una consola SQL dentro del contenedor, sin instalar nada:

```powershell
docker compose exec db mariadb -u huertos -phuertos huertos
```

Consultas útiles:

```sql
SHOW TABLES;
DESCRIBE comunidad;
SELECT version, description, success FROM flyway_schema_history;
```

Para salir: `exit`.

## 3. Ver qué migraciones se aplicaron

Flyway registra cada migración en la tabla `flyway_schema_history`:

```sql
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Si una migración falló, aparece con `success = 0` y el back no arranca hasta resolverla. Ver [Errores comunes de la base](errores-comunes.md).

## 4. Recrear la base

Recrear la base borra **todos los datos locales** y la vuelve a construir desde cero con las migraciones del repo. Es la solución cuando:

- Cambiaste `DB_NAME`, `DB_USER` o `DB_PASSWORD` en el `.env`.
- Tu base quedó desalineada con las migraciones de `develop`, por ejemplo después de probar una migración que luego cambiaste.
- Flyway reporta un error de checksum o de versión en tu máquina.

```powershell
docker compose down -v
docker compose up -d db
```

Después arranca el back para que Flyway vuelva a crear las tablas.

Esto solo afecta a tu máquina. Las pruebas automáticas y el CI siempre usan una base nueva, creada por Testcontainers.
