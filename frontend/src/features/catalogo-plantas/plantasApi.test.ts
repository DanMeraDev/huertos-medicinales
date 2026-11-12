import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { buscarPlantas } from './plantasApi'
import { plantasMock } from './plantasMockHandlers'

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
})
