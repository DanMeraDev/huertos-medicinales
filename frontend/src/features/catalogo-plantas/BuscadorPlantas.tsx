import { useEffect, useRef, useState } from 'react'
import { CampoTexto } from '../../components/CampoTexto'
import './BuscadorPlantas.css'

const RETARDO_BUSQUEDA_MS = 300
const LONGITUD_MINIMA = 2
const CAMPOS_EDITABLES = 'input, textarea, select, [contenteditable]:not([contenteditable="false"])'

export interface BuscadorPlantasProps {
  alBuscar: (criterio: string) => void
}

function estaEditando(destino: EventTarget | null) {
  return destino instanceof Element && destino.closest(CAMPOS_EDITABLES) !== null
}

export function BuscadorPlantas({ alBuscar }: BuscadorPlantasProps) {
  const [texto, setTexto] = useState('')
  const campo = useRef<HTMLInputElement>(null)
  const temporizador = useRef<number | undefined>(undefined)

  useEffect(() => {
    function enfocarConBarra(evento: KeyboardEvent) {
      if (evento.key !== '/' || evento.ctrlKey || evento.altKey || evento.metaKey || estaEditando(evento.target)) {
        return
      }
      evento.preventDefault()
      campo.current?.focus()
    }
    document.addEventListener('keydown', enfocarConBarra)
    return () => {
      document.removeEventListener('keydown', enfocarConBarra)
      window.clearTimeout(temporizador.current)
    }
  }, [])

  function cambiarTexto(valor: string) {
    setTexto(valor)
    window.clearTimeout(temporizador.current)
    const criterio = valor.trim()
    if (criterio.length > 0 && criterio.length < LONGITUD_MINIMA) return
    temporizador.current = window.setTimeout(() => alBuscar(criterio), RETARDO_BUSQUEDA_MS)
  }

  function borrar() {
    window.clearTimeout(temporizador.current)
    setTexto('')
    alBuscar('')
    campo.current?.focus()
  }

  const faltanCaracteres = texto.length > 0 && texto.trim().length < LONGITUD_MINIMA

  return (
    <div className="buscador-plantas">
      <div className="buscador-plantas-campo">
        <CampoTexto
          ref={campo}
          type="search"
          enterKeyHint="search"
          etiqueta="Buscar planta"
          placeholder="Nombre común, científico o kichwa"
          valor={texto}
          alCambiar={cambiarTexto}
        />
        {texto && (
          <button type="button" className="buscador-plantas-borrar" aria-label="Borrar búsqueda" onClick={borrar}>
            <span aria-hidden="true">×</span>
          </button>
        )}
      </div>
      {faltanCaracteres && <p className="buscador-plantas-guia">Escribe al menos 2 caracteres...</p>}
    </div>
  )
}
