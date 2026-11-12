import { http, HttpResponse } from 'msw'
import type {
  SaberAncestralDetalleResponse,
  SaberAncestralResumenResponse,
} from './saberesAncestralesApi'

const PASO_PREPARACION: SaberAncestralDetalleResponse = {
  id: 1,
  ordenPaso: 1,
  codigoHito: 'SA-PASO-1',
  titulo: 'Preparación de la tierra',
  tituloKichwa: 'Allpa wiñay',
  categoria: 'PREPARACION_TIERRA',
  faseLunarIdeal: 'YANA_KILLA_OSCURA',
  horarioEnergetico: '05:00 a 07:00 AM - Salida del sol',
  contenidoEducativo:
    'Se barre y se limpia el terreno con la chaquetaclla mientras la tierra descansa en la luna oscura.',
  ingredientesHerramientas: 'Chaquetaclla, chakana, compost de la comunidad',
  puntosBaseComunidad: 20,
  nombreInsigniaOficial: 'Guardián de la tierra',
}

const PASO_ABONO: SaberAncestralDetalleResponse = {
  id: 2,
  ordenPaso: 2,
  codigoHito: 'SA-PASO-2',
  titulo: 'Abono natural del huerto',
  tituloKichwa: 'Allpa apamuni',
  categoria: 'ABONO_NATURAL',
  faseLunarIdeal: 'WINAK_KILLA_CRECIENTE',
  horarioEnergetico: '07:00 a 09:00 AM - Mañana fresca',
  contenidoEducativo:
    'Se prepara el biolesivo con ortiga y consuema, dejándolo fermentar a la sombra durante tres semanas.',
  ingredientesHerramientas: 'Ortiga, consuema, bidón de fermentación',
  puntosBaseComunidad: 20,
  nombreInsigniaOficial: 'Manos que abonan',
}

const PASO_SIEMBRA: SaberAncestralDetalleResponse = {
  id: 3,
  ordenPaso: 3,
  codigoHito: 'SA-PASO-3',
  titulo: 'Ritual de siembra',
  tituloKichwa: 'Tarpuna samay',
  categoria: 'RITUAL_SIEMBRA',
  faseLunarIdeal: 'PURA_KILLA_LLENA',
  horarioEnergetico: '05:00 a 07:00 AM - Salida del sol',
  contenidoEducativo:
    'Con la luna llena la comunidad siembra ofreciendo la chicha y agradeciendo a la Pachamama.',
  ingredientesHerramientas: 'Semillas de la bodega, chicha de jora, sahumerio',
  puntosBaseComunidad: 20,
  nombreInsigniaOficial: 'Sembrador de la Killa',
}

const DETALLES = [PASO_PREPARACION, PASO_ABONO, PASO_SIEMBRA]

const aResumen = ({
  id,
  ordenPaso,
  codigoHito,
  titulo,
  tituloKichwa,
  categoria,
  faseLunarIdeal,
  nombreInsigniaOficial,
}: SaberAncestralDetalleResponse): SaberAncestralResumenResponse => ({
  id,
  ordenPaso,
  codigoHito,
  titulo,
  tituloKichwa,
  categoria,
  faseLunarIdeal,
  nombreInsigniaOficial,
})

export const saberesMockHandlers = [
  http.get('*/api/publico/saberes-ancestrales', () =>
    HttpResponse.json(DETALLES.map(aResumen)),
  ),
  http.get('*/api/publico/saberes-ancestrales/:id', ({ params }) => {
    const detalle = DETALLES.find((saber) => saber.id === Number(params.id))
    if (!detalle) {
      return HttpResponse.json(
        { detail: 'El saber ancestral solicitado no fue encontrado' },
        { status: 404 },
      )
    }
    return HttpResponse.json(detalle)
  }),
]
