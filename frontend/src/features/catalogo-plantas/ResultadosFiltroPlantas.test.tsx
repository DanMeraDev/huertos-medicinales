import { act, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { ResultadosFiltroPlantas } from './ResultadosFiltroPlantas'
import type { FiltrosPlanta } from './types'

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

function contenido(filtros: FiltrosPlanta, criterio: string, alLimpiar: () => void) {
  return (
    <MemoryRouter>
      <ResultadosFiltroPlantas filtros={filtros} criterio={criterio} alLimpiar={alLimpiar} />
    </MemoryRouter>
  )
}

function renderizar(filtros: FiltrosPlanta, criterio = '') {
  const alLimpiar = vi.fn()
  const vista = render(contenido(filtros, criterio, alLimpiar))
  return { alLimpiar, ...vista }
}

describe('ResultadosFiltroPlantas', () => {
  beforeEach(() => simularPantalla(false))

  it('avisa mientras filtra', () => {
    renderizar({ cualidad: 'FRIA' })

    expect(screen.getByText('Filtrando plantas…')).toBeInTheDocument()
  })

  it('muestra las plantas que cumplen los filtros con su contador', async () => {
    const { container } = renderizar({ cualidad: 'FRIA' })

    expect(await screen.findByText('6 plantas encontradas')).toBeInTheDocument()
    expect(screen.getAllByRole('listitem')).toHaveLength(6)
    expect(container.querySelector('.busqueda-plantas-lista')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Cola de caballo' })).toHaveAttribute('href', '/catalogo-plantas/17')
  })

  it('cuenta en singular cuando hay una sola planta', async () => {
    renderizar({ region: 'Amazonía' })

    expect(await screen.findByText('1 planta encontrada')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Cola de caballo' })).toBeInTheDocument()
  })

  it('combina los filtros con el criterio del buscador', async () => {
    renderizar({ cualidad: 'FRIA' }, 'ma')

    expect(await screen.findByText('1 planta encontrada')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Llantén' })).toBeInTheDocument()
  })

  it('en escritorio muestra los resultados en la cuadrícula de tarjetas', async () => {
    simularPantalla(true)
    const { container } = renderizar({ cualidad: 'FRIA' })

    expect(await screen.findByRole('article', { name: 'Sábila' })).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(6)
    expect(container.querySelector('.catalogo-plantas-grid')).toBeInTheDocument()
    expect(
      within(screen.getByRole('article', { name: 'Llantén' })).getByRole('link', { name: 'Ver ficha completa' }),
    ).toHaveAttribute('href', '/catalogo-plantas/16')
  })

  it('sin resultados ofrece limpiar los filtros', async () => {
    const { alLimpiar } = renderizar({ region: 'Amazonía', ciclo: 'CORTO' })

    expect(await screen.findByRole('status')).toHaveTextContent(
      'No se encontraron plantas para estos parámetros. Intente reajustar los filtros',
    )
    expect(screen.getByRole('status')).toHaveClass('catalogo-plantas-vacio')
    expect(screen.queryByText(/encontradas?$/)).not.toBeInTheDocument()
    const limpiar = screen.getByRole('button', { name: 'Limpiar filtros' })
    expect(limpiar).toHaveClass('boton-secundario')

    await userEvent.click(limpiar)

    expect(alLimpiar).toHaveBeenCalledTimes(1)
  })

  it('muestra un error cuando el filtrado falla', async () => {
    server.use(http.get('*/api/publico/plantas/filtrar', () => new HttpResponse(null, { status: 500 })))
    renderizar({ cualidad: 'FRIA' })

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('No se pudieron aplicar los filtros. Intente nuevamente')
    expect(alerta).toHaveClass('notificacion-error')
    expect(screen.queryByText('Filtrando plantas…')).not.toBeInTheDocument()
  })

  it('descarta la respuesta de filtros anteriores', async () => {
    let responderAnterior: () => void = () => {}
    server.use(
      http.get('*/api/publico/plantas/filtrar', async ({ request }) => {
        if (new URL(request.url).searchParams.get('cualidad') === 'CALIENTE') {
          await new Promise<void>((resolver) => {
            responderAnterior = resolver
          })
          return HttpResponse.json([])
        }
        return undefined
      }),
    )
    const { rerender } = renderizar({ cualidad: 'CALIENTE' })

    rerender(contenido({ cualidad: 'FRIA' }, '', () => {}))
    expect(await screen.findByText('6 plantas encontradas')).toBeInTheDocument()
    await act(async () => {
      responderAnterior()
      await new Promise((resolver) => setTimeout(resolver, 50))
    })

    expect(screen.getByText('6 plantas encontradas')).toBeInTheDocument()
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })
})
