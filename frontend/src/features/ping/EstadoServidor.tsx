import { useEffect, useState } from 'react'
import { obtenerPing, type PingResponse } from './pingApi'

type Estado =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; datos: PingResponse }
  | { tipo: 'error' }

export function EstadoServidor() {
  const [estado, setEstado] = useState<Estado>({ tipo: 'cargando' })

  useEffect(() => {
    obtenerPing()
      .then((datos) => setEstado({ tipo: 'ok', datos }))
      .catch(() => setEstado({ tipo: 'error' }))
  }, [])

  if (estado.tipo === 'cargando') return <p>Consultando el servidor…</p>
  if (estado.tipo === 'error') return <p role="alert">No se pudo conectar con el servidor.</p>
  return (
    <p>
      Servidor en línea. Base de datos: <strong>{estado.datos.baseDeDatos}</strong>
    </p>
  )
}
