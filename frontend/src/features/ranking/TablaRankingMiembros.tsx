import { useMemo, useState } from 'react'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import { Selector } from '../../components/Selector'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import {
  ETIQUETAS_NIVEL_MIEMBRO,
  TONO_NIVEL_MIEMBRO,
  type RankingMiembro,
} from './types'
import './TablaRankingMiembros.css'

export interface TablaRankingMiembrosProps {
  voluntarios: RankingMiembro[]
  cargando?: boolean
}

export function TablaRankingMiembros({ voluntarios, cargando = false }: TablaRankingMiembrosProps) {
  const [busqueda, setBusqueda] = useState('')
  const [nivel, setNivel] = useState('TODOS')

  const opcionesNivel = [
    { valor: 'TODOS', etiqueta: 'Todos los niveles' },
    { valor: 'APRENDIZ', etiqueta: 'Aprendiz' },
    { valor: 'OFICIAL', etiqueta: 'Oficial' },
    { valor: 'MAESTRO', etiqueta: 'Maestro' },
  ]

  const voluntariosFiltrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase()
    return voluntarios.filter((v) => {
      const coincideNivel = !nivel || nivel === 'TODOS' || v.nivel === nivel
      const nombreCompleto = `${v.nombres} ${v.apellidos}`.toLowerCase()
      const coincideBusqueda = texto === '' || nombreCompleto.includes(texto)
      return coincideNivel && coincideBusqueda
    })
  }, [voluntarios, busqueda, nivel])

  const columnas: ColumnaTabla<RankingMiembro>[] = [
    {
      titulo: 'Posición',
      celda: (voluntario) => (
        <span className={`ranking-posicion ranking-pos-${voluntario.posicion <= 3 ? voluntario.posicion : 'comun'}`}>
          #{voluntario.posicion}
        </span>
      ),
    },
    {
      titulo: 'Nombre del Voluntario',
      celda: (voluntario) => (
        <div className="ranking-col-voluntario">
          <span className="ranking-nombre-voluntario">
            {voluntario.nombres} {voluntario.apellidos}
          </span>
          <span className="ranking-movil-subdato">
            <Insignia
              texto={ETIQUETAS_NIVEL_MIEMBRO[voluntario.nivel]}
              tono={TONO_NIVEL_MIEMBRO[voluntario.nivel]}
            />
            <span className="ranking-movil-medallas">🏅 {voluntario.totalInsignias}</span>
          </span>
        </div>
      ),
    },
    {
      titulo: 'Nivel',
      celda: (voluntario) => (
        <Insignia
          texto={ETIQUETAS_NIVEL_MIEMBRO[voluntario.nivel]}
          tono={TONO_NIVEL_MIEMBRO[voluntario.nivel]}
        />
      ),
    },
    {
      titulo: 'Puntos Totales',
      celda: (voluntario) => (
        <span className="ranking-puntos-destacados">{voluntario.puntosTotales}</span>
      ),
    },
    {
      titulo: 'Medallas / Insignias',
      celda: (voluntario) => (
        <span className="ranking-medallas-conteo">🏅 {voluntario.totalInsignias}</span>
      ),
    },
  ]

  if (!cargando && voluntarios.length === 0) {
    return (
      <section className="tabla-ranking-seccion" aria-label="Tabla de honor de voluntarios">
        <div className="ranking-estado-vacio" role="status">
          <span className="ranking-vacio-icono" aria-hidden="true">🌱</span>
          <p className="ranking-vacio-texto">
            No se registran voluntarios participantes en este momento
          </p>
        </div>
      </section>
    )
  }

  return (
    <section className="tabla-ranking-seccion" aria-label="Tabla de honor de voluntarios">
      <div className="tabla-ranking-filtros">
        <div className="tabla-ranking-filtro-item">
          <CampoTexto
            etiqueta="Buscar por nombre o apellido"
            valor={busqueda}
            alCambiar={setBusqueda}
            placeholder="Ej. Carmen Chuquimarca..."
          />
        </div>
        <div className="tabla-ranking-filtro-item">
          <Selector
            etiqueta="Filtrar por nivel"
            opciones={opcionesNivel}
            valor={nivel}
            alCambiar={setNivel}
          />
        </div>
      </div>

      <div className="tabla-ranking-contenedor">
        <Tabla
          columnas={columnas}
          filas={voluntariosFiltrados}
          claveFila={(voluntario) => voluntario.miembroId}
          cargando={cargando}
        />
      </div>
    </section>
  )
}

