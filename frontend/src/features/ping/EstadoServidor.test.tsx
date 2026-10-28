import { render, screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { EstadoServidor } from './EstadoServidor'

describe('EstadoServidor', () => {
  it('muestra la versión de la base cuando el back responde', async () => {
    render(<EstadoServidor />)

    expect(await screen.findByText('11.4.8-MariaDB')).toBeInTheDocument()
  })

  it('muestra un aviso cuando el back falla', async () => {
    server.use(http.get('*/api/ping', () => new HttpResponse(null, { status: 500 })))

    render(<EstadoServidor />)

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo conectar')
  })
})
