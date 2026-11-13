import { apiGet, apiPost } from '../../api/client'

export interface MiembroVoluntario {
  id: number
  cedula: string
  nombreCompleto: string
  nombres: string
  apellidos: string
  nivel: 'APRENDIZ' | 'OFICIAL' | 'MAESTRO'
  puntosTotales: number
  activoEnComunidad: boolean
}

export interface ActividadComunitaria {
  id: number
  nombre: string
  categoria: string
  puntosPorHora: number
  activa: boolean
}

export interface InsigniaMiembro {
  id: number
  codigo: string
  nombre: string
  tipo: string
  descripcion: string
  fechaObtencion: string
}

export interface RegistrarLaborMiembroRequest {
  miembroId: number
  actividadId: number
  horasTrabajadas: number
  fechaLabor: string
  observaciones?: string
}

export interface ResultadoAcreditacionMiembroResponse {
  registroId: number
  miembroId: number
  nombreMiembro: string
  rangoActual: string
  rangoAnterior: string
  subioDeNivel: boolean
  puntosAcreditados: number
  puntosTotalesActuales: number
  nuevosTalentosDesbloqueados: InsigniaMiembro[]
  mensajeCelebracion: string
}

export function listarMiembrosComunidad(): Promise<MiembroVoluntario[]> {
  return apiGet<MiembroVoluntario[]>('/comunidades/mi-comunidad/miembros')
}

export function listarActividadesComunidad(): Promise<ActividadComunitaria[]> {
  return apiGet<ActividadComunitaria[]>('/comunidad/actividades-catalogo')
}

export function obtenerInsigniasMiembro(miembroId: number): Promise<InsigniaMiembro[]> {
  return apiGet<InsigniaMiembro[]>(`/comunidad/actividades-miembros/miembros/${miembroId}/insignias`)
}

export function registrarLaborMiembro(
  datos: RegistrarLaborMiembroRequest,
): Promise<ResultadoAcreditacionMiembroResponse> {
  return apiPost<ResultadoAcreditacionMiembroResponse>('/comunidad/actividades-miembros', datos)
}
