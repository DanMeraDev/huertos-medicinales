export type CategoriaUso =
  | 'ANTIESPASMODICA'
  | 'ANTIINFLAMATORIA'
  | 'ANTIOXIDANTE'
  | 'ANTIPARASITARIA'
  | 'ANTIPIRETICA'
  | 'ASTRINGENTE'
  | 'CICATRIZANTE'
  | 'DIURETICA'
  | 'ENERGETICA'
  | 'DEPURATIVA'
  | 'DESCONGESTIONANTE'
  | 'ESTIMULANTE'
  | 'EXPECTORANTE'
  | 'SEDANTE'

export type CualidadTermica = 'CALIENTE' | 'FRIA'

export interface PlantaResumen {
  id: number
  nombreComun: string
  nombreKichwa: string | null
  nombreCientifico: string
  cualidadTermica: CualidadTermica
  categorias: CategoriaUso[]
  fotografiaUrl: string | null
}

export const ETIQUETAS_CATEGORIA: Record<CategoriaUso, string> = {
  ANTIESPASMODICA: 'Antiespasmódica',
  ANTIINFLAMATORIA: 'Antiinflamatoria',
  ANTIOXIDANTE: 'Antioxidante',
  ANTIPARASITARIA: 'Antiparasitaria',
  ANTIPIRETICA: 'Antipirética',
  ASTRINGENTE: 'Astringente',
  CICATRIZANTE: 'Cicatrizante',
  DIURETICA: 'Diurética',
  ENERGETICA: 'Energética',
  DEPURATIVA: 'Depurativa',
  DESCONGESTIONANTE: 'Descongestionante',
  ESTIMULANTE: 'Estimulante',
  EXPECTORANTE: 'Expectorante',
  SEDANTE: 'Sedante',
}
