import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { Link, MemoryRouter, Route, Routes } from 'react-router'
import { colaDeCaballoDetalle } from '../features/catalogo-plantas/plantasMockHandlers'
import { server } from '../test/mocks/server'
import { DetallePlantaPage } from './DetallePlantaPage'

function simularPantalla(esEscritorio: boolean) {
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    writable: true,
    value: (consulta: string) => ({
      matches: esEscritorio && consulta === '(min-width: 1024px)',
      media: consulta,
      addEventListener: () => {},
      removeEventListener: () => {},
    }),
  })
}

function renderizarEn(ruta: string) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <Link to="/catalogo-plantas/14">Ir a Achicoria</Link>
      <Routes>
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

function contarPeticionesDeDetalle() {
  const contador = { peticiones: 0 }
  server.use(
    http.get('*/api/publico/plantas/:id', () => {
      contador.peticiones += 1
      return undefined
    }),
  )
  return contador
}

describe('DetallePlantaPage', () => {
  beforeEach(() => simularPantalla(false))

  it('avisa mientras carga la ficha', () => {
    renderizarEn('/catalogo-plantas/17')

    expect(screen.getByText('Cargando ficha…')).toBeInTheDocument()
  })

  it('carga la ficha técnica de la planta desde el servidor', async () => {
    renderizarEn('/catalogo-plantas/17')

    expect(await screen.findByRole('heading', { level: 1, name: 'Cola de caballo' })).toBeInTheDocument()
    expect(screen.getByText(colaDeCaballoDetalle.usosMedicinales)).toBeInTheDocument()
    expect(screen.queryByText('Cargando ficha…')).not.toBeInTheDocument()
  })

  it('con un id desconocido avisa que la ficha no está disponible y ofrece volver', async () => {
    renderizarEn('/catalogo-plantas/999')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('La ficha solicitada no está disponible')
    expect(alerta).toHaveClass('notificacion-error')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
  })

  it.each(['abc', '0', '-3', '1.5'])('con el id "%s" avisa que no está disponible sin consultar', async (id) => {
    const contador = contarPeticionesDeDetalle()
    renderizarEn(`/catalogo-plantas/${id}`)

    expect(await screen.findByRole('alert')).toHaveTextContent('La ficha solicitada no está disponible')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
    await act(async () => {
      await new Promise((resolver) => setTimeout(resolver, 50))
    })
    expect(contador.peticiones).toBe(0)
  })

  it('con un error del servidor muestra un mensaje genérico y ofrece volver', async () => {
    server.use(http.get('*/api/publico/plantas/:id', () => new HttpResponse(null, { status: 500 })))
    renderizarEn('/catalogo-plantas/17')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('No se pudo cargar la ficha. Intente nuevamente más tarde')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
  })

  it('descarta la respuesta de la planta anterior al cambiar de id', async () => {
    let responderAnterior: () => void = () => {}
    server.use(
      http.get('*/api/publico/plantas/17', async () => {
        await new Promise<void>((resolver) => {
          responderAnterior = resolver
        })
        return HttpResponse.json(colaDeCaballoDetalle)
      }),
    )
    renderizarEn('/catalogo-plantas/17')

    await userEvent.click(screen.getByRole('link', { name: 'Ir a Achicoria' }))
    expect(await screen.findByRole('heading', { level: 1, name: 'Achicoria' })).toBeInTheDocument()
    await act(async () => {
      responderAnterior()
      await new Promise((resolver) => setTimeout(resolver, 50))
    })

    expect(screen.getByRole('heading', { level: 1, name: 'Achicoria' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { level: 1, name: 'Cola de caballo' })).not.toBeInTheDocument()
  })
})
