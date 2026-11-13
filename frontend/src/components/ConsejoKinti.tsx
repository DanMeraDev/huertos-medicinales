import { KintiGuia, type KintiVariante } from './KintiGuia'
import './ConsejoKinti.css'

interface ConsejoKintiProps {
  mensaje: string
  variante?: KintiVariante
}

export function ConsejoKinti({ mensaje, variante = 'sabiduria' }: ConsejoKintiProps) {
  return (
    <aside className="consejo-kinti" role="note" aria-label="Consejo de Kinti">
      <KintiGuia variante={variante} tamano="sm" alt="" aria-hidden="true" />
      <p className="consejo-kinti-mensaje">{mensaje}</p>
    </aside>
  )
}
