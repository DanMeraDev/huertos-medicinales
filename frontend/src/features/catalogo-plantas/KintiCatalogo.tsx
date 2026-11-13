import { KintiGuia } from '../../components/KintiGuia'
import './KintiCatalogo.css'

export function KintiCatalogo() {
  return (
    <div className="kinti-catalogo">
      <KintiGuia variante="sabiduria" tamano={128} alt="" className="kinti-catalogo-imagen" />
      <p className="kinti-catalogo-globo">
        Aquí puedes conocer sobre las plantas medicinales. ¿Por qué no sembrar las tuyas?
      </p>
    </div>
  )
}
