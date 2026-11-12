import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { CrearUsuarioForm } from './CrearUsuarioForm'

const CEDULA_VALIDA = '1710034065'

function haceAnios(anios: number) {
  const fecha = new Date()
  fecha.setFullYear(fecha.getFullYear() - anios)
  return fecha.toISOString().slice(0, 10)
}

async function llenarFormulario({ cedula = CEDULA_VALIDA, nacimiento = '1990-05-14' } = {}) {
  const usuario = userEvent.setup()
  await usuario.type(screen.getByLabelText('Nombre completo'), 'Rosa Elena Chimbolema')
  await usuario.type(screen.getByLabelText('Cédula de identidad'), cedula)
  await usuario.type(screen.getByLabelText('Fecha de nacimiento'), nacimiento)
  await usuario.selectOptions(screen.getByLabelText('Género'), 'FEMENINO')
  await usuario.type(screen.getByLabelText('Correo electrónico'), 'rosa.chimbolema@huertos.gob.ec')
  await usuario.type(screen.getByLabelText('Teléfono celular'), '0987654321')
  await usuario.selectOptions(screen.getByLabelText('Rol del sistema'), 'TECNICO_MSP')
  await usuario.type(screen.getByLabelText('Contraseña temporal'), 'PasswordSegura2026')
  return usuario
}

describe('CrearUsuarioForm', () => {
  it('muestra a Kinti, los campos y los selectores de género y rol', () => {
    render(<CrearUsuarioForm />)

    expect(screen.getByAltText('Kinti Guía de sabiduría ancestral')).toBeInTheDocument()
    expect(screen.getByLabelText('Correo electrónico')).toHaveAttribute('type', 'email')
    expect(screen.getByLabelText('Teléfono celular')).toHaveAttribute('type', 'tel')
    expect(screen.getByLabelText('Fecha de nacimiento')).toHaveAttribute('type', 'date')
    expect(screen.getByLabelText('Contraseña temporal')).toHaveAttribute('type', 'password')
    expect(screen.getByRole('option', { name: 'Otro' })).toHaveValue('OTRO')
    expect(screen.getByRole('option', { name: 'Técnico SIS' })).toHaveValue('TECNICO_SIS')
    expect(screen.getByRole('option', { name: 'Responsable de comunidad' })).toHaveValue('RESPONSABLE_COMUNIDAD')
    expect(screen.getByRole('button', { name: 'Crear usuario' })).toBeInTheDocument()
  })

  it('no envía el formulario si la cédula no es válida', async () => {
    let peticiones = 0
    server.use(http.post('*/api/usuarios', () => {
      peticiones++
      return HttpResponse.json({}, { status: 201 })
    }))
    render(<CrearUsuarioForm />)

    const usuario = await llenarFormulario({ cedula: '1710034066' })
    await usuario.click(screen.getByRole('button', { name: 'Crear usuario' }))

    expect(screen.getByText('La cédula no es válida')).toBeInTheDocument()
    expect(peticiones).toBe(0)
  })

  it('no envía el formulario si la persona es menor de 18 años', async () => {
    let peticiones = 0
    server.use(http.post('*/api/usuarios', () => {
      peticiones++
      return HttpResponse.json({}, { status: 201 })
    }))
    render(<CrearUsuarioForm />)

    const usuario = await llenarFormulario({ nacimiento: haceAnios(17) })
    await usuario.click(screen.getByRole('button', { name: 'Crear usuario' }))

    expect(screen.getByText('La persona debe ser mayor de 18 años')).toBeInTheDocument()
    expect(peticiones).toBe(0)
  })

  it('avisa en tiempo real si la contraseña es débil', async () => {
    render(<CrearUsuarioForm />)

    await userEvent.setup().type(screen.getByLabelText('Contraseña temporal'), 'corta')

    expect(screen.getByText(
      'Mínimo 12 caracteres, con al menos una mayúscula, una minúscula y un número',
    )).toBeInTheDocument()
  })

  it('crea el usuario, limpia el formulario y muestra la confirmación', async () => {
    render(<CrearUsuarioForm />)

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Crear usuario' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Cuenta creada para Rosa Elena Chimbolema. Deberá cambiar la contraseña en su primer acceso',
    )
    expect(screen.getByLabelText('Nombre completo')).toHaveValue('')
    expect(screen.getByLabelText('Cédula de identidad')).toHaveValue('')
    expect(screen.getByLabelText('Contraseña temporal')).toHaveValue('')
  })

  it('muestra el error del servidor y conserva los datos si la cédula ya existe', async () => {
    server.use(http.post('*/api/usuarios', () =>
      HttpResponse.json(
        { status: 409, detail: 'Ya existe un usuario registrado con la cédula ingresada' },
        { status: 409 },
      ),
    ))
    render(<CrearUsuarioForm />)

    const usuario = await llenarFormulario()
    await usuario.click(screen.getByRole('button', { name: 'Crear usuario' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Ya existe un usuario registrado con la cédula ingresada')
    expect(screen.getByLabelText('Cédula de identidad')).toHaveValue(CEDULA_VALIDA)
  })
})
