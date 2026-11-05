import { http, HttpResponse } from 'msw'
import { server } from '../test/mocks/server'
import { ApiError, apiGet, apiPost, apiPostFormData, apiPut } from './client'

async function capturarError(promesa: Promise<unknown>): Promise<ApiError> {
  try {
    await promesa
  } catch (error) {
    if (error instanceof ApiError) return error
    throw error
  }
  throw new Error('Se esperaba un ApiError')
}

describe('client', () => {
  it('apiGet devuelve el cuerpo de la respuesta', async () => {
    server.use(http.get('*/api/prueba', () => HttpResponse.json({ valor: 1 })))

    await expect(apiGet('/prueba')).resolves.toEqual({ valor: 1 })
  })

  it('apiPost envía el cuerpo como JSON y devuelve la respuesta', async () => {
    let tipoContenido: string | null = null
    let cuerpo: unknown
    server.use(
      http.post('*/api/prueba', async ({ request }) => {
        tipoContenido = request.headers.get('Content-Type')
        cuerpo = await request.json()
        return HttpResponse.json({ id: 7 }, { status: 201 })
      }),
    )

    await expect(apiPost('/prueba', { nombre: 'Manzanilla' })).resolves.toEqual({ id: 7 })
    expect(tipoContenido).toBe('application/json')
    expect(cuerpo).toEqual({ nombre: 'Manzanilla' })
  })

  it('apiPut envía el cuerpo como JSON y devuelve la respuesta', async () => {
    let tipoContenido: string | null = null
    let cuerpo: unknown
    server.use(
      http.put('*/api/prueba/7', async ({ request }) => {
        tipoContenido = request.headers.get('Content-Type')
        cuerpo = await request.json()
        return HttpResponse.json({ id: 7, nombre: 'Ruda' })
      }),
    )

    await expect(apiPut('/prueba/7', { nombre: 'Ruda' })).resolves.toEqual({ id: 7, nombre: 'Ruda' })
    expect(tipoContenido).toBe('application/json')
    expect(cuerpo).toEqual({ nombre: 'Ruda' })
  })

  it('apiPostFormData envía el FormData sin forzar JSON', async () => {
    let tipoContenido: string | null = null
    let nombre: FormDataEntryValue | null = null
    server.use(
      http.post('*/api/prueba', async ({ request }) => {
        tipoContenido = request.headers.get('Content-Type')
        nombre = (await request.formData()).get('nombre')
        return HttpResponse.json({ id: 8 }, { status: 201 })
      }),
    )
    const formData = new FormData()
    formData.append('nombre', 'Ortiga')

    await expect(apiPostFormData('/prueba', formData)).resolves.toEqual({ id: 8 })
    expect(tipoContenido).toContain('multipart/form-data')
    expect(nombre).toBe('Ortiga')
  })

  it('un 400 con errores lanza ApiError con detalle y errores', async () => {
    server.use(
      http.post('*/api/prueba', () =>
        HttpResponse.json(
          { status: 400, detail: 'Datos no válidos', errores: { nombre: 'Este campo es obligatorio' } },
          { status: 400 },
        ),
      ),
    )

    const error = await capturarError(apiPost('/prueba', {}))

    expect(error.status).toBe(400)
    expect(error.detalle).toBe('Datos no válidos')
    expect(error.errores).toEqual({ nombre: 'Este campo es obligatorio' })
  })

  it('un 409 lanza ApiError con el detalle como mensaje', async () => {
    server.use(
      http.post('*/api/prueba', () =>
        HttpResponse.json({ status: 409, detail: 'Ya existe un registro con ese nombre' }, { status: 409 }),
      ),
    )

    const error = await capturarError(apiPost('/prueba', {}))

    expect(error.status).toBe(409)
    expect(error.detalle).toBe('Ya existe un registro con ese nombre')
    expect(error.message).toBe('Ya existe un registro con ese nombre')
    expect(error.errores).toBeUndefined()
  })

  it('un 500 sin cuerpo JSON lanza ApiError sin detalle ni errores', async () => {
    server.use(http.get('*/api/prueba', () => new HttpResponse(null, { status: 500 })))

    const error = await capturarError(apiGet('/prueba'))

    expect(error.status).toBe(500)
    expect(error.detalle).toBeUndefined()
    expect(error.errores).toBeUndefined()
    expect(error.message).toBe('Error 500 en /prueba')
  })

  it('un 204 devuelve undefined', async () => {
    server.use(http.put('*/api/prueba/7', () => new HttpResponse(null, { status: 204 })))

    await expect(apiPut('/prueba/7', {})).resolves.toBeUndefined()
  })
})
