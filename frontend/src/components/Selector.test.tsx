import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Selector } from './Selector'

const opciones = [
  { valor: 'APRENDIZ', etiqueta: 'Aprendiz' },
  { valor: 'EXPERTO', etiqueta: 'Experto' },
]

describe('Selector', () => {
  it('muestra la etiqueta y la opción del valor recibido', () => {
    render(<Selector etiqueta="Nivel" opciones={opciones} valor="EXPERTO" alCambiar={vi.fn()} />)

    expect(screen.getByLabelText('Nivel')).toHaveValue('EXPERTO')
    expect(screen.getByRole('option', { name: 'Aprendiz' })).toBeInTheDocument()
  })

  it('muestra una opción vacía cuando no hay valor elegido', () => {
    render(<Selector etiqueta="Nivel" opciones={opciones} valor="" alCambiar={vi.fn()} />)

    expect(screen.getByLabelText('Nivel')).toHaveDisplayValue('Selecciona una opción')
  })

  it('avisa el valor de la opción elegida', async () => {
    const alCambiar = vi.fn()
    render(<Selector etiqueta="Nivel" opciones={opciones} valor="" alCambiar={alCambiar} />)

    await userEvent.selectOptions(screen.getByLabelText('Nivel'), 'Aprendiz')

    expect(alCambiar).toHaveBeenCalledWith('APRENDIZ')
  })

  it('muestra el error debajo del campo y lo marca como inválido', () => {
    render(
      <Selector
        etiqueta="Nivel"
        opciones={opciones}
        valor=""
        alCambiar={vi.fn()}
        error="Selecciona un nivel"
      />
    )

    const campo = screen.getByLabelText('Nivel')
    expect(campo).toBeInvalid()
    expect(campo).toHaveAccessibleDescription('Selecciona un nivel')
  })
})
