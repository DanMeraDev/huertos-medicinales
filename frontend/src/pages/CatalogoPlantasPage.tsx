import { useState } from 'react'
import { BuscadorPlantas } from '../features/catalogo-plantas/BuscadorPlantas'
import { CatalogoPlantasGrid } from '../features/catalogo-plantas/CatalogoPlantasGrid'
import { ResultadosBusquedaPlantas } from '../features/catalogo-plantas/ResultadosBusquedaPlantas'
import './CatalogoPlantasPage.css'

export function CatalogoPlantasPage() {
  const [criterio, setCriterio] = useState('')
  const [reinicioBuscador, setReinicioBuscador] = useState(0)

  function verTodasLasPlantas() {
    setCriterio('')
    setReinicioBuscador((reinicio) => reinicio + 1)
  }

  return (
    <div className="catalogo-pagina">
      <header className="catalogo-pagina-cabecera">
        <h1>Catálogo de Plantas</h1>
        <BuscadorPlantas key={reinicioBuscador} alBuscar={setCriterio} />
      </header>
      <div className="catalogo-pagina-cuerpo">
        <div className="catalogo-pagina-contenido">
          {criterio ? (
            <ResultadosBusquedaPlantas criterio={criterio} alVerTodas={verTodasLasPlantas} />
          ) : (
            <CatalogoPlantasGrid />
          )}
        </div>
      </div>
    </div>
  )
}
