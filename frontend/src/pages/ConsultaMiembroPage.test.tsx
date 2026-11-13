import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'
import { ConsultaMiembroPage } from './ConsultaMiembroPage'

describe('ConsultaMiembroPage', () => {
  it('renderiza la cabecera, el buscador por cédula y la tarjeta inferior de llamado a la acción', () => {
    render(
      <MemoryRouter initialEntries={['/consulta-miembro']}>
        <ConsultaMiembroPage />
      </MemoryRouter>,
    )

    expect(
      screen.getByRole('heading', { name: /consultar voluntario/i }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText(/cédula de identidad/i)).toBeInTheDocument()
    expect(
      screen.getByText(/¿Deseas ser parte de este listado de guardianes o quieres fundar tu propio huerto comunitario\?/i),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /crear mi comunidad/i })).toBeInTheDocument()
  })

  it('navega a /solicitar-comunidad al pulsar Crear mi comunidad', async () => {
    const usuario = userEvent.setup()
    render(
      <MemoryRouter initialEntries={['/consulta-miembro']}>
        <Routes>
          <Route path="/consulta-miembro" element={<ConsultaMiembroPage />} />
          <Route path="/solicitar-comunidad" element={<h1>Solicitud de Comunidad</h1>} />
        </Routes>
      </MemoryRouter>,
    )

    await usuario.click(screen.getByRole('button', { name: /crear mi comunidad/i }))

    expect(await screen.findByRole('heading', { name: 'Solicitud de Comunidad' })).toBeInTheDocument()
  })
})
