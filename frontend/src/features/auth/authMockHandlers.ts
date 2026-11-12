import { http, HttpResponse } from 'msw'

export const authMockHandlers = [
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
