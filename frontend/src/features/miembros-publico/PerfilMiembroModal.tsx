import type { PerfilPublicoMiembroResponse } from './miembrosPublicoApi'

export interface PerfilMiembroModalProps {
  abierto: boolean
  perfil: PerfilPublicoMiembroResponse | null
  alCerrar: () => void
}

export function PerfilMiembroModal({ abierto, perfil, alCerrar }: PerfilMiembroModalProps) {
  void abierto
  void perfil
  void alCerrar
  return null
}
