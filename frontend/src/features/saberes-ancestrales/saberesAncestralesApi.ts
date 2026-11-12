import { apiGet } from '../../api/client'

export type CategoriaSaber =
  | 'PREPARACION_TIERRA'
  | 'ABONO_NATURAL'
  | 'RITUAL_SIEMBRA'
  | 'SIEMBRA_SENALETICA'
  | 'CUIDADO_RIEGO'
  | 'RITUAL_COSECHA'
  | 'SECADO_EMPAQUETADO'

export type FaseLunar =
  | 'YANA_KILLA_OSCURA'
  | 'WINAK_KILLA_CRECIENTE'
  | 'PURA_KILLA_LLENA'
  | 'WANUK_KILLA_MENGUANTE'

export interface SaberAncestralResumenResponse {
  id: number
  ordenPaso: number
  codigoHito: string
  titulo: string
  tituloKichwa: string | null
  categoria: CategoriaSaber
  faseLunarIdeal: FaseLunar
  nombreInsigniaOficial: string
}

export interface SaberAncestralDetalleResponse {
  id: number
  ordenPaso: number
  codigoHito: string
  titulo: string
  tituloKichwa: string | null
  categoria: CategoriaSaber
  faseLunarIdeal: FaseLunar
  horarioEnergetico: string
  contenidoEducativo: string
  ingredientesHerramientas: string | null
  puntosBaseComunidad: number
  nombreInsigniaOficial: string
}

export const obtenerRutaSaberes = () =>
  apiGet<SaberAncestralResumenResponse[]>('/publico/saberes-ancestrales')

export const obtenerDetalleSaber = (id: number) =>
  apiGet<SaberAncestralDetalleResponse>(`/publico/saberes-ancestrales/${id}`)
