import { RutaSecuencialSaberes } from '../features/saberes-ancestrales/RutaSecuencialSaberes'
import './SaberesAncestralesPage.css'

export function SaberesAncestralesPage() {
  return (
    <main className="pagina-saberes">
      <h1 className="pagina-saberes-titulo">Saberes ancestrales</h1>
      <p className="pagina-saberes-descripcion">
        La ruta secuencial de aprendizaje aprobada por el MSP: cada paso del
        huerto medicinal, con su nombre en kichwa, su fase lunar ideal, sus
        herramientas tradicionales y la insignia oficial que otorga.
      </p>
      <RutaSecuencialSaberes />
    </main>
  )
}
