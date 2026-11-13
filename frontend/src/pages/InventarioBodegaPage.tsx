import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import { Selector } from '../components/Selector'
import { ApiError } from '../api/client'
import { TablaInventarioBodega } from '../features/bodega/TablaInventarioBodega'
import { ModalBajaMaterial } from '../features/bodega/ModalBajaMaterial'
import type { MaterialVegetalResponse } from '../features/bodega/types'
import { obtenerMiComunidad } from '../features/comunidades/comunidadesApi'
import { obtenerDirectorioComunidades } from '../features/comunidades-publico/comunidadesPublicoApi'
import { ConsejoKinti } from '../components/ConsejoKinti'
import './InventarioBodegaPage.css'

export type RolUsuario = 'RESPONSABLE_COMUNIDAD' | 'TECNICO_MSP'

export interface InventarioBodegaPageProps {
  rolInicial?: RolUsuario
  comunidadIdInicial?: number
  comunidadesDisponibles?: { id: number; nombre: string }[]
}

function obtenerRolDesdeStorage(): RolUsuario | null {
  const rol = localStorage.getItem('rol')
  if (rol === 'RESPONSABLE_COMUNIDAD' || rol === 'TECNICO_MSP') {
    return rol
  }
  const token = localStorage.getItem('token')
  if (token) {
    const partes = token.split('.')
    if (partes.length === 3) {
      try {
        const payload = JSON.parse(atob(partes[1].replace(/-/g, '+').replace(/_/g, '/')))
        if (payload.rol === 'RESPONSABLE_COMUNIDAD' || payload.rol === 'TECNICO_MSP') {
          return payload.rol
        }
      } catch {
        return null
      }
    }
  }
  return null
}

export function InventarioBodegaPage({
  rolInicial,
  comunidadIdInicial,
  comunidadesDisponibles,
}: InventarioBodegaPageProps) {
  const [rolDetectado, setRolDetectado] = useState<RolUsuario | null>(() => {
    return rolInicial ?? obtenerRolDesdeStorage()
  })
  const [comunidadSeleccionadaId, setComunidadSeleccionadaId] = useState<number | undefined>(comunidadIdInicial)
  const [nombreComunidad, setNombreComunidad] = useState<string>('')
  const [comunidadesLista, setComunidadesLista] = useState<{ id: number; nombre: string }[]>(
    comunidadesDisponibles ?? [],
  )
  const [errorPagina, setErrorPagina] = useState<string | null>(null)
  const [materialParaBaja, setMaterialParaBaja] = useState<MaterialVegetalResponse | null>(null)
  const [modalBajaAbierto, setModalBajaAbierto] = useState(false)
  const [mensajeExito, setMensajeExito] = useState<string | null>(null)
  const [versionInventario, setVersionInventario] = useState(0)

  function handleAbrirBaja(item: MaterialVegetalResponse) {
    setMaterialParaBaja(item)
    setModalBajaAbierto(true)
    setMensajeExito(null)
  }

  function handleBajaExitosa() {
    setMensajeExito('Se registró la baja de material vegetal correctamente.')
    setVersionInventario((v) => v + 1)
  }

  useEffect(() => {
    let montado = true

    if (rolDetectado === 'RESPONSABLE_COMUNIDAD' || rolDetectado === null) {
      if (comunidadIdInicial && rolDetectado === 'RESPONSABLE_COMUNIDAD') {
        return
      }

      obtenerMiComunidad()
        .then((miComunidad) => {
          if (!montado) return
          setRolDetectado('RESPONSABLE_COMUNIDAD')
          if (!comunidadIdInicial) {
            setComunidadSeleccionadaId(miComunidad.id)
          }
          setNombreComunidad(miComunidad.nombre)
        })
        .catch((err: unknown) => {
          if (!montado) return
          if (err instanceof ApiError && err.status === 403) {
            setRolDetectado('TECNICO_MSP')
          } else {
            const mensaje =
              err instanceof ApiError
                ? (err.detalle ?? err.message)
                : 'Error al obtener la información de la comunidad'
            setErrorPagina(mensaje)
          }
        })
    }

    return () => {
      montado = false
    }
  }, [rolDetectado, comunidadIdInicial])

  useEffect(() => {
    if (rolDetectado !== 'TECNICO_MSP' || comunidadesDisponibles !== undefined) {
      return
    }

    let montado = true

    obtenerDirectorioComunidades()
      .then((directorio) => {
        if (!montado) return
        const mapeadas = directorio.map((c) => ({ id: c.id, nombre: c.nombre }))
        setComunidadesLista(mapeadas)
        if (!comunidadSeleccionadaId && mapeadas.length > 0) {
          setComunidadSeleccionadaId(mapeadas[0].id)
        }
      })
      .catch((err: unknown) => {
        if (!montado) return
        const mensaje =
          err instanceof ApiError
            ? (err.detalle ?? err.message)
            : 'Error al obtener el listado de comunidades'
        setErrorPagina(mensaje)
      })

    return () => {
      montado = false
    }
  }, [rolDetectado, comunidadesDisponibles, comunidadSeleccionadaId])

  const opcionesComunidades = comunidadesLista.map((c) => ({
    valor: String(c.id),
    etiqueta: c.nombre,
  }))

  return (
    <div className="pagina-inventario-bodega">
      <div className="inventario-bodega-cabecera">
        <h1 className="inventario-bodega-titulo">Inventario de Bodega</h1>
        <p className="inventario-bodega-descripcion">
          Consulta y supervisión del material vegetal disponible en las bodegas comunitarias.
        </p>
      </div>

      <ConsejoKinti
        variante="dia"
        mensaje="Revisa el material vegetal disponible antes de planificar una siembra."
      />

      {errorPagina && <Notificacion tipo="error" mensaje={errorPagina} />}
      {mensajeExito && <Notificacion tipo="exito" mensaje={mensajeExito} />}

      {rolDetectado === 'RESPONSABLE_COMUNIDAD' && nombreComunidad && (
        <div className="inventario-comunidad-info">
          <span className="inventario-comunidad-etiqueta">Comunidad asignada</span>
          <span className="inventario-comunidad-nombre">{nombreComunidad}</span>
        </div>
      )}

      {rolDetectado === 'TECNICO_MSP' && (
        <div className="inventario-selector-comunidad">
          <Selector
            etiqueta="Seleccionar Comunidad"
            opciones={opcionesComunidades}
            valor={comunidadSeleccionadaId ? String(comunidadSeleccionadaId) : ''}
            alCambiar={(valor) => setComunidadSeleccionadaId(Number(valor))}
          />
        </div>
      )}

      {comunidadSeleccionadaId ? (
        <TablaInventarioBodega
          key={`${comunidadSeleccionadaId}-${versionInventario}`}
          comunidadId={comunidadSeleccionadaId}
          alDarDeBaja={handleAbrirBaja}
        />
      ) : (
        <p role="status" className="inventario-sin-seleccion">
          Seleccione una comunidad para consultar su inventario de bodega
        </p>
      )}

      <ModalBajaMaterial
        abierto={modalBajaAbierto}
        material={materialParaBaja}
        alCerrar={() => {
          setModalBajaAbierto(false)
          setMaterialParaBaja(null)
        }}
        alExito={handleBajaExitosa}
      />
    </div>
  )
}
