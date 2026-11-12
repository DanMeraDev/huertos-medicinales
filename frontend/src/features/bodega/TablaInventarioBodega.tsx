import { useEffect, useMemo, useState } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { obtenerInventarioPorComunidad } from './bodegaApi'
import type { MaterialVegetalResponse } from './types'
import './TablaInventarioBodega.css'

export interface ComunidadSupervisada {
  id: number
  nombre: string
}

export interface TablaInventarioBodegaProps {
  comunidadId: number
  esTecnicoMsp?: boolean
  comunidadesSupervisadas?: ComunidadSupervisada[]
  alCambiarComunidad?: (comunidadId: number) => void
  alRegistrarBaja?: (material: MaterialVegetalResponse) => void
}

const OPCIONES_TIPO_MATERIAL: OpcionSelector[] = [
  { valor: 'TODOS', etiqueta: 'Todos los tipos' },
  { valor: 'SEMILLAS', etiqueta: 'Semillas' },
  { valor: 'ESTACAS', etiqueta: 'Estacas' },
  { valor: 'ESQUEJES', etiqueta: 'Esquejes' },
  { valor: 'PLANTULAS', etiqueta: 'Plántulas' },
]

export function TablaInventarioBodega({
  comunidadId,
  esTecnicoMsp = false,
  comunidadesSupervisadas = [],
  alCambiarComunidad,
  alRegistrarBaja,
}: TablaInventarioBodegaProps) {
  const [inventario, setInventario] = useState<MaterialVegetalResponse[]>([])
  const [cargando, setCargando] = useState(true)
  const [mensajeError, setMensajeError] = useState<string | null>(null)
  const [busquedaPlanta, setBusquedaPlanta] = useState('')
  const [tipoMaterialFiltro, setTipoMaterialFiltro] = useState('TODOS')

  useEffect(() => {
    let cancelado = false

    obtenerInventarioPorComunidad(comunidadId)
      .then((datos) => {
        if (!cancelado) {
          setInventario(datos)
          setCargando(false)
        }
      })
      .catch((error: unknown) => {
        if (!cancelado) {
          if (error instanceof ApiError) {
            setMensajeError(error.detalle ?? error.message)
          } else {
            setMensajeError('Error al consultar el inventario de la bodega')
          }
          setInventario([])
          setCargando(false)
        }
      })

    return () => {
      cancelado = true
    }
  }, [comunidadId])

  const inventarioFiltrado = useMemo(() => {
    return inventario.filter((item) => {
      const coincideNombre =
        item.nombreComunPlanta.toLowerCase().includes(busquedaPlanta.toLowerCase()) ||
        item.nombreCientificoPlanta.toLowerCase().includes(busquedaPlanta.toLowerCase())

      const coincideTipo =
        tipoMaterialFiltro === 'TODOS' || item.tipoMaterial === tipoMaterialFiltro

      return coincideNombre && coincideTipo
    })
  }, [inventario, busquedaPlanta, tipoMaterialFiltro])

  const columnas: ColumnaTabla<MaterialVegetalResponse>[] = [
    {
      titulo: 'Planta',
      celda: (fila) => (
        <div className="bodega-inventario__planta">
          <span className="bodega-inventario__planta-comun">{fila.nombreComunPlanta}</span>
          <span className="bodega-inventario__planta-cientifico">({fila.nombreCientificoPlanta})</span>
        </div>
      ),
    },
    {
      titulo: 'Tipo de material',
      celda: (fila) => fila.tipoMaterial,
    },
    {
      titulo: 'Stock disponible',
      celda: (fila) => `${fila.cantidadDisponible} ${fila.unidadMedida}`,
    },
    {
      titulo: 'Lote / Origen',
      celda: (fila) => fila.loteOrigen || '—',
    },
    {
      titulo: 'Fecha de ingreso',
      celda: (fila) => fila.fechaIngreso,
    },
    {
      titulo: 'Acciones',
      celda: (fila) => (
        <Boton
          variante="secundario"
          className="bodega-inventario__boton-accion"
          onClick={() => alRegistrarBaja?.(fila)}
        >
          Registrar baja
        </Boton>
      ),
    },
  ]

  const opcionesComunidades: OpcionSelector[] = comunidadesSupervisadas.map((comunidad) => ({
    valor: String(comunidad.id),
    etiqueta: comunidad.nombre,
  }))

  return (
    <section className="bodega-inventario">
      <div className="bodega-inventario__encabezado">
        <h2 className="bodega-inventario__titulo">Inventario de Bodega</h2>
      </div>

      {mensajeError && <Notificacion tipo="error" mensaje={mensajeError} />}

      <div className="bodega-inventario__filtros">
        {esTecnicoMsp && comunidadesSupervisadas.length > 0 && (
          <Selector
            etiqueta="Comunidad a supervisar"
            opciones={opcionesComunidades}
            valor={String(comunidadId)}
            alCambiar={(nuevoValor) => alCambiarComunidad?.(Number(nuevoValor))}
          />
        )}

        <CampoTexto
          etiqueta="Buscar por planta"
          valor={busquedaPlanta}
          alCambiar={setBusquedaPlanta}
          placeholder="Ej. Manzanilla"
        />

        <Selector
          etiqueta="Filtrar por tipo"
          opciones={OPCIONES_TIPO_MATERIAL}
          valor={tipoMaterialFiltro}
          alCambiar={setTipoMaterialFiltro}
        />
      </div>

      <div className="bodega-inventario__tabla-contenedor">
        {inventario.length === 0 && !cargando && !mensajeError ? (
          <p className="bodega-inventario__mensaje-vacio">
            No se registra material vegetal en la bodega de esta comunidad
          </p>
        ) : (
          <Tabla
            columnas={columnas}
            filas={inventarioFiltrado}
            claveFila={(fila) => fila.id}
            cargando={cargando}
          />
        )}
      </div>
    </section>
  )
}
