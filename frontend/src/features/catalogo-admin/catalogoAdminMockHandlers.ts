import { http, HttpResponse } from 'msw'
import type { ActualizarPlantaRequest, CrearPlantaRequest, PlantaDetalleResponse } from './types'

export const fotografiaSubidaMock = 'plantas/3f2a1b4c-5d6e-4f70-8a9b-0c1d2e3f4a5b.jpg'

export const plantaEditableMock: PlantaDetalleResponse = {
  id: 7,
  nombreComun: 'Hierba editable',
  nombreKichwa: 'Yura kiwa',
  nombreCientifico: 'Herba editabilis',
  familiaBotanica: 'Ficticiaceae',
  cualidadTermica: 'FRIA',
  cicloCultivo: 'CORTO',
  origen: 'NATIVA',
  altitudMinMsnm: 2000,
  altitudMaxMsnm: 3200,
  regionesDistribucion: 'Sierra, Costa',
  usosMedicinales: 'Alivia el dolor de estómago',
  preparacionDosificacion: 'Infusión de 5 g en una taza',
  contraindicaciones: 'No usar en embarazo',
  categorias: ['SEDANTE', 'DIURETICA'],
  fotografiaUrl: 'plantas/hierba-editable.jpg',
}

export const plantaEditableHandler = http.get('*/api/publico/plantas/:id', ({ params }) =>
  params.id === String(plantaEditableMock.id) ? HttpResponse.json(plantaEditableMock) : undefined,
)

export const catalogoAdminHandlers = [
  http.post('*/api/msp/plantas/fotografia', () => HttpResponse.json({ fotografiaUrl: fotografiaSubidaMock })),

  http.post('*/api/msp/plantas', async ({ request }) => {
    const datos = (await request.json()) as CrearPlantaRequest
    const planta: PlantaDetalleResponse = { id: 99, ...datos }
    return HttpResponse.json(planta, { status: 201 })
  }),

  http.put('*/api/msp/plantas/:id', async ({ params, request }) => {
    const datos = (await request.json()) as ActualizarPlantaRequest
    const planta: PlantaDetalleResponse = { id: Number(params.id), ...datos }
    return HttpResponse.json(planta)
  }),

  http.patch('*/api/msp/plantas/:id/baja', () => new HttpResponse(null, { status: 204 })),
]
