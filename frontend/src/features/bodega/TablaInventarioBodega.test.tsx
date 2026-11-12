import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { TablaInventarioBodega } from './TablaInventarioBodega'

describe('TablaInventarioBodega', () => {
  it('renderiza la tabla con los materiales disponibles de la comunidad', async () => {
    render(<TablaInventarioBodega comunidadId={1} />)

    expect(await screen.findByRole('heading', { name: 'Inventario de Bodega' })).toBeInTheDocument()
    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    expect(screen.getByText('(Matricaria chamomilla)')).toBeInTheDocument()
    expect(screen.getByText('Menta')).toBeInTheDocument()
    expect(screen.getByText('(Mentha spicata)')).toBeInTheDocument()
    expect(screen.getByText('SEMILLAS')).toBeInTheDocument()
    expect(screen.getByText('ESQUEJES')).toBeInTheDocument()
    expect(screen.getByText('250 GRAMOS')).toBeInTheDocument()
    expect(screen.getByText('15 UNIDADES')).toBeInTheDocument()
    expect(screen.getByText('Lote A1')).toBeInTheDocument()
    expect(screen.getByText('Donación vivero local')).toBeInTheDocument()
    expect(screen.getByText('2025-05-10')).toBeInTheDocument()
    expect(screen.getByText('2025-05-11')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Registrar baja' })).toHaveLength(2)
  })

  it('permite filtrar en tiempo real por nombre de planta', async () => {
    render(<TablaInventarioBodega comunidadId={1} />)

    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    expect(screen.getByText('Menta')).toBeInTheDocument()

    const campoBuscar = screen.getByLabelText(/buscar por planta/i)
    await userEvent.type(campoBuscar, 'Menta')

    expect(screen.getByText('Menta')).toBeInTheDocument()
    expect(screen.queryByText('Manzanilla')).not.toBeInTheDocument()
  })

  it('permite filtrar por tipo de material vegetal', async () => {
    render(<TablaInventarioBodega comunidadId={1} />)

    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    expect(screen.getByText('Menta')).toBeInTheDocument()

    const selectorTipo = screen.getByLabelText(/filtrar por tipo/i)
    await userEvent.selectOptions(selectorTipo, 'SEMILLAS')

    expect(screen.getByText('Manzanilla')).toBeInTheDocument()
    expect(screen.queryByText('Menta')).not.toBeInTheDocument()
  })

  it('muestra mensaje informativo cuando la comunidad no tiene material vegetal', async () => {
    render(<TablaInventarioBodega comunidadId={99} />)

    expect(
      await screen.findByText('No se registra material vegetal en la bodega de esta comunidad'),
    ).toBeInTheDocument()
  })

  it('muestra notificación de error cuando no tiene permisos para consultar la comunidad (403)', async () => {
    render(<TablaInventarioBodega comunidadId={88} />)

    expect(
      await screen.findByText('No tiene permisos para consultar el inventario de otra comunidad'),
    ).toBeInTheDocument()
  })

  it('permite a un técnico MSP seleccionar la comunidad a supervisar', async () => {
    const alSeleccionarComunidad = vi.fn()
    const comunidades = [
      { id: 1, nombre: 'Comunidad Central' },
      { id: 2, nombre: 'Comunidad Norte' },
    ]

    render(
      <TablaInventarioBodega
        comunidadId={1}
        esTecnicoMsp={true}
        comunidadesSupervisadas={comunidades}
        alCambiarComunidad={alSeleccionarComunidad}
      />,
    )

    expect(await screen.findByText('Manzanilla')).toBeInTheDocument()
    const selectorComunidad = screen.getByLabelText(/comunidad a supervisar/i)
    expect(selectorComunidad).toBeInTheDocument()

    await userEvent.selectOptions(selectorComunidad, '2')
    expect(alSeleccionarComunidad).toHaveBeenCalledWith(2)
  })

  it('ejecuta callback al pulsar el botón de registrar baja', async () => {
    const alRegistrarBaja = vi.fn()
    render(<TablaInventarioBodega comunidadId={1} alRegistrarBaja={alRegistrarBaja} />)

    const botonesBaja = await screen.findAllByRole('button', { name: 'Registrar baja' })
    await userEvent.click(botonesBaja[0])

    await waitFor(() => {
      expect(alRegistrarBaja).toHaveBeenCalledWith(
        expect.objectContaining({
          id: 1,
          nombreComunPlanta: 'Manzanilla',
        }),
      )
    })
  })
})

