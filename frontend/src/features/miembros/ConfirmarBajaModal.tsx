import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import type { MiembroDetalleResponse } from './miembrosApi'

export interface ConfirmarBajaModalProps {
  abierto: boolean
  miembro: MiembroDetalleResponse | null
  alCerrar: () => void
  alConfirmar: (motivo: string) => void
  enviando?: boolean
}

export function ConfirmarBajaModal({
  abierto,
  miembro,
  alCerrar,
}: ConfirmarBajaModalProps) {
  if (!abierto || !miembro) return null

  return (
    <Modal
      abierto={abierto}
      titulo="Dar de baja a miembro"
      alCancelar={alCerrar}
      acciones={
        <Boton variante="peligro" onClick={() => {}}>
          Confirmar baja
        </Boton>
      }
    >
      <p>Baja stub</p>
    </Modal>
  )
}
