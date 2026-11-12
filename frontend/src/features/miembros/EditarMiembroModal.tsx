import { useState } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Modal } from '../../components/Modal'
import { Notificacion } from '../../components/Notificacion'
import { Selector } from '../../components/Selector'
import {
  actualizarDatosMiembro,
  type Genero,
  type MiembroDetalleResponse,
} from './miembrosApi'
import './EditarMiembroModal.css'

export interface EditarMiembroModalProps {
  abierto: boolean
  miembro: MiembroDetalleResponse | null
  alCerrar: () => void
  alExito: (miembroActualizado: MiembroDetalleResponse) => void
}

export function EditarMiembroModal({
  abierto,
  miembro,
  alCerrar,
  alExito,
}: EditarMiembroModalProps) {
  if (!abierto || !miembro) return null

  return (
    <EditarMiembroModalContenido
      abierto={abierto}
      miembro={miembro}
      alCerrar={alCerrar}
      alExito={alExito}
    />
  )
}

interface EditarMiembroModalContenidoProps {
  abierto: boolean
  miembro: MiembroDetalleResponse
  alCerrar: () => void
  alExito: (miembroActualizado: MiembroDetalleResponse) => void
}

function EditarMiembroModalContenido({
  abierto,
  miembro,
  alCerrar,
  alExito,
}: EditarMiembroModalContenidoProps) {
  const [genero, setGenero] = useState<Genero>(miembro.genero)
  const [telefono, setTelefono] = useState(miembro.telefono)
  const [errorTelefono, setErrorTelefono] = useState<string | undefined>()
  const [mensajeError, setMensajeError] = useState<string | null>(null)
  const [mensajeExito, setMensajeExito] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  function validar(): boolean {
    if (!telefono || !/^09[0-9]{8}$/.test(telefono.trim())) {
      setErrorTelefono('El teléfono debe iniciar con 09 y tener 10 dígitos')
      return false
    }
    setErrorTelefono(undefined)
    return true
  }

  async function manejarGuardar() {
    setMensajeError(null)
    setMensajeExito(null)
    if (!validar()) {
      return
    }
    setEnviando(true)
    try {
      const respuesta = await actualizarDatosMiembro(miembro.id, {
        genero,
        telefono: telefono.trim(),
      })
      setMensajeExito('Datos del voluntario actualizados correctamente')
      alExito(respuesta)
    } catch (error: unknown) {
      const detalle = error instanceof ApiError ? error.message : 'Error al actualizar los datos'
      setMensajeError(detalle)
    } finally {
      setEnviando(false)
    }
  }

  function manejarCancelar() {
    setErrorTelefono(undefined)
    setMensajeError(null)
    setMensajeExito(null)
    alCerrar()
  }

  return (
    <Modal
      abierto={abierto}
      titulo="Editar datos de voluntario"
      alCancelar={manejarCancelar}
      acciones={
        <Boton
          variante="primario"
          onClick={manejarGuardar}
          disabled={enviando}
        >
          {enviando ? 'Guardando…' : 'Guardar Cambios'}
        </Boton>
      }
    >
      <div className="editar-miembro-cuerpo">
        {mensajeExito && <Notificacion tipo="exito" mensaje={mensajeExito} />}
        {mensajeError && <Notificacion tipo="error" mensaje={mensajeError} />}

        <div className="editar-miembro-institucionales">
          <p className="editar-miembro-aviso-bloqueado">
            Los datos de identidad institucional y nivel se encuentran protegidos y no son editables desde este flujo.
          </p>
          <CampoTexto
            etiqueta="Cédula de Identidad"
            valor={miembro.cedula}
            alCambiar={() => {}}
            readOnly
          />
          <CampoTexto
            etiqueta="Nombres y Apellidos"
            valor={miembro.nombreCompleto}
            alCambiar={() => {}}
            readOnly
          />
          <CampoTexto
            etiqueta="Nivel Actual"
            valor={miembro.nivel}
            alCambiar={() => {}}
            readOnly
          />
        </div>

        <div className="editar-miembro-editables">
          <Selector
            etiqueta="Género"
            opciones={[
              { valor: 'FEMENINO', etiqueta: 'Femenino' },
              { valor: 'MASCULINO', etiqueta: 'Masculino' },
              { valor: 'OTRO', etiqueta: 'Otro' },
            ]}
            valor={genero}
            alCambiar={(v) => setGenero(v as Genero)}
          />

          <CampoTexto
            etiqueta="Teléfono Celular"
            type="tel"
            inputMode="numeric"
            maxLength={10}
            placeholder="Ej: 0981234567"
            valor={telefono}
            alCambiar={setTelefono}
            error={errorTelefono}
          />
        </div>
      </div>
    </Modal>
  )
}
