import { http, HttpResponse } from 'msw'

const FASE_ACTUAL = {
  id: 1,
  faseLunar: 'PURA_KILLA_LLENA',
  nombreKichwa: 'Pura Killa',
  nombreEspanol: 'Luna Llena',
  fechaInicio: '2026-11-01',
  fechaFin: '2026-11-07',
  practicasRecomendadas: 'Cosecha de raíces y hojas medicinales.',
  consejoKinti: 'En la luna llena la planta guarda su medicina.',
}

// Respuestas simuladas del back. Cada feature agrega aquí sus endpoints.
export const handlers = [
  http.get('*/api/ping', () =>
    HttpResponse.json({ estado: 'ok', baseDeDatos: '11.4.8-MariaDB', fecha: '2026-10-31T12:00:00Z' }),
  ),
  http.get('*/api/publico/calendario-lunar/actual', () => HttpResponse.json(FASE_ACTUAL)),
  http.get('*/api/publico/calendario-lunar', () => HttpResponse.json(FASE_ACTUAL)),
]
