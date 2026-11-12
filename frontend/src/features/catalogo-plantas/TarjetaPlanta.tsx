import { useId, useState } from 'react'
import { Link } from 'react-router'
import { urlFoto } from '../../api/fotos'
import '../../components/Boton.css'
import { Insignia, type InsigniaProps } from '../../components/Insignia'
import { Tarjeta } from '../../components/Tarjeta'
import { ETIQUETAS_CATEGORIA, type CualidadTermica, type PlantaResumen } from './types'

const INSIGNIAS_TERMICAS: Record<CualidadTermica, InsigniaProps> = {
  CALIENTE: { texto: 'Caliente', tono: 'error' },
  FRIA: { texto: 'Fría', tono: 'exito' },
}

interface FotoPlantaProps {
  ruta: string | null
  nombreComun: string
}

function FotoPlanta({ ruta, nombreComun }: FotoPlantaProps) {
  const [fallo, setFallo] = useState(false)
  const url = urlFoto(ruta)

  if (!url || fallo) {
    return (
      <div className="planta-foto planta-foto-marcador" role="img" aria-label={`Sin fotografía de ${nombreComun}`}>
        <span aria-hidden="true">{nombreComun.charAt(0)}</span>
      </div>
    )
  }
  return (
    <img
      className="planta-foto"
      src={url}
      alt={`Fotografía de ${nombreComun}`}
      loading="lazy"
      onError={() => setFallo(true)}
    />
  )
}

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
