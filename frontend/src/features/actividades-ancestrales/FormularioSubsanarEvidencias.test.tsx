import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FormularioSubsanarEvidencias } from './FormularioSubsanarEvidencias'
import {
  buscarActividadParaCorregir,
  reenviarEvidencias,
  subirFotografiaActividad,
} from './actividadesAncestralesApi'

vi.mock('./actividadesAncestralesApi', () => ({
  buscarActividadParaCorregir: vi.fn(),
  reenviarEvidencias: vi.fn(),
  subirFotografiaActividad: vi.fn(),
}))

const mockBuscarActividad = vi.mocked(buscarActividadParaCorregir)
const mockReenviarEvidencias = vi.mocked(reenviarEvidencias)
const mockSubirFoto = vi.mocked(subirFotografiaActividad)

describe('FormularioSubsanarEvidencias', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockBuscarActividad.mockResolvedValue({
      id: 21,
      tituloSaber: 'Ritual de siembra',
      estado: 'OBSERVADA_CORREGIR',
      numeroIntento: 1,
      observacionMsp: 'La fotografía no muestra el ritual completo',
      descripcionBitacora: 'Ritual de siembra con chicha de jora',
      evidenciaFotoUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
      evidenciaVideoUrl: null,
    })
    mockSubirFoto.mockResolvedValue({
      fotografiaUrl: 'actividades/9a8b7c6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d.jpg',
    })
    mockReenviarEvidencias.mockResolvedValue({
      id: 21,
      comunidadId: 3,
      tituloSaber: 'Ritual de siembra',
      estado: 'PENDIENTE',
      numeroIntento: 2,
      fechaRealizacion: '2026-11-10',
      participantes: [],
    })
  })

  it('muestra la observación del Técnico MSP y el número de intento', async () => {
    render(<FormularioSubsanarEvidencias idActividad={21} alReenviar={vi.fn()} />)

    expect(await screen.findByText(/la fotografía no muestra el ritual completo/i)).toBeInTheDocument()
    expect(screen.getByText(/intento 1/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/descripción de la bitácora/i)).toHaveValue(
      'Ritual de siembra con chicha de jora',
    )
  })

  it('reenvía las evidencias corregidas con la bitácora y la fotografía actualizadas', async () => {
    const user = userEvent.setup()
    const alReenviar = vi.fn()
    render(<FormularioSubsanarEvidencias idActividad={21} alReenviar={alReenviar} />)

    await screen.findByText(/la fotografía no muestra el ritual completo/i)

    const bitacora = screen.getByLabelText(/descripción de la bitácora/i)
    await user.clear(bitacora)
    await user.type(bitacora, 'Ritual de siembra con chicha de jora y sahumerio')
    await user.type(screen.getByLabelText(/evidencia de video/i), 'https://evidencias.gob.ec/video.mp4')

    await user.click(screen.getByRole('button', { name: /reenviar evidencias corregidas/i }))

    expect(mockReenviarEvidencias).toHaveBeenCalledWith(21, {
      descripcionBitacora: 'Ritual de siembra con chicha de jora y sahumerio',
      evidenciaFotoUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
      evidenciaVideoUrl: 'https://evidencias.gob.ec/video.mp4',
    })
    expect(alReenviar).toHaveBeenCalled()
  })

  it('reemplaza la fotografía de evidencia antes de reenviar', async () => {
    const user = userEvent.setup()
    render(<FormularioSubsanarEvidencias idActividad={21} alReenviar={vi.fn()} />)

    await screen.findByText(/la fotografía no muestra el ritual completo/i)

    await user.upload(screen.getByLabelText(/evidencia fotográfica/i), [
      new File([new ArrayBuffer(10)], 'foto-corregida.jpg', { type: 'image/jpeg' }),
    ])
    await user.click(screen.getByRole('button', { name: /reemplazar fotografía/i }))

    expect(await screen.findByText(/fotografía subida correctamente/i)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reenviar evidencias corregidas/i }))

    expect(mockReenviarEvidencias).toHaveBeenCalledWith(
      21,
      expect.objectContaining({
        evidenciaFotoUrl: 'actividades/9a8b7c6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d.jpg',
      }),
    )
  })

  it('valida la bitácora y la fotografía obligatorias', async () => {
    const user = userEvent.setup()
    render(<FormularioSubsanarEvidencias idActividad={21} alReenviar={vi.fn()} />)

    await screen.findByText(/la fotografía no muestra el ritual completo/i)

    await user.clear(screen.getByLabelText(/descripción de la bitácora/i))
    await user.click(screen.getByRole('button', { name: /reenviar evidencias corregidas/i }))

    expect(await screen.findByText(/la descripción de la bitácora es obligatoria/i)).toBeInTheDocument()
    expect(mockReenviarEvidencias).not.toHaveBeenCalled()
  })

  it('muestra el error del back cuando la actividad ya no está observada', async () => {
    const user = userEvent.setup()
    mockReenviarEvidencias.mockRejectedValue(
      new Error('Solo es posible reenviar evidencias para actividades en estado OBSERVADA_CORREGIR'),
    )
    render(<FormularioSubsanarEvidencias idActividad={21} alReenviar={vi.fn()} />)

    await screen.findByText(/la fotografía no muestra el ritual completo/i)

    await user.click(screen.getByRole('button', { name: /reenviar evidencias corregidas/i }))

    expect(
      await screen.findByText(
        /solo es posible reenviar evidencias para actividades en estado observada_corregir/i,
      ),
    ).toBeInTheDocument()
  })
})
