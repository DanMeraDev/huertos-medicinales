export type TipoMaterialVegetal = 'SEMILLAS' | 'ESTACAS' | 'ESQUEJES' | 'PLANTULAS'
export type UnidadMedida = 'UNIDADES' | 'GRAMOS' | 'MANOJOS'

export interface RegistrarMaterialVegetalRequest {
  plantaId: number
  tipoMaterial: TipoMaterialVegetal
  cantidad: number
  unidadMedida: UnidadMedida
  loteOrigen?: string
  fechaIngreso: string
}

export interface MaterialVegetalResponse {
  id: number
  comunidadId: number
  plantaId: number
  nombreComunPlanta: string
  nombreCientificoPlanta: string
  tipoMaterial: TipoMaterialVegetal
  cantidadDisponible: number
  unidadMedida: UnidadMedida
  loteOrigen?: string
  fechaIngreso: string
}

export interface PlantaResumen {
  id: number
  nombreComun: string
  nombreCientifico?: string
}
