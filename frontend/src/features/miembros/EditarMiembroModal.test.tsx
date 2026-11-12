import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { vi } from 'vitest'
import { server } from '../../test/mocks/server'
import { EditarMiembroModal } from './EditarMiembroModal'
import { mockMiembroDetalle } from './miembrosMockHandlers'

describe('EditarMiembroModal', () => {
  it('renderiza los datos institucionales en solo lectura y los campos editables precargados', () => {
    render(
      <EditarMiembroModal
        abierto={true}
        miembro={mockMiembroDetalle}
        alCerrar={() => {}}
        alExito={() => {}}
      />,
    )

    expect(screen.getByRole('heading', { name: /editar datos de voluntario/i })).toBeInTheDocument()

    const inputCedula = screen.getByLabelText(/cédula de identidad/i)
    expect(inputCedula).toBeInTheDocument()
    expect(inputCedula).toHaveValue('1710034065')
    expect(inputCedula).toHaveAttribute('readonly')

    const inputNombre = screen.getByLabelText(/nombres y apellidos/i)
    expect(inputNombre).toBeInTheDocument()
    expect(inputNombre).toHaveValue('María Tránsito Amaguaña Alba')
    expect(inputNombre).toHaveAttribute('readonly')

    const inputNivel = screen.getByLabelText(/nivel actual/i)
    expect(inputNivel).toBeInTheDocument()
    expect(inputNivel).toHaveValue('APRENDIZ')
    expect(inputNivel).toHaveAttribute('readonly')

    const selectorGenero = screen.getByLabelText(/género/i)
    expect(selectorGenero).toBeInTheDocument()
    expect(selectorGenero).toHaveValue('FEMENINO')

    const inputTelefono = screen.getByLabelText(/teléfono celular/i)
    expect(inputTelefono).toBeInTheDocument()
    expect(inputTelefono).toHaveValue('0981234567')
    expect(inputTelefono).not.toHaveAttribute('readonly')
  })

  it('valida en el cliente que el teléfono inicie con 09 y tenga 10 dígitos', async () => {
    const user = userEvent.setup()
    render(
      <EditarMiembroModal
        abierto={true}
        miembro={mockMiembroDetalle}
        alCerrar={() => {}}
        alExito={() => {}}
      />,
    )

    const inputTelefono = screen.getByLabelText(/teléfono celular/i)
    await user.clear(inputTelefono)
    await user.type(inputTelefono, '0812345678')

    const botonGuardar = screen.getByRole('button', { name: /guardar cambios/i })
    await user.click(botonGuardar)

    expect(await screen.findByText(/el teléfono debe iniciar con 09 y tener 10 dígitos/i)).toBeInTheDocument()
  })

  it('actualiza los datos exitosamente y llama a alExito con los nuevos valores', async () => {
    const alExito = vi.fn()
    const alCerrar = vi.fn()
    const user = userEvent.setup()

    render(
      <EditarMiembroModal
        abierto={true}
        miembro={mockMiembroDetalle}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    const selectorGenero = screen.getByLabelText(/género/i)
    await user.selectOptions(selectorGenero, 'OTRO')

    const inputTelefono = screen.getByLabelText(/teléfono celular/i)
    await user.clear(inputTelefono)
    await user.type(inputTelefono, '0998877665')

    const botonGuardar = screen.getByRole('button', { name: /guardar cambios/i })
    await user.click(botonGuardar)

    expect(await screen.findByText(/datos del voluntario actualizados correctamente/i)).toBeInTheDocument()
    expect(alExito).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 15,
        genero: 'OTRO',
        telefono: '0998877665',
      }),
    )
  })

  it('muestra mensaje de error si el servidor rechaza la actualización', async () => {
    server.use(
      http.put('*/api/comunidades/mi-comunidad/miembros/:miembroId', () => {
        return HttpResponse.json(
          { detail: 'El voluntario no pertenece a la comunidad administrada' },
          { status: 404 },
        )
      }),
    )

    const user = userEvent.setup()
    render(
      <EditarMiembroModal
        abierto={true}
        miembro={mockMiembroDetalle}
        alCerrar={() => {}}
        alExito={() => {}}
      />,
    )

    const botonGuardar = screen.getByRole('button', { name: /guardar cambios/i })
    await user.click(botonGuardar)

    expect(await screen.findByText('El voluntario no pertenece a la comunidad administrada')).toBeInTheDocument()
  })

  it('llama a alCerrar al hacer clic en Cancelar', async () => {
    const alCerrar = vi.fn()
    const user = userEvent.setup()

    render(
      <EditarMiembroModal
        abierto={true}
        miembro={mockMiembroDetalle}
        alCerrar={alCerrar}
        alExito={() => {}}
      />,
    )

    const botonCancelar = screen.getByRole('button', { name: /cancelar/i })
    await user.click(botonCancelar)

    expect(alCerrar).toHaveBeenCalledTimes(1)
  })
})
