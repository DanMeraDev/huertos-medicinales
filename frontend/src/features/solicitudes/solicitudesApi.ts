import { apiGet, apiPost } from '../../api/client'

export type EstadoSolicitud = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA'

export interface SolicitudResumenResponse {
  id: number
  solicitante: string
  fecha: string
  provincia: string
}

export interface SolicitudDetalleCompletoResponse {
  id: number
  nombreComunidad: string
  tipoOrganizacion: string
  provincia: string
  parroquia: string
  direccion: string
  latitud: number
  longitud: number
  altitudMsnm: number
  disenoPrincipal: string
  subtiposSolicitados: string
  cedulaSolicitante: string
  nombreSolicitante: string
  fechaNacimientoSolicitante: string
  generoSolicitante: string
  correoSolicitante: string
  celularSolicitante: string
  estado: EstadoSolicitud
  creadoEn: string
}

export interface ResolucionSolicitudResponse {
  solicitudId: number
  estado: EstadoSolicitud
  mensaje: string
  comunidadId: number | null
  usuarioId: number | null
}

export const obtenerSolicitudesPendientes = () =>
  apiGet<SolicitudResumenResponse[]>('/solicitudes-comunidad?estado=PENDIENTE')

export const obtenerDetalleSolicitud = (id: number) =>
  apiGet<SolicitudDetalleCompletoResponse>(`/solicitudes-comunidad/${id}`)

export const aprobarSolicitud = (id: number) =>
  apiPost<ResolucionSolicitudResponse>(`/solicitudes-comunidad/${id}/aprobar`, {})

export const rechazarSolicitud = (id: number, motivoRechazo: string) =>
  apiPost<ResolucionSolicitudResponse>(`/solicitudes-comunidad/${id}/rechazar`, { motivoRechazo })
