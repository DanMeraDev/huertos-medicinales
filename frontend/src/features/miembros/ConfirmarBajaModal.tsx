import { useState, type ChangeEvent } from 'react'
import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import type { MiembroDetalleResponse } from './miembrosApi'
import './ConfirmarBajaModal.css'

export interface ConfirmarBajaModalProps {
  abierto: boolean
  miembro: MiembroDetalleResponse | null
  alCerrar: () => void
  alConfirmar: (motivo: string) => void
  enviando?: boolean
}

export function ConfirmarBajaModal({
  abierto,
  miembro,
  alCerrar,
  alConfirmar,
  enviando = false,
}: ConfirmarBajaModalProps) {
  const [motivo, setMotivo] = useState('')

  if (!abierto || !miembro) return null

  function manejarCambioMotivo(evento: ChangeEvent<HTMLTextAreaElement>) {
    setMotivo(evento.target.value)
  }

  function manejarConfirmar() {
    alConfirmar(motivo.trim())
  }

  function manejarCerrar() {
    setMotivo('')
    alCerrar()
  }

  const errorExcede = motivo.length > 255 ? 'El motivo no puede superar 255 caracteres' : null

  return (
    <Modal
      abierto={abierto}
      titulo="Dar de baja a miembro"
      alCancelar={manejarCerrar}
      acciones={
        <Boton
          variante="peligro"
          onClick={manejarConfirmar}
          disabled={enviando || motivo.length > 255}
        >
          {enviando ? 'Dando de baja…' : 'Confirmar baja'}
        </Boton>
      }
    >
      <div className="modal-baja-cuerpo">
        <div className="modal-baja-banner-triste">
          <div className="modal-baja-icono-contenedor" aria-hidden="true">
            <svg viewBox="0 0 64 64" width="44" height="44" fill="none" className="modal-baja-icono">
              <circle cx="32" cy="22" r="12" stroke="currentColor" strokeWidth="2.5" />
              <circle cx="27.5" cy="20" r="1.5" fill="currentColor" />
              <circle cx="36.5" cy="20" r="1.5" fill="currentColor" />
              <path
                d="M27 27c1.6-2.1 8.4-2.1 10 0"
                stroke="currentColor"
                strokeWidth="2.3"
                strokeLinecap="round"
              />
              <path
                d="M14 52c2.5-9.5 9.8-14 18-14s15.5 4.5 18 14"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
              />
            </svg>
          </div>
          <p className="modal-baja-mensaje-triste">Da mucha pena que un voluntario se haya ido</p>
        </div>

        <p className="modal-baja-pregunta">
          {`¿Está seguro de dar de baja a ${miembro.nombreCompleto}?`}
        </p>
        <p className="modal-baja-aviso">
          El voluntario dejará de figurar como miembro activo en las actividades y faenas comunitarias, conservando su registro histórico.
        </p>
        <div className="campo">
          <label htmlFor="motivo-baja" className="campo-etiqueta">
            Motivo de la baja (opcional)
          </label>
          <textarea
            id="motivo-baja"
            className="campo-control modal-baja-textarea"
            rows={3}
            maxLength={255}
            value={motivo}
            onChange={manejarCambioMotivo}
            placeholder="Indica la razón de la baja (ej. cambio de domicilio, solicitud personal...)"
          />
          <div className="modal-baja-conteo">
            <span>{motivo.length} / 255 caracteres</span>
          </div>
          {errorExcede && <p className="campo-error">{errorExcede}</p>}
        </div>
      </div>
    </Modal>
  )
}
