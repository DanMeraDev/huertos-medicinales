import { useId } from 'react'
import { Link } from 'react-router'
import '../../components/Boton.css'
import { Insignia } from '../../components/Insignia'
import { Tarjeta } from '../../components/Tarjeta'
import { FotoPlanta } from './FotoPlanta'
import {
  ETIQUETAS_CATEGORIA,
  ETIQUETAS_CICLO,
  ETIQUETAS_ORIGEN,
  INSIGNIAS_TERMICAS,
  type PlantaDetalle,
} from './types'
import { useEsEscritorio } from './useEsEscritorio'
import './CatalogoPlantasGrid.css'
import './FichaTecnicaView.css'

const NO_REGISTRADO = 'No registrado'
const RUTA_CATALOGO = '/catalogo-plantas'
const formatoMetros = new Intl.NumberFormat('es-EC')

export interface FichaTecnicaViewProps {
  planta: PlantaDetalle
}

function formatearAltitud(minima: number | null, maxima: number | null) {
  if (minima !== null && maxima !== null) {
    return `${formatoMetros.format(minima)} – ${formatoMetros.format(maxima)} msnm`
  }
  if (minima !== null) return `Desde ${formatoMetros.format(minima)} msnm`
  if (maxima !== null) return `Hasta ${formatoMetros.format(maxima)} msnm`
  return NO_REGISTRADO
}

function ParametrosCultivo({ planta, className }: { planta: PlantaDetalle; className: string }) {
  const parametros = [
    { etiqueta: 'Altitud', valor: formatearAltitud(planta.altitudMinMsnm, planta.altitudMaxMsnm) },
    { etiqueta: 'Ciclo de cultivo', valor: planta.cicloCultivo ? ETIQUETAS_CICLO[planta.cicloCultivo] : NO_REGISTRADO },
    { etiqueta: 'Familia botánica', valor: planta.familiaBotanica },
    { etiqueta: 'Origen', valor: planta.origen ? ETIQUETAS_ORIGEN[planta.origen] : NO_REGISTRADO },
    { etiqueta: 'Regiones', valor: planta.regionesDistribucion ?? NO_REGISTRADO },
  ]

  return (
    <dl className={`ficha-tecnica-parametros ${className}`}>
      {parametros.map(({ etiqueta, valor }) => (
        <div key={etiqueta} className="ficha-tecnica-parametro">
          <dt>{etiqueta}</dt>
          <dd>{valor}</dd>
        </div>
      ))}
    </dl>
  )
}

function FichaMedica({ planta }: FichaTecnicaViewProps) {
  const idUsos = useId()
  const idPreparacion = useId()
  const idContraindicaciones = useId()

  return (
    <>
      <header className="ficha-tecnica-encabezado">
        <h1 className="ficha-tecnica-titulo">{planta.nombreComun}</h1>
        {planta.nombreKichwa && (
          <p className="ficha-tecnica-kichwa" lang="qu">
            «{planta.nombreKichwa}»
          </p>
        )}
        <p className="ficha-tecnica-cientifico">
          <em>{planta.nombreCientifico}</em>
        </p>
      </header>
      <section className="ficha-tecnica-seccion" aria-labelledby={idUsos}>
        <h2 id={idUsos}>Usos terapéuticos</h2>
        <ul className="planta-categorias" aria-label="Categorías de uso">
          {planta.categorias.map((categoria) => (
            <li key={categoria}>
              <Insignia texto={ETIQUETAS_CATEGORIA[categoria]} />
            </li>
          ))}
        </ul>
        <p>{planta.usosMedicinales}</p>
      </section>
      <section className="ficha-tecnica-seccion" aria-labelledby={idPreparacion}>
        <h2 id={idPreparacion}>Preparación y dosificación</h2>
        <p className="ficha-tecnica-texto-largo">{planta.preparacionDosificacion ?? NO_REGISTRADO}</p>
      </section>
      <aside className="ficha-tecnica-contraindicaciones" aria-labelledby={idContraindicaciones}>
        <h2 id={idContraindicaciones}>Contraindicaciones y precauciones</h2>
        <p className="ficha-tecnica-texto-largo">
          {planta.contraindicaciones ?? 'Sin información registrada sobre contraindicaciones'}
        </p>
      </aside>
    </>
  )
}

function FotoConInsignia({ planta }: FichaTecnicaViewProps) {
  return (
    <div className="ficha-tecnica-foto">
      <FotoPlanta ruta={planta.fotografiaUrl} nombreComun={planta.nombreComun} />
      <div className="ficha-tecnica-insignia">
        <Insignia {...INSIGNIAS_TERMICAS[planta.cualidadTermica]} />
      </div>
    </div>
  )
}

function FichaEscritorio({ planta }: FichaTecnicaViewProps) {
  const idParametros = useId()

  return (
    <div className="ficha-tecnica ficha-tecnica-escritorio">
      <div className="ficha-tecnica-barra">
        <Link to={RUTA_CATALOGO} className="boton boton-secundario ficha-tecnica-volver">
          <span aria-hidden="true">‹</span> Volver al catálogo
        </Link>
        <nav aria-label="Migas de pan">
          <ol className="ficha-tecnica-migas">
            <li>
              <Link to={RUTA_CATALOGO}>Catálogo de Plantas</Link>
            </li>
            <li>
              <span aria-hidden="true" className="ficha-tecnica-separador">
                ›
              </span>
              <span aria-current="page">{planta.nombreComun}</span>
            </li>
          </ol>
        </nav>
      </div>
      <div className="ficha-tecnica-columnas">
        <div className="ficha-tecnica-columna">
          <FotoConInsignia planta={planta} />
          <Tarjeta className="ficha-tecnica-cultivo" aria-labelledby={idParametros}>
            <h2 id={idParametros}>Parámetros de cultivo</h2>
            <ParametrosCultivo planta={planta} className="ficha-tecnica-parametros-cuadricula" />
          </Tarjeta>
        </div>
        <div className="ficha-tecnica-columna">
          <FichaMedica planta={planta} />
        </div>
      </div>
    </div>
  )
}

function FichaMovil({ planta }: FichaTecnicaViewProps) {
  const idParametros = useId()

  return (
    <div className="ficha-tecnica ficha-tecnica-movil">
      <div className="ficha-tecnica-portada">
        <FotoConInsignia planta={planta} />
        <Link to={RUTA_CATALOGO} className="ficha-tecnica-volver-circular" aria-label="Volver al catálogo">
          <span aria-hidden="true">‹</span>
        </Link>
      </div>
      <section className="ficha-tecnica-seccion" aria-labelledby={idParametros}>
        <h2 id={idParametros}>Parámetros de cultivo</h2>
        <ParametrosCultivo planta={planta} className="ficha-tecnica-parametros-fila" />
      </section>
      <FichaMedica planta={planta} />
      <Link to={RUTA_CATALOGO} className="boton boton-secundario ficha-tecnica-volver-inferior">
        Volver al catálogo
      </Link>
    </div>
  )
}

export function FichaTecnicaView({ planta }: FichaTecnicaViewProps) {
  const esEscritorio = useEsEscritorio()

  return esEscritorio ? <FichaEscritorio planta={planta} /> : <FichaMovil planta={planta} />
}
