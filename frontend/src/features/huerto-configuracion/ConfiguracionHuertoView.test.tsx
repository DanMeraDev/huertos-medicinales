import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'
import { ConfiguracionHuertoView } from './ConfiguracionHuertoView'
import * as huertoApi from './huertoApi'

vi.mock('./huertoApi', () => ({
  obtenerHuertoComunidad: vi.fn(),
  actualizarDatosOperativos: vi.fn(),
}))

const huertoMock: huertoApi.HuertoDetalleResponse = {
  id: 1,
  comunidadId: 3,
  nombreHuerto: 'Huerto Allpa Viva',
  latitud: -0.180653,
  longitud: -78.467834,
  altitudMsnm: 2800,
  orientacion: 'ESTE_OESTE',
  areaTotalM2: 150,
  disenoPrincipal: 'EN_TIERRA',
  tipoSuelo: 'ARCILLOSO',
  fuenteAgua: 'GOTEO',
  tipoAbono: 'COMPOST',
  tipoCerca: 'CERCA_VIVA',
  detalleCerca: 'Cabuya y chilca',
  tipoHuerto: 'BASICO',
  puntosHuerto: 10,
  areaOcupadaSubhuertosM2: 40,
}

describe('ConfiguracionHuertoView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(huertoApi.obtenerHuertoComunidad).mockResolvedValue(huertoMock)
    vi.mocked(huertoApi.actualizarDatosOperativos).mockResolvedValue(huertoMock)
  })

  it('precarga los datos del huerto y renderiza el layout split para escritorio', async () => {
    render(<ConfiguracionHuertoView />)

    expect(await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })).toBeInTheDocument()
    expect(screen.getByText('Nivel Básico')).toBeInTheDocument()
    expect(screen.getByTestId('layout-split-huerto')).toHaveClass('huerto-split')
    expect(screen.getByLabelText('Área Total (m²)')).toHaveValue(150)
  })

  it('actualiza latitud y longitud al marcar otro punto en el mapa del huerto', async () => {
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    fireEvent.click(screen.getByRole('application', { name: 'Mapa para elegir la ubicación' }), {
      clientX: 100,
      clientY: 100,
    })

    await waitFor(() =>
      expect(Number((screen.getByLabelText('Latitud') as HTMLInputElement).value)).toBeCloseTo(-0.1828, 3),
    )
    expect(Number((screen.getByLabelText('Longitud') as HTMLInputElement).value)).toBeCloseTo(-78.4657, 3)
  })

  it('no acepta un punto del mapa fuera de Ecuador', async () => {
    server.use(
      http.get('https://nominatim.openstreetmap.org/reverse', () => HttpResponse.json({ error: 'Unable to geocode' })),
    )
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    fireEvent.click(screen.getByRole('application', { name: 'Mapa para elegir la ubicación' }), {
      clientX: 100,
      clientY: 100,
    })

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ese punto está fuera de Ecuador. Elige un lugar dentro del país.',
    )
    expect(screen.getByLabelText('Latitud')).toHaveValue(-0.180653)
  })

  it('cambia entre las tres pestañas móviles correctamente', async () => {
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    const tabTerreno = screen.getByRole('tab', { name: '1. Terreno y Área' })
    const tabSuelo = screen.getByRole('tab', { name: '2. Suelo y Agua' })
    const tabCerramiento = screen.getByRole('tab', { name: '3. Cerramiento' })

    expect(tabTerreno).toHaveAttribute('aria-selected', 'true')

    await userEvent.click(tabSuelo)
    expect(tabSuelo).toHaveAttribute('aria-selected', 'true')
    expect(tabTerreno).toHaveAttribute('aria-selected', 'false')

    await userEvent.click(tabCerramiento)
    expect(tabCerramiento).toHaveAttribute('aria-selected', 'true')
  })

  it('bloquea el guardado y marca error cuando el área está fuera de norma MSP', async () => {
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    const campoArea = screen.getByLabelText('Área Total (m²)')
    await userEvent.clear(campoArea)
    await userEvent.type(campoArea, '10')

    expect(screen.getByTestId('barra-medidor-msp')).toHaveClass('huerto-medidor-invalido')
    expect(
      screen.getByText('El huerto debe medir entre 16.00 m² y 400.00 m² según lineamientos MSP'),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Guardar Configuración del Huerto' })).toBeDisabled()
  })

  it('bloquea la reducción del área por debajo de la suma de subhuertos', async () => {
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    const campoArea = screen.getByLabelText('Área Total (m²)')
    await userEvent.clear(campoArea)
    await userEvent.type(campoArea, '25')

    expect(
      screen.getByText('No puedes reducir a 25 m² porque tus subhuertos ocupan 40 m²'),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Guardar Configuración del Huerto' })).toBeDisabled()
  })

  it('envía la actualización y muestra la notificación de éxito', async () => {
    render(<ConfiguracionHuertoView />)
    await screen.findByRole('heading', { name: /Huerto Allpa Viva/i })

    await userEvent.click(screen.getByRole('button', { name: /Vertical/i }))
    await userEvent.click(screen.getByRole('button', { name: 'Guardar Configuración del Huerto' }))

    await waitFor(() => {
      expect(huertoApi.actualizarDatosOperativos).toHaveBeenCalledWith(
        expect.objectContaining({
          areaTotalM2: 150,
          disenoPrincipal: 'VERTICAL',
        }),
      )
    })

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Configuración del huerto actualizada con éxito',
    )
  })
})