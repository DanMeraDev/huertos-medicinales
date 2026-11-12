import { act, fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { CatalogoPlantasGrid } from './CatalogoPlantasGrid'
import { paginaDePlantas, plantasMock } from './plantasMockHandlers'

let pantallaEscritorio = false
const avisosDePantalla = new Set<() => void>()

function simularPantalla(esEscritorio: boolean) {
  pantallaEscritorio = esEscritorio
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    writable: true,
    value: (consulta: string) => ({
      matches: pantallaEscritorio && consulta === '(min-width: 1024px)',
      media: consulta,
      addEventListener: (_tipo: string, avisar: () => void) => avisosDePantalla.add(avisar),
      removeEventListener: (_tipo: string, avisar: () => void) => avisosDePantalla.delete(avisar),
    }),
  })
}

function cambiarPantalla(esEscritorio: boolean) {
  act(() => {
    pantallaEscritorio = esEscritorio
    ;[...avisosDePantalla].forEach((avisar) => avisar())
  })
}

function renderizar() {
  return render(
    <MemoryRouter>
      <CatalogoPlantasGrid />
    </MemoryRouter>,
  )
}

async function tarjetaDe(nombreComun: string) {
  return screen.findByRole('article', { name: nombreComun })
}

describe('CatalogoPlantasGrid', () => {
  beforeEach(() => simularPantalla(false))

  it('muestra una tarjeta por planta de la primera página', async () => {
    renderizar()

    await tarjetaDe('Achicoria')
    expect(screen.getAllByRole('article')).toHaveLength(12)
  })

  it('muestra los nombres, las categorías y la foto de cada planta', async () => {
    renderizar()

    const chilca = within(await tarjetaDe('Chilca'))
    expect(chilca.getByRole('heading', { name: 'Chilca' })).toBeInTheDocument()
    expect(chilca.getByText('Baccharis latifolia').tagName).toBe('EM')
    expect(chilca.getByText('Chillka')).toBeInTheDocument()
    expect(chilca.getByText('Antiinflamatoria')).toBeInTheDocument()
    expect(chilca.getByText('Energética')).toBeInTheDocument()
    expect(chilca.getByRole('img', { name: 'Fotografía de Chilca' })).toHaveAttribute(
      'src',
      '/api/publico/fotos/plantas/chilca.jpg',
    )
  })

  it('no muestra nombre kichwa cuando la planta no lo tiene', async () => {
    renderizar()

    const achicoria = within(await tarjetaDe('Achicoria'))
    expect(achicoria.queryByText(/kichwa/i)).not.toBeInTheDocument()
  })

  it('muestra la insignia térmica según la cualidad de la planta', async () => {
    renderizar()

    expect(within(await tarjetaDe('Chilca')).getByText('Caliente')).toHaveClass('insignia-error')
    expect(within(await tarjetaDe('Diente de león')).getByText('Fría')).toHaveClass('insignia-exito')
  })

  it('enlaza cada tarjeta con su ficha completa', async () => {
    renderizar()

    const chilca = within(await tarjetaDe('Chilca'))
    expect(chilca.getByRole('link', { name: 'Ver ficha completa' })).toHaveAttribute('href', '/catalogo-plantas/10')
  })

  it('muestra un marcador cuando la foto no carga', async () => {
    renderizar()

    const chilca = within(await tarjetaDe('Chilca'))
    fireEvent.error(chilca.getByRole('img', { name: 'Fotografía de Chilca' }))

    expect(chilca.queryByRole('img', { name: 'Fotografía de Chilca' })).not.toBeInTheDocument()
    expect(chilca.getByRole('img', { name: 'Sin fotografía de Chilca' })).toHaveTextContent('C')
    expect(chilca.getByRole('heading', { name: 'Chilca' })).toBeInTheDocument()
  })

  it('muestra un marcador cuando la planta no tiene foto', async () => {
    const sinFoto = [{ ...plantasMock[0], fotografiaUrl: null }]
    server.use(http.get('*/api/publico/plantas', () => HttpResponse.json(paginaDePlantas(sinFoto, 0, 12))))
    renderizar()

    const achicoria = within(await tarjetaDe('Achicoria'))
    expect(achicoria.getByRole('img', { name: 'Sin fotografía de Achicoria' })).toBeInTheDocument()
  })

  it('avisa cuando no hay plantas registradas', async () => {
    server.use(http.get('*/api/publico/plantas', () => HttpResponse.json(paginaDePlantas([], 0, 12))))
    renderizar()

    expect(
      await screen.findByText('Actualmente no hay plantas medicinales registradas en el catálogo oficial'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('article')).not.toBeInTheDocument()
  })

  it('muestra un error cuando el catálogo no se puede cargar', async () => {
    server.use(http.get('*/api/publico/plantas', () => new HttpResponse(null, { status: 500 })))
    renderizar()

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent(
      'No se pudo cargar el catálogo de plantas. Por favor, intente nuevamente más tarde',
    )
    expect(alerta).toHaveClass('notificacion-error')
  })

  describe('en móvil', () => {
    it('agrega la siguiente página con el botón Cargar más plantas', async () => {
      const { container } = renderizar()
      await tarjetaDe('Achicoria')

      expect(container.querySelector('.catalogo-plantas-movil')).toBeInTheDocument()
      expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument()
      const cargarMas = screen.getByRole('button', { name: 'Cargar más plantas' })
      expect(cargarMas).toHaveClass('boton-secundario')

      await userEvent.click(cargarMas)

      expect(await tarjetaDe('Violeta')).toBeInTheDocument()
      expect(screen.getByRole('article', { name: 'Achicoria' })).toBeInTheDocument()
      expect(screen.getAllByRole('article')).toHaveLength(20)
      expect(screen.queryByRole('button', { name: 'Cargar más plantas' })).not.toBeInTheDocument()
    })
  })

  describe('al cambiar de pantalla', () => {
    it('vuelve a la primera página al pasar de móvil a escritorio', async () => {
      renderizar()
      await tarjetaDe('Achicoria')
      await userEvent.click(screen.getByRole('button', { name: 'Cargar más plantas' }))
      await tarjetaDe('Violeta')

      cambiarPantalla(true)

      expect(await screen.findByText('Mostrando 12 de 20 especies')).toBeInTheDocument()
      expect(screen.getAllByRole('article')).toHaveLength(12)
      expect(screen.queryByRole('article', { name: 'Violeta' })).not.toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Página 1' })).toHaveAttribute('aria-current', 'page')
    })
  })

  describe('en escritorio', () => {
    beforeEach(() => simularPantalla(true))

    it('muestra el paginador y el contador en lugar de Cargar más', async () => {
      const { container } = renderizar()
      await tarjetaDe('Achicoria')

      expect(container.querySelector('.catalogo-plantas-escritorio')).toBeInTheDocument()
      expect(screen.getByRole('navigation', { name: 'Paginación' })).toBeInTheDocument()
      expect(screen.getByText('Mostrando 12 de 20 especies')).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Cargar más plantas' })).not.toBeInTheDocument()
    })

    it('reemplaza las plantas al cambiar de página', async () => {
      renderizar()
      await tarjetaDe('Achicoria')

      await userEvent.click(screen.getByRole('button', { name: 'Página 2' }))

      expect(await tarjetaDe('Ortiga')).toBeInTheDocument()
      expect(screen.queryByRole('article', { name: 'Achicoria' })).not.toBeInTheDocument()
      expect(screen.getAllByRole('article')).toHaveLength(8)
      expect(screen.getByText('Mostrando 8 de 20 especies')).toBeInTheDocument()
    })

    it('vuelve a consultar con el tamaño de página elegido', async () => {
      renderizar()
      await tarjetaDe('Achicoria')

      await userEvent.selectOptions(screen.getByLabelText('Por página'), '24')

      expect(await tarjetaDe('Violeta')).toBeInTheDocument()
      expect(screen.getAllByRole('article')).toHaveLength(20)
      expect(screen.getByText('Mostrando 20 de 20 especies')).toBeInTheDocument()
    })
  })
})
