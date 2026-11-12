import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import { GestionSubhuertosView } from '../features/huerto-subhuertos/GestionSubhuertosView'
import {
  crearSubhuerto,
  obtenerAreaTotalHuerto,
  obtenerSubhuertos,
  type Subhuerto,
} from '../features/huerto-subhuertos/subhuertosApi'

export function SubhuertosPage() {
  const [subhuertos, setSubhuertos] = useState<Subhuerto[]>([])
  const [areaTotalM2, setAreaTotalM2] = useState<number | null>(null)
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  useEffect(() => {
    let activo = true
    Promise.all([obtenerSubhuertos(), obtenerAreaTotalHuerto()])
      .then(([lista, area]) => {
        if (activo) {
          setSubhuertos(lista)
          setAreaTotalM2(area)
          setCargando(false)
        }
      })
      .catch((err) => {
        if (activo) {
          setErrorCarga(err instanceof Error ? err.message : 'No se pudieron cargar los subhuertos')
          setCargando(false)
        }
      })
    return () => {
      activo = false
    }
  }, [])

  if (cargando) {
    return <p>Cargando zonificación del huerto…</p>
  }

  if (errorCarga) {
    return <Notificacion tipo="error" mensaje={errorCarga} />
  }

  return (
    <GestionSubhuertosView
      areaTotalHuertoM2={areaTotalM2}
      subhuertosIniciales={subhuertos}
      alCrear={crearSubhuerto}
    />
  )
}