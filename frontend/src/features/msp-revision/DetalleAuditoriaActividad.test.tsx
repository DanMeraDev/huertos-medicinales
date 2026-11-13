import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { DetalleAuditoriaActividad } from './DetalleAuditoriaActividad'
import { verificarActividad, type ActividadRevision } from './mspRevisionApi'

vi.mock('./mspRevisionApi', () => ({
  verificarActividad: vi.fn(),
}))

const mockVerificarActividad = vi.mocked(verificarActividad)

const actividad: ActividadRevision = {
  id: 21,
  comunidadId: 3,
  nombreComunidad: 'Huerto Comunitario Allpa Viva',
  tituloSaber: 'Ritual de siembra',
  fechaRealizacion: '2026-11-10',
  faseLunarEjecucion: 'PURA_KILLA_LLENA',
  descripcionBitacora: 'Ritual de siembra con chicha de jora y sahumerio',
  evidenciaFotoUrl: 'actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
  evidenciaVideoUrl: 'https://evidencias.gob.ec/video-ritual.mp4',
  numeroIntento: 1,
  estado: 'PENDIENTE',
  participantes: [
    { miembroId: 7, nombreCompleto: 'María Tránsito Amaguaña Alba', horasTrabajadas: 4 },
  ],
}

function renderDetalle(alVerificar = vi.fn(), alCerrar = vi.fn()) {
  render(
    <DetalleAuditoriaActividad
      actividad={actividad}
      alVerificar={alVerificar}
      alCerrar={alCerrar}
    />,
  )
}

describe('DetalleAuditoriaActividad', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockVerificarActividad.mockResolvedValue({
      actividadId: 21,
      estado: 'APROBADA',
      puntosComunidadAcreditados: 20,
      fechaVerificacion: '2026-11-12T15:00:00',
    })
  })

  it('muestra la fotografía, la bitácora y la nómina de participantes', () => {
    renderDetalle()

    const imagen = screen.getByRole('img', { name: /evidencia fotográfica de ritual de siembra/i })
    expect(imagen).toHaveAttribute(
      'src',
      '/api/publico/fotos/actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg',
    )
    expect(screen.getByText(/ritual de siembra con chicha de jora y sahumerio/i)).toBeInTheDocument()
    expect(screen.getByText(/maría tránsito amaguaña alba · 4 horas/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver el video de la evidencia/i })).toHaveAttribute(
      'href',
      'https://evidencias.gob.ec/video-ritual.mp4',
    )
  })

  it('exige la observación detallada cuando el dictamen es observar para corrección', async () => {
    const user = userEvent.setup()
    renderDetalle()

    await user.selectOptions(screen.getByLabelText(/dictamen/i), 'OBSERVADA_CORREGIR')
    await user.click(screen.getByRole('button', { name: /emitir dictamen/i }))

    expect(
      await screen.findByText(/debe ingresar una observación detallada para justificar la observación o rechazo/i),
    ).toBeInTheDocument()
    expect(mockVerificarActividad).not.toHaveBeenCalled()
  })

  it('aprueba la actividad y notifica los puntos acreditados a la comunidad', async () => {
    const user = userEvent.setup()
    const alVerificar = vi.fn()
    renderDetalle(alVerificar)

    await user.click(screen.getByRole('button', { name: /emitir dictamen/i }))

    expect(mockVerificarActividad).toHaveBeenCalledWith(21, {
      nuevoEstado: 'APROBADA',
      observacion: null,
    })
    expect(await screen.findByText(/se acreditaron 20 puntos a la comunidad/i)).toBeInTheDocument()
    expect(alVerificar).toHaveBeenCalled()
  })

  it('rechaza definitivamente con la justificación escrita por el técnico', async () => {
    const user = userEvent.setup()
    mockVerificarActividad.mockResolvedValue({
      actividadId: 21,
      estado: 'RECHAZADA_DEFINITIVA',
      puntosComunidadAcreditados: 0,
      fechaVerificacion: '2026-11-12T15:00:00',
    })
    renderDetalle()

    await user.selectOptions(screen.getByLabelText(/dictamen/i), 'RECHAZADA_DEFINITIVA')
    await user.type(
      screen.getByLabelText(/observación/i),
      'La evidencia no cumple los criterios agroecológicos',
    )
    await user.click(screen.getByRole('button', { name: /emitir dictamen/i }))

    expect(mockVerificarActividad).toHaveBeenCalledWith(21, {
      nuevoEstado: 'RECHAZADA_DEFINITIVA',
      observacion: 'La evidencia no cumple los criterios agroecológicos',
    })
    expect(await screen.findByText(/actividad rechazada definitivamente/i)).toBeInTheDocument()
  })

  it('muestra el error del back cuando la actividad ya fue dictaminada', async () => {
    const user = userEvent.setup()
    mockVerificarActividad.mockRejectedValue(
      new Error('La actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente'),
    )
    renderDetalle()

    await user.click(screen.getByRole('button', { name: /emitir dictamen/i }))

    expect(
      await screen.findByText(
        /la actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente/i,
      ),
    ).toBeInTheDocument()
  })
})
