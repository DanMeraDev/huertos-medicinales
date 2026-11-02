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

  it('vuelve al inicio desde la página no encontrada', async () => {
    renderizarEn('/ruta-inexistente')

    await userEvent.click(screen.getByRole('link', { name: 'Volver al inicio' }))

    expect(screen.getByRole('heading', { name: 'Huertos Medicinales Comunitarios' })).toBeInTheDocument()
    expect(await screen.findByText('11.4.8-MariaDB')).toBeInTheDocument()
  })
})
