import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { mensajeErrorFotografia, registrarPlanta, subirFotografiaPlanta } from './plantasAdminApi'
import type { CrearPlantaRequest } from './types'

async function errorDeSubida(): Promise<unknown> {
  try {
    await subirFotografiaPlanta(new File(['x'], 'foto.jpg', { type: 'image/jpeg' }))
  } catch (error) {
    return error
  }
  throw new Error('Se esperaba un error')
}

describe('plantasAdminApi', () => {
  it('subirFotografiaPlanta envía la imagen como multipart en el campo archivo', async () => {
    let tipoContenido: string | null = null
    let ruta = ''
    let enviado: FormDataEntryValue | null = null
    server.use(
      http.post('*/api/msp/plantas/fotografia', async ({ request }) => {
        tipoContenido = request.headers.get('Content-Type')
        ruta = new URL(request.url).pathname
        enviado = (await request.formData()).get('archivo')
        return HttpResponse.json({ fotografiaUrl: 'plantas/0b8f6c1e-2d4a-4c9b-9f1e-3a5d7c9e1b2f.jpg' })
      }),
    )
    const foto = new File([new Uint8Array([0xff, 0xd8, 0xff])], 'manzanilla.jpg', { type: 'image/jpeg' })

    await expect(subirFotografiaPlanta(foto)).resolves.toEqual({
      fotografiaUrl: 'plantas/0b8f6c1e-2d4a-4c9b-9f1e-3a5d7c9e1b2f.jpg',
    })
    expect(ruta).toBe('/api/msp/plantas/fotografia')
    expect(tipoContenido).toContain('multipart/form-data')
    expect(enviado).toMatchObject({ size: 3, type: 'image/jpeg' })
  })

  it('mensajeErrorFotografia traduce el 415 del back', async () => {
    server.use(
      http.post('*/api/msp/plantas/fotografia', () =>
        HttpResponse.json({ status: 415, detail: 'Formato no admitido' }, { status: 415 }),
      ),
    )

    expect(mensajeErrorFotografia(await errorDeSubida())).toBe(
      'Formato no admitido. Seleccione una imagen JPG, PNG o WEBP',
    )
  })

  it('mensajeErrorFotografia traduce el 413 aunque no traiga JSON', async () => {
    server.use(
      http.post('*/api/msp/plantas/fotografia', () =>
        new HttpResponse('<html><body>413 Request Entity Too Large</body></html>', {
          status: 413,
          headers: { 'Content-Type': 'text/html' },
        }),
      ),
    )

    expect(mensajeErrorFotografia(await errorDeSubida())).toBe(
      'La imagen supera el límite de 5 MB. Intente reducir la resolución',
    )
  })

  it('mensajeErrorFotografia usa un mensaje genérico para otros errores', async () => {
    server.use(http.post('*/api/msp/plantas/fotografia', () => new HttpResponse(null, { status: 500 })))

    expect(mensajeErrorFotografia(await errorDeSubida())).toBe('No se pudo subir la fotografía. Intente nuevamente')
    expect(mensajeErrorFotografia(new TypeError('Failed to fetch'))).toBe(
      'No se pudo subir la fotografía. Intente nuevamente',
    )
  })

  it('registrarPlanta envía la planta como JSON y devuelve su ficha', async () => {
    let ruta = ''
    let cuerpo: unknown = null
    server.use(
      http.post('*/api/msp/plantas', async ({ request }) => {
        ruta = new URL(request.url).pathname
        cuerpo = await request.json()
        return HttpResponse.json({ id: 99, ...(cuerpo as CrearPlantaRequest) }, { status: 201 })
      }),
    )
    const datos: CrearPlantaRequest = {
      nombreComun: 'Hierba de prueba',
      nombreKichwa: null,
      nombreCientifico: 'Herba ficticia',
      familiaBotanica: 'Ficticiaceae',
      cualidadTermica: 'FRIA',
      cicloCultivo: 'CORTO',
      origen: null,
      altitudMinMsnm: 2000,
      altitudMaxMsnm: 3200,
      regionesDistribucion: 'Costa, Sierra',
      usosMedicinales: 'Alivia el dolor',
      preparacionDosificacion: null,
      contraindicaciones: null,
      categorias: ['SEDANTE'],
      fotografiaUrl: null,
    }

    await expect(registrarPlanta(datos)).resolves.toEqual({ id: 99, ...datos })
    expect(ruta).toBe('/api/msp/plantas')
    expect(cuerpo).toEqual(datos)
  })
})
