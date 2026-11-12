import { act, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { ResultadosBusquedaPlantas } from './ResultadosBusquedaPlantas'

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

function renderizar(criterio: string) {
  const alVerTodas = vi.fn()
  const vista = render(
    <MemoryRouter>
      <ResultadosBusquedaPlantas criterio={criterio} alVerTodas={alVerTodas} />
    </MemoryRouter>,
  )
  return { alVerTodas, ...vista }
}

describe('ResultadosBusquedaPlantas', () => {
  beforeEach(() => simularPantalla(false))

  it('avisa mientras busca', () => {
    renderizar('chupa')

    expect(screen.getByText('Buscando plantas…')).toBeInTheDocument()
  })

  it('encuentra una planta por su nombre kichwa', async () => {
    renderizar('chupa')

    expect(await screen.findByText('1 resultado para «chupa»')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Cola de caballo' })).toHaveAttribute('href', '/catalogo-plantas/17')
  })

  it('encuentra una planta aunque el criterio no lleve tilde', async () => {
    renderizar('oregano')

    expect(await screen.findByRole('link', { name: 'Orégano' })).toBeInTheDocument()
    expect(screen.getByText('1 resultado para «oregano»')).toBeInTheDocument()
  })

  it('cuenta en plural cuando hay varios resultados', async () => {
    renderizar('ma')

    expect(await screen.findByText('5 resultados para «ma»')).toBeInTheDocument()
  })

  describe('en móvil', () => {
    it('muestra una lista compacta con miniatura, nombres, insignia térmica y enlace', async () => {
      const { container } = renderizar('chupa')

      const enlace = await screen.findByRole('link', { name: 'Cola de caballo' })
      const fila = within(enlace.closest('li') as HTMLElement)
      expect(container.querySelector('.busqueda-plantas-lista')).toBeInTheDocument()
      expect(screen.queryByRole('article')).not.toBeInTheDocument()
      expect(fila.getByRole('img', { name: 'Fotografía de Cola de caballo' })).toHaveAttribute(
        'src',
        '/api/publico/fotos/plantas/cola-de-caballo.jpg',
      )
      expect(fila.getByText('Equisetum giganteum').tagName).toBe('EM')
      expect(fila.getByText('Caballo chupa')).toBeInTheDocument()
      expect(fila.getByText('Fría')).toHaveClass('insignia-exito')
    })

    it('no muestra nombre kichwa cuando la planta no lo tiene', async () => {
      renderizar('oregano')

      const enlace = await screen.findByRole('link', { name: 'Orégano' })
      expect(within(enlace.closest('li') as HTMLElement).queryByText(/kichwa/i)).not.toBeInTheDocument()
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra los resultados en la cuadrícula de tarjetas', async () => {
      const { container } = renderizar('ma')

      expect(await screen.findByRole('article', { name: 'Manzanilla' })).toBeInTheDocument()
      expect(container.querySelector('.catalogo-plantas-grid')).toBeInTheDocument()
      expect(container.querySelector('.busqueda-plantas-lista')).not.toBeInTheDocument()
      expect(screen.getAllByRole('article')).toHaveLength(5)
      expect(
        within(screen.getByRole('article', { name: 'Matico' })).getByRole('link', { name: 'Ver ficha completa' }),
      ).toHaveAttribute('href', '/catalogo-plantas/6')
    })
  })

  it('sin coincidencias ofrece volver a todas las plantas', async () => {
    const { alVerTodas } = renderizar('xyz')

    expect(await screen.findByRole('status')).toHaveTextContent('No se encontraron plantas para «xyz»')
    const verTodas = screen.getByRole('button', { name: 'Ver todas las plantas' })
    expect(verTodas).toHaveClass('boton-secundario')

    await userEvent.click(verTodas)

    expect(alVerTodas).toHaveBeenCalledTimes(1)
  })

  it('muestra un error cuando la búsqueda falla', async () => {
    server.use(http.get('*/api/publico/plantas/buscar', () => new HttpResponse(null, { status: 500 })))
    renderizar('matico')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('No se pudo realizar la búsqueda. Intente nuevamente')
    expect(alerta).toHaveClass('notificacion-error')
    expect(screen.queryByText('Buscando plantas…')).not.toBeInTheDocument()
  })

  it('descarta la respuesta de un criterio anterior', async () => {
    let responderAnterior: () => void = () => {}
    server.use(
      http.get('*/api/publico/plantas/buscar', async ({ request }) => {
        if (new URL(request.url).searchParams.get('criterio') === 'matico') {
          await new Promise<void>((resolver) => {
            responderAnterior = resolver
          })
          return HttpResponse.json([])
        }
        return undefined
      }),
    )
    const { rerender } = renderizar('matico')

    rerender(
      <MemoryRouter>
        <ResultadosBusquedaPlantas criterio="chupa" alVerTodas={() => {}} />
      </MemoryRouter>,
    )
    expect(await screen.findByRole('link', { name: 'Cola de caballo' })).toBeInTheDocument()
    await act(async () => {
      responderAnterior()
      await new Promise((resolver) => setTimeout(resolver, 50))
    })

    expect(screen.getByText('1 resultado para «chupa»')).toBeInTheDocument()
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })
})
