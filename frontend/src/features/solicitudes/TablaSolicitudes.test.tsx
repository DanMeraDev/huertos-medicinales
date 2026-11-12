import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { TablaSolicitudes } from './TablaSolicitudes'

const MOTIVO = 'La ubicación no cuenta con acceso seguro a fuente de agua.'

async function abrirDetalle() {
  const usuario = userEvent.setup()
  render(<TablaSolicitudes />)
  await usuario.click(await screen.findByRole('button', { name: 'Ver más' }))
  return usuario
}

describe('TablaSolicitudes', () => {
  it('muestra las solicitudes pendientes en una tabla de cuatro columnas con scroll horizontal', async () => {
    const { container } = render(<TablaSolicitudes />)

    const fila = await screen.findByRole('row', { name: /Segundo Tránsito Amaguaña/ })
    expect(screen.getAllByRole('columnheader').map((celda) => celda.textContent)).toEqual([
      'Solicitante',
      'Fecha',
      'Provincia',
      'Acción',
    ])
    expect(within(fila).getByText('10/11/2026')).toBeInTheDocument()
    expect(within(fila).getByText('Pichincha')).toBeInTheDocument()
    expect(container.querySelector('.solicitudes-desplazable table')).not.toBeNull()
    expect(screen.getByText('Varias personas quieren crear su huerto, revisa sus solicitudes aquí')).toBeInTheDocument()
  })

  it('anima con Kinti cuando no hay solicitudes pendientes', async () => {
    server.use(http.get('*/api/solicitudes-comunidad', () => HttpResponse.json([])))
    render(<TablaSolicitudes />)

    expect(
      await screen.findByText('No tienes solicitudes de aprobación pendientes por el momento. ¡Buen trabajo!'),
    ).toBeInTheDocument()
    expect(screen.getByAltText('Kinti Guía en estado vacío')).toBeInTheDocument()
  })

  it('abre la ficha completa en solo lectura al pulsar Ver más', async () => {
    await abrirDetalle()

    const ficha = await screen.findByRole('dialog', { name: 'Solicitud de Huerto Comunitario Allpa Viva' })
    expect(ficha).toHaveTextContent('1710034065')
    expect(ficha).toHaveTextContent('comunidad.allpa@huertos.gob.ec')
    expect(ficha).toHaveTextContent('Calle de las Rosas y Pasaje Los Pinos, lote 14')
    expect(ficha).toHaveTextContent('14/05/1990')
    expect(within(ficha).getByRole('button', { name: 'Aprobar' })).toBeInTheDocument()
    expect(within(ficha).getByRole('button', { name: 'No aprobar' })).toBeInTheDocument()
  })

  it('aprueba tras confirmar, avisa el resultado y actualiza la tabla', async () => {
    const usuario = await abrirDetalle()
    await usuario.click(await screen.findByRole('button', { name: 'Aprobar' }))
    expect(screen.getByRole('dialog', { name: 'Confirmar aprobación' })).toHaveTextContent(
      '¿Confirmas la aprobación de esta solicitud? Se creará la comunidad, su huerto y la cuenta de acceso para el responsable',
    )
    server.use(http.get('*/api/solicitudes-comunidad', () => HttpResponse.json([])))

    await usuario.click(screen.getByRole('button', { name: 'Sí, aprobar' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Solicitud aprobada exitosamente. Comunidad y cuenta de usuario creadas.',
    )
    expect(
      await screen.findByText('No tienes solicitudes de aprobación pendientes por el momento. ¡Buen trabajo!'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('exige un motivo de al menos 20 caracteres para rechazar', async () => {
    const usuario = await abrirDetalle()
    await usuario.click(await screen.findByRole('button', { name: 'No aprobar' }))
    await usuario.type(screen.getByLabelText('Motivo del rechazo'), 'Muy corto')
    await usuario.click(screen.getByRole('button', { name: 'Confirmar Rechazo' }))

    expect(screen.getByText('El motivo es obligatorio y debe tener al menos 20 caracteres')).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Motivo del rechazo' })).toBeInTheDocument()

    await usuario.clear(screen.getByLabelText('Motivo del rechazo'))
    await usuario.type(screen.getByLabelText('Motivo del rechazo'), MOTIVO)
    await usuario.click(screen.getByRole('button', { name: 'Confirmar Rechazo' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Solicitud rechazada. Se registró el motivo del rechazo.')
  })

  it('informa si otro técnico ya resolvió la solicitud', async () => {
    server.use(
      http.post('*/api/solicitudes-comunidad/:id/aprobar', () =>
        HttpResponse.json({ detail: 'Esta solicitud ya ha sido resuelta previamente por otro técnico' }, { status: 409 }),
      ),
    )
    const usuario = await abrirDetalle()
    await usuario.click(await screen.findByRole('button', { name: 'Aprobar' }))
    await usuario.click(screen.getByRole('button', { name: 'Sí, aprobar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Esta solicitud ya ha sido resuelta previamente por otro técnico',
    )
  })
})
