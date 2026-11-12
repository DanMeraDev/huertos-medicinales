import { Link } from 'react-router'
import { Insignia } from '../../components/Insignia'
import { FotoPlanta } from './FotoPlanta'
import { TarjetaPlanta } from './TarjetaPlanta'
import { INSIGNIAS_TERMICAS, type PlantaResumen } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import './CatalogoPlantasGrid.css'
import './ListaResultadosPlantas.css'

export interface ListaResultadosPlantasProps {
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

export function ListaResultadosPlantas({ plantas }: ListaResultadosPlantasProps) {
  const esEscritorio = useEsEscritorio()

  return (
    <ul className={esEscritorio ? 'catalogo-plantas-grid' : 'busqueda-plantas-lista'}>
      {plantas.map((planta) => (
        <li key={planta.id}>{esEscritorio ? <TarjetaPlanta planta={planta} /> : <FilaPlanta planta={planta} />}</li>
      ))}
    </ul>
  )
}
