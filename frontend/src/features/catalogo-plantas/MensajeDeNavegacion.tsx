import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { Notificacion } from '../../components/Notificacion'
import './MensajeDeNavegacion.css'

const DURACION_MENSAJE_MS = 5000

interface EstadoNavegacion {
  mensajeExito?: string
}

function useMensajeDeNavegacion() {
  const { pathname, state } = useLocation()
  const navegar = useNavigate()
  const [mensaje, setMensaje] = useState((state as EstadoNavegacion | null)?.mensajeExito ?? null)

  useEffect(() => {
    if (state) navegar(pathname, { replace: true, state: null })
  }, [state, pathname, navegar])

  useEffect(() => {
    if (!mensaje) return
    const temporizador = setTimeout(() => setMensaje(null), DURACION_MENSAJE_MS)
    return () => clearTimeout(temporizador)
  }, [mensaje])

  return { mensaje, cerrar: () => setMensaje(null) }
}

export function MensajeDeNavegacion() {
  const { mensaje, cerrar } = useMensajeDeNavegacion()
  if (!mensaje) return null

  return (
    <div className="mensaje-navegacion">
      <Notificacion tipo="exito" mensaje={mensaje} />
      <button type="button" className="mensaje-navegacion-cerrar" aria-label="Cerrar notificación" onClick={cerrar}>
        ×
      </button>
    </div>
  )
}
