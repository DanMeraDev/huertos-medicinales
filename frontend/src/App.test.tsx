import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import App from './App'
import { plantaEditableHandler } from './features/catalogo-admin/catalogoAdminMockHandlers'
import { server } from './test/mocks/server'

function renderizarEn(ruta: string) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <App />
    </MemoryRouter>,
  )
}

describe('App', () => {
  it('muestra la página de inicio en /', () => {
    renderizarEn('/')

    expect(screen.getByRole('heading', { level: 1, name: 'Hampi Muyu' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solicitar creación de comunidad' })).toHaveAttribute(
      'href',
      '/solicitar-comunidad',
    )
  })

  it('muestra la página no encontrada en una ruta inexistente', () => {
    renderizarEn('/ruta-inexistente')

    expect(screen.getByRole('heading', { name: 'Página no encontrada' })).toBeInTheDocument()
  })

  it('muestra la página de mi comunidad en /mi-comunidad', async () => {
    renderizarEn('/mi-comunidad')

    expect(await screen.findByRole('heading', { name: 'Mi Comunidad' })).toBeInTheDocument()
  })

  it('muestra la página de directorio de comunidades en /comunidades', async () => {
    renderizarEn('/comunidades')

    expect(await screen.findByRole('heading', { name: 'Directorio de Comunidades' })).toBeInTheDocument()
  })

  it('muestra la página de registro de voluntarios en /comunidad/miembros/nuevo', async () => {
    renderizarEn('/comunidad/miembros/nuevo')

    expect(await screen.findByRole('heading', { name: 'Registrar Voluntario' })).toBeInTheDocument()
  })

  it('muestra la página de gestión de miembros en /comunidad/miembros', async () => {
    renderizarEn('/comunidad/miembros')

    expect(await screen.findByRole('heading', { name: 'Miembros de la Comunidad' })).toBeInTheDocument()
  })

  it('muestra la página de inventario de bodega en /bodega/inventario', async () => {
    renderizarEn('/bodega/inventario')

    expect(await screen.findByRole('heading', { name: 'Inventario de Bodega' })).toBeInTheDocument()
  })

  it('muestra la página de consulta pública de voluntario en /consulta-miembro', () => {
    renderizarEn('/consulta-miembro')

    expect(screen.getByRole('heading', { name: 'Consultar Voluntario' })).toBeInTheDocument()
  })

  it('vuelve al inicio desde la página no encontrada', async () => {
    renderizarEn('/ruta-inexistente')

    await userEvent.click(screen.getByRole('link', { name: 'Volver al inicio' }))

    expect(screen.getByRole('heading', { level: 1, name: 'Hampi Muyu' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solicitar creación de comunidad' })).toBeInTheDocument()
  })

  it('muestra el registro de plantas en /admin/plantas/nueva', () => {
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      writable: true,
      value: (consulta: string) => ({
        matches: false,
        media: consulta,
        addEventListener: () => {},
        removeEventListener: () => {},
      }),
    })
    renderizarEn('/admin/plantas/nueva')

    expect(screen.getByRole('heading', { level: 1, name: 'Registrar planta' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Paso 1 de 3' })).toBeInTheDocument()
  })

  it('muestra la edición de la ficha en /admin/plantas/:id/editar', async () => {
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      writable: true,
      value: (consulta: string) => ({
        matches: false,
        media: consulta,
        addEventListener: () => {},
        removeEventListener: () => {},
      }),
    })
    server.use(plantaEditableHandler)
    renderizarEn('/admin/plantas/7/editar')

    expect(screen.getByRole('heading', { level: 1, name: 'Editar ficha técnica' })).toBeInTheDocument()
    expect(await screen.findByLabelText('Nombre común')).toHaveValue('Hierba editable')
  })
})
