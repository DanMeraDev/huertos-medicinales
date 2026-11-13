import { http, HttpResponse } from 'msw'

export const solicitudesPublicoMockHandlers = [
  http.post('*/api/publico/solicitudes-comunidad', () =>
    HttpResponse.json(
      {
        id: 12,
        nombreComunidad: 'Huerto Comunitario Allpa Viva',
        tipoOrganizacion: 'BARRIO',
        provincia: 'Pichincha',
        parroquia: 'Calderon',
        nombreSolicitante: 'Segundo Tránsito Amaguaña',
        correoSolicitante: 'comunidad.allpa@huertos.gob.ec',
        estado: 'PENDIENTE',
        creadoEn: '2026-11-10T11:00:00',
      },
      { status: 201 },
    ),
  ),
  http.get('https://nominatim.openstreetmap.org/reverse', () =>
    HttpResponse.json({
      address: {
        municipality: 'Calderón',
        county: 'Distrito Metropolitano de Quito',
        state: 'Pichincha',
        country: 'Ecuador',
      },
    }),
  ),
]
