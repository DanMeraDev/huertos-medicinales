import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { KintiGuia } from '../../components/KintiGuia'
import { Modal } from '../../components/Modal'
import { Notificacion, type NotificacionProps } from '../../components/Notificacion'
import { Tabla, type ColumnaTabla } from '../../components/Tabla'
import { DetalleSolicitudModal } from './DetalleSolicitudModal'
import { formatearFecha } from './formatoSolicitud'
import { RechazoSolicitudModal } from './RechazoSolicitudModal'
import {
  aprobarSolicitud,
  obtenerDetalleSolicitud,
  obtenerSolicitudesPendientes,
  rechazarSolicitud,
  type ResolucionSolicitudResponse,
  type SolicitudDetalleCompletoResponse,
  type SolicitudResumenResponse,
} from './solicitudesApi'
import '../../components/CampoTexto.css'
import './TablaSolicitudes.css'

type Paso = 'detalle' | 'aprobacion' | 'rechazo'

export function TablaSolicitudes() {
  const [solicitudes, setSolicitudes] = useState<SolicitudResumenResponse[] | null>(null)
  const [seleccionada, setSeleccionada] = useState<SolicitudDetalleCompletoResponse | null>(null)
  const [paso, setPaso] = useState<Paso>('detalle')
  const [enviando, setEnviando] = useState(false)
  const [aviso, setAviso] = useState<NotificacionProps | null>(null)

  const cargar = useCallback(() => {
    obtenerSolicitudesPendientes()
      .then(setSolicitudes)
      .catch((falla: unknown) => {
        if (!(falla instanceof ApiError)) throw falla
        setSolicitudes([])
        setAviso({ tipo: 'error', mensaje: falla.message })
      })
  }, [])

  useEffect(cargar, [cargar])

  async function verMas(id: number) {
    setAviso(null)
    setPaso('detalle')
    setSeleccionada(await obtenerDetalleSolicitud(id))
  }

  async function resolver(accion: () => Promise<ResolucionSolicitudResponse>) {
    setEnviando(true)
    try {
      const resultado = await accion()
      setAviso({ tipo: 'exito', mensaje: resultado.mensaje })
    } catch (falla) {
      if (!(falla instanceof ApiError)) throw falla
      setAviso({ tipo: 'error', mensaje: falla.message })
    } finally {
      setEnviando(false)
      setSeleccionada(null)
      cargar()
    }
  }

  const columnas: ColumnaTabla<SolicitudResumenResponse>[] = [
    { titulo: 'Solicitante', celda: (fila) => fila.solicitante },
    { titulo: 'Fecha', celda: (fila) => formatearFecha(fila.fecha) },
    { titulo: 'Provincia', celda: (fila) => fila.provincia },
    {
      titulo: 'Acción',
      celda: (fila) => (
        <Boton variante="secondary" className="solicitudes-ver-mas" onClick={() => verMas(fila.id)}>
          Ver más
        </Boton>
      ),
    },
  ]

  const hayPendientes = solicitudes !== null && solicitudes.length > 0

  return (
    <section className="solicitudes">
      {aviso && <Notificacion tipo={aviso.tipo} mensaje={aviso.mensaje} />}
      {solicitudes !== null && (
        <div className="solicitudes-kinti">
          <KintiGuia variante={hayPendientes ? 'dia' : 'vacio'} tamano="md" />
          <p>
            {hayPendientes
              ? 'Varias personas quieren crear su huerto, revisa sus solicitudes aquí'
              : 'No tienes solicitudes de aprobación pendientes por el momento. ¡Buen trabajo!'}
          </p>
        </div>
      )}
      <div className="solicitudes-desplazable">
        <Tabla
          columnas={columnas}
          filas={solicitudes ?? []}
          claveFila={(fila) => fila.id}
          cargando={solicitudes === null}
        />
      </div>
      <DetalleSolicitudModal
        solicitud={paso === 'detalle' ? seleccionada : null}
        alCerrar={() => setSeleccionada(null)}
        alAprobar={() => setPaso('aprobacion')}
        alRechazar={() => setPaso('rechazo')}
      />
      <Modal
        abierto={seleccionada !== null && paso === 'aprobacion'}
        titulo="Confirmar aprobación"
        alCancelar={() => setPaso('detalle')}
        acciones={
          <Boton disabled={enviando} onClick={() => seleccionada && resolver(() => aprobarSolicitud(seleccionada.id))}>
            Sí, aprobar
          </Boton>
        }
      >
        <p>
          ¿Confirmas la aprobación de esta solicitud? Se creará la comunidad, su huerto y la cuenta de acceso para el
          responsable
        </p>
      </Modal>
      <RechazoSolicitudModal
        key={seleccionada?.id}
        abierto={seleccionada !== null && paso === 'rechazo'}
        enviando={enviando}
        alCancelar={() => setPaso('detalle')}
        alConfirmar={(motivo) => seleccionada && resolver(() => rechazarSolicitud(seleccionada.id, motivo))}
      />
    </section>
  )
}
