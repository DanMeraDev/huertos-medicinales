import type { MiembroDetalleResponse } from './miembrosApi'

export interface EditarMiembroModalProps {
  abierto: boolean
  miembro: MiembroDetalleResponse | null
  alCerrar: () => void
  alExito: (miembroActualizado: MiembroDetalleResponse) => void
}

export function EditarMiembroModal({
  abierto,
  miembro,
}: EditarMiembroModalProps) {
  if (!abierto || !miembro) return null

  return (
    <div role="dialog">
      <p>Editar miembro stub</p>
    </div>
  )
}
