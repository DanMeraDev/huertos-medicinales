import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { Boton } from '../../components/Boton'
import { Insignia } from '../../components/Insignia'
import { Notificacion } from '../../components/Notificacion'
import { FotoPlanta } from './FotoPlanta'
import { buscarPlantas } from './plantasApi'
import { TarjetaPlanta } from './TarjetaPlanta'
import { INSIGNIAS_TERMICAS, type PlantaResumen } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import './CatalogoPlantasGrid.css'
import './ResultadosBusquedaPlantas.css'

export interface ResultadosBusquedaPlantasProps {
  criterio: string
  alVerTodas: () => void
}

interface Resultado {
  criterio: string
  plantas: PlantaResumen[]
}

function FilaPlanta({ planta }: { planta: PlantaResumen }) {
  return (
    <div className="busqueda-plantas-fila">
      <div className="busqueda-plantas-miniatura">
        <FotoPlanta ruta={planta.fotografiaUrl} nombreComun={planta.nombreComun} />
      </div>
      <div className="busqueda-plantas-datos">
        <h2 className="busqueda-plantas-nombre">
          <Link to={`/catalogo-plantas/${planta.id}`} className="busqueda-plantas-enlace">
            {planta.nombreComun}
          </Link>
        </h2>
        <p className="planta-secundario">
          <em>{planta.nombreCientifico}</em>
        </p>
        {planta.nombreKichwa && (
          <p className="planta-secundario">
            Kichwa: <span lang="qu">{planta.nombreKichwa}</span>
          </p>
        )}
        <Insignia {...INSIGNIAS_TERMICAS[planta.cualidadTermica]} />
      </div>
    </div>
  )
}

export function ResultadosBusquedaPlantas({ criterio, alVerTodas }: ResultadosBusquedaPlantasProps) {
  const esEscritorio = useEsEscritorio()
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
          <ul className={esEscritorio ? 'catalogo-plantas-grid' : 'busqueda-plantas-lista'}>
            {plantas.map((planta) => (
              <li key={planta.id}>
                {esEscritorio ? <TarjetaPlanta planta={planta} /> : <FilaPlanta planta={planta} />}
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}
