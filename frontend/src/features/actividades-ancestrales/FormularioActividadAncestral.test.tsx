import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FormularioActividadAncestral } from './FormularioActividadAncestral'
import {
  listarSaberesAncestrales,
  listarMiembrosComunidad,
  subirFotografiaActividad,
  registrarActividadAncestral,
} from './actividadesAncestralesApi'

vi.mock('./actividadesAncestralesApi', () => ({
  listarSaberesAncestrales: vi.fn(),
  listarMiembrosComunidad: vi.fn(),
  subirFotografiaActividad: vi.fn(),
  registrarActividadAncestral: vi.fn(),
}))

const mockListarSaberes = vi.mocked(listarSaberesAncestrales)
const mockListarMiembros = vi.mocked(listarMiembrosComunidad)
const mockSubirFoto = vi.mocked(subirFotografiaActividad)
const mockRegistrarActividad = vi.mocked(registrarActividadAncestral)

describe('FormularioActividadAncestral', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockListarSaberes.mockResolvedValue([
      { id: 3, ordenPaso: 3, titulo: 'Ritual de siembra' },
    ])
    mockListarMiembros.mockResolvedValue([
      { id: 7, nombreCompleto: 'María Tránsito Amaguaña Alba' },
      { id: 8, nombreCompleto: 'Segundo Quishpe Cueva' },
    ])
    mockSubirFoto.mockResolvedValue({
      fotografiaUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
    })
    mockRegistrarActividad.mockResolvedValue({
      id: 21,
      comunidadId: 3,
      tituloSaber: 'Ritual de siembra',
      estado: 'PENDIENTE',
      numeroIntento: 1,
      fechaRealizacion: '2026-11-10',
      participantes: [],
    })
  })

  it('renderiza todos los campos del formulario, incluido el input de foto y el botón de envío', async () => {
    render(<FormularioActividadAncestral />)

    expect(await screen.findByLabelText(/hito \/ saber ancestral/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de realización/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fase lunar de ejecución/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/descripción de la bitácora/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/evidencia fotográfica/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/evidencia de video/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/miembro voluntario/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/horas trabajadas/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /agregar participante/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /subir fotografía/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /enviar a revisión msp/i })).toBeInTheDocument()
  })

  it('valida los campos obligatorios, incluida la fotografía sin subir', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByText(/el saber ancestral es obligatorio/i)).toBeInTheDocument()
    expect(screen.getByText(/la fecha de realización es obligatoria/i)).toBeInTheDocument()
    expect(screen.getByText(/la fase lunar de ejecución es obligatoria/i)).toBeInTheDocument()
    expect(screen.getByText(/la descripción de la bitácora es obligatoria/i)).toBeInTheDocument()
    expect(
      screen.getByText(/la fotografía de evidencia es obligatoria para la revisión del msp/i),
    ).toBeInTheDocument()
    expect(
      screen.getByText(/debe registrar al menos un miembro participante en la actividad ancestral/i),
    ).toBeInTheDocument()
  })

  it('sube la fotografía, agrega participantes y envía la petición al back', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await screen.findByText(/ritual de siembra/i)
    await screen.findByText(/maría tránsito amaguaña alba/i)

    await user.selectOptions(screen.getByLabelText(/hito \/ saber ancestral/i), '3')
    await user.type(screen.getByLabelText(/fecha de realización/i), '2026-11-10')
    await user.selectOptions(screen.getByLabelText(/fase lunar de ejecución/i), 'PURA_KILLA_LLENA')
    await user.type(screen.getByLabelText(/descripción de la bitácora/i), 'Ritual de siembra con chicha de jora')

    const inputFoto = screen.getByLabelText(/evidencia fotográfica/i)
    await user.upload(inputFoto, [
      new File([new ArrayBuffer(10)], 'foto.jpg', { type: 'image/jpeg' }),
    ])

    await user.click(screen.getByRole('button', { name: /subir fotografía/i }))

    await screen.findByText(/fotografía subida correctamente/i)

    await user.selectOptions(screen.getByLabelText(/miembro voluntario/i), '7')
    await user.type(screen.getByLabelText(/horas trabajadas/i), '4')
    await user.click(screen.getByRole('button', { name: /agregar participante/i }))

    expect(await screen.findByRole('cell', { name: /maría tránsito amaguaña alba/i })).toBeInTheDocument()
    expect(screen.getByRole('cell', { name: '4' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      /¡actividad ancestral registrada! quedó en revisión del técnico msp/i,
    )
    expect(screen.getByLabelText(/fecha de realización/i)).toHaveValue('')
  })

  it('no permite enviar sin subir la fotografía', async () => {
    const user = userEvent.setup()
    render(<FormularioActividadAncestral />)

    await screen.findByText(/ritual de siembra/i)

    await user.selectOptions(screen.getByLabelText(/hito \/ saber ancestral/i), '3')
    await user.type(screen.getByLabelText(/fecha de realización/i), '2026-11-10')
    await user.selectOptions(screen.getByLabelText(/fase lunar de ejecución/i), 'PURA_KILLA_LLENA')
    await user.type(screen.getByLabelText(/descripción de la bitácora/i), 'Ritual de siembra con chicha de jora')
    await user.type(screen.getByLabelText(/evidencia de video/i), 'https://evidencias.gob.ec/video.mp4')

    await user.selectOptions(screen.getByLabelText(/miembro voluntario/i), '7')
    await user.type(screen.getByLabelText(/horas trabajadas/i), '4')
    await user.click(screen.getByRole('button', { name: /agregar participante/i }))

    await user.click(screen.getByRole('button', { name: /enviar a revisión msp/i }))

    expect(await screen.findByText(/la fotografía de evidencia es obligatoria para la revisión del msp/i)).toBeInTheDocument()
  })
})
