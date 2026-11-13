import { beforeEach, describe, expect, it } from 'vitest'
import { cerrarSesion, guardarSesion, obtenerSesionActiva } from './sesion'

function crearJwtPrueba(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
  return `${header}.${body}.firma`
}

describe('sesion', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('retorna null cuando no existe token almacenado', () => {
    expect(obtenerSesionActiva()).toBeNull()
  })

  it('decodifica el rol y correo desde un JWT vigente', () => {
    const exp = Math.floor(Date.now() / 1000) + 3600
    const token = crearJwtPrueba({ sub: 'msp@salud.gob.ec', rol: 'TECNICO_MSP', exp })
    localStorage.setItem('token', token)

    const sesion = obtenerSesionActiva()
    expect(sesion).toEqual({
      token,
      rol: 'TECNICO_MSP',
      correo: 'msp@salud.gob.ec',
      nombreCompleto: undefined,
    })
  })

  it('limpia la sesión cuando el JWT está expirado', () => {
    const exp = Math.floor(Date.now() / 1000) - 60
    const token = crearJwtPrueba({ sub: 'msp@salud.gob.ec', rol: 'TECNICO_MSP', exp })
    localStorage.setItem('token', token)
    localStorage.setItem('rol', 'TECNICO_MSP')

    expect(obtenerSesionActiva()).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('guarda y limpia la sesión correctamente', () => {
    guardarSesion({
      token: 'token-jwt',
      rol: 'RESPONSABLE_COMUNIDAD',
      nombreCompleto: 'Rosa Quishpe',
    })

    expect(obtenerSesionActiva()).toEqual({
      token: 'token-jwt',
      rol: 'RESPONSABLE_COMUNIDAD',
      correo: undefined,
      nombreCompleto: 'Rosa Quishpe',
    })

    cerrarSesion()
    expect(obtenerSesionActiva()).toBeNull()
  })
})
