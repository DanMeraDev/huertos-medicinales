import { http, HttpResponse } from 'msw'
import { bodegaMockHandlers } from '../../features/bodega/bodegaMockHandlers'

// Respuestas simuladas del back. Cada feature agrega aquí sus endpoints.
export const handlers = [
  http.get('*/api/ping', () =>
    HttpResponse.json({ estado: 'ok', baseDeDatos: '11.4.8-MariaDB', fecha: '2026-10-31T12:00:00Z' }),
  ),
  ...bodegaMockHandlers,
]
