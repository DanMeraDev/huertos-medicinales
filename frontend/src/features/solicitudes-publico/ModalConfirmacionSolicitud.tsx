import { Boton } from '../../components/Boton'
import { KintiGuia } from '../../components/KintiGuia'
import { Modal } from '../../components/Modal'
import type { SolicitudComunidadResponse } from './solicitudesPublicoApi'

interface ModalConfirmacionSolicitudProps {
  solicitud: SolicitudComunidadResponse | null
  alCerrar: () => void
}

export function ModalConfirmacionSolicitud({ solicitud, alCerrar }: ModalConfirmacionSolicitudProps) {
  return (
    <Modal
      abierto={solicitud !== null}
      titulo="¡Felicidades por sembrar esperanza!"
      alCancelar={alCerrar}
      acciones={<Boton onClick={alCerrar}>Entendido</Boton>}
    >
      <KintiGuia variante="celebracion" className="confirmacion-kinti" />
      <p>
        Tu solicitud para constituir el huerto <strong>{solicitud?.nombreComunidad}</strong> ha sido recibida con
        éxito. El equipo técnico del MSP revisará la propuesta y te enviaremos una notificación al correo{' '}
        <strong>{solicitud?.correoSolicitante}</strong> con los resultados y las instrucciones de acceso.
      </p>
    </Modal>
  )
}
