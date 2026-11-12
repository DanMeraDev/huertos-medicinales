import { apiGet, apiPost, type PaginaResponse } from '../../api/client'
import type { MaterialVegetalResponse, PlantaResumen, RegistrarMaterialVegetalRequest } from './types'

export function registrarMaterialVegetal(data: RegistrarMaterialVegetalRequest): Promise<MaterialVegetalResponse> {
  return apiPost<MaterialVegetalResponse>('/bodega/material-vegetal', data)
}

export async function obtenerPlantasActivas(): Promise<PlantaResumen[]> {
  const respuesta = await apiGet<PaginaResponse<PlantaResumen>>('/publico/plantas')
  return respuesta.contenido
}
