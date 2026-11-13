import { useEffect, useState } from 'react'

export type RolUsuario = 'TECNICO_SIS' | 'TECNICO_MSP' | 'RESPONSABLE_COMUNIDAD'

export interface SesionActiva {
  token: string
  rol: RolUsuario
  correo?: string
  nombreCompleto?: string
}

const EVENTO_SESION = 'hampi-sesion-cambio'

function esRolValido(valor: unknown): valor is RolUsuario {
  return valor === 'TECNICO_SIS' || valor === 'TECNICO_MSP' || valor === 'RESPONSABLE_COMUNIDAD'
}

function decodificarPayloadJwt(token: string): Record<string, unknown> | null {
  const partes = token.split('.')
  if (partes.length !== 3) {
    return null
  }
  try {
    const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/')
    const relleno = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const json = atob(relleno)
    const datos: unknown = JSON.parse(json)
    if (typeof datos === 'object' && datos !== null) {
      return datos as Record<string, unknown>
    }
    return null
  } catch {
    return null
  }
}

function limpiarAlmacenamiento(): void {
  localStorage.removeItem('token')
  localStorage.removeItem('rol')
  localStorage.removeItem('nombreUsuario')
}

export function obtenerSesionActiva(): SesionActiva | null {
  if (typeof window === 'undefined') {
    return null
  }

  const token = localStorage.getItem('token')
  if (!token) {
    return null
  }

  const payload = decodificarPayloadJwt(token)
  if (payload && typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now()) {
    limpiarAlmacenamiento()
    return null
  }

  const rolJwt = payload?.rol
  const rolAlmacenado = localStorage.getItem('rol')
  const rol = esRolValido(rolJwt) ? rolJwt : esRolValido(rolAlmacenado) ? rolAlmacenado : null
  if (!rol) {
    return null
  }

  const correo = typeof payload?.sub === 'string' ? payload.sub : undefined
  const nombreCompleto = localStorage.getItem('nombreUsuario') ?? undefined

  return {
    token,
    rol,
    correo,
    nombreCompleto,
  }
}

export function guardarSesion(datos: {
  token: string
  rol: RolUsuario
  nombreCompleto?: string
}): void {
  localStorage.setItem('token', datos.token)
  localStorage.setItem('rol', datos.rol)
  if (datos.nombreCompleto) {
    localStorage.setItem('nombreUsuario', datos.nombreCompleto)
  } else {
    localStorage.removeItem('nombreUsuario')
  }
  window.dispatchEvent(new Event(EVENTO_SESION))
}

export function cerrarSesion(): void {
  limpiarAlmacenamiento()
  window.dispatchEvent(new Event(EVENTO_SESION))
}

export function useSesion(): SesionActiva | null {
  const [sesion, setSesion] = useState<SesionActiva | null>(() => obtenerSesionActiva())

  useEffect(() => {
    function actualizar() {
      setSesion(obtenerSesionActiva())
    }

    window.addEventListener('storage', actualizar)
    window.addEventListener(EVENTO_SESION, actualizar)
    return () => {
      window.removeEventListener('storage', actualizar)
      window.removeEventListener(EVENTO_SESION, actualizar)
    }
  }, [])

  return sesion
}
