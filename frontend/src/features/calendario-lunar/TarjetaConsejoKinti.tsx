import { KintiGuia } from '../../components/KintiGuia'
import type { CalendarioLunarResponse } from './calendarioLunarApi'
import './TarjetaConsejoKinti.css'

export interface TarjetaConsejoKintiProps {
  fase: CalendarioLunarResponse
}

export function TarjetaConsejoKinti({ fase }: TarjetaConsejoKintiProps) {
  return (
    <section className="tarjeta-kinti">
      <KintiGuia
        variante="lunar"
        tamano="md"
        alt="Kinti en su versión nocturna, junto al calendario lunar"
      />
      <div className="tarjeta-kinti-cuerpo">
        <div className="tarjeta-kinti-bloque">
          <h3 className="tarjeta-kinti-titulo">Prácticas recomendadas para el huerto</h3>
          <p className="tarjeta-kinti-texto">{fase.practicasRecomendadas}</p>
        </div>
        <div className="tarjeta-kinti-bloque">
          <h3 className="tarjeta-kinti-titulo">Consejo de Kinti</h3>
          <p className="tarjeta-kinti-texto tarjeta-kinti-consejo">{fase.consejoKinti}</p>
        </div>
      </div>
    </section>
  )
}
