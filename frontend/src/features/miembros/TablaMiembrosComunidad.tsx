import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { Notificacion, type NotificacionProps } from '../../components/Notificacion'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { ConfirmarBajaModal } from './ConfirmarBajaModal'
import {
  cambiarEstadoMiembro,
  listarMiembrosDeMiComunidad,
  type MiembroDetalleResponse,
} from './miembrosApi'
import './TablaMiembrosComunidad.css'

type FiltroEstado = 'TODOS' | 'ACTIVOS' | 'INACTIVOS'

export function TablaMiembrosComunidad() {
  const [miembros, setMiembros] = useState<MiembroDetalleResponse[]>([])
  const [cargando, setCargando] = useState(true)
  const [busqueda, setBusqueda] = useState('')
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>('TODOS')
  const [aviso, setAviso] = useState<NotificacionProps | null>(null)
  const [miembroParaBaja, setMiembroParaBaja] = useState<MiembroDetalleResponse | null>(null)
  const [enviando, setEnviando] = useState(false)

  const cargar = useCallback(() => {
    setCargando(true)
    listarMiembrosDeMiComunidad()
      .then((datos) => {
        setMiembros(datos)
      })
      .catch((error: unknown) => {
        const mensaje = error instanceof ApiError ? error.message : 'Error al cargar los miembros'
        setAviso({ tipo: 'error', mensaje })
      })
      .finally(() => {
        setCargando(false)
      })
  }, [])

  useEffect(() => {
    cargar()
  }, [cargar])

  async function manejarConfirmarBaja(motivo: string) {
    if (!miembroParaBaja) return
    setEnviando(true)
    setAviso(null)
    try {
      const respuesta = await cambiarEstadoMiembro(miembroParaBaja.id, {
        activo: false,
        motivoBaja: motivo || null,
      })
      setMiembros((prev) =>
        prev.map((m) =>
          m.id === miembroParaBaja.id ? { ...m, activoEnComunidad: false } : m,
        ),
      )
      setAviso({ tipo: 'exito', mensaje: respuesta.mensaje })
      setMiembroParaBaja(null)
    } catch (error: unknown) {
      const mensaje = error instanceof ApiError ? error.message : 'Error al dar de baja al miembro'
      setAviso({ tipo: 'error', mensaje })
    } finally {
      setEnviando(false)
    }
  }

  async function manejarReactivar(miembro: MiembroDetalleResponse) {
    setEnviando(true)
    setAviso(null)
    try {
      const respuesta = await cambiarEstadoMiembro(miembro.id, { activo: true })
      setMiembros((prev) =>
        prev.map((m) =>
          m.id === miembro.id ? { ...m, activoEnComunidad: true } : m,
        ),
      )
      setAviso({ tipo: 'exito', mensaje: respuesta.mensaje })
    } catch (error: unknown) {
      const mensaje = error instanceof ApiError ? error.message : 'Error al reactivar al miembro'
      setAviso({ tipo: 'error', mensaje })
    } finally {
      setEnviando(false)
    }
  }

  const miembrosFiltrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase()
    return miembros.filter((m) => {
      const coincideTexto =
        !texto ||
        m.nombreCompleto.toLowerCase().includes(texto) ||
        m.cedula.includes(texto)
      const coincideEstado =
        filtroEstado === 'TODOS'
          ? true
          : filtroEstado === 'ACTIVOS'
            ? m.activoEnComunidad
            : !m.activoEnComunidad
      return coincideTexto && coincideEstado
    })
  }, [miembros, busqueda, filtroEstado])

  const columnas: ColumnaTabla<MiembroDetalleResponse>[] = [
    {
      titulo: 'Cédula',
      celda: (fila) => fila.cedula,
    },
    {
      titulo: 'Nombre Completo',
      celda: (fila) => fila.nombreCompleto,
    },
    {
      titulo: 'Nivel',
      celda: (fila) => fila.nivel,
    },
    {
      titulo: 'Puntos',
      celda: (fila) => fila.puntosTotales,
    },
    {
      titulo: 'Estado',
      celda: (fila) => (
        <span
          className={`miembro-estado-badge ${fila.activoEnComunidad ? 'activo' : 'inactivo'}`}
        >
          {fila.activoEnComunidad ? 'Activo' : 'Inactivo'}
        </span>
      ),
    },
    {
      titulo: 'Acciones',
      celda: (fila) => (
        <div className="miembro-acciones">
          {fila.activoEnComunidad ? (
            <Boton
              variante="peligro"
              aria-label={`Dar de baja a ${fila.nombreCompleto}`}
              onClick={() => setMiembroParaBaja(fila)}
              disabled={enviando}
            >
              Dar de baja
            </Boton>
          ) : (
            <Boton
              variante="secundario"
              aria-label={`Reactivar a ${fila.nombreCompleto}`}
              onClick={() => manejarReactivar(fila)}
              disabled={enviando}
            >
              Reactivar
            </Boton>
          )}
        </div>
      ),
    },
  ]

  return (
    <section className="gestion-miembros-contenedor">
      <div className="gestion-miembros-cabecera">
        <div>
          <h2 className="gestion-miembros-titulo">Miembros de la Comunidad</h2>
          <p className="modal-baja-aviso">
            Gestiona la nómina comunitaria, altas y bajas de los voluntarios registrados.
          </p>
        </div>
        <Link to="/comunidad/miembros/nuevo" className="boton boton-primario">
          Registrar Voluntario
        </Link>
      </div>

      {aviso && <Notificacion tipo={aviso.tipo} mensaje={aviso.mensaje} />}

      <div className="gestion-miembros-filtros">
        <div className="gestion-miembros-busqueda">
          <input
            type="search"
            aria-label="Buscar por nombre o cédula"
            placeholder="Buscar por nombre o cédula..."
            className="campo-control"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div className="gestion-miembros-botones-estado">
          <Boton
            variante={filtroEstado === 'TODOS' ? 'primario' : 'secundario'}
            onClick={() => setFiltroEstado('TODOS')}
          >
            Todos
          </Boton>
          <Boton
            variante={filtroEstado === 'ACTIVOS' ? 'primario' : 'secundario'}
            onClick={() => setFiltroEstado('ACTIVOS')}
          >
            Activos
          </Boton>
          <Boton
            variante={filtroEstado === 'INACTIVOS' ? 'primario' : 'secundario'}
            onClick={() => setFiltroEstado('INACTIVOS')}
          >
            Inactivos
          </Boton>
        </div>
      </div>

      <Tabla
        columnas={columnas}
        filas={miembrosFiltrados}
        claveFila={(fila) => fila.id}
        cargando={cargando}
      />

      <ConfirmarBajaModal
        abierto={miembroParaBaja !== null}
        miembro={miembroParaBaja}
        alCerrar={() => setMiembroParaBaja(null)}
        alConfirmar={manejarConfirmarBaja}
        enviando={enviando}
      />
    </section>
  )
}
