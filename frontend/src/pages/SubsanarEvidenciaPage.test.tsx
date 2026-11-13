import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { SubsanarEvidenciaPage } from './SubsanarEvidenciaPage'
import {
  buscarActividadParaCorregir,
  reenviarEvidencias,
} from '../features/actividades-ancestrales/actividadesAncestralesApi'

vi.mock('../features/actividades-ancestrales/actividadesAncestralesApi', () => ({
  buscarActividadParaCorregir: vi.fn(),
  reenviarEvidencias: vi.fn(),
  subirFotografiaActividad: vi.fn(),
}))

const mockBuscarActividad = vi.mocked(buscarActividadParaCorregir)
const mockReenviarEvidencias = vi.mocked(reenviarEvidencias)

function renderizarPagina() {
  return render(
    <MemoryRouter initialEntries={['/actividades-ancestrales/21/subsanar']}>
      <Routes>
        <Route path="/actividades-ancestrales/:id/subsanar" element={<SubsanarEvidenciaPage />} />
        <Route
          path="/actividades-ancestrales/observadas"
          element={<p>Bandeja de actividades observadas</p>}
        />
        <Route path="*" element={<p>Página no encontrada</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('SubsanarEvidenciaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockBuscarActividad.mockResolvedValue({
      id: 21,
      tituloSaber: 'Ritual de siembra',
      estado: 'OBSERVADA_CORREGIR',
      numeroIntento: 1,
      observacionMsp: 'La fotografía no muestra el ritual completo',
      descripcionBitacora: 'Ritual de siembra con chicha de jora',
      evidenciaFotoUrl: 'actividades/foto-inicial.jpg',
      evidenciaVideoUrl: null,
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

  it(
    'muestra el mensaje de reenvío y vuelve a la bandeja sin mostrar la página no encontrada',
    async () => {
      renderizarPagina()

      await screen.findByText('Ritual de siembra')

      await userEvent.click(
        screen.getByRole('button', { name: /reenviar evidencias corregidas/i }),
      )

      expect(
        await screen.findByText(/evidencias corregidas reenviadas al técnico msp/i),
      ).toBeInTheDocument()

      await waitFor(
        () => expect(screen.getByText('Bandeja de actividades observadas')).toBeInTheDocument(),
        { timeout: 4000 },
      )
      expect(screen.queryByText('Página no encontrada')).not.toBeInTheDocument()
    },
    10000,
  )
})
