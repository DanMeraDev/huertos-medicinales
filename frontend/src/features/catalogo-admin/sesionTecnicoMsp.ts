interface PayloadSesion {
  rol?: unknown
  exp?: unknown
}

function leerPayload(token: string): PayloadSesion | null {
  const partes = token.split('.')
  if (partes.length !== 3) return null
  try {
    return JSON.parse(atob(partes[1].replace(/-/g, '+').replace(/_/g, '/'))) as PayloadSesion
  } catch {
    return null
  }
}

export function esTecnicoMsp() {
  const token = localStorage.getItem('token')
  const payload = token ? leerPayload(token) : null
  return payload?.rol === 'TECNICO_MSP' && typeof payload.exp === 'number' && payload.exp * 1000 > Date.now()
}
