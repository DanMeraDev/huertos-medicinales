# Primeros pasos

Qué instalar, cómo clonar el repo, cómo funciona el `.env` y cómo levantar todo con Docker. Está escrito para **Windows 10 u 11** y los comandos son para **PowerShell**.

Cuando termines esta guía, sigue con la de la parte en la que vas a trabajar:

- [Back: ejecución](backend/ejecucion.md)
- [Front: ejecución](frontend/ejecucion.md)
- [Base de datos: ejecución](db/ejecucion.md)

## 1. Prerrequisitos

| Herramienta | Versión | Quién la necesita |
|---|---|---|
| Git | Reciente | Todos |
| Docker Desktop | Reciente, con WSL 2 | Todos: base de datos, pruebas de integración y levantar todo |
| JDK | **21** | Quien trabaje en el back |
| Node.js | **22 LTS** | Quien trabaje en el front |
| IntelliJ IDEA o VS Code | Reciente | Todos |

Maven **no** se instala: el back trae el wrapper `mvnw.cmd`, que descarga la versión correcta la primera vez.

### 1.1 Instalar con winget

`winget` viene incluido en Windows 10 y 11. Abre PowerShell **como administrador** y ejecuta:

```powershell
winget install Git.Git
winget install Docker.DockerDesktop
winget install EclipseAdoptium.Temurin.21.JDK
```

Para Node.js 22, descarga el instalador **v22 LTS** desde https://nodejs.org/en/download y sigue el asistente. Usa la 22, que es la misma que usa el CI.

Docker Desktop necesita WSL 2. Si al abrirlo pide instalarlo o actualizarlo:

```powershell
wsl --install
wsl --update
```

Reinicia el equipo después de instalar Docker Desktop y WSL.

### 1.2 Configurar JAVA_HOME

El wrapper de Maven usa `JAVA_HOME` para encontrar Java. Revisa si ya está configurado:

```powershell
echo $env:JAVA_HOME
```

Si no muestra nada, busca la carpeta del JDK en `C:\Program Files\Eclipse Adoptium\`, que se llama algo como `jdk-21.0.x-hotspot`. Ejecuta lo siguiente, cambiando la ruta por la tuya:

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot", "User")
```

Cierra y vuelve a abrir PowerShell y el IDE para que tomen el cambio.

### 1.3 Ajustes de Windows recomendados

```powershell
# Permite rutas largas en Git (node_modules las tiene)
git config --global core.longpaths true

# Permite ejecutar npm en PowerShell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

Clona el repo en una carpeta corta y **fuera de OneDrive**, por ejemplo `C:\dev\`. OneDrive bloquea archivos mientras sincroniza y rompe `node_modules` y `target`.

### 1.4 Verificar la instalación

Abre una PowerShell nueva y comprueba:

```powershell
git --version
docker --version
docker compose version
java -version          # debe decir 21
node -v                # debe decir v22
```

Docker Desktop tiene que estar **abierto** (ícono de la ballena en la barra de tareas) para que funcionen los comandos `docker`.

## 2. Clonar y preparar el `.env`

```powershell
cd C:\dev
git clone https://github.com/DanMeraDev/huertos-medicinales.git
cd huertos-medicinales
Copy-Item .env.example .env
```

El archivo `.env` queda en la **raíz del repo** y lo leen las tres partes, sin configurar nada en el IDE:

- **docker-compose:** lo lee siempre.
- **Back en local:** lo lee solo con el perfil `dev`, desde cualquier IDE o terminal.
- **Front en local:** `npm run dev` lo lee para saber en qué puerto está el back.

El `.env` es personal: Git lo ignora, así que cada integrante puede cambiar sus puertos sin afectar a los demás.

### Variables

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

Cuando el back busca un valor, el orden de prioridad es:
1. La variable de entorno del sistema o del IDE.
2. El `.env`.
3. El valor por defecto de `application-dev.yml`.

> **Importante:** MariaDB toma `DB_NAME`, `DB_USER` y `DB_PASSWORD` solo la **primera vez** que crea su volumen. Si los cambias después, tienes que recrear la base, como explica [Base de datos: ejecución](db/ejecucion.md#4-recrear-la-base).

## 3. Levantar todo con Docker

Es la forma más simple de ver la aplicación funcionando, y no necesita Java ni Node.

```powershell
docker compose up -d --build
```

La primera vez tarda varios minutos, porque descarga imágenes y compila. Comprueba que los tres servicios estén arriba:

```powershell
docker compose ps
```

| Servicio | URL |
|---|---|
| Front | http://localhost:5173 |
| Ping del API | http://localhost:8080/api/ping |
| Swagger | http://localhost:8080/swagger-ui.html |

Si el front muestra *Servidor en línea. Base de datos: 11.4...*, todo está conectado.

Después de cambiar el código, vuelve a ejecutar `docker compose up -d --build` para reconstruir.

Para programar es mejor otra forma de trabajo: solo la base en Docker, con el back y el front en tu máquina. Así los cambios se ven al instante y puedes depurar con el IDE. Está explicada en la guía de ejecución de cada parte.

## 4. Comandos útiles de Docker

| Comando | Qué hace |
|---|---|
| `docker compose ps` | Muestra qué servicios están corriendo y su estado |
| `docker compose logs -f api` | Muestra los logs del back en vivo (`Ctrl+C` para salir) |
| `docker compose stop` | Detiene los servicios sin borrar nada |
| `docker compose stop api web` | Detiene solo el back y el front, y deja la base |
| `docker compose down` | Detiene y elimina los contenedores; **los datos se conservan** |
| `docker compose down -v` | Elimina también el volumen de la base; **se pierden los datos locales** |
| `docker compose up -d --build api` | Reconstruye y reinicia solo el back |

## 5. Errores comunes de instalación y Docker

Los errores propios de cada parte están en su guía: [back](backend/errores-comunes.md), [front](frontend/errores-comunes.md) y [base de datos](db/errores-comunes.md).

### `error during connect: ... open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`

Docker Desktop no está abierto. Ábrelo desde el menú Inicio y espera a que el ícono de la ballena deje de moverse antes de repetir el comando.

### Docker Desktop no arranca: `WSL 2 installation is incomplete` o `Virtualization support not detected`

- **WSL:** ejecuta `wsl --update` como administrador y reinicia.
- **Virtualización:** está apagada en la BIOS. Entra a la BIOS al encender el equipo, normalmente con F2, F10 o Supr, y activa **Intel VT-x** o **AMD-V / SVM**. Para comprobarlo, en el Administrador de tareas, pestaña *Rendimiento → CPU*, debe decir *Virtualización: Habilitado*.

### `Ports are not available` o `port is already allocated` al hacer `docker compose up`

Otro programa usa uno de los puertos del proyecto (3306, 8080 o 5173). Para saber cuál lo usa, sigue la guía de esa parte:

- **3306:** [base de datos](db/errores-comunes.md#puerto-3306-ocupado).
- **8080:** [back](backend/errores-comunes.md#puerto-8080-ocupado).
- **5173:** [front](frontend/errores-comunes.md#puerto-5173-ocupado).

### `Filename too long` al clonar o al hacer `npm install`

```powershell
git config --global core.longpaths true
```

Y clona en una ruta corta, como `C:\dev\huertos-medicinales`.

### `'git' no se reconoce como un comando interno o externo`

Git no está instalado, o la terminal se abrió antes de instalarlo. Cierra y abre PowerShell. Si sigue, instálalo con `winget install Git.Git`.
