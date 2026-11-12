import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { buscarPlantas, obtenerDetallePlanta } from './plantasApi'
import { colaDeCaballoDetalle, plantasMock } from './plantasMockHandlers'

describe('plantasApi', () => {
  it('buscarPlantas codifica el criterio en la URL', async () => {
    let criterioRecibido: string | null = null
    server.use(
      http.get('*/api/publico/plantas/buscar', ({ request }) => {
        criterioRecibido = new URL(request.url).searchParams.get('criterio')
        return HttpResponse.json([plantasMock[0]])
      }),
    )

    await expect(buscarPlantas('té & ruda?#1')).resolves.toEqual([plantasMock[0]])
    expect(criterioRecibido).toBe('té & ruda?#1')
  })

  it('buscarPlantas no es atendida por el handler de la ficha técnica', async () => {
    await expect(buscarPlantas('chupa')).resolves.toEqual([plantasMock[4]])
  })

  it('obtenerDetallePlanta pide la ficha técnica de la planta por su id', async () => {
    let rutaRecibida: string | null = null
    server.use(
      http.get('*/api/publico/plantas/:id', ({ request }) => {
        rutaRecibida = new URL(request.url).pathname
        return HttpResponse.json(colaDeCaballoDetalle)
      }),
    )

    await expect(obtenerDetallePlanta(17)).resolves.toEqual(colaDeCaballoDetalle)
    expect(rutaRecibida).toBe('/api/publico/plantas/17')
  })
})
