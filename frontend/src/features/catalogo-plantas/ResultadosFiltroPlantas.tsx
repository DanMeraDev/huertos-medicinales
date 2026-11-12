import { useEffect, useState } from 'react'
import { Boton } from '../../components/Boton'
import { Notificacion } from '../../components/Notificacion'
import { ListaResultadosPlantas } from './ListaResultadosPlantas'
import { filtrarPlantas } from './plantasApi'
import type { FiltrosPlanta, PlantaResumen } from './types'
import './CatalogoPlantasGrid.css'
import './ListaResultadosPlantas.css'

export interface ResultadosFiltroPlantasProps {
  filtros: FiltrosPlanta
  criterio: string
  alLimpiar: () => void
}

interface Resultado {
  consulta: string
  plantas: PlantaResumen[]
}

export function ResultadosFiltroPlantas({ filtros, criterio, alLimpiar }: ResultadosFiltroPlantasProps) {
  const consulta = JSON.stringify([filtros, criterio])
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [consultaFallida, setConsultaFallida] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    filtrarPlantas(filtros, criterio)
      .then((plantas) => {
        if (vigente) setResultado({ consulta, plantas })
      })
      .catch(() => {
        if (vigente) setConsultaFallida(consulta)
      })
    return () => {
      vigente = false
    }
  }, [consulta, filtros, criterio])

  const error = consultaFallida === consulta
  const plantas = resultado?.consulta === consulta ? resultado.plantas : null
  const cargando = plantas === null && !error

  return (
    <section className="catalogo-plantas" aria-busy={cargando}>
      {error && <Notificacion tipo="error" mensaje="No se pudieron aplicar los filtros. Intente nuevamente" />}
      {cargando && <p className="catalogo-plantas-aviso">Filtrando plantas…</p>}
      {plantas?.length === 0 && (
        <div className="catalogo-plantas-vacio busqueda-plantas-vacio">
          <p role="status">No se encontraron plantas para estos parámetros. Intente reajustar los filtros</p>
          <Boton variante="secundario" onClick={alLimpiar}>
            Limpiar filtros
          </Boton>
        </div>
      )}
      {plantas !== null && plantas.length > 0 && (
        <>
          <p className="catalogo-plantas-aviso">
            {plantas.length} {plantas.length === 1 ? 'planta encontrada' : 'plantas encontradas'}
          </p>
          <ListaResultadosPlantas plantas={plantas} />
        </>
      )}
    </section>
  )
}
