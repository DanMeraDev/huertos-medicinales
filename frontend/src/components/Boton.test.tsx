import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Boton } from './Boton'

describe('Boton', () => {
  it('muestra el texto y avisa al hacer clic', async () => {
    const alHacerClic = vi.fn()
    render(<Boton onClick={alHacerClic}>Guardar</Boton>)

    await userEvent.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(alHacerClic).toHaveBeenCalledOnce()
  })

  it('usa la variante primaria por defecto', () => {
    render(<Boton>Guardar</Boton>)

    expect(screen.getByRole('button')).toHaveClass('boton-primario')
  })

  it('aplica la variante recibida', () => {
    render(<Boton variante="peligro">Eliminar</Boton>)

    expect(screen.getByRole('button')).toHaveClass('boton-peligro')
  })

  it('no avisa el clic cuando está deshabilitado', async () => {
    const alHacerClic = vi.fn()
    render(<Boton disabled onClick={alHacerClic}>Guardar</Boton>)

    await userEvent.click(screen.getByRole('button'))

    expect(screen.getByRole('button')).toBeDisabled()
    expect(alHacerClic).not.toHaveBeenCalled()
  })
})
