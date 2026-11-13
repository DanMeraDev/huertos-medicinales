import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { Notificacion } from '../components/Notificacion'
import { PodioVoluntarios } from '../features/ranking/PodioVoluntarios'
import { TablaRankingMiembros } from '../features/ranking/TablaRankingMiembros'
import { obtenerRankingMiembros } from '../features/ranking/rankingApi'
import type { RankingMiembro } from '../features/ranking/types'
import { ConsejoKinti } from '../components/ConsejoKinti'
import './RankingVoluntariosPage.css'

export function RankingVoluntariosPage() {
  const [voluntarios, setVoluntarios] = useState<RankingMiembro[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let montado = true

    obtenerRankingMiembros()
      .then((datos) => {
        if (montado) {
          setVoluntarios(datos)
          setCargando(false)
        }
      })
      .catch((err: unknown) => {
        if (montado) {
          setError(
            err instanceof Error
              ? err.message
              : 'No se pudo cargar el medallero de voluntarios. Por favor, intenta de nuevo.',
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
        <h1 className="ranking-pagina-titulo">Tabla de Honor y Medallero Nacional</h1>
        <p className="ranking-pagina-subtitulo">
          Reconocimiento a los miembros y voluntarios más activos en la preservación de huertos y saberes ancestrales en Ecuador.
        </p>
      </header>

      <ConsejoKinti
        variante="celebracion"
        mensaje="Estos voluntarios son el corazón de los huertos. ¡Cada labor registrada los acerca al podio!"
      />

      <nav className="ranking-pestanas" aria-label="Selector de ranking">
        <Link to="/ranking/comunidades" className="ranking-pestana">
          🏡 Ranking de Comunidades
        </Link>
        <Link to="/ranking/voluntarios" className="ranking-pestana ranking-pestana-activa">
          🏅 Ranking de Voluntarios
        </Link>
      </nav>

      {error && <Notificacion tipo="error" mensaje={error} />}

      <div className="ranking-pagina-contenido">
        <PodioVoluntarios voluntarios={voluntarios} />
        <TablaRankingMiembros voluntarios={voluntarios} cargando={cargando} />
      </div>
    </div>
  )
}

