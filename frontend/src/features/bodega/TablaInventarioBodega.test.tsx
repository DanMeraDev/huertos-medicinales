import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { InventarioBodegaPage } from '../../pages/InventarioBodegaPage'
import { TablaInventarioBodega } from './TablaInventarioBodega'
import type { MaterialVegetalResponse } from './types'

const DATOS_PRUEBA: MaterialVegetalResponse[] = [
  {
    id: 10,
    comunidadId: 1,
    plantaId: 1,
    nombreComunPlanta: 'Manzanilla',
    nombreCientificoPlanta: 'Matricaria chamomilla',
    tipoMaterial: 'SEMILLAS',
    cantidadDisponible: 500,
    unidadMedida: 'GRAMOS',
    loteOrigen: 'Lote MZ-2026-A',
    fechaIngreso: '2026-11-10',
  },
  {
    id: 20,
    comunidadId: 1,
    plantaId: 2,
    nombreComunPlanta: 'Menta',
    nombreCientificoPlanta: 'Mentha spicata',
    tipoMaterial: 'ESQUEJES',
    cantidadDisponible: 25,
    unidadMedida: 'UNIDADES',
    loteOrigen: 'Donación vivero local',
    fechaIngreso: '2026-11-11',
  },
  {
    id: 30,
    comunidadId: 1,
    plantaId: 3,
    nombreComunPlanta: 'Ruda',
    nombreCientificoPlanta: 'Ruta graveolens',
    tipoMaterial: 'PLANTULAS',
    cantidadDisponible: 12,
    unidadMedida: 'UNIDADES',
    fechaIngreso: '2026-11-12',
  },
]

describe('TablaInventarioBodega', () => {
  it('renderiza la lista de inventario cargada desde MSW', async () => {
    render(<TablaInventarioBodega comunidadId={1} />)

    expect(await screen.findAllByText('Manzanilla')).toHaveLength(2)
    expect(screen.getAllByText('Menta')).toHaveLength(2)
    expect(screen.getAllByText(/500 GRAMOS/i).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/25 UNIDADES/i).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/Matricaria chamomilla/i).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/Mentha spicata/i).length).toBeGreaterThan(0)
  })

  it('permite filtrar en tiempo real por nombre común de planta', async () => {
    render(<TablaInventarioBodega items={DATOS_PRUEBA} />)

    const campoBuscar = screen.getByLabelText(/buscar por planta/i)
    await userEvent.type(campoBuscar, 'Manzanilla')

    expect(screen.getAllByText('Manzanilla').length).toBeGreaterThan(0)
    expect(screen.queryByText('Menta')).not.toBeInTheDocument()
    expect(screen.queryByText('Ruda')).not.toBeInTheDocument()
  })

  it('permite filtrar en tiempo real por nombre científico de planta', async () => {
    render(<TablaInventarioBodega items={DATOS_PRUEBA} />)

    const campoBuscar = screen.getByLabelText(/buscar por planta/i)
    await userEvent.type(campoBuscar, 'spicata')

    expect(screen.getAllByText('Menta').length).toBeGreaterThan(0)
    expect(screen.queryByText('Manzanilla')).not.toBeInTheDocument()
    expect(screen.queryByText('Ruda')).not.toBeInTheDocument()
  })

  it('permite filtrar por tipo de material vegetal', async () => {
    render(<TablaInventarioBodega items={DATOS_PRUEBA} />)

    const selectorTipo = screen.getByLabelText(/tipo de material/i)
    await userEvent.selectOptions(selectorTipo, 'ESQUEJES')

    expect(screen.getAllByText('Menta').length).toBeGreaterThan(0)
    expect(screen.queryByText('Manzanilla')).not.toBeInTheDocument()
    expect(screen.queryByText('Ruda')).not.toBeInTheDocument()
  })

  it('muestra el mensaje de estado vacío cuando la comunidad no tiene registros', async () => {
    render(<TablaInventarioBodega comunidadId={2} />)

    expect(
      await screen.findByText('No se registra material vegetal en la bodega de esta comunidad'),
    ).toBeInTheDocument()
  })

  it('muestra mensaje informativo cuando el filtro no produce coincidencias', async () => {
    render(<TablaInventarioBodega items={DATOS_PRUEBA} />)

    const campoBuscar = screen.getByLabelText(/buscar por planta/i)
    await userEvent.type(campoBuscar, 'Especie Inexistente')

    expect(screen.getByText('No se encontraron registros que coincidan con la búsqueda')).toBeInTheDocument()
  })

  it('muestra botones de acción Dar de baja como delimitación BO-CU09', async () => {
    const alDarDeBaja = vi.fn()
    render(<TablaInventarioBodega items={DATOS_PRUEBA} alDarDeBaja={alDarDeBaja} />)

    const botones = screen.getAllByRole('button', { name: 'Dar de baja' })
    expect(botones.length).toBeGreaterThan(0)

    await userEvent.click(botones[0])
    expect(alDarDeBaja).toHaveBeenCalledWith(DATOS_PRUEBA[0])
  })

  it('muestra mensaje de error ante respuesta 403 Forbidden', async () => {
    render(<TablaInventarioBodega comunidadId={999} />)

    expect(
      await screen.findByText('No tiene permisos para consultar el inventario de otra comunidad'),
    ).toBeInTheDocument()
  })
})

describe('InventarioBodegaPage', () => {
  it('para RESPONSABLE_COMUNIDAD carga automáticamente su comunidad sin mostrar selector', async () => {
    render(<InventarioBodegaPage rolInicial="RESPONSABLE_COMUNIDAD" />)

    expect(await screen.findByText('Huerto Comunitario Allpa Viva')).toBeInTheDocument()
    expect(screen.queryByLabelText(/seleccionar comunidad/i)).not.toBeInTheDocument()
    expect(await screen.findAllByText('Manzanilla')).toHaveLength(2)
  })

  it('para TECNICO_MSP muestra el selector de comunidades y actualiza el inventario al cambiar', async () => {
    const comunidadesSimuladas = [
      { id: 1, nombre: 'Comunidad San Francisco' },
      { id: 2, nombre: 'Comunidad Santa Clara' },
    ]

    render(
      <InventarioBodegaPage
        rolInicial="TECNICO_MSP"
        comunidadesDisponibles={comunidadesSimuladas}
        comunidadIdInicial={1}
      />,
    )

    const selector = await screen.findByLabelText(/seleccionar comunidad/i)
    expect(selector).toBeInTheDocument()

    expect(await screen.findAllByText('Manzanilla')).toHaveLength(2)

    await userEvent.selectOptions(selector, '2')

    expect(
      await screen.findByText('No se registra material vegetal en la bodega de esta comunidad'),
    ).toBeInTheDocument()
  })

  it('muestra mensaje informativo cuando el técnico aún no ha seleccionado comunidad', () => {
    render(
      <InventarioBodegaPage
        rolInicial="TECNICO_MSP"
        comunidadesDisponibles={[{ id: 1, nombre: 'Comunidad San Francisco' }]}
      />,
    )

    expect(
      screen.getByText('Seleccione una comunidad para consultar su inventario de bodega'),
    ).toBeInTheDocument()
  })
})
