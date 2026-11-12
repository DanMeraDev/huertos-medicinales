import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { FormularioRegistroLabor } from './FormularioRegistroLabor'
import type {
  ActividadComunitaria,
  InsigniaMiembro,
  MiembroVoluntario,
  ResultadoAcreditacionMiembroResponse,
} from './registroLaboresApi'

const miembrosMock: MiembroVoluntario[] = [
  {
    id: 1,
    cedula: '1712345678',
    nombreCompleto: 'Rosa Elena Quispe',
    nombres: 'Rosa Elena',
    apellidos: 'Quispe',
    nivel: 'APRENDIZ',
    puntosTotales: 46,
    activoEnComunidad: true,
  },
  {
    id: 2,
    cedula: '1798765432',
    nombreCompleto: 'Juan Carlos Toapanta',
    nombres: 'Juan Carlos',
    apellidos: 'Toapanta',
    nivel: 'OFICIAL',
    puntosTotales: 80,
    activoEnComunidad: true,
  },
]

const actividadesMock: ActividadComunitaria[] = [
  {
    id: 10,
    nombre: 'Deshierbe y aporque',
    categoria: 'LIMPIEZA_DESHIERBE',
    puntosPorHora: 1,
    activa: true,
  },
  {
    id: 20,
    nombre: 'Riego por goteo',
    categoria: 'RIEGO',
    puntosPorHora: 1,
    activa: true,
  },
]

const insigniasMock: InsigniaMiembro[] = [
  {
    id: 100,
    codigo: 'TALENTO_RIEGO_1',
    nombre: 'Guardián del Agua',
    tipo: 'TALENTO_COMUNIDAD',
    descripcion: 'Realizó 5 jornadas de riego',
    fechaObtencion: '2026-11-01',
  },
]

describe('FormularioRegistroLabor', () => {
  it('renderiza tarjetas, botones rapidos de horas y campos requeridos', () => {
    render(
      <FormularioRegistroLabor
        miembros={miembrosMock}
        actividades={actividadesMock}
        alObtenerInsignias={vi.fn().mockResolvedValue([])}
        alRegistrar={vi.fn()}
      />,
    )

    expect(screen.getByRole('heading', { name: /Registrar Jornada de Voluntario/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/Voluntario/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Labor Comunitaria/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Fecha de la labor/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Horas trabajadas/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '1h' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '2h' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '4h' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '8h' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Acreditar Puntos al Miembro/i })).toBeInTheDocument()
  })

  it('al seleccionar voluntario y labor proyecta puntos dinamicamente y muestra ficha motivacional', async () => {
    const alObtenerInsignias = vi.fn().mockResolvedValue(insigniasMock)

    render(
      <FormularioRegistroLabor
        miembros={miembrosMock}
        actividades={actividadesMock}
        alObtenerInsignias={alObtenerInsignias}
        alRegistrar={vi.fn()}
      />,
    )

    fireEvent.change(screen.getByLabelText(/Voluntario/i), { target: { value: '2' } })
    fireEvent.change(screen.getByLabelText(/Labor Comunitaria/i), { target: { value: '10' } })

    await waitFor(() => {
      expect(alObtenerInsignias).toHaveBeenCalledWith(2)
    })

    expect(screen.getAllByText(/Juan Carlos Toapanta/).length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText(/1798765432/).length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('OFICIAL').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('Guardián del Agua')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: '4h' }))

    expect(screen.getByText(/Puntos actuales: 80/i)).toBeInTheDocument()
    expect(screen.getByText(/Tras esta labor: 84/i)).toBeInTheDocument()
    expect(screen.getByText(/\(\+4 pts\)/i)).toBeInTheDocument()
  })

  it('muestra advertencia y no permite enviar si la jornada excede 24 horas', async () => {
    const alRegistrar = vi.fn()

    render(
      <FormularioRegistroLabor
        miembros={miembrosMock}
        actividades={actividadesMock}
        alObtenerInsignias={vi.fn().mockResolvedValue([])}
        alRegistrar={alRegistrar}
      />,
    )

    fireEvent.change(screen.getByLabelText(/Voluntario/i), { target: { value: '1' } })
    fireEvent.change(screen.getByLabelText(/Labor Comunitaria/i), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/Horas trabajadas/i), { target: { value: '25' } })

    expect(screen.getByText(/La jornada no puede exceder las 24 horas continuas/i)).toBeInTheDocument()

    const botonEnviar = screen.getByRole('button', { name: /Acreditar Puntos al Miembro/i })
    expect(botonEnviar).toBeDisabled()
  })

  it('muestra advertencia y no permite enviar si la fecha es futura', async () => {
    const alRegistrar = vi.fn()

    render(
      <FormularioRegistroLabor
        miembros={miembrosMock}
        actividades={actividadesMock}
        alObtenerInsignias={vi.fn().mockResolvedValue([])}
        alRegistrar={alRegistrar}
      />,
    )

    fireEvent.change(screen.getByLabelText(/Voluntario/i), { target: { value: '1' } })
    fireEvent.change(screen.getByLabelText(/Labor Comunitaria/i), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/Fecha de la labor/i), { target: { value: '2099-01-01' } })

    expect(screen.getByText(/La fecha de la labor no puede ser futura/i)).toBeInTheDocument()

    const botonEnviar = screen.getByRole('button', { name: /Acreditar Puntos al Miembro/i })
    expect(botonEnviar).toBeDisabled()
  })

  it('registra labor exitosamente y despliega modal de celebracion con ascenso de nivel', async () => {
    const respuestaMock: ResultadoAcreditacionMiembroResponse = {
      registroId: 101,
      miembroId: 1,
      nombreMiembro: 'Rosa Elena Quispe',
      rangoActual: 'OFICIAL',
      rangoAnterior: 'APRENDIZ',
      subioDeNivel: true,
      puntosAcreditados: 4,
      puntosTotalesActuales: 50,
      nuevosTalentosDesbloqueados: [
        {
          id: 201,
          codigo: 'TALENTO_DESHIERBE_1',
          nombre: 'Manos de Tierra',
          tipo: 'TALENTO_COMUNIDAD',
          descripcion: 'Completó sus primeras labores de deshierbe',
          fechaObtencion: '2026-11-12',
        },
      ],
      mensajeCelebracion: '¡Felicitaciones Rosa Elena Quispe! Has ascendido al rango OFICIAL.',
    }

    const alRegistrar = vi.fn().mockResolvedValue(respuestaMock)

    render(
      <FormularioRegistroLabor
        miembros={miembrosMock}
        actividades={actividadesMock}
        alObtenerInsignias={vi.fn().mockResolvedValue([])}
        alRegistrar={alRegistrar}
      />,
    )

    fireEvent.change(screen.getByLabelText(/Voluntario/i), { target: { value: '1' } })
    fireEvent.change(screen.getByLabelText(/Labor Comunitaria/i), { target: { value: '10' } })
    fireEvent.click(screen.getByRole('button', { name: '4h' }))
    fireEvent.change(screen.getByLabelText(/Observaciones/i), { target: { value: 'Excelente labor de deshierbe' } })

    fireEvent.click(screen.getByRole('button', { name: /Acreditar Puntos al Miembro/i }))

    await waitFor(() => {
      expect(alRegistrar).toHaveBeenCalledWith({
        miembroId: 1,
        actividadId: 10,
        horasTrabajadas: 4,
        fechaLabor: expect.any(String),
        observaciones: 'Excelente labor de deshierbe',
      })
    })

    const modal = await screen.findByRole('dialog')
    expect(modal).toBeInTheDocument()
    expect(within(modal).getByText(/¡Reconocimiento Comunitario!/i)).toBeInTheDocument()
    expect(within(modal).getByText(/¡Ascendió al rango OFICIAL!/i)).toBeInTheDocument()
    expect(within(modal).getByText(/Manos de Tierra/i)).toBeInTheDocument()
    expect(within(modal).getByText(/50 pts/i)).toBeInTheDocument()

    fireEvent.click(within(modal).getByRole('button', { name: /Continuar/i }))
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })
  })
})
