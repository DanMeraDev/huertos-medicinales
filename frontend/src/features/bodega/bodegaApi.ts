import { apiGet, apiPost } from '../../api/client'
import type { MaterialVegetalResponse, PlantaResumen, RegistrarMaterialVegetalRequest } from './types'

export function registrarMaterialVegetal(data: RegistrarMaterialVegetalRequest): Promise<MaterialVegetalResponse> {
  return apiPost<MaterialVegetalResponse>('/bodega/material-vegetal', data)
}

export function obtenerPlantasActivas(): Promise<PlantaResumen[]> {
  return apiGet<PlantaResumen[]>('/plantas')
}

