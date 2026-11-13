import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { CrearTalentoForm } from './CrearTalentoForm'
import { mockActividadesParaTalentos } from './talentosMockHandlers'

describe('CrearTalentoForm', () => {
  it('renderiza todos los campos del formulario, previsualización y mensaje de Kinti', async () => {
    render(<CrearTalentoForm actividades={mockActividadesParaTalentos} />)

    expect(screen.getByRole('heading', { name: /definir nueva medalla o talento/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/actividad asociada/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre de la insignia o talento/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/meta de repeticiones/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/descripción del reconocimiento/i)).toBeInTheDocument()
    expect(screen.getByText(/previsualización de medalla/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /registrar medalla de comunidad/i })).toBeInTheDocument()
    expect(
      screen.getByText(/¡Fomentar la participación comunitaria es grandioso! Define medallas de talento/i),
    ).toBeInTheDocument()
  })

  it('bloquea el envío si la meta de repeticiones es menor o igual a cero', async () => {
    const usuario = userEvent.setup()
    render(<CrearTalentoForm actividades={mockActividadesParaTalentos} />)

    await usuario.selectOptions(screen.getByLabelText(/actividad asociada/i), '2')
    await usuario.type(screen.getByLabelText(/nombre de la insignia o talento/i), 'Guardián del Riego 1')
    await usuario.clear(screen.getByLabelText(/meta de repeticiones/i))
    await usuario.type(screen.getByLabelText(/meta de repeticiones/i), '0')
    await usuario.type(screen.getByLabelText(/descripción del reconocimiento/i), 'Descripción de prueba')

    await usuario.click(screen.getByRole('button', { name: /registrar medalla de comunidad/i }))

    expect(
      await screen.findByText('La meta de repeticiones debe ser un número entero mayor a 0'),
    ).toBeInTheDocument()
  })

  it('bloquea el envío si el nombre de la insignia tiene caracteres especiales no permitidos', async () => {
    const usuario = userEvent.setup()
    render(<CrearTalentoForm actividades={mockActividadesParaTalentos} />)

    await usuario.selectOptions(screen.getByLabelText(/actividad asociada/i), '2')
    await usuario.type(screen.getByLabelText(/nombre de la insignia o talento/i), 'Insignia @ # $')
    await usuario.type(screen.getByLabelText(/meta de repeticiones/i), '10')
    await usuario.type(screen.getByLabelText(/descripción del reconocimiento/i), 'Descripción de prueba')

    await usuario.click(screen.getByRole('button', { name: /registrar medalla de comunidad/i }))

    expect(
      await screen.findByText('El nombre de la insignia solo puede contener letras, números y espacios'),
    ).toBeInTheDocument()
  })

  it('actualiza la previsualización de la medalla de manera dinámica al escribir el nombre', async () => {
    const usuario = userEvent.setup()
    render(<CrearTalentoForm actividades={mockActividadesParaTalentos} />)

    const inputNombre = screen.getByLabelText(/nombre de la insignia o talento/i)
    await usuario.type(inputNombre, 'Sembrador Ancestral 1')

    const previsualizacion = screen.getByTestId('previsualizacion-medalla')
    expect(previsualizacion).toHaveTextContent('Sembrador Ancestral 1')
  })

  it('registra exitosamente la medalla, limpia el formulario y muestra notificación de éxito', async () => {
    const usuario = userEvent.setup()
    const alExito = vi.fn()

    render(
      <CrearTalentoForm
        actividades={mockActividadesParaTalentos}
        alExito={alExito}
      />,
    )

    await usuario.selectOptions(screen.getByLabelText(/actividad asociada/i), '2')
    await usuario.type(screen.getByLabelText(/nombre de la insignia o talento/i), 'Sembrador Constante 2')
    await usuario.type(screen.getByLabelText(/meta de repeticiones/i), '30')
    await usuario.type(screen.getByLabelText(/descripción del reconocimiento/i), 'Completó 30 labores')

    await usuario.click(screen.getByRole('button', { name: /registrar medalla de comunidad/i }))

    await waitFor(() => {
      expect(
        screen.getByText(
          'Medalla registrada con éxito. ¡Tus miembros ya pueden comenzar a acumular repeticiones para obtenerla!',
        ),
      ).toBeInTheDocument()
    })

    expect(alExito).toHaveBeenCalledTimes(1)
    expect(screen.getByLabelText(/nombre de la insignia o talento/i)).toHaveValue('')
  })

  it('muestra notificación de error y conserva datos cuando el nombre de la medalla ya existe', async () => {
    const usuario = userEvent.setup()
    render(<CrearTalentoForm actividades={mockActividadesParaTalentos} />)

    await usuario.selectOptions(screen.getByLabelText(/actividad asociada/i), '2')
    await usuario.type(screen.getByLabelText(/nombre de la insignia o talento/i), 'Talento Duplicado')
    await usuario.type(screen.getByLabelText(/meta de repeticiones/i), '50')
    await usuario.type(screen.getByLabelText(/descripción del reconocimiento/i), 'Descripción de prueba')

    await usuario.click(screen.getByRole('button', { name: /registrar medalla de comunidad/i }))

    expect(
      await screen.findByText('Ya existe un talento o medalla registrada con este nombre en tu comunidad'),
    ).toBeInTheDocument()

    expect(screen.getByLabelText(/nombre de la insignia o talento/i)).toHaveValue('Talento Duplicado')
    expect(screen.getByLabelText(/descripción del reconocimiento/i)).toHaveValue('Descripción de prueba')
  })
})
