import { apiGet, apiPatch, apiPost, apiPut } from '../../api/client'

export type Genero = 'FEMENINO' | 'MASCULINO' | 'OTRO'
export type NivelMiembro = 'APRENDIZ' | 'OFICIAL' | 'MAESTRO'

export interface RegistrarMiembroRequest {
  cedula: string
  nombres: string
  apellidos: string
  fechaNacimiento: string
  genero: Genero
  telefono: string
  nivel: NivelMiembro
  consentimientoDatos: boolean
}

export interface MiembroDetalleResponse {
  id: number
  comunidadId: number
  cedula: string
  nombreCompleto: string
  nombres: string
  apellidos: string
  fechaNacimiento: string
  edad: number
  genero: Genero
  telefono: string
  nivel: NivelMiembro
  puntosTotales: number
  activoEnComunidad: boolean
  fechaIngreso: string
}

export interface CambiarEstadoMiembroRequest {
  activo: boolean
  motivoBaja?: string | null
}

export interface MiembroEstadoResponse {
  miembroId: number
  comunidadId: number
  nombreMiembro: string
  activoEnComunidad: boolean
  fechaBaja: string | null
  motivoBaja: string | null
  mensaje: string
}

export function registrarMiembroEnComunidad(
  datos: RegistrarMiembroRequest,
): Promise<MiembroDetalleResponse> {
  return apiPost<MiembroDetalleResponse>('/comunidades/mi-comunidad/miembros', datos)
}

export function listarMiembrosDeMiComunidad(): Promise<MiembroDetalleResponse[]> {
  return apiGet<MiembroDetalleResponse[]>('/comunidades/mi-comunidad/miembros')
}

export function cambiarEstadoMiembro(
  miembroId: number,
  datos: CambiarEstadoMiembroRequest,
): Promise<MiembroEstadoResponse> {
  return apiPatch<MiembroEstadoResponse>(`/comunidades/mi-comunidad/miembros/${miembroId}/estado`, datos)
}

export interface ActualizarMiembroRequest {
  genero: Genero
  telefono: string
}

export function actualizarDatosMiembro(
  miembroId: number,
  datos: ActualizarMiembroRequest,
): Promise<MiembroDetalleResponse> {
  return apiPut<MiembroDetalleResponse>(`/comunidades/mi-comunidad/miembros/${miembroId}`, datos)
}

export interface FichaInternaMiembroResponse {
  id: number
  comunidadId: number
  nombreComunidad: string
  cedula: string
  nombres: string
  apellidos: string
  nombreCompleto: string
  fechaNacimiento: string
  edad: number
  genero: Genero
  telefono: string
  nivel: NivelMiembro
  puntosTotales: number
  activoEnComunidad: boolean
  fechaIngreso: string
  fechaBaja: string | null
  motivoBaja: string | null
}

export function obtenerFichaMiembro(
  miembroId: number,
): Promise<FichaInternaMiembroResponse> {
  return apiGet<FichaInternaMiembroResponse>(`/comunidades/mi-comunidad/miembros/${miembroId}`)
}

