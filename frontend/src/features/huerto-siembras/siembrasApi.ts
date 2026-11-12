import { apiGet, apiPost } from '../../api/client'

export type FaseLunar =
  | 'YANA_KILLA_OSCURA'
  | 'WINAK_KILLA_CRECIENTE'
  | 'PURA_KILLA_LLENA'
  | 'WANUK_KILLA_MENGUANTE'

export type CualidadTermica = 'CALIENTE' | 'FRIA'

export interface HuertoDetalle {
  id: number
  comunidadId: number
  nombreHuerto: string
  altitudMsnm: number
}

export interface SubhuertoItem {
  id: number
  huertoId: number
  nombreZona: string
  subtipo: string
  areaM2: number
  activo: boolean
}

export interface MaterialBodegaItem {
  id: number
  comunidadId: number
  plantaId: number
  nombreComunPlanta: string
  nombreCientificoPlanta: string
  tipoMaterial: string
  cantidadDisponible: number
  unidadMedida: string
  loteOrigen?: string
  fechaIngreso: string
}

export interface PlantaDetalleItem {
  id: number
  nombreComun: string
  nombreCientifico: string
  cualidadTermica: CualidadTermica
  altitudMinMsnm?: number | null
  altitudMaxMsnm?: number | null
}

export interface RegistrarSiembraPayload {
  subhuertoId: number
  bodegaMaterialId: number
  cantidadSembrada: number
  fechaSiembra: string
  faseLunarSiembra: FaseLunar
  aceptaRiesgoAltitud: boolean
}

export interface SiembraDetalle {
  id: number
  subhuertoId: number
  nombreSubhuerto: string
  bodegaMaterialId: number
  plantaId: number
  nombreComunPlanta: string
  cualidadTermica: CualidadTermica
  cantidadSembrada: number
  fechaSiembra: string
  faseLunarSiembra: FaseLunar
  tuvoAlertaAltitud: boolean
  estadoCultivo: string
}

export function obtenerHuerto(): Promise<HuertoDetalle> {
  return apiGet<HuertoDetalle>('/comunidad/huerto')
}

export function obtenerSubhuertos(): Promise<SubhuertoItem[]> {
  return apiGet<SubhuertoItem[]>('/comunidad/subhuertos')
}

export function obtenerInventarioBodega(comunidadId: number): Promise<MaterialBodegaItem[]> {
  return apiGet<MaterialBodegaItem[]>(`/bodega/comunidades/${comunidadId}/material-vegetal`)
}

export function obtenerDetallePlanta(plantaId: number): Promise<PlantaDetalleItem> {
  return apiGet<PlantaDetalleItem>(`/publico/plantas/${plantaId}`)
}

export function registrarSiembra(datos: RegistrarSiembraPayload): Promise<SiembraDetalle> {
  return apiPost<SiembraDetalle>('/comunidad/siembras', datos)
}

