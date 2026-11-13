import { useId } from 'react'
import { Link } from 'react-router'
import { useSesion } from '../../api/sesion'
import '../../components/Boton.css'
import { KintiGuia } from '../../components/KintiGuia'
import { mensajeKintiPlanta } from './mensajeKintiPlanta'
import type { PlantaDetalle } from './types'
import './KintiFichaPlanta.css'

export function KintiFichaPlanta({ planta }: { planta: PlantaDetalle }) {
  const idTitulo = useId()
  const sesion = useSesion()
  const mensaje = mensajeKintiPlanta(planta)

  return (
    <section className="kinti-ficha" aria-labelledby={idTitulo}>
      <KintiGuia variante="celebracion" tamano={128} alt="" className="kinti-ficha-imagen" />
      <div className="kinti-ficha-contenido">
        <h2 id={idTitulo} className="kinti-ficha-titulo">
          Funda tu comunidad y siembra: {planta.nombreComun}
        </h2>
        {mensaje && <p className="kinti-ficha-mensaje">{mensaje}</p>}
        {sesion === null && (
          <Link to="/solicitar-comunidad" className="boton boton-primario kinti-ficha-boton">
            Solicitar creación de comunidad
          </Link>
        )}
      </div>
    </section>
  )
}
