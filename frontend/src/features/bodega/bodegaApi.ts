import { apiGet, apiPost, type PaginaResponse } from '../../api/client'
import type {
  BajaMaterialResponse,
  MaterialVegetalResponse,
  PlantaResumen,
  RegistrarBajaMaterialRequest,
  RegistrarMaterialVegetalRequest,
} from './types'

export function registrarMaterialVegetal(data: RegistrarMaterialVegetalRequest): Promise<MaterialVegetalResponse> {
  return apiPost<MaterialVegetalResponse>('/bodega/material-vegetal', data)
}

export async function obtenerPlantasActivas(): Promise<PlantaResumen[]> {
  const respuesta = await apiGet<PaginaResponse<PlantaResumen>>('/publico/plantas?page=0&size=100')
  return respuesta.contenido
}

export function obtenerInventarioPorComunidad(comunidadId: number): Promise<MaterialVegetalResponse[]> {
  return apiGet<MaterialVegetalResponse[]>(`/bodega/comunidades/${comunidadId}/material-vegetal`)
}

export function registrarBajaMaterial(
  id: number,
  data: RegistrarBajaMaterialRequest,
): Promise<BajaMaterialResponse> {
  return apiPost<BajaMaterialResponse>(`/bodega/material-vegetal/${id}/bajas`, data)
}
