import { http, HttpResponse } from 'msw'
import type { CrearPlantaRequest, PlantaDetalleResponse } from './types'

export const fotografiaSubidaMock = 'plantas/3f2a1b4c-5d6e-4f70-8a9b-0c1d2e3f4a5b.jpg'

export const catalogoAdminHandlers = [
  http.post('*/api/msp/plantas/fotografia', () => HttpResponse.json({ fotografiaUrl: fotografiaSubidaMock })),

  http.post('*/api/msp/plantas', async ({ request }) => {
    const datos = (await request.json()) as CrearPlantaRequest
    const planta: PlantaDetalleResponse = { id: 99, ...datos }
    return HttpResponse.json(planta, { status: 201 })
  }),
]
