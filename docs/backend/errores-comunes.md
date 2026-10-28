# Errores comunes del back

Errores frecuentes al instalar, compilar o correr el back en Windows, y cómo resolverlos. Los errores de conexión con la base están en [Errores comunes de la base de datos](../db/errores-comunes.md).

Relacionados: [Ejecución del back](ejecucion.md) · [Primeros pasos](../primeros-pasos.md)

## Instalación de Java

### `'java' no se reconoce como un comando interno o externo`

En inglés: `'java' is not recognized as an internal or external command`.

Java no está instalado, o la terminal se abrió antes de instalarlo. Cierra y abre PowerShell. Si sigue, reinstala con:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

### `JAVA_HOME not found in your environment` o `JAVA_HOME is set to an invalid directory`

Falta configurar `JAVA_HOME`, o apunta a una carpeta que ya no existe. Sigue el paso [1.2 de Primeros pasos](../primeros-pasos.md#12-configurar-java_home) y vuelve a abrir la terminal y el IDE.

### `error: release version 21 not supported` o `invalid target release: 21`

Se está compilando con un JDK anterior, como el 17 o el 11. Comprueba con `java -version` y `echo $env:JAVA_HOME` que los dos apunten a la 21. En IntelliJ, revisa además:

- *Project Structure → SDK*: 21
- *Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE*: 21

## Arranque

### Puerto 8080 ocupado

El back se detiene al arrancar con:

```
Unable to start embedded Tomcat server
...
Caused by: java.net.BindException: Address already in use
```

Según la versión, también puede aparecer como `La dirección ya se está usando` o `Web server failed to start. Port 8080 was already in use`. En todos los casos, otro proceso usa el 8080.

Lo más común es que el **back de Docker siga encendido** después de un `docker compose up`:

```powershell
docker compose stop api
```

Si no es eso, busca qué proceso es:

```powershell
netstat -ano | findstr :8080
# La última columna es el PID, por ejemplo 12345
Get-Process -Id 12345
Stop-Process -Id 12345
```

Otra opción es usar otro puerto: pon `API_PORT=8081` en el `.env`. El front en local se ajusta solo.

### En los logs aparece `Using generated security password: ...`

Es un aviso, no un error. Spring Security genera una clave temporal porque todavía no está implementado el inicio de sesión. Desaparece con el caso de uso de autenticación.

### `Schema-validation: missing table [...]` o `missing column [...]`

Una entidad no coincide con las tablas de la base. Ver [Errores comunes de la base de datos](../db/errores-comunes.md#schema-validation-missing-table--o-missing-column-).

## Compilación en el IDE

### `cannot find symbol` en getters, constructores o `log` (IntelliJ)

Lombok no está generando código. Activa *Enable annotation processing*, como en el paso 4 de [IntelliJ](ejecucion.md#desde-intellij-idea). Luego, en la pestaña Maven, pulsa **Reload All Maven Projects**.

### No encuentra las clases `...MapperImpl`

MapStruct genera los mappers al compilar. Recompila con *Build → Rebuild Project* (IntelliJ), o ejecuta `.\mvnw.cmd compile` desde `backend`.

### VS Code marca errores en todo el proyecto después de cambiar el `pom.xml`

`Ctrl+Shift+P` → *Java: Clean Java Language Server Workspace* y reinicia VS Code.

## Pruebas

### `Could not find a valid Docker environment`

Testcontainers no encuentra Docker. Abre Docker Desktop y espera a que esté listo. Si el error sigue, en Docker Desktop, *Settings → General*, activa **Use the WSL 2 based engine**.

### Las pruebas de integración tardan mucho la primera vez

Es normal: Testcontainers descarga la imagen de MariaDB. Las siguientes veces la reutiliza y tardan unos segundos.
