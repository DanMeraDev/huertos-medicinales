# Guía de ejecución en Windows

Cómo instalar lo necesario, configurar el `.env` y correr el back y el front en Windows 10 u 11, con IntelliJ IDEA o VS Code. Al final hay una sección de errores comunes con su solución.

Todos los comandos son para **PowerShell**. Ábrelo desde el menú Inicio o con la terminal integrada del IDE.

## Resumen rápido

| Quiero... | Comando (desde la raíz del repo) |
|---|---|
| Levantar todo con Docker | `docker compose up -d --build` |
| Levantar solo la base | `docker compose up -d db` |
| Correr el back en local | `cd backend; .\mvnw.cmd spring-boot:run` |
| Correr el front en local | `cd frontend; npm run dev` |
| Correr las pruebas del back | `cd backend; .\mvnw.cmd verify` |
| Correr las pruebas del front | `cd frontend; npm run test:run` |
| Apagar todo | `docker compose down` |

## 1. Prerrequisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| Git | Reciente | Clonar y trabajar con el repo |
| Docker Desktop | Reciente, con WSL 2 | Base de datos, pruebas de integración y levantar todo |
| JDK | **21** | Compilar y correr el back fuera de Docker |
| Node.js | **22 LTS** | Correr el front fuera de Docker |
| IntelliJ IDEA o VS Code | Reciente | Editor |

Maven **no** se instala: el back trae el wrapper `mvnw.cmd`, que descarga la versión correcta la primera vez.

### 1.1 Instalar con winget

`winget` viene incluido en Windows 10 y 11. Abre PowerShell **como administrador** y ejecuta:

```powershell
winget install Git.Git
winget install EclipseAdoptium.Temurin.21.JDK
winget install Docker.DockerDesktop
```

Para Node.js 22, descarga el instalador **v22 LTS** desde https://nodejs.org/en/download y sigue el asistente. Usa la 22, que es la misma que usa el CI.

Docker Desktop necesita WSL 2. Si al abrirlo pide instalarlo o actualizarlo:

```powershell
wsl --install
wsl --update
```

Reinicia el equipo después de instalar Docker Desktop y WSL.

### 1.2 Configurar JAVA_HOME

El wrapper de Maven usa `JAVA_HOME` para encontrar Java. Revisa si ya está:

```powershell
echo $env:JAVA_HOME
```

Si no muestra nada, busca la carpeta del JDK en `C:\Program Files\Eclipse Adoptium\` (se llama algo como `jdk-21.0.x-hotspot`) y ejecuta, cambiando la ruta por la tuya:

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot", "User")
```

Cierra y vuelve a abrir PowerShell (y el IDE) para que tome el cambio.

### 1.3 Ajustes de Windows recomendados

```powershell
# Permite rutas largas en Git (node_modules las tiene)
git config --global core.longpaths true

# Permite ejecutar npm en PowerShell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

Clona el repo en una carpeta corta y **fuera de OneDrive**, por ejemplo `C:\dev\`. OneDrive bloquea archivos mientras sincroniza y rompe `node_modules` y `target`.

### 1.4 Verificar la instalación

Abre una PowerShell nueva y comprueba cada herramienta:

```powershell
git --version
java -version          # debe decir 21
node -v                # debe decir v22
npm -v
docker --version
docker compose version
```

Docker Desktop tiene que estar **abierto** (ícono de la ballena en la barra de tareas) para que funcionen los comandos `docker`.

## 2. Clonar y preparar el `.env`

```powershell
cd C:\dev
git clone https://github.com/DanMeraDev/huertos-medicinales.git
cd huertos-medicinales
Copy-Item .env.example .env
```

El archivo `.env` queda en la **raíz del repo** y lo leen las tres partes:

- **docker-compose:** lo lee siempre.
- **Back en local (perfil `dev`):** lo lee solo, desde cualquier IDE o terminal.
- **Front en local (`npm run dev`):** lo lee solo, para saber en qué puerto está el back.

No hay que configurar nada en el IDE. El `.env` es personal: Git lo ignora, así que cada integrante puede cambiar sus puertos sin afectar a los demás.

### Variables del `.env`

| Variable | Valor por defecto | Para qué |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil del back dentro de Docker (`dev` o `prod`) |
| `DB_NAME` | `huertos` | Nombre de la base |
| `DB_USER` | `huertos` | Usuario de la base |
| `DB_PASSWORD` | `huertos` | Clave de la base |
| `DB_ROOT_PASSWORD` | `root` | Clave del usuario root de MariaDB |
| `DB_PORT` | `3306` | Puerto de MariaDB en tu máquina |
| `API_PORT` | `8080` | Puerto del back |
| `WEB_PORT` | `5173` | Puerto del front |

Orden de prioridad cuando el back busca un valor: **variable de entorno del sistema o del IDE**, luego **`.env`**, luego **el valor por defecto** de `application-dev.yml`.

> **Importante:** MariaDB toma `DB_NAME`, `DB_USER` y `DB_PASSWORD` solo la **primera vez** que crea su volumen. Si los cambias después, tienes que recrear la base (ver el error [Access denied](#access-denied-for-user-huertos-using-password-yes)).

## 3. Opción A: todo en Docker

La forma más simple de ver la aplicación funcionando, sin Java ni Node instalados.

```powershell
docker compose up -d --build
```

La primera vez tarda varios minutos porque descarga imágenes y compila. Comprueba que los tres servicios estén arriba:

```powershell
docker compose ps
```

| Servicio | URL |
|---|---|
| Front | http://localhost:5173 |
| Ping del API | http://localhost:8080/api/ping |
| Swagger | http://localhost:8080/swagger-ui.html |

Si el front muestra *Servidor en línea. Base de datos: 11.4...*, todo está conectado.

Después de hacer cambios en el código, vuelve a ejecutar `docker compose up -d --build` para reconstruir.

## 4. Opción B: desarrollo en local

Para programar es mejor dejar **solo la base en Docker** y correr el back y el front en tu máquina. Así los cambios se ven al instante y puedes depurar con el IDE.

### 4.1 Levantar la base

```powershell
docker compose up -d db
```

Si antes levantaste todo con la opción A, apaga el back y el front de Docker para liberar los puertos 8080 y 5173:

```powershell
docker compose stop api web
```

### 4.2 Correr el back

**Desde la terminal:**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Está listo cuando aparece `Started HuertosApiApplication`. Prueba http://localhost:8080/api/ping.

**Desde IntelliJ IDEA:**

1. *File → Open* y selecciona el archivo `backend\pom.xml`. Elige **Open as Project**.
2. Espera a que termine de cargar Maven (barra inferior).
3. *File → Project Structure → Project → SDK*: elige **21**. Si no aparece, *Add SDK → JDK* y selecciona la carpeta de Temurin 21.
4. Si IntelliJ pregunta por **annotation processing** (lo necesita Lombok), acepta. Si no pregunta, actívalo en *Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing*.
5. Abre `HuertosApiApplication.java` y pulsa el triángulo verde junto a `main`.

**Desde VS Code:**

1. Instala las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack**.
2. *File → Open Folder* y abre la **raíz del repo**.
3. Espera a que termine de cargar el proyecto Java (abajo a la izquierda).
4. Si tienes varios JDK: `Ctrl+Shift+P` → *Java: Configure Java Runtime* y elige 21 para el proyecto.
5. Abre `HuertosApiApplication.java` y pulsa **Run** sobre el método `main`, o usa el panel **Spring Boot Dashboard**.

En los dos IDE el `.env` se lee solo: no hace falta agregar variables a la configuración de ejecución.

**Si necesitas una variable solo para ti, sin tocar el `.env`:**

- **IntelliJ:** *Run → Edit Configurations → HuertosApiApplication → Environment variables*, por ejemplo `API_PORT=8081`.
- **VS Code:** en `.vscode/launch.json`, agrega `"env": { "API_PORT": "8081" }` a la configuración de ejecución.

### 4.3 Correr el front

En otra terminal:

```powershell
cd frontend
npm install        # solo la primera vez o cuando cambie package.json
npm run dev
```

Abre http://localhost:5173. Vite redirige las llamadas a `/api` al back según `API_PORT` del `.env`. Al guardar un archivo, la página se actualiza sola.

### 4.4 Apagar

- **Back y front en local:** `Ctrl+C` en sus terminales, o el botón de detener del IDE.
- **Base:** `docker compose stop db`. Los datos se conservan.

## 5. Correr las pruebas

```powershell
cd backend
.\mvnw.cmd verify
```

- Docker Desktop tiene que estar abierto: las pruebas de integración levantan su propia MariaDB con Testcontainers. No usan la base de `docker compose` ni tus datos.
- El reporte de cobertura queda en `backend\target\site\jacoco\index.html`.

```powershell
cd frontend
npm run test:run   # una vez
npm test           # modo observador, se repiten al guardar
npm run coverage   # con cobertura
```

## 6. Errores comunes

### Instalación

#### `'java' no se reconoce como un comando interno o externo`

En inglés: `'java' is not recognized as an internal or external command`.

Java no está instalado o la terminal se abrió antes de instalarlo. Cierra y abre PowerShell. Si sigue, reinstala con `winget install EclipseAdoptium.Temurin.21.JDK`.

#### `JAVA_HOME not found in your environment` o `JAVA_HOME is set to an invalid directory`

Falta configurar `JAVA_HOME`, o apunta a una carpeta que ya no existe. Sigue el paso [1.2](#12-configurar-java_home) y vuelve a abrir la terminal y el IDE.

#### `error: release version 21 not supported` o `invalid target release: 21`

Se está compilando con un JDK anterior (17 u 11). Comprueba con `java -version` y `echo $env:JAVA_HOME` que ambos apunten a la 21. En IntelliJ, revisa además:

- *Project Structure → SDK*: 21
- *Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE*: 21

#### `npm.ps1 no se puede cargar porque la ejecución de scripts está deshabilitada en este sistema`

PowerShell bloquea scripts por defecto. Ejecuta una vez:

```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

#### `Filename too long` al clonar o al hacer `npm install`

```powershell
git config --global core.longpaths true
```

Y clona en una ruta corta, como `C:\dev\huertos-medicinales`.

### Docker

#### `error during connect: ... open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`

Docker Desktop no está abierto. Ábrelo desde el menú Inicio y espera a que el ícono de la ballena deje de animarse antes de repetir el comando.

#### Docker Desktop no arranca: `WSL 2 installation is incomplete` o `Virtualization support not detected`

- **WSL:** ejecuta `wsl --update` como administrador y reinicia.
- **Virtualización:** está apagada en la BIOS. Entra a la BIOS al encender el equipo (normalmente F2, F10 o Supr) y activa **Intel VT-x** o **AMD-V / SVM**. En el Administrador de tareas, pestaña *Rendimiento → CPU*, debe decir *Virtualización: Habilitado*.

#### `Ports are not available: exposing port TCP 0.0.0.0:3306` o `port is already allocated`

Otro programa ya usa el puerto 3306. En Windows casi siempre es **MySQL instalado como servicio** o **XAMPP**. Tienes dos opciones:

1. **Detener ese programa.** En XAMPP, *Stop* en MySQL. Si es un servicio, en PowerShell como administrador:
   ```powershell
   Get-Service *mysql*, *maria*
   Stop-Service MySQL80        # usa el nombre que mostró el comando anterior
   ```
2. **Usar otro puerto para la base del proyecto.** En el `.env`, cambia `DB_PORT=3307` y vuelve a ejecutar `docker compose up -d db`. El back en local también tomará el 3307 automáticamente.

### Base de datos

#### `Socket fail to connect to localhost:3306. Connection refused`

El back no encuentra la base. Revisa:

```powershell
docker compose ps
```

- Si `db` no aparece: `docker compose up -d db`.
- Si aparece como `starting`: espera unos segundos a que pase a `healthy`.
- Si cambiaste `DB_PORT` en el `.env`: comprueba que el back se esté ejecutando con el perfil `dev`, que es el que lee el `.env`.

#### `Unknown database 'huertos'` o `Access denied for user 'huertos'@'%' to database 'huertos'`

El back se conectó a **otra** base que no tiene la base `huertos`, o la base del contenedor se creó con otro nombre. Casi siempre es un MySQL o XAMPP local escuchando en el 3306, mientras el contenedor no pudo usar ese puerto. Revisa quién usa el puerto:

```powershell
netstat -ano | findstr :3306
```

La última columna es el PID. Averigua de qué programa es con `Get-Process -Id <PID>`. Si no es Docker (`com.docker.backend` o `wslrelay`), sigue la solución de [puerto 3306 ocupado](#ports-are-not-available-exposing-port-tcp-00003306-o-port-is-already-allocated).

Si sí es Docker, el volumen se creó con otro `DB_NAME`. Recréalo como se explica en el error siguiente.

#### `Access denied for user 'huertos'@'...' (using password: YES)`

La clave del `.env` no coincide con la de la base. Pasa cuando se cambia `DB_USER` o `DB_PASSWORD` después de crear el volumen, porque MariaDB solo los lee la primera vez. Recrea la base (**borra los datos locales**):

```powershell
docker compose down -v
docker compose up -d db
```

También puede ser el MySQL de XAMPP respondiendo en el 3306: revisa el error anterior.

#### `Validate failed: Migrations have failed validation` o `Migration checksum mismatch`

Alguien modificó una migración de Flyway que ya se había aplicado en tu base. Las migraciones aplicadas no se editan: se crea una nueva (`V2__...`). Para alinear tu base local con el repo, recréala:

```powershell
docker compose down -v
docker compose up -d db
```

Si el cambio a la migración vino de un PR, avisa en el issue: no debió fusionarse.

#### `Schema-validation: missing table [...]` o `missing column [...]`

Hay una entidad en `model` sin su migración en `db/migration`, o con un nombre distinto. Hibernate está en modo `validate` y no crea tablas por su cuenta. Agrega la migración que falta o corrige el nombre en la entidad.

### Back

#### `Unable to start embedded Tomcat server` ... `java.net.BindException: Address already in use`

En Windows en español puede decir `La dirección ya se está usando`, y en otras versiones `Web server failed to start. Port 8080 was already in use`. Las tres significan que otro proceso usa el 8080. Lo más común es que el **back de Docker siga encendido** después de un `docker compose up`:

```powershell
docker compose stop api
```

Si no es eso, busca el proceso:

```powershell
netstat -ano | findstr :8080
# La última columna es el PID, por ejemplo 12345
Get-Process -Id 12345
Stop-Process -Id 12345
```

Otra opción es usar otro puerto: en el `.env`, `API_PORT=8081`. El front en local se ajusta solo.

#### `cannot find symbol` en getters, constructores o `log` (IntelliJ)

Lombok no está generando código. Activa *Enable annotation processing* (paso 4 de [IntelliJ](#42-correr-el-back)) y luego, en la pestaña Maven, **Reload All Maven Projects**.

#### `Could not find a valid Docker environment` al correr las pruebas

Testcontainers no encuentra Docker. Abre Docker Desktop y espera a que esté listo. Si persiste, en Docker Desktop, *Settings → General*, activa **Use the WSL 2 based engine**.

#### En los logs aparece `Using generated security password: ...`

Es solo un aviso, no un error. Spring Security genera una clave temporal porque todavía no está implementado el inicio de sesión. Desaparece con el caso de uso de autenticación.

### Front

#### `'vite' no se reconoce como un comando interno o externo`

Faltan las dependencias. Ejecuta `npm install` dentro de `frontend`.

#### `Vite requires Node.js version 20.19+ or 22.12+`

Tu Node.js es antiguo. Instala la versión 22 LTS (paso [1.1](#11-instalar-con-winget)) y comprueba con `node -v`.

#### La página dice *No se pudo conectar con el servidor* y la terminal muestra `http proxy error: /api/ping ... ECONNREFUSED`

El front no encuentra el back. Comprueba que el back esté corriendo (http://localhost:8080/api/ping) y que `API_PORT` en el `.env` sea el puerto donde corre. Si cambiaste el `.env`, reinicia `npm run dev`.

#### `Port 5173 is in use, trying another one...`

El front de Docker sigue encendido. Vite usará el 5174 y funciona igual, pero para evitar confusiones apágalo con `docker compose stop web`.

## 7. Comandos útiles de Docker

| Comando | Qué hace |
|---|---|
| `docker compose ps` | Muestra qué servicios están corriendo y su estado |
| `docker compose logs -f api` | Muestra los logs del back en vivo (`Ctrl+C` para salir) |
| `docker compose stop` | Detiene los servicios sin borrar nada |
| `docker compose down` | Detiene y elimina los contenedores; **los datos se conservan** |
| `docker compose down -v` | Elimina también el volumen de la base; **se pierden los datos locales** |
| `docker compose up -d --build api` | Reconstruye y reinicia solo el back |
| `docker compose exec db mariadb -u huertos -phuertos huertos` | Abre una consola SQL dentro de la base |
