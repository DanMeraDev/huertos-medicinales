import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import { PodioComunidades } from '../features/ranking/PodioComunidades'
import { TablaRankingComunidades } from '../features/ranking/TablaRankingComunidades'
import { obtenerRankingComunidades } from '../features/ranking/rankingApi'
import type { RankingComunidad } from '../features/ranking/types'
import './RankingComunidadesPage.css'

export function RankingComunidadesPage() {
  const [comunidades, setComunidades] = useState<RankingComunidad[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let montado = true

    obtenerRankingComunidades()
      .then((datos) => {
        if (montado) {
          setComunidades(datos)
          setCargando(false)
        }
      })
      .catch((err: unknown) => {
        if (montado) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudo cargar el ranking de comunidades. Por favor, intenta de nuevo.',
          )
          setCargando(false)
        }
      })

    return () => {
      montado = false
    }
  }, [])

  return (
    <div className="ranking-pagina">
      <header className="ranking-pagina-cabecera">
        <h1 className="ranking-pagina-titulo">Ranking General de Comunidades</h1>
        <p className="ranking-pagina-subtitulo">
          Tabla de posiciones de comunidades de huertos medicinales en Ecuador, visualizando los puntos acumulados por actividades oficiales y saberes ancestrales.
        </p>
      </header>

      {error && <Notificacion tipo="error" mensaje={error} />}

      <div className="ranking-pagina-contenido">
        <PodioComunidades comunidades={comunidades} />
        <TablaRankingComunidades comunidades={comunidades} cargando={cargando} />
      </div>
    </div>
  )
}
