import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { BuscadorPerfilCedula } from './BuscadorPerfilCedula'

describe('BuscadorPerfilCedula', () => {
  it('renderiza el banner de Kinti, el campo de cédula y el botón de búsqueda', () => {
    render(<BuscadorPerfilCedula />)

    expect(screen.getByAltText(/kinti guía/i)).toBeInTheDocument()
    expect(
      screen.getByText(/nuestros voluntarios son el alma viva de los huertos/i),
    ).toBeInTheDocument()
    expect(screen.getByLabelText(/cédula de identidad/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /buscar voluntario/i })).toBeInTheDocument()
  })

  it('valida en cliente que la cédula tenga exactamente 10 dígitos numéricos antes de consultar', async () => {
    const usuario = userEvent.setup()
    render(<BuscadorPerfilCedula />)

    await usuario.type(screen.getByLabelText(/cédula de identidad/i), '17100')
    await usuario.click(screen.getByRole('button', { name: /buscar voluntario/i }))

    expect(
      await screen.findByText('La cédula debe contener exactamente 10 dígitos numéricos'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('consulta la cédula con MSW, abre el modal con comunidades, insignias y nota LOPDP y permite cerrarlo', async () => {
    const usuario = userEvent.setup()
    render(<BuscadorPerfilCedula />)

    await usuario.type(screen.getByLabelText(/cédula de identidad/i), '1710034065')
    await usuario.click(screen.getByRole('button', { name: /buscar voluntario/i }))

    const dialogo = await screen.findByRole('dialog')
    expect(dialogo).toBeInTheDocument()
    expect(dialogo).toHaveTextContent('¡Sembrador/a ejemplar de nuestra red!')
    expect(dialogo).toHaveTextContent('María Tránsito Amaguaña Alba')
    expect(dialogo).toHaveTextContent('OFICIAL')
    expect(dialogo).toHaveTextContent('120')
    expect(dialogo).toHaveTextContent('Huerto Comunitario Allpa Viva')
    expect(dialogo).toHaveTextContent('Sembradores de San Antonio')
    expect(dialogo).toHaveTextContent('Maestro del Abonaje 1')
    expect(dialogo).toHaveTextContent('Guardián de la Chakra Kiwa')
    expect(dialogo).toHaveTextContent(/LOPDP Ecuador/i)

    await usuario.click(screen.getByRole('button', { name: /cerrar/i }))

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })
  })

  it('muestra notificación de error 404 cuando no existe voluntario con la cédula ingresada', async () => {
    const usuario = userEvent.setup()
    render(<BuscadorPerfilCedula />)

    await usuario.type(screen.getByLabelText(/cédula de identidad/i), '9999999999')
    await usuario.click(screen.getByRole('button', { name: /buscar voluntario/i }))

    expect(
      await screen.findByText(
        'No se encontró ningún voluntario registrado con la cédula ingresada. Verifica el número o anímate a registrar una nueva comunidad',
      ),
    ).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
