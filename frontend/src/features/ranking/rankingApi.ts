import { apiGet } from '../../api/client'
import type { RankingComunidad } from './types'

export function obtenerRankingComunidades(): Promise<RankingComunidad[]> {
  return apiGet<RankingComunidad[]>('/publico/ranking/comunidades')
}
