import { apiGet, apiPost } from '../../api/client'

export interface CrearTalentoComunidadRequest {
  actividadComunidadId: number
  nombreInsignia: string
  descripcion: string
  metaRepeticiones: number
}

export interface TalentoComunidadResponse {
  id: number
  comunidadId: number
  actividadComunidadId: number
  nombreActividad: string
  nombreInsignia: string
  descripcion: string
  metaRepeticiones: number
  iconoCodigo: string
  activo: boolean
  creadoEn: string
}

export interface ActividadOpcion {
  id: number
  nombre: string
}

export function obtenerTalentosComunidad(): Promise<TalentoComunidadResponse[]> {
  return apiGet<TalentoComunidadResponse[]>('/comunidades/mi-comunidad/talentos')
}

export function crearTalentoComunidad(
  datos: CrearTalentoComunidadRequest,
): Promise<TalentoComunidadResponse> {
  return apiPost<TalentoComunidadResponse>('/comunidades/mi-comunidad/talentos', datos)
}

export function obtenerActividadesParaTalentos(): Promise<ActividadOpcion[]> {
  return apiGet<ActividadOpcion[]>('/comunidad/actividades-catalogo')
}
