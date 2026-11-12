import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { RutaSecuencialSaberes } from './RutaSecuencialSaberes'

describe('RutaSecuencialSaberes', () => {
  it('renderiza la ruta secuencial con los pasos del back en orden', async () => {
    render(<RutaSecuencialSaberes />)

    const ruta = await screen.findByRole('list', {
      name: 'Ruta secuencial de saberes ancestrales',
    })
    const pasos = within(ruta).getAllByRole('listitem')
    expect(pasos).toHaveLength(3)
    expect(within(pasos[0]).getByRole('heading', { name: 'Preparación de la tierra' })).toBeInTheDocument()
    expect(within(pasos[0]).getByText('Allpa wiñay')).toBeInTheDocument()
    expect(within(pasos[1]).getByRole('heading', { name: 'Abono natural del huerto' })).toBeInTheDocument()
    expect(within(pasos[2]).getByRole('heading', { name: 'Ritual de siembra' })).toBeInTheDocument()
    expect(screen.getByText('🌑 Yana Killa · Luna oscura')).toBeInTheDocument()
    expect(screen.getByText('🌕 Pura Killa · Luna llena')).toBeInTheDocument()
    expect(screen.getByText('Guardián de la tierra')).toBeInTheDocument()
  })

  it('abre la ficha con el contenido pedagógico del paso elegido', async () => {
    render(<RutaSecuencialSaberes />)

    const ruta = await screen.findByRole('list', {
      name: 'Ruta secuencial de saberes ancestrales',
    })
    await userEvent.click(
      within(within(ruta).getAllByRole('listitem')[0]).getByRole('button', {
        name: 'Ver contenido del paso',
      }),
    )

    const ficha = await screen.findByRole('dialog', { name: 'Preparación de la tierra' })
    expect(
      within(ficha).getByText('05:00 a 07:00 AM - Salida del sol'),
    ).toBeInTheDocument()
    expect(
      within(ficha).getByText(
        'Se barre y se limpia el terreno con la chaquetaclla mientras la tierra descansa en la luna oscura.',
      ),
    ).toBeInTheDocument()
    expect(
      within(ficha).getByText('Chaquetaclla, chakana, compost de la comunidad'),
    ).toBeInTheDocument()
    expect(
      within(ficha).getByText('Otorga 20 puntos base a la comunidad.'),
    ).toBeInTheDocument()
  })

  it('avisa cuando el saber ancestral del paso no fue encontrado', async () => {
    server.use(
      http.get('*/api/publico/saberes-ancestrales/:id', () =>
        HttpResponse.json(
          { detail: 'El saber ancestral solicitado no fue encontrado' },
          { status: 404 },
        ),
      ),
    )
    render(<RutaSecuencialSaberes />)

    const botonesPaso = await screen.findAllByRole('button', {
      name: 'Ver contenido del paso',
    })
    await userEvent.click(botonesPaso[0])

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El saber ancestral solicitado no fue encontrado',
    )
  })

  it('avisa cuando el back no responde', async () => {
    server.use(
      http.get('*/api/publico/saberes-ancestrales', () =>
        HttpResponse.error(),
      ),
    )
    render(<RutaSecuencialSaberes />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo consultar la biblioteca de saberes ancestrales.',
    )
  })
})
