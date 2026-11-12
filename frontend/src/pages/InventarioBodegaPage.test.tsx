import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../test/mocks/server'
import { InventarioBodegaPage } from './InventarioBodegaPage'

describe('InventarioBodegaPage', () => {
  it('carga la bodega de la comunidad del responsable sin mostrar el selector de comunidad', async () => {
    render(<InventarioBodegaPage />)

    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    expect(screen.getByText('Menta')).toBeInTheDocument()
    expect(screen.queryByLabelText(/comunidad a supervisar/i)).not.toBeInTheDocument()
  })

  it('permite al técnico MSP elegir la comunidad a supervisar', async () => {
    server.use(
      http.get('*/api/bodega/contexto', () =>
        HttpResponse.json({
          rol: 'TECNICO_MSP',
          comunidades: [
            { id: 1, nombre: 'Comunidad San Pedro' },
            { id: 99, nombre: 'Comunidad Vacia' },
          ],
        }),
      ),
    )
    render(<InventarioBodegaPage />)

    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    await userEvent.selectOptions(screen.getByLabelText(/comunidad a supervisar/i), '99')

    expect(
      await screen.findByText('No se registra material vegetal en la bodega de esta comunidad'),
    ).toBeInTheDocument()
  })

  it('muestra una notificación de error si no se puede obtener el contexto del usuario', async () => {
    server.use(
      http.get('*/api/bodega/contexto', () =>
        HttpResponse.json({ detail: 'No existe una comunidad asociada al usuario' }, { status: 404 }),
      ),
    )
    render(<InventarioBodegaPage />)

    expect(await screen.findByRole('alert')).toHaveTextContent('No existe una comunidad asociada al usuario')
  })
})
