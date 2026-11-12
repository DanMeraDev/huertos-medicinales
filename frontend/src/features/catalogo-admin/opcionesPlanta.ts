import type { CategoriaUso, CicloCultivo, CualidadTermica, OrigenPlanta } from './types'

export type Region = 'Costa' | 'Sierra' | 'Amazonía' | 'Galápagos'

export interface Opcion<T extends string> {
  valor: T
  etiqueta: string
}

export const OPCIONES_CUALIDAD: Opcion<CualidadTermica>[] = [
  { valor: 'CALIENTE', etiqueta: 'Caliente' },
  { valor: 'FRIA', etiqueta: 'Fría' },
]

export const OPCIONES_CICLO: Opcion<CicloCultivo>[] = [
  { valor: 'CORTO', etiqueta: 'Corto' },
  { valor: 'LARGO', etiqueta: 'Largo' },
]

export const OPCIONES_ORIGEN: Opcion<OrigenPlanta>[] = [
  { valor: 'NATIVA', etiqueta: 'Nativa' },
  { valor: 'ENDEMICA', etiqueta: 'Endémica' },
  { valor: 'INTRODUCIDA', etiqueta: 'Introducida' },
]

export const OPCIONES_REGION: Opcion<Region>[] = [
  { valor: 'Costa', etiqueta: 'Costa' },
  { valor: 'Sierra', etiqueta: 'Sierra' },
  { valor: 'Amazonía', etiqueta: 'Amazonía' },
  { valor: 'Galápagos', etiqueta: 'Galápagos' },
]

export const OPCIONES_CATEGORIA: Opcion<CategoriaUso>[] = [
  { valor: 'ANTIESPASMODICA', etiqueta: 'Antiespasmódica' },
  { valor: 'ANTIINFLAMATORIA', etiqueta: 'Antiinflamatoria' },
  { valor: 'ANTIOXIDANTE', etiqueta: 'Antioxidante' },
  { valor: 'ANTIPARASITARIA', etiqueta: 'Antiparasitaria' },
  { valor: 'ANTIPIRETICA', etiqueta: 'Antipirética' },
  { valor: 'ASTRINGENTE', etiqueta: 'Astringente' },
  { valor: 'CICATRIZANTE', etiqueta: 'Cicatrizante' },
  { valor: 'DIURETICA', etiqueta: 'Diurética' },
  { valor: 'ENERGETICA', etiqueta: 'Energética' },
  { valor: 'DEPURATIVA', etiqueta: 'Depurativa' },
  { valor: 'DESCONGESTIONANTE', etiqueta: 'Descongestionante' },
  { valor: 'ESTIMULANTE', etiqueta: 'Estimulante' },
  { valor: 'EXPECTORANTE', etiqueta: 'Expectorante' },
  { valor: 'SEDANTE', etiqueta: 'Sedante' },
]

export function enOrdenDeOpciones<T extends string>(opciones: Opcion<T>[], seleccionados: T[]) {
  return opciones.map((opcion) => opcion.valor).filter((valor) => seleccionados.includes(valor))
}
