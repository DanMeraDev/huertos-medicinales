import { useEffect, useState } from 'react'
import { Boton } from '../../components/Boton'
import { Notificacion } from '../../components/Notificacion'
import { Paginador } from '../../components/Paginador'
import { obtenerPlantasPublicas } from './plantasApi'
import { TarjetaPlanta } from './TarjetaPlanta'
import type { PlantaResumen } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import './CatalogoPlantasGrid.css'

const TAMANO_INICIAL = 12

interface Consulta {
  pagina: number
  tamano: number
  acumular: boolean
}

interface Resultado {
  plantas: PlantaResumen[]
  pagina: number
  totalElementos: number
  totalPaginas: number
}

export function CatalogoPlantasGrid() {
  const esEscritorio = useEsEscritorio()
  const [consulta, setConsulta] = useState<Consulta>({ pagina: 0, tamano: TAMANO_INICIAL, acumular: false })
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    let vigente = true
    obtenerPlantasPublicas(consulta.pagina, consulta.tamano)
      .then((respuesta) => {
        if (!vigente) return
        setResultado((anterior) => ({
          plantas: consulta.acumular && anterior ? [...anterior.plantas, ...respuesta.contenido] : respuesta.contenido,
          pagina: respuesta.pagina,
          totalElementos: respuesta.totalElementos,
          totalPaginas: respuesta.totalPaginas,
        }))
        setError(false)
      })
      .catch(() => {
        if (vigente) setError(true)
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [consulta])

  function consultar(nueva: Consulta) {
    setCargando(true)
    setConsulta(nueva)
  }

  const hayMasPaginas = resultado !== null && resultado.pagina + 1 < resultado.totalPaginas

  return (
    <section
      className={`catalogo-plantas catalogo-plantas-${esEscritorio ? 'escritorio' : 'movil'}`}
      aria-busy={cargando}
    >
      {error && (
        <Notificacion
          tipo="error"
          mensaje="No se pudo cargar el catálogo de plantas. Por favor, intente nuevamente más tarde"
        />
      )}
      {cargando && resultado === null && <p className="catalogo-plantas-aviso">Cargando plantas…</p>}
      {resultado?.totalElementos === 0 && (
        <p role="status" className="catalogo-plantas-vacio">
          Actualmente no hay plantas medicinales registradas en el catálogo oficial
        </p>
      )}
      {resultado !== null && resultado.plantas.length > 0 && (
        <>
          {esEscritorio && (
            <p className="catalogo-plantas-aviso">
              Mostrando {resultado.plantas.length} de {resultado.totalElementos} especies
            </p>
          )}
          <ul className="catalogo-plantas-grid">
            {resultado.plantas.map((planta) => (
              <li key={planta.id}>
                <TarjetaPlanta planta={planta} />
              </li>
            ))}
          </ul>
          {esEscritorio ? (
            <Paginador
              pagina={resultado.pagina}
              totalPaginas={resultado.totalPaginas}
              tamano={consulta.tamano}
              alCambiarPagina={(pagina) => consultar({ pagina, tamano: consulta.tamano, acumular: false })}
              alCambiarTamano={(tamano) => consultar({ pagina: 0, tamano, acumular: false })}
            />
          ) : (
            hayMasPaginas && (
              <div className="catalogo-plantas-mas">
                <Boton
                  variante="secundario"
                  disabled={cargando}
                  onClick={() => consultar({ pagina: resultado.pagina + 1, tamano: consulta.tamano, acumular: true })}
                >
                  Cargar más plantas
                </Boton>
              </div>
            )
          )}
        </>
      )}
    </section>
  )
}
