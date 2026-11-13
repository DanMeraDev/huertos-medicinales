import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router'
import { guardarSesion } from '../../api/sesion'
import { server } from '../../test/mocks/server'
import { CambiarClaveForm } from './CambiarClaveForm'

function renderizarFormulario() {
  render(
    <MemoryRouter initialEntries={['/cambiar-clave']}>
      <Routes>
        <Route path="/cambiar-clave" element={<CambiarClaveForm />} />
        <Route path="/" element={<p>Página de inicio</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

async function llenarClaves(actual: string, nueva: string, confirmacion: string) {
  const usuario = userEvent.setup()
  await usuario.type(screen.getByLabelText('Contraseña Actual'), actual)
  await usuario.type(screen.getByLabelText('Nueva Contraseña'), nueva)
  await usuario.type(screen.getByLabelText('Confirmar Nueva Contraseña'), confirmacion)
  await usuario.click(screen.getByRole('button', { name: 'Actualizar Contraseña' }))
  return usuario
}

describe('CambiarClaveForm', () => {
  it('muestra los tres campos de contraseña y el botón de actualizar', () => {
    renderizarFormulario()

    expect(screen.getByLabelText('Contraseña Actual')).toHaveAttribute('type', 'password')
    expect(screen.getByLabelText('Nueva Contraseña')).toHaveAttribute('type', 'password')
    expect(screen.getByLabelText('Confirmar Nueva Contraseña')).toHaveAttribute('type', 'password')
    expect(screen.getByRole('button', { name: 'Actualizar Contraseña' })).toBeInTheDocument()
  })

  it('pide confirmar el cambio antes de enviarlo', async () => {
    renderizarFormulario()

    await llenarClaves('PasswordSegura2026', 'NuevaClaveSegura2027', 'NuevaClaveSegura2027')

    expect(screen.getByRole('dialog', { name: 'Confirmar cambio de contraseña' })).toHaveTextContent(
      '¿Estás seguro de que deseas actualizar tu contraseña de acceso? A partir de tu próximo inicio de sesión deberás utilizar tu nueva clave.',
    )
  })

  it('actualiza la contraseña, avisa el éxito, cierra la sesión y lleva al inicio', async () => {
    guardarSesion({ token: 'token-anterior', rol: 'RESPONSABLE_COMUNIDAD' })
    renderizarFormulario()

    const usuario = await llenarClaves('PasswordSegura2026', 'NuevaClaveSegura2027', 'NuevaClaveSegura2027')
    await usuario.click(screen.getByRole('button', { name: 'Sí, cambiar contraseña' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Contraseña actualizada exitosamente')
    expect(await screen.findByText('Página de inicio', {}, { timeout: 3000 })).toBeInTheDocument()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('avisa que la confirmación no coincide sin abrir el modal', async () => {
    renderizarFormulario()

    await llenarClaves('PasswordSegura2026', 'NuevaClaveSegura2027', 'OtraClaveSegura2027')

    expect(screen.getByText('La nueva contraseña y su confirmación no coinciden')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('muestra el error y vacía las claves si la contraseña actual es incorrecta', async () => {
    server.use(
      http.put('*/api/usuarios/cambiar-clave', () =>
        HttpResponse.json({ detail: 'La contraseña actual ingresada es incorrecta' }, { status: 400 }),
      ),
    )
    renderizarFormulario()

    const usuario = await llenarClaves('ClaveEquivocada2026', 'NuevaClaveSegura2027', 'NuevaClaveSegura2027')
    await usuario.click(screen.getByRole('button', { name: 'Sí, cambiar contraseña' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('La contraseña actual ingresada es incorrecta')
    expect(screen.getByLabelText('Contraseña Actual')).toHaveValue('')
    expect(screen.getByLabelText('Nueva Contraseña')).toHaveValue('')
    expect(screen.getByLabelText('Confirmar Nueva Contraseña')).toHaveValue('')
  })
})
