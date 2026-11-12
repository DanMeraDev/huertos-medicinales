import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { GestionSubhuertosView } from './GestionSubhuertosView'
import type { Subhuerto } from './subhuertosApi'

const subhuertosMock: Subhuerto[] = [
  {
    id: 1,
    huertoId: 10,
    nombreZona: 'Surcos Norte',
    subtipo: 'SURCOS_WACHU',
    areaM2: 30,
    activo: true,
  },
  {
    id: 2,
    huertoId: 10,
    nombreZona: 'Terrazas Medicinales',
    subtipo: 'TERRAZAS_ANTI',
    areaM2: 20,
    activo: true,
  },
]

describe('GestionSubhuertosView', () => {
  it('renderiza dashboard de capacidad, tarjetas y cuadrícula de 3 columnas en escritorio', async () => {
    const alCrear = vi.fn().mockResolvedValue({
      id: 3,
      huertoId: 10,
      nombreZona: 'Macetas Sombra',
      subtipo: 'MACETAS_PISO',
      areaM2: 15,
      activo: true,
    })

    render(
      <GestionSubhuertosView
        areaTotalHuertoM2={100}
        subhuertosIniciales={subhuertosMock}
        alCrear={alCrear}
      />
    )

    expect(screen.getByRole('heading', { name: /Zonificación y Subhuertos/i })).toBeInTheDocument()
    expect(screen.getByText(/50\.00 m² ocupados de 100\.00 m²/i)).toBeInTheDocument()
    expect(screen.getByText(/2 \/ 3 subtipos MSP en uso/i)).toBeInTheDocument()
    expect(screen.getAllByText('Surcos Norte').length).toBeGreaterThanOrEqual(1)

    fireEvent.click(screen.getByRole('button', { name: /\+ Nuevo Subhuerto/i }))

    const dialogo = screen.getByRole('dialog')
    fireEvent.change(within(dialogo).getByLabelText(/Nombre de la zona/i), {
      target: { value: 'Macetas Sombra' },
    })
    fireEvent.click(within(dialogo).getByRole('button', { name: /Macetas en Piso/i }))
    fireEvent.change(within(dialogo).getByLabelText(/Área de la zona/i), {
      target: { value: '15' },
    })
    fireEvent.click(within(dialogo).getByRole('button', { name: /Crear Subhuerto/i }))

    await waitFor(() => {
      expect(alCrear).toHaveBeenCalledWith({
        nombreZona: 'Macetas Sombra',
        subtipo: 'MACETAS_PISO',
        areaM2: 15,
      })
    })
  })

  it('bloquea subtipos nuevos con candado y aviso cuando ya hay 3 subtipos en uso', () => {
    const tresSubtipos: Subhuerto[] = [
      ...subhuertosMock,
      {
        id: 3,
        huertoId: 10,
        nombreZona: 'Macetas Patio',
        subtipo: 'MACETAS_PISO',
        areaM2: 10,
        activo: true,
      },
    ]

    render(
      <GestionSubhuertosView
        areaTotalHuertoM2={100}
        subhuertosIniciales={tresSubtipos}
        alCrear={vi.fn()}
      />
    )

    fireEvent.click(screen.getByRole('button', { name: /\+ Nuevo Subhuerto/i }))
    const dialogo = screen.getByRole('dialog')

    expect(
      within(dialogo).getByText(/Máximo 3 subtipos por huerto según norma MSP/i)
    ).toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: /Vertical en Pared/i })).toBeDisabled()
    expect(within(dialogo).getByRole('button', { name: /Surcos \(Wachu\)/i })).not.toBeDisabled()
  })

  it('muestra error de espacio disponible si el área ingresada excede los m² libres', async () => {
    const alCrear = vi.fn()

    render(
      <GestionSubhuertosView
        areaTotalHuertoM2={100}
        subhuertosIniciales={subhuertosMock}
        alCrear={alCrear}
      />
    )

    fireEvent.click(screen.getByRole('button', { name: /\+ Nuevo Subhuerto/i }))
    const dialogo = screen.getByRole('dialog')

    fireEvent.change(within(dialogo).getByLabelText(/Nombre de la zona/i), {
      target: { value: 'Zona Grande' },
    })
    fireEvent.change(within(dialogo).getByLabelText(/Área de la zona/i), {
      target: { value: '60' },
    })
    fireEvent.click(within(dialogo).getByRole('button', { name: /Crear Subhuerto/i }))

    expect(
      within(dialogo).getByText(/El área ingresada supera el espacio disponible del huerto/i)
    ).toBeInTheDocument()
    expect(alCrear).not.toHaveBeenCalled()
  })
})