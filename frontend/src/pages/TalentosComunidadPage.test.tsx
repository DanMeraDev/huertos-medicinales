import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import { TalentosComunidadPage } from './TalentosComunidadPage'

function renderizarPagina() {
  return render(
    <MemoryRouter>
      <TalentosComunidadPage />
    </MemoryRouter>,
  )
}

describe('TalentosComunidadPage', () => {
  it('carga y renderiza la cabecera, el formulario de creación y la galería de talentos', async () => {
    renderizarPagina()

    expect(
      screen.getByRole('heading', { name: /insignias y talentos de la comunidad/i }),
    ).toBeInTheDocument()

    expect(await screen.findByText('Maestro del Abonaje 1')).toBeInTheDocument()
    expect(screen.getByText(/50 rep/i)).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /definir nueva medalla o talento/i })).toBeInTheDocument()
  })

  it('permite registrar una nueva medalla y la agrega dinámicamente a la galería', async () => {
    const usuario = userEvent.setup()
    renderizarPagina()

    expect(await screen.findByText('Maestro del Abonaje 1')).toBeInTheDocument()

    await usuario.selectOptions(screen.getByLabelText(/actividad asociada/i), '1')
    await usuario.type(screen.getByLabelText(/nombre de la insignia o talento/i), 'Guardián del Riego 2')
    await usuario.type(screen.getByLabelText(/meta de repeticiones/i), '25')
    await usuario.type(screen.getByLabelText(/descripción del reconocimiento/i), 'Completó 25 riegos')

    await usuario.click(screen.getByRole('button', { name: /registrar medalla de comunidad/i }))

    await waitFor(() => {
      expect(
        screen.getByText(
          'Medalla registrada con éxito. ¡Tus miembros ya pueden comenzar a acumular repeticiones para obtenerla!',
        ),
      ).toBeInTheDocument()
    })

    expect(await screen.findByText('Guardián del Riego 2')).toBeInTheDocument()
    expect(screen.getByText(/25 rep/i)).toBeInTheDocument()
  })
})
