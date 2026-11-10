/**
 * Cliente HTTP único para hablar con el back. En desarrollo Vite redirige /api a Spring Boot.
 */
export class ApiError extends Error {
  readonly status: number
  readonly detalle?: string
  readonly errores?: Record<string, string>

  constructor(status: number, message: string, detalle?: string, errores?: Record<string, string>) {
    super(message)
    this.status = status
    this.detalle = detalle
    this.errores = errores
  }
}

export interface PaginaResponse<T> {
  contenido: T[]
  pagina: number
  tamanio: number
  totalElementos: number
  totalPaginas: number
}

interface ProblemDetail {
  detail?: string
  errores?: Record<string, string>
}

async function leerProblema(respuesta: Response): Promise<ProblemDetail> {
  try {
    return (await respuesta.json()) as ProblemDetail
  } catch {
    return {}
  }
}

async function solicitar<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')

  const respuesta = await fetch(new URL(`/api${path}`, window.location.origin), { ...init, headers })

  if (!respuesta.ok) {
    const problema = await leerProblema(respuesta)
    throw new ApiError(
      respuesta.status,
      problema.detail ?? `Error ${respuesta.status} en ${path}`,
      problema.detail,
      problema.errores,
    )
  }
  if (respuesta.status === 204) {
    return undefined as T
  }
  return respuesta.json() as Promise<T>
}

function conJson(metodo: string, body: unknown): RequestInit {
  return {
    method: metodo,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

export function apiGet<T>(path: string): Promise<T> {
  return solicitar<T>(path)
}

export function apiPost<T>(path: string, body: unknown): Promise<T> {
  return solicitar<T>(path, conJson('POST', body))
}

export function apiPut<T>(path: string, body: unknown): Promise<T> {
  return solicitar<T>(path, conJson('PUT', body))
}

export function apiPostFormData<T>(path: string, formData: FormData): Promise<T> {
  return solicitar<T>(path, { method: 'POST', body: formData })
}
