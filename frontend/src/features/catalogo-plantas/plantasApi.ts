import { apiGet, type PaginaResponse } from '../../api/client'
import type { PlantaResumen } from './types'

export const obtenerPlantasPublicas = (pagina: number, tamano: number) =>
  apiGet<PaginaResponse<PlantaResumen>>(`/publico/plantas?page=${pagina}&size=${tamano}`)

export const buscarPlantas = (criterio: string) => apiGet<PlantaResumen[]>(`/publico/plantas/buscar?criterio=${criterio}`)
