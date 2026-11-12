import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { FormularioSiembra } from './FormularioSiembra'
import type {
  HuertoDetalle,
  MaterialBodegaItem,
  PlantaDetalleItem,
  SiembraDetalle,
  SubhuertoItem,
} from './siembrasApi'

const huertoMock: HuertoDetalle = {
  id: 10,
  comunidadId: 5,
  nombreHuerto: 'Huerto San Antonio',
  altitudMsnm: 2800,
}

const subhuertosMock: SubhuertoItem[] = [
  {
    id: 1,
    huertoId: 10,
    nombreZona: 'Zona Huerto Principal',
    subtipo: 'SURCOS_WACHU',
    areaM2: 50,
    activo: true,
  },
  {
    id: 2,
    huertoId: 10,
    nombreZona: 'Zona Terrazas Altas',
    subtipo: 'TERRAZAS_ANTI',
    areaM2: 30,
    activo: true,
  },
]

const materialesMock: MaterialBodegaItem[] = [
  {
    id: 101,
    comunidadId: 5,
    plantaId: 201,
    nombreComunPlanta: 'Manzanilla',
    nombreCientificoPlanta: 'Matricaria chamomilla',
    tipoMaterial: 'SEMILLAS',
    cantidadDisponible: 10,
    unidadMedida: 'GRAMOS',
    fechaIngreso: '2026-11-01',
  },
  {
    id: 102,
    comunidadId: 5,
    plantaId: 202,
    nombreComunPlanta: 'Cedrón',
    nombreCientificoPlanta: 'Aloysia citrodora',
    tipoMaterial: 'ESQUEJES',
    cantidadDisponible: 5,
    unidadMedida: 'UNIDADES',
    fechaIngreso: '2026-11-02',
  },
]

const plantasDetalleMock: Record<number, PlantaDetalleItem> = {
  201: {
    id: 201,
    nombreComun: 'Manzanilla',
    nombreCientifico: 'Matricaria chamomilla',
    cualidadTermica: 'CALIENTE',
    altitudMinMsnm: 2000,
    altitudMaxMsnm: 3200,
  },
  202: {
    id: 202,
    nombreComun: 'Cedrón',
    nombreCientifico: 'Aloysia citrodora',
    cualidadTermica: 'FRIA',
    altitudMinMsnm: 1000,
    altitudMaxMsnm: 2400,
  },
}

describe('FormularioSiembra', () => {
  it('renderiza cabecera fija con altitud, tarjetas táctiles y fases lunares en kichwa', () => {
    render(
      <FormularioSiembra
        huerto={huertoMock}
        subhuertos={subhuertosMock}
        materiales={materialesMock}
        plantasDetalle={plantasDetalleMock}
      />
    )

    expect(screen.getByText(/Altitud:\s*2800\s*msnm/i)).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Subhuerto/i })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Lote de bodega/i })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Cantidad y fecha/i })).toBeInTheDocument()

    expect(screen.getByRole('button', { name: /Yana/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Wiñak/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Pura/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Wañuk/i })).toBeInTheDocument()

    expect(screen.getByRole('button', { name: /Confirmar Siembra/i })).toBeInTheDocument()
  })

  it('valida stock insuficiente y muestra error cuando la cantidad supera la disponible', async () => {
    render(
      <FormularioSiembra
        huerto={huertoMock}
        subhuertos={subhuertosMock}
        materiales={materialesMock}
        plantasDetalle={plantasDetalleMock}
      />
    )

    fireEvent.click(screen.getByText('Manzanilla'))

    const inputCantidad = screen.getByLabelText(/Cantidad a sembrar/i)
    fireEvent.change(inputCantidad, { target: { value: '15' } })

    expect(screen.getByText(/Stock insuficiente en bodega/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Confirmar Siembra/i })).toBeDisabled()
  })

  it('muestra split view de escritorio con validacion agronomica y desglose de stock', async () => {
    render(
      <FormularioSiembra
        huerto={huertoMock}
        subhuertos={subhuertosMock}
        materiales={materialesMock}
        plantasDetalle={plantasDetalleMock}
      />
    )

    fireEvent.click(screen.getByText('Manzanilla'))

    const inputCantidad = screen.getByLabelText(/Cantidad a sembrar/i)
    fireEvent.change(inputCantidad, { target: { value: '4' } })

    const panelAgronomico = screen.getByTestId('panel-agronomico')
    expect(panelAgronomico).toBeInTheDocument()

    expect(within(panelAgronomico).getByText(/Stock disponible: 10/i)).toBeInTheDocument()
    expect(within(panelAgronomico).getByText(/A sembrar: 4/i)).toBeInTheDocument()
    expect(within(panelAgronomico).getByText(/Saldo restante: 6/i)).toBeInTheDocument()

    expect(within(panelAgronomico).getByText(/CALIENTE/i)).toBeInTheDocument()
    expect(within(panelAgronomico).getByText(/Rango óptimo: 2000 - 3200 msnm/i)).toBeInTheDocument()
  })

  it('activa alerta de altitud con Kinti y dialogo cuando la altitud del huerto esta fuera del rango optimo', async () => {
    const alRegistrar = vi.fn().mockResolvedValue({
      id: 99,
      subhuertoId: 1,
      nombreSubhuerto: 'Zona Huerto Principal',
      bodegaMaterialId: 102,
      plantaId: 202,
      nombreComunPlanta: 'Cedrón',
      cualidadTermica: 'FRIA',
      cantidadSembrada: 2,
      fechaSiembra: '2026-11-12',
      faseLunarSiembra: 'WINAK_KILLA_CRECIENTE',
      tuvoAlertaAltitud: true,
      estadoCultivo: 'SEMBRADO',
    } as SiembraDetalle)

    render(
      <FormularioSiembra
        huerto={huertoMock}
        subhuertos={subhuertosMock}
        materiales={materialesMock}
        plantasDetalle={plantasDetalleMock}
        alRegistrar={alRegistrar}
      />
    )

    fireEvent.click(screen.getByText('Zona Huerto Principal'))
    fireEvent.click(screen.getByText('Cedrón'))

    const inputCantidad = screen.getByLabelText(/Cantidad a sembrar/i)
    fireEvent.change(inputCantidad, { target: { value: '2' } })

    fireEvent.click(screen.getByRole('button', { name: /Wiñak/i }))

    fireEvent.click(screen.getByRole('button', { name: /Confirmar Siembra/i }))

    const alertaDialogo = await screen.findByRole('dialog')
    expect(alertaDialogo).toBeInTheDocument()
    expect(within(alertaDialogo).getByText(/Alerta de Altitud/i)).toBeInTheDocument()
    expect(
      within(alertaDialogo).getByText(/se encuentra fuera del rango óptimo/i)
    ).toBeInTheDocument()

    const botonAceptarRiesgo = within(alertaDialogo).getByRole('button', {
      name: /Sembrar de todos modos/i,
    })
    fireEvent.click(botonAceptarRiesgo)

    await waitFor(() => {
      expect(alRegistrar).toHaveBeenCalledWith(
        expect.objectContaining({
          subhuertoId: 1,
          bodegaMaterialId: 102,
          cantidadSembrada: 2,
          faseLunarSiembra: 'WINAK_KILLA_CRECIENTE',
          aceptaRiesgoAltitud: true,
        })
      )
    })
  })

  it('registra siembra exitosamente sin alerta si la planta es apta para la altitud', async () => {
    const alRegistrar = vi.fn().mockResolvedValue({
      id: 50,
      subhuertoId: 1,
      nombreSubhuerto: 'Zona Huerto Principal',
      bodegaMaterialId: 101,
      plantaId: 201,
      nombreComunPlanta: 'Manzanilla',
      cualidadTermica: 'CALIENTE',
      cantidadSembrada: 3,
      fechaSiembra: '2026-11-12',
      faseLunarSiembra: 'PURA_KILLA_LLENA',
      tuvoAlertaAltitud: false,
      estadoCultivo: 'SEMBRADO',
    } as SiembraDetalle)

    render(
      <FormularioSiembra
        huerto={huertoMock}
        subhuertos={subhuertosMock}
        materiales={materialesMock}
        plantasDetalle={plantasDetalleMock}
        alRegistrar={alRegistrar}
      />
    )

    fireEvent.click(screen.getByText('Zona Huerto Principal'))
    fireEvent.click(screen.getByText('Manzanilla'))

    const inputCantidad = screen.getByLabelText(/Cantidad a sembrar/i)
    fireEvent.change(inputCantidad, { target: { value: '3' } })

    fireEvent.click(screen.getByRole('button', { name: /Pura/i }))

    fireEvent.click(screen.getByRole('button', { name: /Confirmar Siembra/i }))

    await waitFor(() => {
      expect(alRegistrar).toHaveBeenCalledWith(
        expect.objectContaining({
          subhuertoId: 1,
          bodegaMaterialId: 101,
          cantidadSembrada: 3,
          faseLunarSiembra: 'PURA_KILLA_LLENA',
          aceptaRiesgoAltitud: false,
        })
      )
    })
  })
})
