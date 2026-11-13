import { apiGet } from '../../api/client'
import type { RankingComunidad, RankingMiembro } from './types'

export function obtenerRankingComunidades(): Promise<RankingComunidad[]> {
  return apiGet<RankingComunidad[]>('/publico/ranking/comunidades')
}

export function obtenerRankingMiembros(): Promise<RankingMiembro[]> {
  return apiGet<RankingMiembro[]>('/publico/ranking/miembros')
}
