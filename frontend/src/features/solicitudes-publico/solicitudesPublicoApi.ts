import { apiPost } from '../../api/client'

export type TipoOrganizacion = 'INDIVIDUAL' | 'FAMILIA' | 'CONDOMINIO' | 'BARRIO' | 'COMITE_LOCAL'

export type DisenoPrincipal = 'EN_TIERRA' | 'VERTICAL' | 'COLGANTE' | 'EN_PISO'

export type Genero = 'FEMENINO' | 'MASCULINO' | 'OTRO'

export type EstadoSolicitud = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA'

export interface CrearSolicitudComunidadRequest {
  nombreComunidad: string
  tipoOrganizacion: TipoOrganizacion
  provincia: string
  parroquia: string
  direccion: string
  latitud: number
  longitud: number
  altitudMsnm: number
  disenoPrincipal: DisenoPrincipal
  subtiposSolicitados: string
  cedulaSolicitante: string
  nombreSolicitante: string
  fechaNacimiento: string
  generoSolicitante: Genero
  correoSolicitante: string
  celularSolicitante: string
  consentimientoDatos: boolean
}

export interface SolicitudComunidadResponse {
  id: number
  nombreComunidad: string
  tipoOrganizacion: TipoOrganizacion
  provincia: string
  parroquia: string
  nombreSolicitante: string
  correoSolicitante: string
  estado: EstadoSolicitud
  creadoEn: string
}

export function enviarSolicitudComunidad(datos: CrearSolicitudComunidadRequest) {
  return apiPost<SolicitudComunidadResponse>('/publico/solicitudes-comunidad', datos)
}
