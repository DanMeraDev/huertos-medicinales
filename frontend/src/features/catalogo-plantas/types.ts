import type { InsigniaProps } from '../../components/Insignia'

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

export type CicloCultivo = 'CORTO' | 'LARGO'

export type OrigenPlanta = 'NATIVA' | 'ENDEMICA' | 'INTRODUCIDA'

export interface PlantaResumen {
  id: number
  nombreComun: string
  nombreKichwa: string | null
  nombreCientifico: string
  cualidadTermica: CualidadTermica
  categorias: CategoriaUso[]
  fotografiaUrl: string | null
}

export interface PlantaDetalle {
  id: number
  nombreComun: string
  nombreKichwa: string | null
  nombreCientifico: string
  familiaBotanica: string
  cualidadTermica: CualidadTermica
  cicloCultivo: CicloCultivo | null
  origen: OrigenPlanta | null
  altitudMinMsnm: number | null
  altitudMaxMsnm: number | null
  regionesDistribucion: string | null
  usosMedicinales: string
  preparacionDosificacion: string | null
  contraindicaciones: string | null
  categorias: CategoriaUso[]
  fotografiaUrl: string | null
}

export const INSIGNIAS_TERMICAS: Record<CualidadTermica, InsigniaProps> = {
  CALIENTE: { texto: 'Caliente', tono: 'error' },
  FRIA: { texto: 'Fría', tono: 'exito' },
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

export const ETIQUETAS_CICLO: Record<CicloCultivo, string> = {
  CORTO: 'Corto',
  LARGO: 'Largo',
}

export const ETIQUETAS_ORIGEN: Record<OrigenPlanta, string> = {
  NATIVA: 'Nativa',
  ENDEMICA: 'Endémica',
  INTRODUCIDA: 'Introducida',
}
