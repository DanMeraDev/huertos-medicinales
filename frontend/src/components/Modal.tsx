import { useEffect, useEffectEvent, useId, type MouseEvent, type ReactNode } from 'react'
import { Boton } from './Boton'
import './Modal.css'

export interface ModalProps {
  abierto: boolean
  titulo: string
  children: ReactNode
  acciones?: ReactNode
  alCancelar: () => void
  textoCancelar?: string
  className?: string
  cerrarConEscape?: boolean
  cerrarAlTocarFondo?: boolean
}

export function Modal({
  abierto,
  titulo,
  children,
  acciones,
  alCancelar,
  textoCancelar = 'Cancelar',
  className,
  cerrarConEscape = false,
  cerrarAlTocarFondo = false,
}: ModalProps) {
  const idTitulo = useId()
  const cancelarConEscape = useEffectEvent((evento: KeyboardEvent) => {
    if (evento.key === 'Escape') alCancelar()
  })

  useEffect(() => {
    if (!abierto || !cerrarConEscape) return
    function alPresionarTecla(evento: KeyboardEvent) {
      cancelarConEscape(evento)
    }
    document.addEventListener('keydown', alPresionarTecla)
    return () => document.removeEventListener('keydown', alPresionarTecla)
  }, [abierto, cerrarConEscape])

  function cancelarAlTocarFondo(evento: MouseEvent<HTMLDivElement>) {
    if (cerrarAlTocarFondo && evento.target === evento.currentTarget) alCancelar()
  }

  if (!abierto) return null

  const claseModal = ['modal', className].filter(Boolean).join(' ')

  return (
    <div className="modal-fondo" onClick={cancelarAlTocarFondo}>
      <div role="dialog" aria-modal="true" aria-labelledby={idTitulo} className={claseModal}>
        <h2 id={idTitulo} className="modal-titulo">
          {titulo}
        </h2>
        <div className="modal-contenido">{children}</div>
        <div className="modal-acciones">
          <Boton variante="secundario" onClick={alCancelar}>
            {textoCancelar}
          </Boton>
          {acciones}
        </div>
      </div>
    </div>
  )
}
