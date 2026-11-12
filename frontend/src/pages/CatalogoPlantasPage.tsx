import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { Notificacion } from '../components/Notificacion'
import { esTecnicoMsp } from '../features/catalogo-admin/sesionTecnicoMsp'
import { BuscadorPlantas } from '../features/catalogo-plantas/BuscadorPlantas'
import { CatalogoPlantasGrid } from '../features/catalogo-plantas/CatalogoPlantasGrid'
import { PanelFiltrosPlantas } from '../features/catalogo-plantas/PanelFiltrosPlantas'
import { ResultadosBusquedaPlantas } from '../features/catalogo-plantas/ResultadosBusquedaPlantas'
import { ResultadosFiltroPlantas } from '../features/catalogo-plantas/ResultadosFiltroPlantas'
import { contarFiltrosActivos, type FiltrosPlanta } from '../features/catalogo-plantas/types'
import '../components/Boton.css'
import './CatalogoPlantasPage.css'

const DURACION_MENSAJE_MS = 5000

interface EstadoCatalogo {
  mensajeExito?: string
}

function useMensajeDeNavegacion() {
  const { pathname, state } = useLocation()
  const navegar = useNavigate()
  const [mensaje, setMensaje] = useState((state as EstadoCatalogo | null)?.mensajeExito ?? null)

  useEffect(() => {
    if (state) navegar(pathname, { replace: true, state: null })
  }, [state, pathname, navegar])

  useEffect(() => {
    if (!mensaje) return
    const temporizador = setTimeout(() => setMensaje(null), DURACION_MENSAJE_MS)
    return () => clearTimeout(temporizador)
  }, [mensaje])

  return { mensaje, cerrar: () => setMensaje(null) }
}

export function CatalogoPlantasPage() {
  const [criterio, setCriterio] = useState('')
  const [reinicioBuscador, setReinicioBuscador] = useState(0)
  const [filtros, setFiltros] = useState<FiltrosPlanta>({})
  const mensajeExito = useMensajeDeNavegacion()

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
      {mensajeExito.mensaje && (
        <div className="catalogo-pagina-aviso">
          <Notificacion tipo="exito" mensaje={mensajeExito.mensaje} />
          <button
            type="button"
            className="catalogo-pagina-aviso-cerrar"
            aria-label="Cerrar notificación"
            onClick={mensajeExito.cerrar}
          >
            ×
          </button>
        </div>
      )}
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
