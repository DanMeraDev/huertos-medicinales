import { http, HttpResponse } from 'msw'

export const usuariosMockHandlers = [
  http.post('*/api/usuarios', () =>
    HttpResponse.json(
      {
        id: 5,
        nombreCompleto: 'Rosa Elena Chimbolema',
        identificacion: '1710034065',
        fechaNacimiento: '1990-05-14',
        genero: 'FEMENINO',
        correo: 'rosa.chimbolema@huertos.gob.ec',
        telefono: '0987654321',
        rol: 'TECNICO_MSP',
        activo: true,
        requiereCambioClave: true,
        creadoEn: '2026-11-10T10:30:00',
      },
      { status: 201 },
    ),
  ),
]
