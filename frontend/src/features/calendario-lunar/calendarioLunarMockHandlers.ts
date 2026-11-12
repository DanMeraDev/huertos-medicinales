import { http, HttpResponse } from 'msw'
import type { CalendarioLunarResponse } from './calendarioLunarApi'

const FASE_ACTUAL: CalendarioLunarResponse = {
  id: 1,
  faseLunar: 'PURA_KILLA_LLENA',
  nombreKichwa: 'Pura Killa',
  nombreEspanol: 'Luna Llena',
  fechaInicio: '2026-11-01',
  fechaFin: '2026-11-07',
  practicasRecomendadas: 'Cosecha de raíces y hojas medicinales.',
  consejoKinti: 'En la luna llena la planta guarda su medicina.',
}

export const calendarioLunarMockHandlers = [
  http.get('*/api/publico/calendario-lunar/actual', () => HttpResponse.json(FASE_ACTUAL)),
  http.get('*/api/publico/calendario-lunar', () => HttpResponse.json(FASE_ACTUAL)),
]
