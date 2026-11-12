import { apiPost } from '../../api/client'

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

export function registrarMiembroEnComunidad(
  datos: RegistrarMiembroRequest,
): Promise<MiembroDetalleResponse> {
  return apiPost<MiembroDetalleResponse>('/comunidades/mi-comunidad/miembros', datos)
}
