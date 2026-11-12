import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter } from 'react-router'
import { server } from '../../test/mocks/server'
import { DirectorioComunidadesTabla } from './DirectorioComunidadesTabla'

describe('DirectorioComunidadesTabla', () => {
  it('renderiza la cabecera motivacional con Kinti y la tarjeta de llamado a la acción', async () => {
    render(
      <MemoryRouter>
        <DirectorioComunidadesTabla />
      </MemoryRouter>,
    )

    expect(
      await screen.findByText(/¡Todas estas comunidades son nuestros guardianes de la tierra!/i),
    ).toBeInTheDocument()
    expect(
      screen.getByText(/¿Deseas fundar un huerto en tu barrio o sumarte a este movimiento sagrado\?/i),
    ).toBeInTheDocument()
    const botonPostular = screen.getByRole('link', { name: /solicitar crear mi comunidad/i })
    expect(botonPostular).toHaveAttribute('href', '/solicitar-comunidad')
  })

  it('renderiza las 6 columnas del directorio con los datos de las comunidades', async () => {
    render(
      <MemoryRouter>
        <DirectorioComunidadesTabla />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Huerto Comunitario Allpa Viva')).toBeInTheDocument()
    expect(screen.getByText('Segundo Tránsito Amaguaña')).toBeInTheDocument()
    expect(screen.getByText('0991234567')).toBeInTheDocument()
    expect(screen.getByText('comunidad.allpa@huertos.gob.ec')).toBeInTheDocument()
    expect(screen.getByText('14')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /ver en el mapa/i })).toBeInTheDocument()
  })

  it('abre el modal territorial al pulsar Ver en el mapa y muestra los datos geográficos', async () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter>
        <DirectorioComunidadesTabla />
      </MemoryRouter>,
    )

    const botonMapa = await screen.findByRole('button', { name: /ver en el mapa/i })
    await user.click(botonMapa)

    expect(
      await screen.findByRole('heading', { name: /ubicación territorial - huerto comunitario allpa viva/i }),
    ).toBeInTheDocument()
    expect(screen.getByText(/pichincha/i)).toBeInTheDocument()
    expect(screen.getByText(/calderon/i)).toBeInTheDocument()
    expect(screen.getByText(/2650/i)).toBeInTheDocument()
  })

  it('muestra el estado vacío con Kinti cuando no existen comunidades registradas', async () => {
    server.use(
      http.get('*/api/publico/comunidades', () => HttpResponse.json([])),
    )

    render(
      <MemoryRouter>
        <DirectorioComunidadesTabla />
      </MemoryRouter>,
    )

    expect(
      await screen.findByText(/aún no se han registrado comunidades en la red\. ¡sé el primero en sembrar la tuya!/i),
    ).toBeInTheDocument()
  })
})
