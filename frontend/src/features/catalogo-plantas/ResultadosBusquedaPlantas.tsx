import { useEffect, useState } from 'react'
import { Boton } from '../../components/Boton'
import { Notificacion } from '../../components/Notificacion'
import { ListaResultadosPlantas } from './ListaResultadosPlantas'
import { buscarPlantas } from './plantasApi'
import type { PlantaResumen } from './types'
import './CatalogoPlantasGrid.css'
import './ListaResultadosPlantas.css'

export interface ResultadosBusquedaPlantasProps {
  criterio: string
  alVerTodas: () => void
}

interface Resultado {
  criterio: string
  plantas: PlantaResumen[]
}

export function ResultadosBusquedaPlantas({ criterio, alVerTodas }: ResultadosBusquedaPlantasProps) {
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [criterioFallido, setCriterioFallido] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    buscarPlantas(criterio)
      .then((plantas) => {
        if (vigente) setResultado({ criterio, plantas })
      })
      .catch(() => {
        if (vigente) setCriterioFallido(criterio)
      })
    return () => {
      vigente = false
    }
  }, [criterio])

  const error = criterioFallido === criterio
  const plantas = resultado?.criterio === criterio ? resultado.plantas : null
  const cargando = plantas === null && !error

  return (
    <section className="catalogo-plantas" aria-busy={cargando}>
      {error && <Notificacion tipo="error" mensaje="No se pudo realizar la búsqueda. Intente nuevamente" />}
      {cargando && <p className="catalogo-plantas-aviso">Buscando plantas…</p>}
      {plantas?.length === 0 && (
        <div className="catalogo-plantas-vacio busqueda-plantas-vacio">
          <p role="status">No se encontraron plantas para «{criterio}»</p>
          <Boton variante="secundario" onClick={alVerTodas}>
            Ver todas las plantas
          </Boton>
        </div>
      )}
      {plantas !== null && plantas.length > 0 && (
        <>
          <p className="catalogo-plantas-aviso">
            {plantas.length} {plantas.length === 1 ? 'resultado' : 'resultados'} para «{criterio}»
          </p>
          <ListaResultadosPlantas plantas={plantas} />
        </>
      )}
    </section>
  )
}
