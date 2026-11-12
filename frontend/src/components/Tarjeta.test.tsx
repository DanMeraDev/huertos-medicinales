import { render, screen } from '@testing-library/react'
import { Tarjeta } from './Tarjeta'

describe('Tarjeta', () => {
  it('muestra el contenido dentro de la superficie de tarjeta', () => {
    render(<Tarjeta aria-label="Manzanilla">Contenido</Tarjeta>)

    const tarjeta = screen.getByRole('article', { name: 'Manzanilla' })
    expect(tarjeta).toHaveTextContent('Contenido')
    expect(tarjeta).toHaveClass('tarjeta')
  })

  it('agrega la clase recibida sin perder la clase base', () => {
    render(<Tarjeta className="planta-tarjeta">Contenido</Tarjeta>)

    expect(screen.getByRole('article')).toHaveClass('tarjeta', 'planta-tarjeta')
  })
})
