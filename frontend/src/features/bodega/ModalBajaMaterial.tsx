import { useState, type FormEvent, type MouseEvent } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Modal } from '../../components/Modal'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import { registrarBajaMaterial } from './bodegaApi'
import { OPCIONES_CAUSA_FRACASO, type BajaMaterialResponse, type CausaFracaso, type MaterialVegetalResponse } from './types'
import './ModalBajaMaterial.css'

export interface ModalBajaMaterialProps {
  abierto: boolean
  material: MaterialVegetalResponse | null
  alCerrar: () => void
  alExito: (respuesta: BajaMaterialResponse) => void
}

export function ModalBajaMaterial({ abierto, material, alCerrar, alExito }: ModalBajaMaterialProps) {
  const [cantidadBaja, setCantidadBaja] = useState('')
  const [causaFracaso, setCausaFracaso] = useState<string>('')
  const [observacion, setObservacion] = useState('')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (!material) {
    return null
  }

  function limpiarFormulario() {
    setCantidadBaja('')
    setCausaFracaso('')
    setObservacion('')
    setErrores({})
    setErrorEnvio(null)
    setEnviando(false)
  }

  function manejarCancelar() {
    limpiarFormulario()
    alCerrar()
  }

  function validar(): boolean {
    const nuevosErrores: Record<string, string> = {}
    const valorCantidad = Number(cantidadBaja)

    if (!cantidadBaja || isNaN(valorCantidad) || valorCantidad <= 0) {
      nuevosErrores.cantidadBaja = 'La cantidad a dar de baja debe ser mayor a cero'
    } else {
      const partes = cantidadBaja.split('.')
      if (partes.length > 1 && partes[1].length > 2) {
        nuevosErrores.cantidadBaja = 'La cantidad admite hasta 2 decimales'
      } else if (material && valorCantidad > material.cantidadDisponible) {
        nuevosErrores.cantidadBaja = 'La cantidad a dar de baja supera el stock disponible en bodega'
      }
    }

    if (!causaFracaso) {
      nuevosErrores.causaFracaso = 'Debe seleccionar una causa de fracaso válida para el registro de la baja'
    }

    if (observacion.length > 300) {
      nuevosErrores.observacion = 'La observación no puede superar los 300 caracteres'
    }

    setErrores(nuevosErrores)
    return Object.keys(nuevosErrores).length === 0
  }

  async function ejecutarBaja() {
    setErrorEnvio(null)

    if (!validar() || !material) {
      return
    }

    setEnviando(true)

    try {
      const respuesta = await registrarBajaMaterial(material.id, {
        cantidadBaja: Number(cantidadBaja),
        causaFracaso: causaFracaso as CausaFracaso,
        observacion: observacion.trim() ? observacion.trim() : undefined,
      })

      limpiarFormulario()
      alExito(respuesta)
      alCerrar()
    } catch (err: unknown) {
      const mensaje =
        err instanceof ApiError
          ? (err.detalle ?? err.message)
          : 'Ocurrió un error inesperado al registrar la baja'
      setErrorEnvio(mensaje)
    } finally {
      setEnviando(false)
    }
  }

  function manejarEnvio(evento?: FormEvent | MouseEvent) {
    evento?.preventDefault()
    void ejecutarBaja()
  }

  const opcionesCausas: OpcionSelector[] = OPCIONES_CAUSA_FRACASO.map((opcion) => ({
    valor: opcion.valor,
    etiqueta: opcion.etiqueta,
  }))

  return (
    <Modal
      abierto={abierto}
      titulo="Dar de Baja Material Vegetal"
      alCancelar={manejarCancelar}
      acciones={
        <Boton
          variante="peligro"
          type="button"
          onClick={manejarEnvio}
          disabled={enviando}
        >
          {enviando ? 'Confirmando…' : 'Confirmar Baja'}
        </Boton>
      }
    >
      <div className="modal-baja-info">
        <div className="modal-baja-info-item">
          <span className="modal-baja-info-etiqueta">Planta:</span>
          <span className="modal-baja-info-valor">
            {material.nombreComunPlanta}
            {material.nombreCientificoPlanta ? ` (${material.nombreCientificoPlanta})` : ''}
          </span>
        </div>
        <div className="modal-baja-info-item">
          <span className="modal-baja-info-etiqueta">Stock Disponible:</span>
          <span className="modal-baja-info-valor">
            {material.cantidadDisponible} {material.unidadMedida}
          </span>
        </div>
      </div>

      {errorEnvio && <Notificacion tipo="error" mensaje={errorEnvio} />}

      <form id="formulario-baja-material" className="modal-baja-formulario" onSubmit={manejarEnvio} noValidate>
        <CampoTexto
          etiqueta="Cantidad a dar de baja *"
          type="number"
          step="0.01"
          min="0.01"
          max={material.cantidadDisponible}
          valor={cantidadBaja}
          alCambiar={(val) => {
            setCantidadBaja(val)
            if (errores.cantidadBaja) {
              setErrores((prev) => ({ ...prev, cantidadBaja: '' }))
            }
          }}
          error={errores.cantidadBaja}
          placeholder="0.00"
          required
        />

        <Selector
          etiqueta="Causa del Fracaso *"
          opciones={opcionesCausas}
          valor={causaFracaso}
          alCambiar={(val) => {
            setCausaFracaso(val)
            if (errores.causaFracaso) {
              setErrores((prev) => ({ ...prev, causaFracaso: '' }))
            }
          }}
          error={errores.causaFracaso}
          required
        />

        <CampoTexto
          etiqueta="Observaciones / Detalle"
          valor={observacion}
          alCambiar={(val) => {
            setObservacion(val)
            if (errores.observacion) {
              setErrores((prev) => ({ ...prev, observacion: '' }))
            }
          }}
          error={errores.observacion}
          placeholder="Detalle opcional sobre la pérdida (máx. 300 caracteres)"
          maxLength={300}
        />
        <div
          className={`modal-baja-contador ${observacion.length > 300 ? 'modal-baja-contador-excedido' : ''}`}
        >
          {observacion.length}/300 caracteres
        </div>
      </form>
    </Modal>
  )
}
