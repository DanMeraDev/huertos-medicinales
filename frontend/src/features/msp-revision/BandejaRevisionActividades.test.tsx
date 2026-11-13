import { render, screen } from '@testing-library/react'
import { BandejaRevisionActividades } from './BandejaRevisionActividades'
import type { ActividadRevision } from './mspRevisionApi'

const actividadEjemplo: ActividadRevision = {
  id: 21,
  comunidadId: 3,
  nombreComunidad: 'Huerto Comunitario Allpa Viva',
  tituloSaber: 'Ritual de siembra',
  fechaRealizacion: '2026-11-10',
  faseLunarEjecucion: 'PURA_KILLA_LLENA',
  descripcionBitacora: 'Ritual de siembra con sahumerio',
  evidenciaFotoUrl: 'actividades/foto.jpg',
  evidenciaVideoUrl: null,
  numeroIntento: 1,
  estado: 'PENDIENTE',
  participantes: [],
}

describe('BandejaRevisionActividades', () => {
  it('muestra a Kinti en estado vacío cuando no hay actividades por revisar', () => {
    render(
      <BandejaRevisionActividades actividades={[]} cargando={false} alAuditar={vi.fn()} />,
    )

    expect(screen.getByAltText('Kinti Guía en estado vacío')).toBeInTheDocument()
    expect(
      screen.getByText('No se han realizado actividades del saber ancestral'),
    ).toBeInTheDocument()
  })

  it('muestra a Kinti celebración cuando existen actividades por revisar', () => {
    render(
      <BandejaRevisionActividades
        actividades={[actividadEjemplo]}
        cargando={false}
        alAuditar={vi.fn()}
      />,
    )

    expect(screen.getByAltText('Kinti Guía celebrando logro')).toBeInTheDocument()
    expect(
      screen.getByText(
        'Las comunidades están aprendiendo el saber ancestral de nuestros antepasados, celébralo revisando sus actividades',
      ),
    ).toBeInTheDocument()
    expect(screen.getByText('Huerto Comunitario Allpa Viva')).toBeInTheDocument()
  })

  it('oculta el mensaje de Kinti mientras está cargando', () => {
    render(<BandejaRevisionActividades actividades={[]} cargando={true} alAuditar={vi.fn()} />)

    expect(screen.queryByAltText('Kinti Guía en estado vacío')).not.toBeInTheDocument()
    expect(screen.getByText('Cargando…')).toBeInTheDocument()
  })
})
