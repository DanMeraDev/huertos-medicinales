import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import App from './App'

function renderizarEn(ruta: string) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <App />
    </MemoryRouter>,
  )
}

describe('App', () => {
  it('muestra la página de inicio en /', async () => {
    renderizarEn('/')

    expect(screen.getByRole('heading', { name: 'Huertos Medicinales Comunitarios' })).toBeInTheDocument()
    expect(await screen.findByText('11.4.8-MariaDB')).toBeInTheDocument()
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

  it('vuelve al inicio desde la página no encontrada', async () => {
    renderizarEn('/ruta-inexistente')

    await userEvent.click(screen.getByRole('link', { name: 'Volver al inicio' }))

    expect(screen.getByRole('heading', { name: 'Huertos Medicinales Comunitarios' })).toBeInTheDocument()
    expect(await screen.findByText('11.4.8-MariaDB')).toBeInTheDocument()
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
})
