import { act, render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { plantaEditableHandler } from '../features/catalogo-admin/catalogoAdminMockHandlers'
import { server } from '../test/mocks/server'
import { AdminEditarPlantaPage } from './AdminEditarPlantaPage'

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
      <Routes>
        <Route path="/admin/plantas/:id/editar" element={<AdminEditarPlantaPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

function contarPeticionesDeFicha() {
  const contador = { peticiones: 0 }
  server.use(
    http.get('*/api/publico/plantas/:id', () => {
      contador.peticiones += 1
      return undefined
    }),
  )
  return contador
}

describe('AdminEditarPlantaPage', () => {
  beforeEach(() => {
    simularPantalla(true)
    server.use(plantaEditableHandler)
  })

  it('avisa mientras carga la ficha', () => {
    renderizarEn('/admin/plantas/7/editar')

    expect(screen.getByRole('heading', { level: 1, name: 'Editar ficha técnica' })).toBeInTheDocument()
    expect(screen.getByText('Cargando ficha…')).toBeInTheDocument()
  })

  it('precarga el formulario con la ficha de la planta', async () => {
    renderizarEn('/admin/plantas/7/editar')

    expect(await screen.findByLabelText('Nombre común')).toHaveValue('Hierba editable')
    expect(screen.getByLabelText('Nombre científico')).toHaveValue('Herba editabilis')
    expect(screen.queryByText('Cargando ficha…')).not.toBeInTheDocument()
  })

  it('si la planta no existe avisa y ofrece volver al catálogo', async () => {
    renderizarEn('/admin/plantas/999/editar')

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('La planta no existe o fue dada de baja')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
    expect(screen.queryByLabelText('Nombre común')).not.toBeInTheDocument()
  })

  it('con un error del servidor muestra un mensaje genérico', async () => {
    server.use(http.get('*/api/publico/plantas/:id', () => new HttpResponse(null, { status: 500 })))
    renderizarEn('/admin/plantas/7/editar')

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo cargar la ficha. Intente nuevamente más tarde')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
  })

  it.each(['abc', '0', '-3', '1.5'])('con el id "%s" avisa que no está disponible sin consultar', async (id) => {
    const contador = contarPeticionesDeFicha()
    renderizarEn(`/admin/plantas/${id}/editar`)

    expect(await screen.findByRole('alert')).toHaveTextContent('La planta no existe o fue dada de baja')
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/catalogo-plantas')
    await act(async () => {
      await new Promise((resolver) => setTimeout(resolver, 50))
    })
    expect(contador.peticiones).toBe(0)
  })
})
