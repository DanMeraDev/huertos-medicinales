import { useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { Notificacion } from '../components/Notificacion'
import { obtenerContextoBodega } from '../features/bodega/bodegaApi'
import { TablaInventarioBodega } from '../features/bodega/TablaInventarioBodega'
import type { ContextoBodegaResponse } from '../features/bodega/types'

export function InventarioBodegaPage() {
  const [contexto, setContexto] = useState<ContextoBodegaResponse | null>(null)
  const [comunidadId, setComunidadId] = useState<number | null>(null)
  const [mensajeError, setMensajeError] = useState<string | null>(null)

  useEffect(() => {
    let montado = true
    obtenerContextoBodega()
      .then((datos) => {
        if (montado) {
          setContexto(datos)
          setComunidadId(datos.comunidades[0]?.id ?? null)
        }
      })
      .catch((error: unknown) => {
        if (montado) {
          setMensajeError(
            error instanceof ApiError
              ? (error.detalle ?? error.message)
              : 'No se pudo obtener la información de la bodega',
          )
        }
      })
    return () => {
      montado = false
    }
  }, [])

  if (mensajeError) {
    return <Notificacion tipo="error" mensaje={mensajeError} />
  }

  if (!contexto || comunidadId === null) {
    return <p>Cargando…</p>
  }

  return (
    <div className="pagina-inventario-bodega">
      <TablaInventarioBodega
        comunidadId={comunidadId}
        esTecnicoMsp={contexto.rol === 'TECNICO_MSP'}
        comunidadesSupervisadas={contexto.comunidades}
        alCambiarComunidad={setComunidadId}
      />
    </div>
  )
}
