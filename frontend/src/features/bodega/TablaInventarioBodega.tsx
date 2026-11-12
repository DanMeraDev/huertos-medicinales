import { useEffect, useMemo, useState } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { Tarjeta } from '../../components/Tarjeta'
import { ApiError } from '../../api/client'
import { obtenerInventarioPorComunidad } from './bodegaApi'
import type { MaterialVegetalResponse, TipoMaterialVegetal } from './types'
import './TablaInventarioBodega.css'

export interface TablaInventarioBodegaProps {
  comunidadId?: number
  items?: MaterialVegetalResponse[]
  cargando?: boolean
  error?: string | null
  alDarDeBaja?: (item: MaterialVegetalResponse) => void
}

const OPCIONES_FILTRO_TIPO: OpcionSelector[] = [
  { valor: 'TODOS', etiqueta: 'Todos los tipos' },
  { valor: 'SEMILLAS', etiqueta: 'Semillas' },
  { valor: 'ESTACAS', etiqueta: 'Estacas' },
  { valor: 'ESQUEJES', etiqueta: 'Esquejes' },
  { valor: 'PLANTULAS', etiqueta: 'Plántulas' },
]

const MAPA_TIPO_MATERIAL: Record<TipoMaterialVegetal, string> = {
  SEMILLAS: 'Semillas',
  ESTACAS: 'Estacas',
  ESQUEJES: 'Esquejes',
  PLANTULAS: 'Plántulas',
}

export function TablaInventarioBodega({
  comunidadId,
  items,
  cargando: cargandoProp,
  error: errorProp,
  alDarDeBaja,
}: TablaInventarioBodegaProps) {
  const [inventarioCargado, setInventarioCargado] = useState<MaterialVegetalResponse[]>([])
  const [comunidadCargadaId, setComunidadCargadaId] = useState<number | null>(null)
  const [errorInterno, setErrorInterno] = useState<string | null>(null)
  const [busqueda, setBusqueda] = useState('')
  const [filtroTipo, setFiltroTipo] = useState<string>('TODOS')

  useEffect(() => {
    if (items !== undefined || comunidadId === undefined) {
      return
    }

    let montado = true

    obtenerInventarioPorComunidad(comunidadId)
      .then((datos) => {
        if (montado) {
          setInventarioCargado(datos)
          setComunidadCargadaId(comunidadId)
          setErrorInterno(null)
        }
      })
      .catch((err: unknown) => {
        if (montado) {
          const mensaje =
            err instanceof ApiError
              ? (err.detalle ?? err.message)
              : 'Error al consultar el inventario de la comunidad'
          setErrorInterno(mensaje)
          setComunidadCargadaId(comunidadId)
        }
      })

    return () => {
      montado = false
    }
  }, [comunidadId, items])

  const itemsBase = items ?? inventarioCargado
  const estaCargando =
    cargandoProp ??
    (comunidadId !== undefined && items === undefined && comunidadCargadaId !== comunidadId && !errorInterno)
  const mensajeError = errorProp ?? errorInterno

  const itemsFiltrados = useMemo(() => {
    return itemsBase.filter((item) => {
      const termino = busqueda.trim().toLowerCase()
      const coincideTexto =
        !termino ||
        item.nombreComunPlanta.toLowerCase().includes(termino) ||
        (item.nombreCientificoPlanta && item.nombreCientificoPlanta.toLowerCase().includes(termino))
      const coincideTipo = !filtroTipo || filtroTipo === 'TODOS' || item.tipoMaterial === filtroTipo
      return coincideTexto && coincideTipo
    })
  }, [itemsBase, busqueda, filtroTipo])

  const columnas: ColumnaTabla<MaterialVegetalResponse>[] = [
    {
      titulo: 'Planta',
      celda: (fila) => (
        <div>
          <div className="inventario-planta-comun">{fila.nombreComunPlanta}</div>
          {fila.nombreCientificoPlanta && (
            <div className="inventario-planta-cientifico">{fila.nombreCientificoPlanta}</div>
          )}
        </div>
      ),
    },
    {
      titulo: 'Tipo de Material',
      celda: (fila) => MAPA_TIPO_MATERIAL[fila.tipoMaterial] ?? fila.tipoMaterial,
    },
    {
      titulo: 'Stock Disponible',
      celda: (fila) => `${fila.cantidadDisponible} ${fila.unidadMedida}`,
    },
    {
      titulo: 'Lote / Origen',
      celda: (fila) => fila.loteOrigen || '—',
    },
    {
      titulo: 'Fecha de Ingreso',
      celda: (fila) => fila.fechaIngreso,
    },
    {
      titulo: 'Acciones',
      celda: (fila) => (
        <Boton variante="peligro" onClick={() => alDarDeBaja?.(fila)}>
          Dar de baja
        </Boton>
      ),
    },
  ]

  if (mensajeError) {
    return <Notificacion tipo="error" mensaje={mensajeError} />
  }

  if (estaCargando) {
    return <p className="tabla-inventario-cargando">Cargando inventario de bodega…</p>
  }

  if (comunidadId === undefined && items === undefined) {
    return (
      <p role="status" className="tabla-inventario-vacio">
        Seleccione una comunidad para consultar su inventario de bodega
      </p>
    )
  }

  return (
    <div className="tabla-inventario-contenedor">
      <div className="tabla-inventario-filtros">
        <CampoTexto
          etiqueta="Buscar por planta"
          placeholder="Nombre común o científico…"
          valor={busqueda}
          alCambiar={setBusqueda}
        />
        <Selector
          etiqueta="Tipo de material"
          opciones={OPCIONES_FILTRO_TIPO}
          valor={filtroTipo}
          alCambiar={setFiltroTipo}
        />
      </div>

      {itemsBase.length === 0 ? (
        <p role="status" className="tabla-inventario-vacio">
          No se registra material vegetal en la bodega de esta comunidad
        </p>
      ) : itemsFiltrados.length === 0 ? (
        <p role="status" className="tabla-inventario-vacio">
          No se encontraron registros que coincidan con la búsqueda
        </p>
      ) : (
        <>
          <div className="tabla-inventario-escritorio">
            <Tabla
              columnas={columnas}
              filas={itemsFiltrados}
              claveFila={(fila) => fila.id}
            />
          </div>

          <div className="tabla-inventario-movil">
            {itemsFiltrados.map((item) => (
              <Tarjeta key={item.id} className="tarjeta-inventario">
                <div className="tarjeta-inventario-cabecera">
                  <div>
                    <h3 className="tarjeta-inventario-titulo">{item.nombreComunPlanta}</h3>
                    {item.nombreCientificoPlanta && (
                      <span className="tarjeta-inventario-cientifico">
                        {item.nombreCientificoPlanta}
                      </span>
                    )}
                  </div>
                  <span className="tarjeta-inventario-tipo">
                    {MAPA_TIPO_MATERIAL[item.tipoMaterial] ?? item.tipoMaterial}
                  </span>
                </div>

                <div className="tarjeta-inventario-detalles">
                  <div className="tarjeta-inventario-dato">
                    <span className="tarjeta-inventario-etiqueta">Stock Disponible</span>
                    <span className="tarjeta-inventario-valor">
                      {item.cantidadDisponible} {item.unidadMedida}
                    </span>
                  </div>
                  <div className="tarjeta-inventario-dato">
                    <span className="tarjeta-inventario-etiqueta">Lote / Origen</span>
                    <span className="tarjeta-inventario-valor">{item.loteOrigen || '—'}</span>
                  </div>
                  <div className="tarjeta-inventario-dato">
                    <span className="tarjeta-inventario-etiqueta">Fecha de Ingreso</span>
                    <span className="tarjeta-inventario-valor">{item.fechaIngreso}</span>
                  </div>
                </div>

                <div className="tarjeta-inventario-acciones">
                  <Boton variante="peligro" onClick={() => alDarDeBaja?.(item)}>
                    Dar de baja
                  </Boton>
                </div>
              </Tarjeta>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
