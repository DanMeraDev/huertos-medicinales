import { useEffect, useState } from 'react'
import { Notificacion } from '../components/Notificacion'
import { BandejaRevisionActividades } from '../features/msp-revision/BandejaRevisionActividades'
import { DetalleAuditoriaActividad } from '../features/msp-revision/DetalleAuditoriaActividad'
import {
  listarPendientes,
  type ActividadRevision,
} from '../features/msp-revision/mspRevisionApi'
import './RevisionActividadesPage.css'

export function RevisionActividadesPage() {
  const [actividades, setActividades] = useState<ActividadRevision[]>([])
  const [auditada, setAuditada] = useState<ActividadRevision | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    listarPendientes()
      .then((pendientes) => {
        if (vigente) {
          setActividades(pendientes)
          setError(null)
        }
      })
      .catch(() => {
        if (vigente) setError('No se pudo cargar la bandeja de actividades pendientes')
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [])

  async function recargarBandeja() {
    try {
      setActividades(await listarPendientes())
    } catch {
      setError('No se pudo actualizar la bandeja de actividades pendientes')
    }
  }

  function manejarVerificacion() {
    void recargarBandeja()
  }

  return (
    <div className="pagina-revision-actividades">
      <header className="pagina-revision-encabezado">
        <h1 className="pagina-revision-titulo">Revisión de actividades ancestrales</h1>
        <p className="pagina-revision-descripcion">
          Audita las evidencias que enviaron las comunidades y emite el dictamen: aprobar, observar para
          corrección o rechazar definitivamente.
        </p>
      </header>

      {error && <Notificacion tipo="error" mensaje={error} />}

      <div className="pagina-revision-panel">
        <BandejaRevisionActividades
          actividades={actividades}
          cargando={cargando}
          alAuditar={setAuditada}
        />

        {auditada && (
          <DetalleAuditoriaActividad
            actividad={auditada}
            alVerificar={manejarVerificacion}
            alCerrar={() => setAuditada(null)}
          />
        )}
      </div>
    </div>
  )
}
