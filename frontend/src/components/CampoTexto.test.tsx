import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CampoTexto } from './CampoTexto'

describe('CampoTexto', () => {
  it('muestra la etiqueta asociada al valor recibido', () => {
    render(<CampoTexto etiqueta="Nombre" valor="Ana" alCambiar={vi.fn()} />)

    expect(screen.getByLabelText('Nombre')).toHaveValue('Ana')
  })

  it('avisa el nuevo valor cuando el usuario escribe', async () => {
    const alCambiar = vi.fn()
    render(<CampoTexto etiqueta="Nombre" valor="" alCambiar={alCambiar} />)

    await userEvent.type(screen.getByLabelText('Nombre'), 'L')

    expect(alCambiar).toHaveBeenCalledWith('L')
  })

  it('muestra el error debajo del campo y lo marca como inválido', () => {
    render(
      <CampoTexto etiqueta="Nombre" valor="" alCambiar={vi.fn()} error="El nombre es obligatorio" />
    )

    const campo = screen.getByLabelText('Nombre')
    expect(campo).toBeInvalid()
    expect(campo).toHaveAccessibleDescription('El nombre es obligatorio')
  })

  it('no muestra error cuando no lo recibe', () => {
    render(<CampoTexto etiqueta="Nombre" valor="Ana" alCambiar={vi.fn()} />)

    expect(screen.getByLabelText('Nombre')).toBeValid()
  })
})
