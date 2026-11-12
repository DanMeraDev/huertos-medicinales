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

export type CausaFracaso =
  | 'HELADA_CLIMA_EXTREMO'
  | 'PLAGA_ENFERMEDAD'
  | 'SEQUIA_FALTA_RIEGO'
  | 'SUELO_INADECUADO'
  | 'DANIO_EXTERNO'
  | 'CADUCIDAD_PUDRICION'

export const OPCIONES_CAUSA_FRACASO = [
  { valor: 'HELADA_CLIMA_EXTREMO', etiqueta: 'Helada / Clima extremo' },
  { valor: 'PLAGA_ENFERMEDAD', etiqueta: 'Plaga o enfermedad' },
  { valor: 'SEQUIA_FALTA_RIEGO', etiqueta: 'Sequía / Falta de riego' },
  { valor: 'SUELO_INADECUADO', etiqueta: 'Suelo inadecuado' },
  { valor: 'DANIO_EXTERNO', etiqueta: 'Daño externo' },
  { valor: 'CADUCIDAD_PUDRICION', etiqueta: 'Caducidad / Pudrición' },
] as const

export interface RegistrarBajaMaterialRequest {
  cantidadBaja: number
  causaFracaso: CausaFracaso
  observacion?: string
}

export interface BajaMaterialResponse {
  id: number
  bodegaMaterialId: number
  cantidadBaja: number
  causaFracaso: CausaFracaso
  observacion?: string
  fechaBaja: string
}

