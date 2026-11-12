import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../test/mocks/server'
import { CatalogoPlantasPage } from './CatalogoPlantasPage'

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

function registrarPeticiones() {
  const peticiones = { buscar: [] as URLSearchParams[], filtrar: [] as URLSearchParams[] }
  server.use(
    http.get('*/api/publico/plantas/buscar', ({ request }) => {
      peticiones.buscar.push(new URL(request.url).searchParams)
      return undefined
    }),
    http.get('*/api/publico/plantas/filtrar', ({ request }) => {
      peticiones.filtrar.push(new URL(request.url).searchParams)
      return undefined
    }),
  )
  return peticiones
}

function renderizar() {
  render(
    <MemoryRouter>
      <CatalogoPlantasPage />
    </MemoryRouter>,
  )
}

function opcion(grupo: string, nombre: string) {
  return within(screen.getByRole('group', { name: grupo })).getByRole('button', { name: nombre })
}

describe('CatalogoPlantasPage', () => {
  beforeEach(() => simularPantalla(true))

  it('sin búsqueda ni filtros muestra el catálogo paginado junto a la barra de filtros', async () => {
    const peticiones = registrarPeticiones()
    renderizar()

    expect(await screen.findByText('Mostrando 12 de 20 especies')).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(12)
    expect(screen.getByRole('complementary', { name: 'Filtros' })).toBeInTheDocument()
    expect(peticiones.buscar).toHaveLength(0)
    expect(peticiones.filtrar).toHaveLength(0)
  })

  it('con solo una búsqueda sigue usando la búsqueda por nombre', async () => {
    const peticiones = registrarPeticiones()
    renderizar()

    await userEvent.type(screen.getByRole('searchbox', { name: 'Buscar planta' }), 'chupa')

    expect(await screen.findByText('1 resultado para «chupa»')).toBeInTheDocument()
    expect(peticiones.buscar.at(-1)?.get('criterio')).toBe('chupa')
    expect(peticiones.filtrar).toHaveLength(0)
  })

  it('con un filtro activo muestra los resultados del filtrado', async () => {
    const peticiones = registrarPeticiones()
    renderizar()

    await userEvent.click(opcion('Cualidad térmica', 'Fría'))

    expect(await screen.findByText('6 plantas encontradas')).toBeInTheDocument()
    expect(peticiones.filtrar.at(-1)?.get('cualidad')).toBe('FRIA')
    expect(peticiones.filtrar.at(-1)?.has('criterio')).toBe(false)
    expect(screen.queryByText(/Mostrando \d+ de \d+ especies/)).not.toBeInTheDocument()
  })

  it('combina el filtro con el criterio del buscador', async () => {
    const peticiones = registrarPeticiones()
    renderizar()
    await userEvent.type(screen.getByRole('searchbox', { name: 'Buscar planta' }), 'ma')
    await screen.findByText('5 resultados para «ma»')

    await userEvent.click(opcion('Cualidad térmica', 'Fría'))

    expect(await screen.findByText('1 planta encontrada')).toBeInTheDocument()
    expect(screen.getByRole('article', { name: 'Llantén' })).toBeInTheDocument()
    expect(peticiones.filtrar.at(-1)?.get('cualidad')).toBe('FRIA')
    expect(peticiones.filtrar.at(-1)?.get('criterio')).toBe('ma')
  })

  it('Limpiar filtros vuelve al catálogo paginado', async () => {
    renderizar()
    await userEvent.click(opcion('Región', 'Amazonía'))
    await userEvent.click(opcion('Ciclo de cultivo', 'Corto'))
    await screen.findByText('No se encontraron plantas para estos parámetros. Intente reajustar los filtros')

    await userEvent.click(screen.getByRole('button', { name: 'Limpiar filtros' }))

    expect(await screen.findByText('Mostrando 12 de 20 especies')).toBeInTheDocument()
    expect(opcion('Región', 'Todas')).toHaveAttribute('aria-pressed', 'true')
  })

  it('en móvil aplica los filtros desde el panel inferior', async () => {
    simularPantalla(false)
    const peticiones = registrarPeticiones()
    renderizar()

    await userEvent.click(screen.getByRole('button', { name: 'Filtros' }))
    await userEvent.click(opcion('Cualidad térmica', 'Fría'))
    expect(peticiones.filtrar).toHaveLength(0)
    await userEvent.click(screen.getByRole('button', { name: 'Aplicar filtros' }))

    expect(await screen.findByText('6 plantas encontradas')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Filtros (1 activo)' })).toBeInTheDocument()
  })
})
