import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { CatalogoActividadesView } from './CatalogoActividadesView'
import type { ActividadCatalogo } from './actividadesCatalogoApi'

const mockActividades: ActividadCatalogo[] = [
  {
    id: 1,
    nombre: 'Riego matutino de eras',
    categoria: 'RIEGO',
    puntosPorHora: 1,
    activa: true,
  },
  {
    id: 2,
    nombre: 'Deshierbe manual',
    categoria: 'LIMPIEZA_DESHIERBE',
    puntosPorHora: 1,
    activa: true,
  },
]

describe('CatalogoActividadesView', () => {
  it('muestra tarjetas en móvil, tabla en escritorio y permite crear actividad', async () => {
    const alCrear = vi.fn().mockResolvedValue({
      id: 3,
      nombre: 'Aplicación de compost',
      categoria: 'ABONO_COMPOST',
      puntosPorHora: 1,
      activa: true,
    })

    render(<CatalogoActividadesView actividadesIniciales={mockActividades} alCrear={alCrear} />)

    expect(screen.getByRole('heading', { name: /Catálogo de Labores Comunitarias/i })).toBeInTheDocument()
    expect(screen.getByRole('table')).toBeInTheDocument()
    expect(screen.getAllByText('Riego matutino de eras').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('1 pt / hora').length).toBeGreaterThanOrEqual(2)

    fireEvent.click(screen.getByRole('button', { name: /\+ Nueva Actividad Cotidiana/i }))

    const dialogo = screen.getByRole('dialog')
    const inputNombre = within(dialogo).getByLabelText(/Nombre de la actividad/i)
    fireEvent.change(inputNombre, { target: { value: 'Aplicación de compost' } })
    fireEvent.click(within(dialogo).getByRole('button', { name: /Abono y Compost/i }))
    fireEvent.click(within(dialogo).getByRole('button', { name: /Guardar Actividad/i }))

    await waitFor(() => {
      expect(alCrear).toHaveBeenCalledWith({
        nombre: 'Aplicación de compost',
        categoria: 'ABONO_COMPOST',
      })
    })
  })

  it('mantiene el diálogo abierto y muestra error cuando el nombre está duplicado (409)', async () => {
    const alCrear = vi
      .fn()
      .mockRejectedValue(new Error('Ya existe una actividad con este nombre en tu comunidad'))

    render(<CatalogoActividadesView actividadesIniciales={mockActividades} alCrear={alCrear} />)

    fireEvent.click(screen.getByRole('button', { name: /\+ Nueva Actividad Cotidiana/i }))
    const dialogo = screen.getByRole('dialog')
    fireEvent.change(within(dialogo).getByLabelText(/Nombre de la actividad/i), {
      target: { value: 'Riego matutino de eras' },
    })
    fireEvent.click(within(dialogo).getByRole('button', { name: /Guardar Actividad/i }))

    await waitFor(() => {
      expect(
        screen.getAllByText('Ya existe una actividad con este nombre en tu comunidad').length
      ).toBeGreaterThanOrEqual(1)
    })
  })
})