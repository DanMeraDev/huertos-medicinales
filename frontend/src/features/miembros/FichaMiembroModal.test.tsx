import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { FichaMiembroModal } from './FichaMiembroModal'
import { mockFichaMiembro } from './miembrosMockHandlers'

describe('FichaMiembroModal', () => {
  it('renderiza la ficha completa con avatar andino y campos en modo solo lectura', () => {
    const alCerrar = vi.fn()
    const { container } = render(
      <FichaMiembroModal
        abierto={true}
        ficha={mockFichaMiembro}
        alCerrar={alCerrar}
      />,
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()
    expect(screen.getByTestId('avatar-andino')).toBeInTheDocument()
    expect(screen.getByText('APRENDIZ')).toBeInTheDocument()
    expect(screen.getByText('1710034065')).toBeInTheDocument()
    expect(screen.getByText('FEMENINO')).toBeInTheDocument()
    expect(screen.getByText('0981234567')).toBeInTheDocument()
    expect(screen.getByText(/25\/03\/2010/)).toBeInTheDocument()
    expect(screen.getByText(/16 años/)).toBeInTheDocument()
    expect(screen.getByText(/10\/11\/2026/)).toBeInTheDocument()
    expect(screen.getByText('Activo')).toBeInTheDocument()
    expect(screen.getByText(/45 puntos/)).toBeInTheDocument()

    const camposEditables = container.querySelectorAll('input, select, textarea')
    expect(camposEditables.length).toBe(0)
  })

  it('muestra el mensaje motivacional correspondiente de Kinti según el nivel', () => {
    const { rerender } = render(
      <FichaMiembroModal
        abierto={true}
        ficha={{ ...mockFichaMiembro, nivel: 'APRENDIZ' }}
        alCerrar={vi.fn()}
      />,
    )

    expect(
      screen.getByText(/¡Recién inicia su siembra en el camino de las plantas medicinales!/i),
    ).toBeInTheDocument()

    rerender(
      <FichaMiembroModal
        abierto={true}
        ficha={{ ...mockFichaMiembro, nivel: 'OFICIAL' }}
        alCerrar={vi.fn()}
      />,
    )

    expect(
      screen.getByText(/¡Ha demostrado constancia en las labores del huerto!/i),
    ).toBeInTheDocument()

    rerender(
      <FichaMiembroModal
        abierto={true}
        ficha={{ ...mockFichaMiembro, nivel: 'MAESTRO' }}
        alCerrar={vi.fn()}
      />,
    )

    expect(
      screen.getByText(/¡Un verdadero guardián de los saberes ancestrales!/i),
    ).toBeInTheDocument()
  })

  it('muestra estado suspendido/inactivo con fecha y motivo de baja para miembros inactivos', () => {
    render(
      <FichaMiembroModal
        abierto={true}
        ficha={{
          ...mockFichaMiembro,
          activoEnComunidad: false,
          fechaBaja: '2026-11-12',
          motivoBaja: 'Cambio de residencia a otra parroquia',
        }}
        alCerrar={vi.fn()}
      />,
    )

    expect(screen.getByText('Suspendido / Inactivo')).toBeInTheDocument()
    expect(screen.getByText(/12\/11\/2026/)).toBeInTheDocument()
    expect(screen.getByText('Cambio de residencia a otra parroquia')).toBeInTheDocument()
  })

  it('llama a alCerrar al pulsar el botón Cerrar', async () => {
    const usuario = userEvent.setup()
    const alCerrar = vi.fn()

    render(
      <FichaMiembroModal
        abierto={true}
        ficha={mockFichaMiembro}
        alCerrar={alCerrar}
      />,
    )

    const botonCerrar = screen.getByRole('button', { name: /cerrar/i })
    await usuario.click(botonCerrar)

    expect(alCerrar).toHaveBeenCalledTimes(1)
  })

  it('consulta la ficha del miembro desde el servidor cuando se proporciona miembroId', async () => {
    render(
      <FichaMiembroModal
        abierto={true}
        miembroId={15}
        alCerrar={vi.fn()}
      />,
    )

    await waitFor(() => {
      expect(screen.getByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()
    })
    expect(screen.getByText('1710034065')).toBeInTheDocument()
  })
})
