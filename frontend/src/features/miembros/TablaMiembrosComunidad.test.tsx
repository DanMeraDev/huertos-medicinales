import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { TablaMiembrosComunidad } from './TablaMiembrosComunidad'

function renderizarTabla() {
  return render(
    <MemoryRouter>
      <TablaMiembrosComunidad />
    </MemoryRouter>,
  )
}

describe('TablaMiembrosComunidad', () => {
  it('renderiza la lista de miembros de la comunidad con sus datos y estados', async () => {
    renderizarTabla()

    expect(await screen.findByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()
    expect(screen.getByText('1710034065')).toBeInTheDocument()
    expect(screen.getByText('APRENDIZ')).toBeInTheDocument()
    expect(screen.getByText('José Manuel Quishpe')).toBeInTheDocument()
    expect(screen.getByText('1710034073')).toBeInTheDocument()
    expect(screen.getByText('OFICIAL')).toBeInTheDocument()

    expect(screen.getByRole('button', { name: /dar de baja a maría tránsito amaguaña alba/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /reactivar a josé manuel quishpe/i })).toBeInTheDocument()
  })

  it('filtra los miembros por nombre o cédula', async () => {
    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()

    const buscador = screen.getByPlaceholderText(/buscar por nombre o cédula/i)
    await user.type(buscador, 'Quishpe')

    expect(screen.getByText('José Manuel Quishpe')).toBeInTheDocument()
    expect(screen.queryByText('María Tránsito Amaguaña Alba')).not.toBeInTheDocument()
  })

  it('filtra los miembros por estado activo e inactivo', async () => {
    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()

    const filtroInactivos = screen.getByRole('button', { name: /^inactivos$/i })
    await user.click(filtroInactivos)

    expect(screen.getByText('José Manuel Quishpe')).toBeInTheDocument()
    expect(screen.queryByText('María Tránsito Amaguaña Alba')).not.toBeInTheDocument()

    const filtroActivos = screen.getByRole('button', { name: /^activos$/i })
    await user.click(filtroActivos)

    expect(screen.getByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()
    expect(screen.queryByText('José Manuel Quishpe')).not.toBeInTheDocument()
  })

  it('abre el modal de confirmación de baja y actualiza el estado tras confirmar', async () => {
    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()

    const botonBaja = screen.getByRole('button', { name: /dar de baja a maría tránsito amaguaña alba/i })
    await user.click(botonBaja)

    expect(
      screen.getByText((_content, element) => {
        return (
          element?.tagName.toLowerCase() === 'p' &&
          /¿está seguro de dar de baja a maría tránsito amaguaña alba\?/i.test(element.textContent ?? '')
        )
      }),
    ).toBeInTheDocument()

    const inputMotivo = screen.getByLabelText(/motivo de la baja/i)
    await user.type(inputMotivo, 'Cambio de domicilio fuera de la parroquia')

    const botonConfirmar = screen.getByRole('button', { name: /confirmar baja/i })
    await user.click(botonConfirmar)

    expect(await screen.findByText(/el miembro ha sido dado de baja de la comunidad exitosamente/i)).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: /reactivar a maría tránsito amaguaña alba/i })).toBeInTheDocument()
  })

  it('permite reactivar a un miembro inactivo directamente', async () => {
    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('José Manuel Quishpe')).toBeInTheDocument()

    const botonReactivar = screen.getByRole('button', { name: /reactivar a josé manuel quishpe/i })
    await user.click(botonReactivar)

    expect(await screen.findByText(/el miembro ha sido reactivado en la comunidad exitosamente/i)).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: /dar de baja a josé manuel quishpe/i })).toBeInTheDocument()
  })

  it('muestra mensaje de error cuando el servidor rechaza el cambio de estado', async () => {
    server.use(
      http.patch('*/api/comunidades/mi-comunidad/miembros/:miembroId/estado', () => {
        return HttpResponse.json(
          { detail: 'El voluntario ya se encuentra en el estado solicitado' },
          { status: 409 },
        )
      }),
    )

    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('José Manuel Quishpe')).toBeInTheDocument()

    const botonReactivar = screen.getByRole('button', { name: /reactivar a josé manuel quishpe/i })
    await user.click(botonReactivar)

    expect(await screen.findByText('El voluntario ya se encuentra en el estado solicitado')).toBeInTheDocument()
  })

  it('abre el modal de edición de datos de voluntario al hacer clic en Editar', async () => {
    const user = userEvent.setup()
    renderizarTabla()

    expect(await screen.findByText('María Tránsito Amaguaña Alba')).toBeInTheDocument()

    const botonEditar = screen.getByRole('button', { name: /editar a maría tránsito amaguaña alba/i })
    await user.click(botonEditar)

    expect(await screen.findByRole('heading', { name: /editar datos de voluntario/i })).toBeInTheDocument()
  })
})
