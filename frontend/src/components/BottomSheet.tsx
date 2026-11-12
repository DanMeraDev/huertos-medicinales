import { useEffect, useEffectEvent, useId, useRef, type MouseEvent, type ReactNode } from 'react'
import './BottomSheet.css'

export interface BottomSheetProps {
  abierto: boolean
  titulo: string
  alCerrar: () => void
  children: ReactNode
  pie?: ReactNode
}

export function BottomSheet({ abierto, titulo, alCerrar, children, pie }: BottomSheetProps) {
  const idTitulo = useId()
  const panel = useRef<HTMLDivElement>(null)
  const cerrarConEscape = useEffectEvent((evento: KeyboardEvent) => {
    if (evento.key === 'Escape') alCerrar()
  })

  useEffect(() => {
    if (!abierto) return
    const enfocadoAntes = document.activeElement
    const desplazamientoAntes = document.body.style.overflow
    function alPresionarTecla(evento: KeyboardEvent) {
      cerrarConEscape(evento)
    }
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', alPresionarTecla)
    panel.current?.focus()
    return () => {
      document.removeEventListener('keydown', alPresionarTecla)
      document.body.style.overflow = desplazamientoAntes
      if (enfocadoAntes instanceof HTMLElement) enfocadoAntes.focus()
    }
  }, [abierto])

  function cerrarAlTocarFondo(evento: MouseEvent<HTMLDivElement>) {
    if (evento.target === evento.currentTarget) alCerrar()
  }

  if (!abierto) return null

  return (
    <div className="bottom-sheet-fondo" onClick={cerrarAlTocarFondo}>
      <div
        ref={panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        tabIndex={-1}
        className="bottom-sheet"
      >
        <div className="bottom-sheet-cabecera">
          <h2 id={idTitulo} className="bottom-sheet-titulo">
            {titulo}
          </h2>
          <button type="button" className="bottom-sheet-cerrar" aria-label="Cerrar" onClick={alCerrar}>
            <span aria-hidden="true">×</span>
          </button>
        </div>
        <div className="bottom-sheet-contenido">{children}</div>
        {pie && <div className="bottom-sheet-pie">{pie}</div>}
      </div>
    </div>
  )
}
