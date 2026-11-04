import { useId, type ReactNode } from 'react'
import { Boton } from './Boton'
import './Modal.css'

export interface ModalProps {
  abierto: boolean
  titulo: string
  children: ReactNode
  acciones?: ReactNode
  alCancelar: () => void
}

export function Modal({ abierto, titulo, children, acciones, alCancelar }: ModalProps) {
  const idTitulo = useId()

  if (!abierto) return null

  return (
    <div className="modal-fondo">
      <div role="dialog" aria-modal="true" aria-labelledby={idTitulo} className="modal">
        <h2 id={idTitulo} className="modal-titulo">
          {titulo}
        </h2>
        <div className="modal-contenido">{children}</div>
        <div className="modal-acciones">
          <Boton variante="secundario" onClick={alCancelar}>
            Cancelar
          </Boton>
          {acciones}
        </div>
      </div>
    </div>
  )
}
