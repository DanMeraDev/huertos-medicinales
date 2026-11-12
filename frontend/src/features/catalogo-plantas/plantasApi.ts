import { apiGet, type PaginaResponse } from '../../api/client'
import type { FiltrosPlanta, PlantaDetalle, PlantaResumen } from './types'

export const obtenerPlantasPublicas = (pagina: number, tamano: number) =>
  apiGet<PaginaResponse<PlantaResumen>>(`/publico/plantas?page=${pagina}&size=${tamano}`)

export const buscarPlantas = (criterio: string) => apiGet<PlantaResumen[]>(`/publico/plantas/buscar?criterio=${encodeURIComponent(criterio)}`)

export const obtenerDetallePlanta = (id: number) => apiGet<PlantaDetalle>(`/publico/plantas/${id}`)

export const filtrarPlantas = (filtros: FiltrosPlanta, criterio?: string) => {
  const parametros = new URLSearchParams()
  Object.entries({ ...filtros, criterio }).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== '') parametros.append(clave, String(valor))
  })
  return apiGet<PlantaResumen[]>(`/publico/plantas/filtrar?${parametros}`)
}
