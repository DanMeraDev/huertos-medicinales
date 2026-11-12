import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { vi } from 'vitest'
import { server } from '../../test/mocks/server'
import { ModalBajaMaterial } from './ModalBajaMaterial'
import type { MaterialVegetalResponse } from './types'

const MATERIAL_PRUEBA: MaterialVegetalResponse = {
  id: 1,
  comunidadId: 1,
  plantaId: 1,
  nombreComunPlanta: 'Manzanilla',
  nombreCientificoPlanta: 'Matricaria chamomilla',
  tipoMaterial: 'SEMILLAS',
  cantidadDisponible: 50,
  unidadMedida: 'GRAMOS',
  loteOrigen: 'Lote MZ-2026-A',
  fechaIngreso: '2026-11-10',
}

describe('ModalBajaMaterial', () => {
  it('no renderiza nada cuando abierto es false', () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={false}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('renderiza la información del material vegetal en solo lectura y los controles del formulario', () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Dar de Baja Material Vegetal' })).toBeInTheDocument()
    expect(screen.getByText('Manzanilla (Matricaria chamomilla)')).toBeInTheDocument()
    expect(screen.getByText('50 GRAMOS')).toBeInTheDocument()
    expect(screen.getByLabelText(/cantidad a dar de baja/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/causa del fracaso/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/observaciones \/ detalle/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Confirmar Baja' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument()
  })

  it('valida que la cantidad no sea menor o igual a cero', async () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.type(screen.getByLabelText(/cantidad a dar de baja/i), '0')
    await userEvent.selectOptions(screen.getByLabelText(/causa del fracaso/i), 'PLAGA_ENFERMEDAD')
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar Baja' }))

    expect(
      await screen.findByText('La cantidad a dar de baja debe ser mayor a cero'),
    ).toBeInTheDocument()
    expect(alExito).not.toHaveBeenCalled()
  })

  it('valida que la cantidad a dar de baja no supere el stock disponible', async () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.type(screen.getByLabelText(/cantidad a dar de baja/i), '55')
    await userEvent.selectOptions(screen.getByLabelText(/causa del fracaso/i), 'PLAGA_ENFERMEDAD')
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar Baja' }))

    expect(
      await screen.findByText('La cantidad a dar de baja supera el stock disponible en bodega'),
    ).toBeInTheDocument()
    expect(alExito).not.toHaveBeenCalled()
  })

  it('valida que se seleccione una causa de fracaso obligatoria', async () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.type(screen.getByLabelText(/cantidad a dar de baja/i), '10')
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar Baja' }))

    expect(
      await screen.findByText(
        'Debe seleccionar una causa de fracaso válida para el registro de la baja',
      ),
    ).toBeInTheDocument()
    expect(alExito).not.toHaveBeenCalled()
  })

  it('envía los datos correctamente al completar el formulario y llama alExito y alCerrar', async () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.type(screen.getByLabelText(/cantidad a dar de baja/i), '12.5')
    await userEvent.selectOptions(screen.getByLabelText(/causa del fracaso/i), 'PLAGA_ENFERMEDAD')
    await userEvent.type(screen.getByLabelText(/observaciones \/ detalle/i), 'Hongos en las hojas')
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar Baja' }))

    await waitFor(() => {
      expect(alExito).toHaveBeenCalledWith(
        expect.objectContaining({
          bodegaMaterialId: 1,
          cantidadBaja: 12.5,
          causaFracaso: 'PLAGA_ENFERMEDAD',
          observacion: 'Hongos en las hojas',
        }),
      )
    })
    expect(alCerrar).toHaveBeenCalled()
  })

  it('muestra notificación de error si el servidor responde con error', async () => {
    server.use(
      http.post('*/api/bodega/material-vegetal/:id/bajas', () => {
        return HttpResponse.json(
          { detail: 'Error interno en bodega comunitaria' },
          { status: 500 },
        )
      }),
    )

    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.type(screen.getByLabelText(/cantidad a dar de baja/i), '10')
    await userEvent.selectOptions(screen.getByLabelText(/causa del fracaso/i), 'SEQUIA_FALTA_RIEGO')
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar Baja' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Error interno en bodega comunitaria')
    expect(alExito).not.toHaveBeenCalled()
    expect(alCerrar).not.toHaveBeenCalled()
  })

  it('llama a alCerrar al hacer clic en Cancelar', async () => {
    const alCerrar = vi.fn()
    const alExito = vi.fn()

    render(
      <ModalBajaMaterial
        abierto={true}
        material={MATERIAL_PRUEBA}
        alCerrar={alCerrar}
        alExito={alExito}
      />,
    )

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(alCerrar).toHaveBeenCalledTimes(1)
  })
})

