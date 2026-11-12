import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { ConsultarCalendarioLunar } from './ConsultarCalendarioLunar'

describe('ConsultarCalendarioLunar', () => {
  it('muestra la fase vigente cuando el back responde', async () => {
    render(<ConsultarCalendarioLunar />)

    expect(await screen.findByRole('heading', { name: 'Pura Killa' })).toBeInTheDocument()
    expect(screen.getByText('Fase vigente')).toBeInTheDocument()
    expect(screen.getByText('Cosecha de raíces y hojas medicinales.')).toBeInTheDocument()
  })

  it('consulta la fase de la fecha elegida', async () => {
    let fechaConsultada: string | null = null
    server.use(
      http.get('*/api/publico/calendario-lunar', ({ request }) => {
        fechaConsultada = new URL(request.url).searchParams.get('fecha')
        return HttpResponse.json({
          id: 2,
          faseLunar: 'WANUK_KILLA_MENGUANTE',
          nombreKichwa: 'Wañuk Killa',
          nombreEspanol: 'Luna Menguante',
          fechaInicio: '2026-12-01',
          fechaFin: '2026-12-08',
          practicasRecomendadas: 'La savia desciende a la raíz.',
          consejoKinti: 'En la luna menguante todo baja a la raíz.',
        })
      }),
    )
    render(<ConsultarCalendarioLunar />)

    await userEvent.type(await screen.findByLabelText('Consultar otra fecha'), '2026-12-05')

    expect(await screen.findByRole('heading', { name: 'Wañuk Killa' })).toBeInTheDocument()
    expect(fechaConsultada).toBe('2026-12-05')
  })

  it('avisa cuando no hay datos lunares para la fecha consultada', async () => {
    server.use(
      http.get('*/api/publico/calendario-lunar', () =>
        HttpResponse.json(
          { detail: 'No hay datos lunares disponibles para la fecha seleccionada' },
          { status: 404 },
        ),
      ),
    )
    render(<ConsultarCalendarioLunar />)

    await userEvent.type(await screen.findByLabelText('Consultar otra fecha'), '2027-06-01')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No hay datos lunares disponibles para la fecha seleccionada',
    )
  })
})
