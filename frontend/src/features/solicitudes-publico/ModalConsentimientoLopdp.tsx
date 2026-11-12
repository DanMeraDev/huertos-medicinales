import { useState } from 'react'
import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'

interface ModalConsentimientoLopdpProps {
  abierto: boolean
  alCancelar: () => void
  alAceptar: () => void
}

export function ModalConsentimientoLopdp({ abierto, alCancelar, alAceptar }: ModalConsentimientoLopdpProps) {
  const [acepta, setAcepta] = useState(false)

  function cancelar() {
    setAcepta(false)
    alCancelar()
  }

  function aceptar() {
    setAcepta(false)
    alAceptar()
  }

  return (
    <Modal
      abierto={abierto}
      titulo="Protección de Datos Personales (LOPDP Ecuador)"
      alCancelar={cancelar}
      acciones={
        <Boton disabled={!acepta} onClick={aceptar}>
          Aceptar y Continuar
        </Boton>
      }
    >
      <div className="lopdp-texto">
        <p>
          En cumplimiento con la Ley Orgánica de Protección de Datos Personales (LOPDP) del Ecuador, te informamos
          que los datos personales y de contacto proporcionados en este formulario serán tratados exclusivamente por
          la Red de Huertos Comunitarios y el Ministerio de Salud Pública (MSP).
        </p>
        <ul>
          <li>
            <strong>Finalidad:</strong> Gestionar la postulación, verificación y articulación técnica de la nueva
            comunidad de siembra.
          </li>
          <li>
            <strong>Uso no comercial:</strong> Tus datos jamás serán comercializados, cedidos ni empleados para fines
            publicitarios o lucrativos.
          </li>
          <li>
            <strong>Gamificación y Perfil Público:</strong> Al formar parte de la red, tu participación comunitaria,
            reconocimientos, insignias y aportes podrán ser consultados de forma pública mediante tu número de cédula
            dentro del sistema para promover la transparencia y la participación ciudadana.
          </li>
          <li>
            <strong>Tus derechos:</strong> Puedes ejercer tus derechos de acceso, rectificación, actualización y
            eliminación de datos conforme lo estipula la ley.
          </li>
        </ul>
      </div>
      <label className="lopdp-casilla">
        <input type="checkbox" checked={acepta} onChange={(evento) => setAcepta(evento.target.checked)} />
        Declaro que soy mayor de 18 años, que la información proporcionada es fidedigna y autorizo de forma libre,
        expresa e informada el tratamiento de mis datos personales bajo los términos indicados.
      </label>
    </Modal>
  )
}
