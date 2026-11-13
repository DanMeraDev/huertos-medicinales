import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { server } from '../../test/mocks/server'
import { LoginForm } from './LoginForm'

function renderizarLogin() {
  render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/mi-comunidad" element={<p>Panel de la comunidad</p>} />
        <Route path="/cambiar-clave" element={<p>Cambio de clave</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

async function ingresarCredenciales(correo: string, password: string) {
  const usuario = userEvent.setup()
  await usuario.type(screen.getByLabelText('Correo electrónico'), correo)
  await usuario.type(screen.getByLabelText('Contraseña'), password)
  await usuario.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
}

describe('LoginForm', () => {
  beforeEach(() => localStorage.clear())

  it('muestra a Kinti, los campos y el botón de ingreso', () => {
    renderizarLogin()

    expect(screen.getByAltText('Kinti Guía celebrando logro')).toBeInTheDocument()
    expect(screen.getByLabelText('Correo electrónico')).toHaveAttribute('type', 'email')
    expect(screen.getByLabelText('Contraseña')).toHaveAttribute('type', 'password')
    expect(screen.getByRole('button', { name: 'Iniciar sesión' })).toHaveClass('login-boton')
  })

  it('permite mostrar y ocultar la contraseña con el botón de ojo', async () => {
    const usuario = userEvent.setup()
    renderizarLogin()

    const campoClave = screen.getByLabelText('Contraseña')
    expect(campoClave).toHaveAttribute('type', 'password')

    const botonMostrar = screen.getByRole('button', { name: 'Mostrar contraseña' })
    await usuario.click(botonMostrar)

    expect(campoClave).toHaveAttribute('type', 'text')
    const botonOcultar = screen.getByRole('button', { name: 'Ocultar contraseña' })
    await usuario.click(botonOcultar)

    expect(campoClave).toHaveAttribute('type', 'password')
  })

  it('guarda el token y lleva al panel del rol', async () => {
    renderizarLogin()

    await ingresarCredenciales('rosa@correo.com', 'Password123!')

    expect(await screen.findByText('Panel de la comunidad')).toBeInTheDocument()
    expect(localStorage.getItem('token')).toBe('token-jwt')
  })

  it('lleva al cambio de clave cuando el usuario debe cambiarla', async () => {
    server.use(
      http.post('*/api/publico/auth/login', () =>
        HttpResponse.json({
          token: 'token-jwt',
          tipoToken: 'Bearer',
          id: 1,
          correo: 'rosa@correo.com',
          nombreCompleto: 'Rosa Quishpe',
          rol: 'RESPONSABLE_COMUNIDAD',
          requiereCambioClave: true,
        }),
      ),
    )
    renderizarLogin()

    await ingresarCredenciales('rosa@correo.com', 'Password123!')

    expect(await screen.findByText('Cambio de clave')).toBeInTheDocument()
  })

  it('muestra el error y vacía la contraseña con credenciales incorrectas', async () => {
    server.use(
      http.post('*/api/publico/auth/login', () =>
        HttpResponse.json({ detail: 'Correo o contraseña incorrectos' }, { status: 401 }),
      ),
    )
    renderizarLogin()

    await ingresarCredenciales('rosa@correo.com', 'Mala123!')

    expect(await screen.findByRole('alert')).toHaveTextContent('Correo o contraseña incorrectos')
    expect(screen.getByLabelText('Contraseña')).toHaveValue('')
    expect(screen.getByLabelText('Correo electrónico')).toHaveValue('rosa@correo.com')
    expect(localStorage.getItem('token')).toBeNull()
  })
})
