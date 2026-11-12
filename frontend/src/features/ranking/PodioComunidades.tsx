import { Insignia } from '../../components/Insignia'
import { Tarjeta } from '../../components/Tarjeta'
import { ETIQUETAS_TIPO_ORGANIZACION, type RankingComunidad } from './types'
import './PodioComunidades.css'

export interface PodioComunidadesProps {
  comunidades: RankingComunidad[]
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

export function PodioComunidades({ comunidades }: PodioComunidadesProps) {
  const top3 = comunidades.slice(0, 3)

  if (top3.length === 0) {
    return null
  }

  return (
    <section className="podio-seccion" aria-label="Cuadro de honor de comunidades">
      <h2 className="podio-titulo">Comunidades Líderes</h2>
      <div className="podio-contenedor">
        {top3.map((comunidad) => {
          const config = CONFIG_PODIO[comunidad.posicion as 1 | 2 | 3] ?? {
            icono: '🏅',
            titulo: `${comunidad.posicion}º Lugar`,
            tono: 'neutro' as const,
            clase: '',
          }

          return (
            <Tarjeta
              key={comunidad.comunidadId}
              className={`podio-tarjeta ${config.clase}`}
            >
              <div className="podio-cabecera">
                <span className="podio-medalla" aria-hidden="true">
                  {config.icono}
                </span>
                <Insignia texto={config.titulo} tono={config.tono} />
              </div>
              <h3 className="podio-nombre">{comunidad.nombreComunidad}</h3>
              <p className="podio-ubicacion">
                📍 {comunidad.parroquia}, {comunidad.provincia}
              </p>
              <span className="podio-tipo">
                {ETIQUETAS_TIPO_ORGANIZACION[comunidad.tipoOrganizacion]}
              </span>
              <div className="podio-puntos">
                <span className="podio-puntos-valor">{comunidad.puntosComunidad}</span>
                <span className="podio-puntos-etiqueta">puntos</span>
              </div>
            </Tarjeta>
          )
        })}
      </div>
    </section>
  )
}
