import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { FormularioIngresoMaterial } from './FormularioIngresoMaterial'
import type { PlantaResumen } from './types'

const PLANTAS_PRUEBA: PlantaResumen[] = [
  { id: 1, nombreComun: 'Manzanilla' },
  { id: 2, nombreComun: 'Menta' },
]

describe('FormularioIngresoMaterial', () => {
  it('renderiza todos los campos obligatorios y el botón de acción', () => {
    render(<FormularioIngresoMaterial plantasDisponibles={PLANTAS_PRUEBA} />)

    expect(screen.getByAltText('Kinti Guía en estado de alerta')).toHaveAttribute(
      'src',
      '/kinti/kinti-alerta.svg',
    )
    expect(
      screen.getByText(/¡qué alegría que haya llegado más material para el cultivo!/i),
    ).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Ingreso de Material Vegetal' })).toBeInTheDocument()
    expect(screen.getByLabelText(/planta del catálogo/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/tipo de material/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/cantidad ingresada/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/unidad de medida/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/lote u origen/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de ingreso/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Registrar Ingreso' })).toBeInTheDocument()
  })

  it('carga el catálogo de plantas desde la API pública si no se pasan plantasDisponibles', async () => {
    render(<FormularioIngresoMaterial />)

    expect(await screen.findByRole('option', { name: 'Manzanilla' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Menta' })).toBeInTheDocument()
  })

  it('muestra notificación de error cuando falla la carga del catálogo de plantas', async () => {
    server.use(
      http.get('*/api/publico/plantas', () => {
        return HttpResponse.json({ detail: 'Error en servicio de catálogo' }, { status: 500 })
      }),
    )

    render(<FormularioIngresoMaterial />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Error en servicio de catálogo')
  })

  it('valida que la cantidad sea obligatoria y mayor a cero', async () => {
    render(<FormularioIngresoMaterial plantasDisponibles={PLANTAS_PRUEBA} />)

    await userEvent.selectOptions(screen.getByLabelText(/planta del catálogo/i), '1')
    await userEvent.selectOptions(screen.getByLabelText(/tipo de material/i), 'SEMILLAS')
    await userEvent.clear(screen.getByLabelText(/cantidad ingresada/i))
    await userEvent.type(screen.getByLabelText(/cantidad ingresada/i), '0')
    await userEvent.selectOptions(screen.getByLabelText(/unidad de medida/i), 'GRAMOS')

    await userEvent.click(screen.getByRole('button', { name: 'Registrar Ingreso' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('La cantidad debe ser mayor a cero')
  })

  it('envía los datos correctamente y muestra notificación de éxito', async () => {
    const alTenerExito = vi.fn()
    render(<FormularioIngresoMaterial plantasDisponibles={PLANTAS_PRUEBA} onExito={alTenerExito} />)

    await userEvent.selectOptions(screen.getByLabelText(/planta del catálogo/i), '2')
    await userEvent.selectOptions(screen.getByLabelText(/tipo de material/i), 'ESQUEJES')
    await userEvent.clear(screen.getByLabelText(/cantidad ingresada/i))
    await userEvent.type(screen.getByLabelText(/cantidad ingresada/i), '15.50')
    await userEvent.selectOptions(screen.getByLabelText(/unidad de medida/i), 'UNIDADES')
    await userEvent.type(screen.getByLabelText(/lote u origen/i), 'Donación vivero local')

    await userEvent.click(screen.getByRole('button', { name: 'Registrar Ingreso' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Ingreso de material vegetal registrado con éxito')

    await waitFor(() => {
      expect(alTenerExito).toHaveBeenCalledWith(
        expect.objectContaining({
          plantaId: 2,
          tipoMaterial: 'ESQUEJES',
          cantidadDisponible: 15.5,
          unidadMedida: 'UNIDADES',
        }),
      )
    })
  })

  it('muestra notificación de error cuando la planta no existe en el catálogo (404)', async () => {
    const plantasConInexistente: PlantaResumen[] = [{ id: 999, nombreComun: 'Planta Fantasma' }]
    render(<FormularioIngresoMaterial plantasDisponibles={plantasConInexistente} />)

    await userEvent.selectOptions(screen.getByLabelText(/planta del catálogo/i), '999')
    await userEvent.selectOptions(screen.getByLabelText(/tipo de material/i), 'SEMILLAS')
    await userEvent.clear(screen.getByLabelText(/cantidad ingresada/i))
    await userEvent.type(screen.getByLabelText(/cantidad ingresada/i), '5')
    await userEvent.selectOptions(screen.getByLabelText(/unidad de medida/i), 'GRAMOS')

    await userEvent.click(screen.getByRole('button', { name: 'Registrar Ingreso' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('La planta seleccionada no existe en el catálogo')
  })
})
