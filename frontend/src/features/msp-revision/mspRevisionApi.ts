import { apiGet, apiPost } from '../../api/client'

export type EstadoVerificacion =
  | 'PENDIENTE'
  | 'OBSERVADA_CORREGIR'
  | 'APROBADA'
  | 'RECHAZADA_DEFINITIVA'

export type FaseLunar =
  | 'YANA_KILLA_OSCURA'
  | 'WINAK_KILLA_CRECIENTE'
  | 'PURA_KILLA_LLENA'
  | 'WANUK_KILLA_MENGUANTE'

export interface ParticipanteRevision {
  miembroId: number
  nombreCompleto: string
  horasTrabajadas: number
}

export interface ActividadRevision {
  id: number
  comunidadId: number
  nombreComunidad: string
  tituloSaber: string
  fechaRealizacion: string
  faseLunarEjecucion: FaseLunar
  descripcionBitacora: string
  evidenciaFotoUrl: string
  evidenciaVideoUrl: string | null
  numeroIntento: number
  estado: EstadoVerificacion
  participantes: ParticipanteRevision[]
}

export interface VerificarActividadMspRequest {
  nuevoEstado: EstadoVerificacion
  observacion: string | null
}

export interface ResultadoVerificacion {
  actividadId: number
  estado: EstadoVerificacion
  puntosComunidadAcreditados: number
  fechaVerificacion: string
}

export const listarPendientes = () =>
  apiGet<ActividadRevision[]>('/msp/actividades-ancestrales/pendientes')

export const verificarActividad = (id: number, datos: VerificarActividadMspRequest) =>
  apiPost<ResultadoVerificacion>(`/msp/actividades-ancestrales/${id}/verificar`, datos)
