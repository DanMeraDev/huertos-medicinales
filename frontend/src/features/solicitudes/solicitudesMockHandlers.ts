import { http, HttpResponse } from 'msw'
import type { SolicitudDetalleCompletoResponse, SolicitudResumenResponse } from './solicitudesApi'

export const mockSolicitudesPendientes: SolicitudResumenResponse[] = [
  { id: 12, solicitante: 'Segundo Tránsito Amaguaña', fecha: '2026-11-10', provincia: 'Pichincha' },
]

export const mockDetalleSolicitud: SolicitudDetalleCompletoResponse = {
  id: 12,
  nombreComunidad: 'Huerto Comunitario Allpa Viva',
  tipoOrganizacion: 'BARRIO',
  provincia: 'Pichincha',
  parroquia: 'Calderón',
  direccion: 'Calle de las Rosas y Pasaje Los Pinos, lote 14',
  latitud: -0.0987123,
  longitud: -78.4321456,
  altitudMsnm: 2650,
  disenoPrincipal: 'EN_TIERRA',
  subtiposSolicitados: 'SURCOS_WACHU,TERRAZAS_ANTI',
  cedulaSolicitante: '1710034065',
  nombreSolicitante: 'Segundo Tránsito Amaguaña',
  fechaNacimientoSolicitante: '1990-05-14',
  generoSolicitante: 'MASCULINO',
  correoSolicitante: 'comunidad.allpa@huertos.gob.ec',
  celularSolicitante: '0991234567',
  estado: 'PENDIENTE',
  creadoEn: '2026-11-10T11:00:00',
}

export const solicitudesMockHandlers = [
  http.get('*/api/solicitudes-comunidad', () => HttpResponse.json(mockSolicitudesPendientes)),
  http.get('*/api/solicitudes-comunidad/:id', () => HttpResponse.json(mockDetalleSolicitud)),
  http.post('*/api/solicitudes-comunidad/:id/aprobar', () =>
    HttpResponse.json({
      solicitudId: 12,
      estado: 'APROBADA',
      mensaje: 'Solicitud aprobada exitosamente. Comunidad y cuenta de usuario creadas.',
      comunidadId: 3,
      usuarioId: 8,
    }),
  ),
  http.post('*/api/solicitudes-comunidad/:id/rechazar', () =>
    HttpResponse.json({
      solicitudId: 12,
      estado: 'RECHAZADA',
      mensaje: 'Solicitud rechazada. Se registró el motivo del rechazo.',
      comunidadId: null,
      usuarioId: null,
    }),
  ),
]
