# Ejecución del front

Cómo correr el front en tu máquina, cómo se conecta con el back y cómo correr sus pruebas.

Antes, completa [Primeros pasos](../primeros-pasos.md): Node.js 22, el permiso para ejecutar npm en PowerShell y el `.env`.

Relacionados: [Estructura del front](estructura.md) · [Errores comunes del front](errores-comunes.md)

## 1. Instalar dependencias

La primera vez, o cuando cambie `package.json` después de un `git pull`:

```powershell
cd frontend
npm install
```

## 2. Correr el front

El front necesita el back corriendo para mostrar datos. Hay dos formas:

- **Back en Docker:** si no vas a tocar el back, desde la raíz del repo ejecuta `docker compose up -d db api`.
- **Back en local:** sigue [Ejecución del back](../backend/ejecucion.md).

Si el front de Docker está encendido, apágalo para liberar el puerto 5173:

```powershell
docker compose stop web
```

Luego:

```powershell
cd frontend
npm run dev
```

Abre http://localhost:5173. Al guardar un archivo, la página se actualiza sola. Para detenerlo: `Ctrl+C`.

### Cómo se conecta con el back

El front llama siempre a rutas relativas como `/api/ping`. Vite redirige todo lo que empieza con `/api` al back, en el puerto `API_PORT` del `.env` de la raíz (8080 por defecto). Por eso:

- No hay que escribir `http://localhost:8080` en el código.
- Si alguien usa otro puerto para el back, basta con cambiar `API_PORT` en su `.env` y reiniciar `npm run dev`.

Dentro de Docker, el que redirige `/api` al back es nginx, configurado en `frontend/nginx.conf`.

### En VS Code

Se recomiendan estas extensiones:

- **ESLint** o **oxc**, para ver los avisos del linter en el editor.
- **Vitest**, para correr las pruebas desde la vista *Testing*.

Corre `npm run dev` en la terminal integrada (`Ctrl+ñ` o *Terminal → New Terminal*).

### En IntelliJ IDEA

IntelliJ Ultimate soporta TypeScript y React sin plugins. Abre la carpeta `frontend` y ejecuta los scripts desde la pestaña **npm** o desde la terminal integrada.

## 3. Scripts

| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática |
| `npm run build` | Revisa los tipos de TypeScript y genera la versión final en `dist/` |
| `npm run preview` | Sirve la carpeta `dist/` para revisar la versión final |
| `npm run lint` | Revisa el código con oxlint |
| `npm test` | Pruebas en modo observador: se repiten al guardar |
| `npm run test:run` | Pruebas una sola vez, como en el CI |
| `npm run coverage` | Pruebas con reporte de cobertura en `coverage/` |

## 4. Antes de abrir un Pull Request

Corre lo mismo que corre el CI:

```powershell
cd frontend
npm run lint
npm run test:run
npm run build
```

Si alguno falla en tu máquina, también fallará en GitHub.
