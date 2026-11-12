import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import {
  crearActividad,
  listarActividades,
  type ActividadCatalogo,
} from '../features/comunidad-actividades/actividadesCatalogoApi'
import { CatalogoActividadesView } from '../features/comunidad-actividades/CatalogoActividadesView'

export function CatalogoActividadesPage() {
  const [actividades, setActividades] = useState<ActividadCatalogo[]>([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  useEffect(() => {
    let activo = true
    listarActividades()
      .then((datos) => {
        if (activo) {
          setActividades(datos)
          setCargando(false)
        }
      })
      .catch((err) => {
        if (activo) {
          setErrorCarga(
            err instanceof Error ? err.message : 'No se pudo cargar el catálogo de actividades'
          )
          setCargando(false)
        }
      })
    return () => {
      activo = false
    }
  }, [])

  if (errorCarga) {
    return <Notificacion tipo="error" mensaje={errorCarga} />
  }

  return (
    <CatalogoActividadesView
      key={cargando ? 'cargando' : 'listo'}
      actividadesIniciales={actividades}
      cargando={cargando}
      alCrear={crearActividad}
    />
  )
}