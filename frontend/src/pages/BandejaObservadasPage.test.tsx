import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { BandejaObservadasPage } from './BandejaObservadasPage'

function renderizarPagina() {
  return render(
    <MemoryRouter initialEntries={['/actividades-ancestrales/observadas']}>
      <Routes>
        <Route path="/actividades-ancestrales/observadas" element={<BandejaObservadasPage />} />
        <Route
          path="/actividades-ancestrales/:id/subsanar"
          element={<p>Formulario de subsanación</p>}
        />
      </Routes>
    </MemoryRouter>,
  )
}

describe('BandejaObservadasPage', () => {
  it('lista las actividades observadas con la observación del Técnico MSP', async () => {
    renderizarPagina()

    expect(await screen.findByText('Ritual de siembra')).toBeInTheDocument()
    expect(screen.getByText('La fotografía no muestra el ritual completo')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Subsanar' })).toBeInTheDocument()
  })

  it('al pulsar Subsanar entra al formulario de la actividad seleccionada', async () => {
    renderizarPagina()

    await userEvent.click(await screen.findByRole('button', { name: 'Subsanar' }))

    expect(await screen.findByText('Formulario de subsanación')).toBeInTheDocument()
  })
})
