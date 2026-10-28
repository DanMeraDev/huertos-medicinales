/**
 * Cliente HTTP único para hablar con el back. En desarrollo Vite redirige /api a Spring Boot.
 */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

export async function apiGet<T>(path: string): Promise<T> {
  const respuesta = await fetch(new URL(`/api${path}`, window.location.origin), {
    headers: { Accept: 'application/json' },
  })
  if (!respuesta.ok) {
    throw new ApiError(respuesta.status, `Error ${respuesta.status} en ${path}`)
  }
  return respuesta.json() as Promise<T>
}
