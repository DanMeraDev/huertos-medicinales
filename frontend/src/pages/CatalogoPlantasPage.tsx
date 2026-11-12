import { useState } from 'react'
import { BuscadorPlantas } from '../features/catalogo-plantas/BuscadorPlantas'
import { CatalogoPlantasGrid } from '../features/catalogo-plantas/CatalogoPlantasGrid'
import { PanelFiltrosPlantas } from '../features/catalogo-plantas/PanelFiltrosPlantas'
import { ResultadosBusquedaPlantas } from '../features/catalogo-plantas/ResultadosBusquedaPlantas'
import { ResultadosFiltroPlantas } from '../features/catalogo-plantas/ResultadosFiltroPlantas'
import { contarFiltrosActivos, type FiltrosPlanta } from '../features/catalogo-plantas/types'
import './CatalogoPlantasPage.css'

export function CatalogoPlantasPage() {
  const [criterio, setCriterio] = useState('')
  const [reinicioBuscador, setReinicioBuscador] = useState(0)
  const [filtros, setFiltros] = useState<FiltrosPlanta>({})

  function verTodasLasPlantas() {
    setCriterio('')
    setReinicioBuscador((reinicio) => reinicio + 1)
  }

  function resultados() {
    if (contarFiltrosActivos(filtros) > 0) {
      return <ResultadosFiltroPlantas filtros={filtros} criterio={criterio} alLimpiar={() => setFiltros({})} />
    }
    if (criterio) {
      return <ResultadosBusquedaPlantas criterio={criterio} alVerTodas={verTodasLasPlantas} />
    }
    return <CatalogoPlantasGrid />
  }

  return (
    <div className="catalogo-pagina">
      <header className="catalogo-pagina-cabecera">
        <h1>Catálogo de Plantas</h1>
        <BuscadorPlantas key={reinicioBuscador} alBuscar={setCriterio} />
      </header>
      <div className="catalogo-pagina-cuerpo">
        <PanelFiltrosPlantas filtros={filtros} alCambiar={setFiltros} />
        <div className="catalogo-pagina-contenido">{resultados()}</div>
      </div>
    </div>
  )
}
