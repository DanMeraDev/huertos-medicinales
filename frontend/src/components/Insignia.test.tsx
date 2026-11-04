import { render, screen } from '@testing-library/react'
import { Insignia } from './Insignia'

describe('Insignia', () => {
  it('muestra el texto con el tono neutro por defecto', () => {
    render(<Insignia texto="APRENDIZ" />)

    expect(screen.getByText('APRENDIZ')).toHaveClass('insignia-neutro')
  })

  it('aplica el tono recibido', () => {
    render(<Insignia texto="INACTIVO" tono="error" />)

    expect(screen.getByText('INACTIVO')).toHaveClass('insignia-error')
  })
})
