import { http, HttpResponse } from 'msw'

// Respuestas simuladas del back. Cada feature agrega aquí sus endpoints.
export const handlers = [
  http.get('*/api/ping', () =>
    HttpResponse.json({ estado: 'ok', baseDeDatos: '11.4.8-MariaDB', fecha: '2026-10-31T12:00:00Z' }),
  ),
  http.post('*/api/publico/auth/login', () =>
    HttpResponse.json({
      token: 'token-jwt',
      tipoToken: 'Bearer',
      id: 1,
      correo: 'rosa@correo.com',
      nombreCompleto: 'Rosa Quishpe',
      rol: 'RESPONSABLE_COMUNIDAD',
      requiereCambioClave: false,
    }),
  ),
]
