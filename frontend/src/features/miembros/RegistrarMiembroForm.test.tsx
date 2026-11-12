import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { RegistrarMiembroForm } from './RegistrarMiembroForm'

describe('RegistrarMiembroForm', () => {
  it('renderiza la cabecera motivacional con KintiGuia y todos los campos del formulario', () => {
    render(<RegistrarMiembroForm />)

    expect(screen.getByText(/¡Mejores son dos que uno, que la comunidad siga creciendo!/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/cédula de identidad/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nombres/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de nacimiento/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/género/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/teléfono celular/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nivel inicial/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /registrar voluntario/i })).toBeInTheDocument()
  })

  it('valida en el cliente cédula, nombres, teléfono y edad mínima de 10 años', async () => {
    const user = userEvent.setup()
    render(<RegistrarMiembroForm />)

    const inputCedula = screen.getByLabelText(/cédula de identidad/i)
    const inputNombres = screen.getByLabelText(/nombres/i)
    const inputApellidos = screen.getByLabelText(/apellidos/i)
    const inputFecha = screen.getByLabelText(/fecha de nacimiento/i)
    const inputTelefono = screen.getByLabelText(/teléfono celular/i)
    const botonRegistrar = screen.getByRole('button', { name: /registrar voluntario/i })

    await user.type(inputCedula, '12345')
    await user.type(inputNombres, 'Maria 123')
    await user.type(inputApellidos, 'Amaguaña #')
    await user.type(inputFecha, '2020-01-01')
    await user.type(inputTelefono, '0812345678')
    await user.click(botonRegistrar)

    expect(await screen.findByText(/la cédula ingresada no es válida/i)).toBeInTheDocument()
    expect(screen.getByText(/los nombres solo admiten letras y espacios/i)).toBeInTheDocument()
    expect(screen.getByText(/los apellidos solo admiten letras y espacios/i)).toBeInTheDocument()
    expect(screen.getByText(/el teléfono debe iniciar con 09 y tener 10 dígitos/i)).toBeInTheDocument()
    expect(screen.getByText(/la persona voluntaria debe tener al menos 10 años de edad cumplidos/i)).toBeInTheDocument()
  })

  it('abre el modal LOPDP y mantiene Confirmar Registro deshabilitado hasta marcar la casilla', async () => {
    const user = userEvent.setup()
    render(<RegistrarMiembroForm />)

    await user.type(screen.getByLabelText(/cédula de identidad/i), '1710034065')
    await user.type(screen.getByLabelText(/nombres/i), 'María Tránsito')
    await user.type(screen.getByLabelText(/apellidos/i), 'Amaguaña Alba')
    await user.type(screen.getByLabelText(/fecha de nacimiento/i), '2010-03-25')
    await user.selectOptions(screen.getByLabelText(/género/i), 'FEMENINO')
    await user.type(screen.getByLabelText(/teléfono celular/i), '0981234567')

    await user.click(screen.getByRole('button', { name: /registrar voluntario/i }))

    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText(/cumplimiento lopdp - consentimiento de voluntario/i)).toBeInTheDocument()

    const botonConfirmar = screen.getByRole('button', { name: /confirmar registro/i })
    expect(botonConfirmar).toBeDisabled()

    const checkbox = screen.getByRole('checkbox', {
      name: /certifico como responsable comunitario/i,
    })
    await user.click(checkbox)
    expect(botonConfirmar).toBeEnabled()

    await user.click(checkbox)
    expect(botonConfirmar).toBeDisabled()
  })

  it('registra exitosamente tras confirmar en modal y muestra notificación de bienvenida', async () => {
    const user = userEvent.setup()
    const alGuardarExitoso = vi.fn()
    render(<RegistrarMiembroForm alGuardarExitoso={alGuardarExitoso} />)

    const inputCedula = screen.getByLabelText(/cédula de identidad/i)
    const inputNombres = screen.getByLabelText(/nombres/i)
    const inputApellidos = screen.getByLabelText(/apellidos/i)
    const inputFecha = screen.getByLabelText(/fecha de nacimiento/i)
    const selectorGenero = screen.getByLabelText(/género/i)
    const inputTelefono = screen.getByLabelText(/teléfono celular/i)

    await user.type(inputCedula, '1710034065')
    await user.type(inputNombres, 'María Tránsito')
    await user.type(inputApellidos, 'Amaguaña Alba')
    await user.type(inputFecha, '2010-03-25')
    await user.selectOptions(selectorGenero, 'FEMENINO')
    await user.type(inputTelefono, '0981234567')

    await user.click(screen.getByRole('button', { name: /registrar voluntario/i }))

    const checkbox = await screen.findByRole('checkbox', {
      name: /certifico como responsable comunitario/i,
    })
    await user.click(checkbox)

    const botonConfirmar = screen.getByRole('button', { name: /confirmar registro/i })
    await user.click(botonConfirmar)

    expect(await screen.findByRole('status')).toHaveTextContent(
      /¡voluntario\/a registrado\/a exitosamente en la comunidad!/i,
    )
    expect(inputCedula).toHaveValue('')
    expect(inputNombres).toHaveValue('')
    expect(alGuardarExitoso).toHaveBeenCalledTimes(1)
  })

  it('muestra notificación de error cuando el voluntario ya está registrado en la comunidad', async () => {
    const user = userEvent.setup()
    render(<RegistrarMiembroForm />)

    await user.type(screen.getByLabelText(/cédula de identidad/i), '1710034069')
    await user.type(screen.getByLabelText(/nombres/i), 'Segundo')
    await user.type(screen.getByLabelText(/apellidos/i), 'Quishpe')
    await user.type(screen.getByLabelText(/fecha de nacimiento/i), '2000-01-15')
    await user.selectOptions(screen.getByLabelText(/género/i), 'MASCULINO')
    await user.type(screen.getByLabelText(/teléfono celular/i), '0987654321')

    await user.click(screen.getByRole('button', { name: /registrar voluntario/i }))

    const checkbox = await screen.findByRole('checkbox', {
      name: /certifico como responsable comunitario/i,
    })
    await user.click(checkbox)

    const botonConfirmar = screen.getByRole('button', { name: /confirmar registro/i })
    await user.click(botonConfirmar)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad',
    )
  })
})
