import { apiGet, apiPost } from '../../api/client'

export type FaseLunar =
  | 'YANA_KILLA_OSCURA'
  | 'WINAK_KILLA_CRECIENTE'
  | 'PURA_KILLA_LLENA'
  | 'WANUK_KILLA_MENGUANTE'

export type EstadoVerificacion =
  | 'PENDIENTE'
  | 'OBSERVADA_CORREGIR'
  | 'APROBADA'
  | 'RECHAZADA_DEFINITIVA'

export interface SaberAncestralOpcion {
  id: number
  ordenPaso: number
  titulo: string
}

export interface MiembroComunidadOpcion {
  id: number
  nombreCompleto: string
}

export interface ParticipanteActividad {
  miembroId: number
  horasTrabajadas: number
}

export interface RegistrarActividadAncestralRequest {
  saberAncestralId: number
  fechaRealizacion: string
  faseLunarEjecucion: FaseLunar
  descripcionBitacora: string
  evidenciaFotoUrl: string
  evidenciaVideoUrl?: string | null
  participantes: ParticipanteActividad[]
}

export interface ActividadAncestralResponse {
  id: number
  comunidadId: number
  tituloSaber: string
  estado: EstadoVerificacion
  numeroIntento: number
  fechaRealizacion: string
  participantes: ParticipanteActividad[]
}

export const listarSaberesAncestrales = () =>
  apiGet<SaberAncestralOpcion[]>('/publico/saberes-ancestrales')

export const listarMiembrosComunidad = () =>
  apiGet<MiembroComunidadOpcion[]>('/comunidades/mi-comunidad/miembros')

export function registrarActividadAncestral(
  datos: RegistrarActividadAncestralRequest,
): Promise<ActividadAncestralResponse> {
  return apiPost<ActividadAncestralResponse>('/actividades-ancestrales', datos)
}
