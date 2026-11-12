import { useId } from 'react'
import { Link } from 'react-router'
import '../../components/Boton.css'
import { Insignia } from '../../components/Insignia'
import { Tarjeta } from '../../components/Tarjeta'
import { FotoPlanta } from './FotoPlanta'
import { ETIQUETAS_CATEGORIA, INSIGNIAS_TERMICAS, type PlantaResumen } from './types'

export interface TarjetaPlantaProps {
  planta: PlantaResumen
}

export function TarjetaPlanta({ planta }: TarjetaPlantaProps) {
  const idTitulo = useId()

  return (
    <Tarjeta className="planta-tarjeta" aria-labelledby={idTitulo}>
      <FotoPlanta ruta={planta.fotografiaUrl} nombreComun={planta.nombreComun} />
      <div className="planta-cuerpo">
        <h2 id={idTitulo} className="planta-nombre">
          {planta.nombreComun}
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
        <ul className="planta-categorias" aria-label="Categorías de uso">
          {planta.categorias.map((categoria) => (
            <li key={categoria}>
              <Insignia texto={ETIQUETAS_CATEGORIA[categoria]} />
            </li>
          ))}
        </ul>
        <Link to={`/catalogo-plantas/${planta.id}`} className="boton boton-primario planta-enlace">
          Ver ficha completa
        </Link>
      </div>
    </Tarjeta>
  )
}
