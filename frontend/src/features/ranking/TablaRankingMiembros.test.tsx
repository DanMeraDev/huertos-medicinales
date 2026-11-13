import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { TablaRankingMiembros } from './TablaRankingMiembros'
import type { RankingMiembro } from './types'

const VOLUNTARIOS_PRUEBA: RankingMiembro[] = [
  {
    posicion: 1,
    miembroId: 101,
    nombres: 'Carmen',
    apellidos: 'Chuquimarca',
    nivel: 'MAESTRO',
    puntosTotales: 340,
    totalInsignias: 5,
  },
  {
    posicion: 2,
    miembroId: 102,
    nombres: 'Luis',
    apellidos: 'Guamán',
    nivel: 'OFICIAL',
    puntosTotales: 150,
    totalInsignias: 3,
  },
  {
    posicion: 3,
    miembroId: 103,
    nombres: 'Ana',
    apellidos: 'Tipantuña',
    nivel: 'APRENDIZ',
    puntosTotales: 40,
    totalInsignias: 1,
  },
]

describe('TablaRankingMiembros', () => {
  it('renderiza la tabla con los encabezados y los datos de los voluntarios', () => {
    render(<TablaRankingMiembros voluntarios={VOLUNTARIOS_PRUEBA} />)

    expect(screen.getByRole('columnheader', { name: 'Posición' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Nombre del Voluntario' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Nivel' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Puntos Totales' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Medallas / Insignias' })).toBeInTheDocument()

    expect(screen.getByText('Carmen Chuquimarca')).toBeInTheDocument()
    expect(screen.getByText('Luis Guamán')).toBeInTheDocument()
    expect(screen.getByText('Ana Tipantuña')).toBeInTheDocument()

    expect(screen.getByText('340')).toBeInTheDocument()
    expect(screen.getByText('150')).toBeInTheDocument()
    expect(screen.getByText('40')).toBeInTheDocument()
  })

  it('filtra dinámicamente por nombre o apellido ingresado', async () => {
    const usuario = userEvent.setup()
    render(<TablaRankingMiembros voluntarios={VOLUNTARIOS_PRUEBA} />)

    const campoBusqueda = screen.getByLabelText(/buscar por nombre o apellido/i)
    await usuario.type(campoBusqueda, 'Guamán')

    expect(screen.getByText('Luis Guamán')).toBeInTheDocument()
    expect(screen.queryByText('Carmen Chuquimarca')).not.toBeInTheDocument()
    expect(screen.queryByText('Ana Tipantuña')).not.toBeInTheDocument()
  })

  it('filtra dinámicamente por nivel seleccionado', async () => {
    const usuario = userEvent.setup()
    render(<TablaRankingMiembros voluntarios={VOLUNTARIOS_PRUEBA} />)

    const selectorNivel = screen.getByLabelText(/filtrar por nivel/i)
    await usuario.selectOptions(selectorNivel, 'MAESTRO')

    expect(screen.getByText('Carmen Chuquimarca')).toBeInTheDocument()
    expect(screen.queryByText('Luis Guamán')).not.toBeInTheDocument()
    expect(screen.queryByText('Ana Tipantuña')).not.toBeInTheDocument()
  })

  it('muestra mensaje informativo de estado vacío cuando no hay voluntarios', () => {
    render(<TablaRankingMiembros voluntarios={[]} />)

    expect(
      screen.getByText('No se registran voluntarios participantes en este momento'),
    ).toBeInTheDocument()
  })
})

