import { useMemo, useState } from 'react'
import { CampoTexto } from '../../components/CampoTexto'
import { Selector } from '../../components/Selector'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { ETIQUETAS_TIPO_ORGANIZACION, type RankingComunidad } from './types'
import './TablaRankingComunidades.css'

export interface TablaRankingComunidadesProps {
  comunidades: RankingComunidad[]
  cargando?: boolean
}

export function TablaRankingComunidades({ comunidades, cargando = false }: TablaRankingComunidadesProps) {
  const [busqueda, setBusqueda] = useState('')
  const [provincia, setProvincia] = useState('TODAS')

  const opcionesProvincia = useMemo(() => {
    const unicas = Array.from(new Set(comunidades.map((c) => c.provincia))).sort()
    return [
      { valor: 'TODAS', etiqueta: 'Todas las provincias' },
      ...unicas.map((prov) => ({ valor: prov, etiqueta: prov })),
    ]
  }, [comunidades])

  const comunidadesFiltradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase()
    return comunidades.filter((c) => {
      const coincideProvincia = !provincia || provincia === 'TODAS' || c.provincia === provincia
      const coincideBusqueda = texto === '' || c.nombreComunidad.toLowerCase().includes(texto)
      return coincideProvincia && coincideBusqueda
    })
  }, [comunidades, busqueda, provincia])

  const sinPuntos = comunidades.length > 0 && comunidades.every((c) => c.puntosComunidad === 0)

  const columnas: ColumnaTabla<RankingComunidad>[] = [
    {
      titulo: 'Posición',
      celda: (comunidad) => (
        <span className={`ranking-posicion ranking-pos-${comunidad.posicion <= 3 ? comunidad.posicion : 'comun'}`}>
          #{comunidad.posicion}
        </span>
      ),
    },
    {
      titulo: 'Nombre de la Comunidad',
      celda: (comunidad) => (
        <div className="ranking-col-comunidad">
          <span className="ranking-nombre-comunidad">{comunidad.nombreComunidad}</span>
          <span className="ranking-movil-subdato">
            {comunidad.parroquia}, {comunidad.provincia} · {ETIQUETAS_TIPO_ORGANIZACION[comunidad.tipoOrganizacion]}
          </span>
        </div>
      ),
    },
    {
      titulo: 'Provincia / Parroquia',
      celda: (comunidad) => `${comunidad.provincia} / ${comunidad.parroquia}`,
    },
    {
      titulo: 'Tipo de Organización',
      celda: (comunidad) => ETIQUETAS_TIPO_ORGANIZACION[comunidad.tipoOrganizacion],
    },
    {
      titulo: 'Puntos Acumulados',
      celda: (comunidad) => (
        <span className="ranking-puntos-destacados">{comunidad.puntosComunidad}</span>
      ),
    },
  ]

  return (
    <section className="tabla-ranking-seccion" aria-label="Tabla de posiciones de comunidades">
      <div className="tabla-ranking-filtros">
        <div className="tabla-ranking-filtro-item">
          <CampoTexto
            etiqueta="Buscar comunidad por nombre"
            valor={busqueda}
            alCambiar={setBusqueda}
            placeholder="Ej. Allpa Kawsay..."
          />
        </div>
        <div className="tabla-ranking-filtro-item">
          <Selector
            etiqueta="Filtrar por provincia"
            opciones={opcionesProvincia}
            valor={provincia}
            alCambiar={setProvincia}
          />
        </div>
      </div>

      {sinPuntos && (
        <div className="ranking-aviso-sin-puntos" role="status">
          <span className="ranking-aviso-icono" aria-hidden="true">🌱</span>
          <p className="ranking-aviso-texto">
            ¡Sé parte de la primera comunidad en sumar puntos ancestrales!
          </p>
        </div>
      )}

      <div className="tabla-ranking-contenedor">
        <Tabla
          columnas={columnas}
          filas={comunidadesFiltradas}
          claveFila={(comunidad) => comunidad.comunidadId}
          cargando={cargando}
        />
      </div>
    </section>
  )
}
