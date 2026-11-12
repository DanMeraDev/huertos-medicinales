import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FormularioActividadAncestral } from './FormularioActividadAncestral'

describe('FormularioActividadAncestral', () => {
  it('renderiza todos los campos del formulario y el botón de envío', async () => {
    render(<FormularioActividadAncestral />)

    expect(await screen.findByLabelText(/hito \/ saber ancestral/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de realización/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fase lunar de ejecución/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/descripción de la bitácora/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/evidencia fotográfica/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/evidencia de video/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/miembro voluntario/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/horas trabajadas/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /agregar participante/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /enviar a revisión msp/i })).toBeInTheDocument()
  })

  it('valida los campos obligatorios y la nómina de participantes', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByText(/el saber ancestral es obligatorio/i)).toBeInTheDocument()
    expect(screen.getByText(/la fecha de realización es obligatoria/i)).toBeInTheDocument()
    expect(screen.getByText(/la fase lunar de ejecución es obligatoria/i)).toBeInTheDocument()
    expect(screen.getByText(/la descripción de la bitácora es obligatoria/i)).toBeInTheDocument()
    expect(
      screen.getByText(/la fotografía de evidencia es obligatoria para la revisión del msp/i),
    ).toBeInTheDocument()
    expect(
      screen.getByText(/debe registrar al menos un miembro participante en la actividad ancestral/i),
    ).toBeInTheDocument()
  })

  it('agrega participantes a la nómina y envía la petición al back', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await screen.findByText(/ritual de siembra/i)
    await screen.findByText(/maría tránsito amaguaña alba/i)

    await user.selectOptions(screen.getByLabelText(/hito \/ saber ancestral/i), '3')
    await user.type(screen.getByLabelText(/fecha de realización/i), '2026-11-10')
    await user.selectOptions(screen.getByLabelText(/fase lunar de ejecución/i), 'PURA_KILLA_LLENA')
    await user.type(screen.getByLabelText(/descripción de la bitácora/i), 'Ritual de siembra con chicha de jora')
    await user.type(screen.getByLabelText(/evidencia fotográfica/i), 'https://evidencias.gob.ec/foto.jpg')

    await user.selectOptions(screen.getByLabelText(/miembro voluntario/i), '7')
    await user.type(screen.getByLabelText(/horas trabajadas/i), '4')
    await user.click(screen.getByRole('button', { name: /agregar participante/i }))

    expect(await screen.findByRole('cell', { name: /maría tránsito amaguaña alba/i })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: '4' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      /¡actividad ancestral registrada! quedó en revisión del técnico msp/i,
    )
    expect(screen.getByLabelText(/fecha de realización/i)).toHaveValue('')
  })

  it('muestra el error del back cuando la evidencia fotográfica es rechazada', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await screen.findByText(/ritual de siembra/i)
    await screen.findByText(/maría tránsito amaguaña alba/i)

    await user.selectOptions(screen.getByLabelText(/hito \/ saber ancestral/i), '3')
    await user.type(screen.getByLabelText(/fecha de realización/i), '2026-11-10')
    await user.selectOptions(screen.getByLabelText(/fase lunar de ejecución/i), 'PURA_KILLA_LLENA')
    await user.type(screen.getByLabelText(/descripción de la bitácora/i), 'Ritual de siembra con chicha de jora')
    await user.type(screen.getByLabelText(/evidencia fotográfica/i), 'https://error.gob.ec/foto.jpg')

    await user.selectOptions(screen.getByLabelText(/miembro voluntario/i), '7')
    await user.type(screen.getByLabelText(/horas trabajadas/i), '4')
    await user.click(screen.getByRole('button', { name: /agregar participante/i }))

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'La fotografía de evidencia es obligatoria para la revisión del MSP',
    )
  })
})
