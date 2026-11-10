import { render, screen } from '@testing-library/react'
import { Notificacion } from './Notificacion'

describe('Notificacion', () => {
  it('muestra un mensaje de éxito como estado', () => {
    render(<Notificacion tipo="exito" mensaje="Miembro registrado correctamente" />)

    const notificacion = screen.getByRole('status')
    expect(notificacion).toHaveTextContent('Miembro registrado correctamente')
    expect(notificacion).toHaveClass('notificacion-exito')
  })

  it('muestra un mensaje de error como alerta', () => {
    render(<Notificacion tipo="error" mensaje="No se pudo registrar el miembro" />)

    const notificacion = screen.getByRole('alert')
    expect(notificacion).toHaveTextContent('No se pudo registrar el miembro')
    expect(notificacion).toHaveClass('notificacion-error')
  })
})
