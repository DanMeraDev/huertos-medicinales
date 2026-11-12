import type { FichaInternaMiembroResponse } from './miembrosApi'

export interface FichaMiembroModalProps {
  abierto: boolean
  miembroId?: number | null
  ficha?: FichaInternaMiembroResponse | null
  alCerrar: () => void
}

export function FichaMiembroModal(_props: FichaMiembroModalProps) {
  return null
}
