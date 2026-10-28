# Errores comunes del front

Errores frecuentes al instalar o correr el front en Windows, y cómo resolverlos.

Relacionados: [Ejecución del front](ejecucion.md) · [Primeros pasos](../primeros-pasos.md)

## Instalación

### `'node' no se reconoce como un comando interno o externo`

Node.js no está instalado, o la terminal se abrió antes de instalarlo. Cierra y abre PowerShell. Si sigue, instala la versión **22 LTS** desde https://nodejs.org/en/download.

### `npm.ps1 no se puede cargar porque la ejecución de scripts está deshabilitada en este sistema`

En inglés: `npm.ps1 cannot be loaded because running scripts is disabled on this system`.

PowerShell bloquea scripts por defecto. Ejecuta una vez:

```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### `Vite requires Node.js version 20.19+ or 22.12+`

Tu Node.js es antiguo. Instala la versión 22 LTS y comprueba con `node -v`.

### `npm install` falla con `EPERM: operation not permitted` o `EBUSY: resource busy or locked`

Otro programa tiene bloqueados archivos de `node_modules`: el antivirus, OneDrive o un `npm run dev` que sigue abierto. Cierra los `npm run dev` abiertos y el editor, borra la carpeta e instala de nuevo:

```powershell
cd frontend
Remove-Item -Recurse -Force node_modules
npm install
```

Si el repo está dentro de OneDrive, muévelo a una carpeta como `C:\dev\`.

### `Filename too long`

```powershell
git config --global core.longpaths true
```

## Arranque

### `'vite' no se reconoce como un comando interno o externo`

Faltan las dependencias. Ejecuta `npm install` dentro de `frontend`.

### Puerto 5173 ocupado

`npm run dev` muestra `Port 5173 is in use, trying another one...` y arranca en el 5174.

Casi siempre es el **front de Docker**, que sigue encendido. Funciona igual en el 5174, pero para evitar confusiones apágalo:

```powershell
docker compose stop web
```

Si no es Docker, busca el proceso:

```powershell
netstat -ano | findstr :5173
Get-Process -Id <PID>
```

O usa otro puerto: pon `WEB_PORT=5174` en el `.env`.

## Conexión con el back

### La página dice *No se pudo conectar con el servidor*

La terminal de `npm run dev` muestra algo como:

```
[vite] http proxy error: /api/ping
Error: connect ECONNREFUSED 127.0.0.1:8080
```

El front no encuentra el back. Revisa:

1. **Que el back esté corriendo:** abre http://localhost:8080/api/ping. Si no responde, levántalo según [Ejecución del back](../backend/ejecucion.md), o con `docker compose up -d db api`.
2. **Que el puerto coincida:** `API_PORT` en el `.env` debe ser el puerto donde corre el back. Si lo cambiaste, reinicia `npm run dev`.

### El back responde `403` a una llamada del front

La ruta está protegida y todavía no hay inicio de sesión. Hoy solo es pública `/api/ping`; el resto se habilita con el caso de uso de autenticación.

## Pruebas

### `[MSW] Error: intercepted a request without a matching request handler`

La prueba llamó a un endpoint que no está simulado. Agrega su respuesta en `src/test/mocks/handlers.ts`. Las pruebas nunca deben llamar al back real.

### `TestingLibraryElementError: Unable to find an element with the text: ...`

La prueba buscó un texto que no está en pantalla, o lo buscó antes de que apareciera. Para lo que aparece después de una llamada al API, usa `await screen.findByText(...)` en lugar de `getByText`. El error muestra el HTML que sí había, lo que ayuda a ver la diferencia.
