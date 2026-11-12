import { apiGet, apiPut } from '../../api/client'

export type TipoOrganizacion = 'INDIVIDUAL' | 'FAMILIA' | 'CONDOMINIO' | 'BARRIO' | 'COMITE_LOCAL'

export interface ComunidadDetalleResponse {
  id: number
  nombre: string
  tipoOrganizacion: TipoOrganizacion
  provincia: string
  parroquia: string
  direccion: string
  telefonoContacto: string
  correoContacto: string
  responsableId: number
  nombreResponsable: string
  puntosComunidad: number
  activa: boolean
  creadoEn: string
}

export interface ActualizarComunidadRequest {
  nombre: string
  telefonoContacto: string
  correoContacto: string
}

export function obtenerMiComunidad(): Promise<ComunidadDetalleResponse> {
  return apiGet<ComunidadDetalleResponse>('/comunidades/mi-comunidad')
}

export function actualizarMiComunidad(datos: ActualizarComunidadRequest): Promise<ComunidadDetalleResponse> {
  return apiPut<ComunidadDetalleResponse>('/comunidades/mi-comunidad', datos)
}
