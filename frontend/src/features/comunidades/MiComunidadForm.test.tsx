import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { MiComunidadForm } from './MiComunidadForm'

describe('MiComunidadForm', () => {
  it('renderiza la información fija y los campos editables', async () => {
    render(<MiComunidadForm />)

    expect(await screen.findByText('Huerto Comunitario Allpa Viva')).toBeInTheDocument()
    expect(screen.getByText('Pichincha')).toBeInTheDocument()
    expect(screen.getByText('Calderón')).toBeInTheDocument()
    expect(screen.getByText('Segundo Tránsito Amaguaña')).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre de la comunidad/i)).toHaveValue('Huerto Comunitario Allpa Viva')
    expect(screen.getByLabelText(/teléfono de contacto/i)).toHaveValue('0991234567')
    expect(screen.getByLabelText(/correo de contacto/i)).toHaveValue('comunidad.allpa@huertos.gob.ec')
  })

  it('bloquea el guardado ante formato erróneo de teléfono o correo', async () => {
    const user = userEvent.setup()
    render(<MiComunidadForm />)

    const inputTelefono = await screen.findByLabelText(/teléfono de contacto/i)
    const inputCorreo = screen.getByLabelText(/correo de contacto/i)
    const botonGuardar = screen.getByRole('button', { name: /guardar cambios/i })

    await user.clear(inputTelefono)
    await user.type(inputTelefono, '12345')

    await user.clear(inputCorreo)
    await user.type(inputCorreo, 'invalido')

    await user.click(botonGuardar)

    expect(screen.getByText(/el teléfono debe tener 10 dígitos y empezar con 09/i)).toBeInTheDocument()
    expect(screen.getByText(/formato de correo inválido/i)).toBeInTheDocument()
  })

  it('actualiza exitosamente y muestra la notificación de éxito', async () => {
    const user = userEvent.setup()
    render(<MiComunidadForm />)

    const inputNombre = await screen.findByLabelText(/nombre de la comunidad/i)
    const inputTelefono = screen.getByLabelText(/teléfono de contacto/i)
    const inputCorreo = screen.getByLabelText(/correo de contacto/i)
    const botonGuardar = screen.getByRole('button', { name: /guardar cambios/i })

    await user.clear(inputNombre)
    await user.type(inputNombre, 'Huerto Comunitario Nueva Esperanza')

    await user.clear(inputTelefono)
    await user.type(inputTelefono, '0998765432')

    await user.clear(inputCorreo)
    await user.type(inputCorreo, 'contacto.nuevo@huertos.gob.ec')

    await user.click(botonGuardar)

    expect(await screen.findByRole('status')).toHaveTextContent(
      /información de la comunidad actualizada correctamente/i,
    )
    expect(inputNombre).toHaveValue('Huerto Comunitario Nueva Esperanza')
  })

  it('muestra notificación de error si el backend falla al actualizar', async () => {
    server.use(
      http.put('*/api/comunidades/mi-comunidad', () =>
        HttpResponse.json({ detail: 'Ya existe una comunidad registrada con este nombre' }, { status: 409 }),
      ),
    )

    const user = userEvent.setup()
    render(<MiComunidadForm />)

    const botonGuardar = await screen.findByRole('button', { name: /guardar cambios/i })
    await user.click(botonGuardar)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ya existe una comunidad registrada con este nombre',
    )
  })
})
