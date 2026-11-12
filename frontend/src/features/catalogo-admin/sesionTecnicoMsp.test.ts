import { esTecnicoMsp } from './sesionTecnicoMsp'

function base64Url(datos: object) {
  return btoa(JSON.stringify(datos)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

function token(payload: object) {
  return `${base64Url({ alg: 'HS256' })}.${base64Url(payload)}.firma`
}

const EN_UNA_HORA = Math.floor(Date.now() / 1000) + 3600
const HACE_UNA_HORA = Math.floor(Date.now() / 1000) - 3600

describe('esTecnicoMsp', () => {
  afterEach(() => localStorage.clear())

  it('sin token devuelve false', () => {
    expect(esTecnicoMsp()).toBe(false)
  })

  it('con un token mal formado devuelve false', () => {
    localStorage.setItem('token', 'no-es-un-jwt')
    expect(esTecnicoMsp()).toBe(false)

    localStorage.setItem('token', 'cabecera.%%%.firma')
    expect(esTecnicoMsp()).toBe(false)
  })

  it('con otro rol devuelve false', () => {
    localStorage.setItem('token', token({ sub: 'rosa@huertos.gob.ec', rol: 'RESPONSABLE_COMUNIDAD', exp: EN_UNA_HORA }))

    expect(esTecnicoMsp()).toBe(false)
  })

  it('con un token de técnico MSP expirado devuelve false', () => {
    localStorage.setItem('token', token({ sub: 'tecnico@msp.gob.ec', rol: 'TECNICO_MSP', exp: HACE_UNA_HORA }))

    expect(esTecnicoMsp()).toBe(false)
  })

  it('con un token vigente de técnico MSP devuelve true', () => {
    localStorage.setItem('token', token({ sub: 'tecnico@msp.gob.ec', rol: 'TECNICO_MSP', exp: EN_UNA_HORA }))

    expect(esTecnicoMsp()).toBe(true)
  })
})
