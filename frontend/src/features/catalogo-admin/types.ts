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

export interface CrearPlantaRequest {
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

export type ActualizarPlantaRequest = CrearPlantaRequest

export interface PlantaDetalleResponse extends CrearPlantaRequest {
  id: number
}

export interface DarBajaPlantaRequest {
  motivo: string
}

export interface FotografiaSubidaResponse {
  fotografiaUrl: string
}
