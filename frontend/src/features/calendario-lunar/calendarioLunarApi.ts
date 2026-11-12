import { apiGet } from '../../api/client'

export type FaseLunar =
  | 'YANA_KILLA_OSCURA'
  | 'WINAK_KILLA_CRECIENTE'
  | 'PURA_KILLA_LLENA'
  | 'WANUK_KILLA_MENGUANTE'

export interface CalendarioLunarResponse {
  id: number
  faseLunar: FaseLunar
  nombreKichwa: string
  nombreEspanol: string
  fechaInicio: string
  fechaFin: string
  practicasRecomendadas: string
  consejoKinti: string
}

export const obtenerFaseLunarActual = () =>
  apiGet<CalendarioLunarResponse>('/publico/calendario-lunar/actual')

export const obtenerFasePorFecha = (fecha: string) =>
  apiGet<CalendarioLunarResponse>(`/publico/calendario-lunar?fecha=${encodeURIComponent(fecha)}`)
