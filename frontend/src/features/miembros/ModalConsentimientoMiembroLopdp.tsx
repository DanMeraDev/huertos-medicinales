import { useState, useEffect } from 'react'
import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import './ModalConsentimientoMiembroLopdp.css'

export interface ModalConsentimientoMiembroLopdpProps {
  abierto: boolean
  alConfirmar: () => void
  alCancelar: () => void
  cargando?: boolean
}

export function ModalConsentimientoMiembroLopdp({
  abierto,
  alConfirmar,
  alCancelar,
  cargando = false,
}: ModalConsentimientoMiembroLopdpProps) {
  const [certificado, setCertificado] = useState(false)

  useEffect(() => {
    if (abierto) {
      setCertificado(false)
    }
  }, [abierto])

  return (
    <Modal
      abierto={abierto}
      titulo="Cumplimiento LOPDP - Consentimiento de Voluntario"
      alCancelar={alCancelar}
      acciones={
        <Boton
          variante="primario"
          onClick={alConfirmar}
          disabled={!certificado || cargando}
        >
          {cargando ? 'Registrando...' : 'Confirmar Registro'}
        </Boton>
      }
    >
      <div className="modal-lopdp-cuerpo">
        <p className="modal-lopdp-introduccion">
          Antes de registrar a este voluntario/a en el sistema comunitario, confirma el cumplimiento de la Ley Orgánica de Protección de Datos Personales (LOPDP):
        </p>
        <ul className="modal-lopdp-lista">
          <li>
            <strong>Consentimiento Informado:</strong> Confirmas que la persona voluntaria (o su representante legal/tutor en caso de ser menor de edad) ha manifestado su consentimiento previo para registrar su cédula, nombres, edad y teléfono en esta plataforma.
          </li>
          <li>
            <strong>Visibilidad Pública de Gamificación:</strong> Se le ha comunicado al voluntario que su nombre, nivel, comunidades e insignias obtenidas serán visibles públicamente en la plataforma al ingresar su número de cédula.
          </li>
          <li>
            <strong>Uso Exclusivo:</strong> Los datos se destinarán con exclusividad al registro de actividades de huertos y el seguimiento de saberes comunitarios, sin fines de comercialización ni traspaso a terceros ajenos a la red.
          </li>
        </ul>
        <label className="modal-lopdp-check-etiqueta">
          <input
            type="checkbox"
            checked={certificado}
            onChange={(e) => setCertificado(e.target.checked)}
            className="modal-lopdp-checkbox"
          />
          <span>
            Certifico como Responsable Comunitario que cuento con la autorización expresa del titular (o de su representante legal) para el registro y visualización de sus datos en la plataforma.
          </span>
        </label>
      </div>
    </Modal>
  )
}
