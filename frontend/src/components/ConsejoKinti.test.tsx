import { render, screen } from '@testing-library/react'
import { ConsejoKinti } from './ConsejoKinti'

describe('ConsejoKinti', () => {
  it('muestra a Kinti junto al consejo', () => {
    render(<ConsejoKinti variante="alerta" mensaje="Revisa las observaciones antes de reenviar." />)

    const consejo = screen.getByRole('note', { name: 'Consejo de Kinti' })
    expect(consejo).toHaveTextContent('Revisa las observaciones antes de reenviar.')
    expect(consejo.querySelector('img[src$="/kinti/kinti-alerta.svg"]')).toBeInTheDocument()
  })
})
