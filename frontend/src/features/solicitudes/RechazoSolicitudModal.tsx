import { useId, useState } from 'react'
import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'

const LONGITUD_MINIMA_MOTIVO = 20

export interface RechazoSolicitudModalProps {
  abierto: boolean
  enviando: boolean
  alCancelar: () => void
  alConfirmar: (motivo: string) => void
}

export function RechazoSolicitudModal({ abierto, enviando, alCancelar, alConfirmar }: RechazoSolicitudModalProps) {
  const idMotivo = useId()
  const [motivo, setMotivo] = useState('')
  const [intentoEnviar, setIntentoEnviar] = useState(false)
  const motivoInvalido = motivo.trim().length < LONGITUD_MINIMA_MOTIVO

  function confirmar() {
    setIntentoEnviar(true)
    if (!motivoInvalido) alConfirmar(motivo.trim())
  }

  return (
    <Modal
      abierto={abierto}
      titulo="No aprobar solicitud"
      alCancelar={alCancelar}
      acciones={
        <Boton variante="danger" onClick={confirmar} disabled={enviando}>
          Confirmar Rechazo
        </Boton>
      }
    >
      <div className="campo">
        <label htmlFor={idMotivo} className="campo-etiqueta">
          Motivo del rechazo
        </label>
        <textarea
          id={idMotivo}
          className="campo-control solicitud-motivo"
          maxLength={500}
          value={motivo}
          onChange={(evento) => setMotivo(evento.target.value)}
          aria-invalid={intentoEnviar && motivoInvalido}
        />
        {intentoEnviar && motivoInvalido && (
          <p className="campo-error">El motivo es obligatorio y debe tener al menos 20 caracteres</p>
        )}
      </div>
    </Modal>
  )
}
