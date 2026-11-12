import { render, screen } from '@testing-library/react'
import { VisorFaseLunar } from './VisorFaseLunar'
import type { CalendarioLunarResponse } from './calendarioLunarApi'

const FASE: CalendarioLunarResponse = {
  id: 1,
  faseLunar: 'PURA_KILLA_LLENA',
  nombreKichwa: 'Pura Killa',
  nombreEspanol: 'Luna Llena',
  fechaInicio: '2026-11-01',
  fechaFin: '2026-11-07',
  practicasRecomendadas: 'Cosecha de raíces y hojas medicinales.',
  consejoKinti: 'En la luna llena la planta guarda su medicina.',
}

describe('VisorFaseLunar', () => {
  it('muestra el nombre de la fase en kichwa y en español', () => {
    render(<VisorFaseLunar fase={FASE} vigente />)

    expect(screen.getByRole('heading', { name: 'Pura Killa' })).toBeInTheDocument()
    expect(screen.getByText('Luna Llena')).toBeInTheDocument()
  })

  it('muestra el rango de fechas de vigencia de la fase', () => {
    render(<VisorFaseLunar fase={FASE} vigente />)

    expect(screen.getByText(/1 de noviembre de 2026/)).toBeInTheDocument()
    expect(screen.getByText(/7 de noviembre de 2026/)).toBeInTheDocument()
  })

  it('distingue la fase vigente de la consultada por fecha', () => {
    const { rerender } = render(<VisorFaseLunar fase={FASE} vigente />)

    expect(screen.getByText('Fase vigente')).toBeInTheDocument()

    rerender(<VisorFaseLunar fase={FASE} vigente={false} />)

    expect(screen.getByText('Fecha consultada')).toBeInTheDocument()
  })
})
