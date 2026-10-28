# Estructura del front

Aplicación en React 19 con TypeScript y Vite, organizada por funcionalidades (*features*). Este documento explica qué va en cada carpeta, cómo se nombran los archivos y qué se crea al implementar un caso de uso.

Relacionados: [Ejecución del front](ejecucion.md) · [Errores comunes del front](errores-comunes.md)

## 1. Vista general

```
frontend/
├── src/
│   ├── main.tsx              punto de entrada: monta <App /> en el HTML
│   ├── App.tsx               componente raíz: rutas y proveedores globales
│   ├── index.css             estilos globales
│   ├── api/                  cliente HTTP para hablar con el back
│   ├── features/             una carpeta por funcionalidad o caso de uso
│   ├── components/           componentes reutilizables entre features
│   ├── pages/                pantallas completas, que arman features
│   └── test/                 configuración de pruebas y API simulada
├── public/                   archivos estáticos servidos tal cual (favicon, modelos 3D)
├── index.html                HTML base donde se monta la aplicación
├── vite.config.ts            configuración de Vite, del proxy al back y de Vitest
├── tsconfig*.json            configuración de TypeScript
├── .oxlintrc.json            reglas del linter
├── package.json              dependencias y scripts
├── Dockerfile                imagen del front para docker-compose
└── nginx.conf                servidor web dentro de Docker; redirige /api al back
```

## 2. Carpetas de `src/`

### `api/`

El **único** lugar desde donde se llama al back.

- `client.ts` tiene las funciones base (`apiGet` y las que se agreguen: `apiPost`, `apiPut`, `apiDelete`), que arman la URL, envían la petición y convierten los errores en `ApiError`.
- Cuando se implemente el inicio de sesión, el token JWT se agrega aquí, en un solo sitio, y todas las llamadas lo enviarán.
- **Regla:** ningún componente usa `fetch` directamente.

### `features/`

Una carpeta por funcionalidad del sistema. Todo lo de una feature vive junto: sus llamadas al API, sus componentes, sus hooks y sus pruebas.

```
features/
├── ping/                 ejemplo existente
│   ├── pingApi.ts
│   ├── EstadoServidor.tsx
│   └── EstadoServidor.test.tsx
├── comunidades/
├── huerto-real/
├── plantas/
├── bodega/
├── actividades/
├── gamificacion/
├── huerto-virtual/       escena 3D con React Three Fiber
└── foro/
```

- **Nombre de la carpeta:** en minúsculas y con guion, en plural cuando es un recurso (`comunidades`, `plantas`).
- **Regla:** una feature puede usar `api/` y `components/`, pero **no** importa archivos de otra feature. Si dos features necesitan lo mismo, ese código pasa a `components/` o a un archivo compartido.

### `components/`

Componentes de interfaz reutilizables que no pertenecen a una feature: botones, campos de formulario, tarjetas, el menú, mensajes de carga o de error.

- **Regla:** no llaman al API ni conocen el negocio. Reciben datos por props y avisan con callbacks.

### `pages/`

Una pantalla completa por archivo, asociada a una ruta. Arma la pantalla con componentes de una o varias features.

- **Nombre:** `<Nombre>Page.tsx`, por ejemplo `InicioPage.tsx` o `ComunidadesPage.tsx`.
- **Regla:** casi sin lógica. Si una página empieza a crecer, esa lógica va a la feature.

### `test/`

- `setup.ts`: prepara Vitest, Testing Library y MSW antes de las pruebas.
- `mocks/handlers.ts`: respuestas simuladas del back. Cada endpoint que use el front se agrega aquí.
- `mocks/server.ts`: el servidor MSW que intercepta las llamadas durante las pruebas.

Gracias a esto, las pruebas del front no necesitan el back levantado.

## 3. Ejemplo: archivos de un caso de uso

> El código es solo ilustrativo, para mostrar dónde va cada pieza.

Para *CU-01: Registro de comunidad*:

```
frontend/src/
├── features/comunidades/
│   ├── comunidadesApi.ts          tipos y llamadas al API
│   ├── ComunidadForm.tsx          formulario de registro
│   ├── ComunidadForm.test.tsx     sus pruebas
│   └── ListaComunidades.tsx
├── pages/ComunidadesPage.tsx      la pantalla que los muestra
└── test/mocks/handlers.ts         se agregan los endpoints /api/comunidades
```

**Llamadas al API**, con los tipos iguales a los DTOs del back:

```ts
import { apiGet, apiPost } from '../../api/client'

export interface ComunidadRequest {
  nombre: string
  contactoResponsable: string
}

export interface ComunidadResponse extends ComunidadRequest {
  id: number
}

export const listarComunidades = () => apiGet<ComunidadResponse[]>('/comunidades')

export const registrarComunidad = (datos: ComunidadRequest) =>
  apiPost<ComunidadResponse>('/comunidades', datos)
```

**Respuesta simulada** en `test/mocks/handlers.ts`:

```ts
http.post('*/api/comunidades', async ({ request }) => {
  const datos = (await request.json()) as ComunidadRequest
  return HttpResponse.json({ id: 1, ...datos }, { status: 201 })
}),
```

## 4. Convenciones

| Elemento | Convención | Ejemplo |
|---|---|---|
| Componente | PascalCase, archivo `.tsx` con el mismo nombre | `ComunidadForm.tsx` exporta `ComunidadForm` |
| Página | PascalCase terminado en `Page` | `ComunidadesPage.tsx` |
| Llamadas al API | camelCase terminado en `Api`, archivo `.ts` | `comunidadesApi.ts` |
| Hook | camelCase empezando con `use` | `useComunidades.ts` |
| Prueba | Mismo nombre del archivo con `.test` | `ComunidadForm.test.tsx` |
| Tipos | PascalCase; los del API, igual que el DTO del back | `ComunidadResponse` |
| Carpeta de feature | minúsculas con guion | `huerto-virtual` |

- **Exportaciones con nombre** (`export function ComunidadForm`), no `export default`, salvo en `App.tsx`. Así el nombre es el mismo en todos los imports.
- **Textos de la interfaz en español**, igual que los nombres del negocio en el código.
- **Pruebas con Testing Library:** se busca por lo que ve el usuario (`getByRole`, `getByLabelText`, `findByText`), no por clases CSS ni por `id`.

## 5. Librerías que se agregan más adelante

Para mantener el esqueleto simple, estas librerías del stack se instalan en el incremento en que se usan, cada una en su propio Pull Request:

| Librería | Para qué | Dónde se usa |
|---|---|---|
| React Router | Rutas entre páginas | `App.tsx` y `pages/` |
| React Three Fiber y drei | Huerto virtual en 3D | `features/huerto-virtual/` |
| Leaflet / React Leaflet | Mapa de ubicación del huerto | `features/huerto-real/` |
| Plugin PWA de Vite | Instalación como aplicación | `vite.config.ts` |
| Playwright | Pruebas de extremo a extremo | Carpeta `e2e/` en la raíz del repo |
