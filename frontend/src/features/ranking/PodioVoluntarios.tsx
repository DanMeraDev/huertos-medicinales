import { Insignia } from '../../components/Insignia'
import { Tarjeta } from '../../components/Tarjeta'
import { ETIQUETAS_NIVEL_MIEMBRO, TONO_NIVEL_MIEMBRO, type RankingMiembro } from './types'
import './PodioVoluntarios.css'

export interface PodioVoluntariosProps {
  voluntarios: RankingMiembro[]
}

const CONFIG_PODIO = {
  1: {
    icono: '🥇',
    titulo: '1º Lugar · Oro',
    tono: 'advertencia' as const,
    clase: 'podio-oro',
  },
  2: {
    icono: '🥈',
    titulo: '2º Lugar · Plata',
    tono: 'neutro' as const,
    clase: 'podio-plata',
  },
  3: {
    icono: '🥉',
    titulo: '3º Lugar · Bronce',
    tono: 'exito' as const,
    clase: 'podio-bronce',
  },
}

export function PodioVoluntarios({ voluntarios }: PodioVoluntariosProps) {
  const top3 = voluntarios.slice(0, 3)

  if (top3.length === 0) {
    return null
  }

  return (
    <section className="podio-voluntarios-seccion" aria-label="Cuadro de honor de voluntarios">
      <h2 className="podio-voluntarios-titulo">Voluntarios Destacados</h2>
      <div className="podio-voluntarios-contenedor">
        {top3.map((voluntario) => {
          const config = CONFIG_PODIO[voluntario.posicion as 1 | 2 | 3] ?? {
            icono: '🏅',
            titulo: `${voluntario.posicion}º Lugar`,
            tono: 'neutro' as const,
            clase: '',
          }

          return (
            <Tarjeta
              key={voluntario.miembroId}
              className={`podio-tarjeta ${config.clase}`}
            >
              <div className="podio-cabecera">
                <span className="podio-medalla" aria-hidden="true">
                  {config.icono}
                </span>
                <Insignia texto={config.titulo} tono={config.tono} />
              </div>
              <h3 className="podio-nombre">{voluntario.nombres} {voluntario.apellidos}</h3>
              <div className="podio-rango-nivel">
                <Insignia
                  texto={ETIQUETAS_NIVEL_MIEMBRO[voluntario.nivel]}
                  tono={TONO_NIVEL_MIEMBRO[voluntario.nivel]}
                />
              </div>
              <div className="podio-estadisticas">
                <div className="podio-stat-item">
                  <span className="podio-stat-valor">{voluntario.puntosTotales}</span>
                  <span className="podio-stat-label">puntos</span>
                </div>
                <div className="podio-stat-item">
                  <span className="podio-stat-valor">🏅 {voluntario.totalInsignias}</span>
                  <span className="podio-stat-label">medallas</span>
                </div>
              </div>
            </Tarjeta>
          )
        })}
      </div>
    </section>
  )
}

