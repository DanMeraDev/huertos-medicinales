import { useState } from 'react'
import { Link } from 'react-router'
import { esTecnicoMsp } from '../features/catalogo-admin/sesionTecnicoMsp'
import { BuscadorPlantas } from '../features/catalogo-plantas/BuscadorPlantas'
import { CatalogoPlantasGrid } from '../features/catalogo-plantas/CatalogoPlantasGrid'
import { MensajeDeNavegacion } from '../features/catalogo-plantas/MensajeDeNavegacion'
import { PanelFiltrosPlantas } from '../features/catalogo-plantas/PanelFiltrosPlantas'
import { ResultadosBusquedaPlantas } from '../features/catalogo-plantas/ResultadosBusquedaPlantas'
import { ResultadosFiltroPlantas } from '../features/catalogo-plantas/ResultadosFiltroPlantas'
import { contarFiltrosActivos, type FiltrosPlanta } from '../features/catalogo-plantas/types'
import '../components/Boton.css'
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
      <MensajeDeNavegacion />
      <header className="catalogo-pagina-cabecera">
        <div className="catalogo-pagina-titulo">
          <h1>Catálogo de Plantas</h1>
          {esTecnicoMsp() && (
            <Link to="/admin/plantas/nueva" className="boton boton-primario catalogo-pagina-registrar">
              Registrar planta
            </Link>
          )}
        </div>
        <BuscadorPlantas key={reinicioBuscador} alBuscar={setCriterio} />
      </header>
      <div className="catalogo-pagina-cuerpo">
        <PanelFiltrosPlantas filtros={filtros} alCambiar={setFiltros} />
        <div className="catalogo-pagina-contenido">{resultados()}</div>
      </div>
    </div>
  )
}
