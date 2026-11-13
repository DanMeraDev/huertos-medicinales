import { useState } from 'react'
import { BottomSheet } from '../../components/BottomSheet'
import { Boton } from '../../components/Boton'
import { Modal } from '../../components/Modal'
import { CampoAreaTexto } from './CampoAreaTexto'
import { AVISO_SESION, esErrorDeEstado, type Aviso } from './envioPlanta'
import { AvisoFormulario } from './PartesFormularioPlanta'
import { darDeBajaPlanta } from './plantasAdminApi'
import { useEsEscritorio } from './useEsEscritorio'
import './ModalConfirmacionBaja.css'

const TITULO = 'Confirmar baja de planta medicinal'
const MAXIMO_MOTIVO = 300
const MOTIVO_OBLIGATORIO = 'El motivo de la baja es obligatorio'
const MENSAJE_ERROR_BAJA = 'No se pudo dar de baja la planta. Intente nuevamente'

function avisoDeError(error: unknown): Aviso {
  if (esErrorDeEstado(error, 401, 403)) return AVISO_SESION
  if (esErrorDeEstado(error, 400, 404)) return { mensaje: error.detalle ?? error.message, enlace: null }
  return { mensaje: MENSAJE_ERROR_BAJA, enlace: null }
}

export interface ModalConfirmacionBajaProps {
  plantaId: number
  nombrePlanta: string
  abierto: boolean
  alCerrar: () => void
  alDarDeBaja: () => void
}

export function ModalConfirmacionBaja({
  plantaId,
  nombrePlanta,
  abierto,
  alCerrar,
  alDarDeBaja,
}: ModalConfirmacionBajaProps) {
  const esEscritorio = useEsEscritorio()
  const [motivo, setMotivo] = useState('')
  const [errorMotivo, setErrorMotivo] = useState<string>()
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const [enviando, setEnviando] = useState(false)

  function cerrar() {
    if (enviando) return
    setMotivo('')
    setErrorMotivo(undefined)
    setAviso(null)
    alCerrar()
  }

  function mostrarError(error: unknown) {
    const errorDelCampo = esErrorDeEstado(error, 400) ? error.errores?.motivo : undefined
    if (errorDelCampo) setErrorMotivo(errorDelCampo)
    else setAviso(avisoDeError(error))
  }

  async function confirmar() {
    const motivoRecortado = motivo.trim()
    if (!motivoRecortado) {
      setErrorMotivo(MOTIVO_OBLIGATORIO)
      return
    }
    setErrorMotivo(undefined)
    setAviso(null)
    setEnviando(true)
    try {
      await darDeBajaPlanta(plantaId, { motivo: motivoRecortado })
      alDarDeBaja()
    } catch (error) {
      mostrarError(error)
    } finally {
      setEnviando(false)
    }
  }

  const contenido = (
    <div className="confirmar-baja-planta-contenido">
      <span role="img" aria-label="Advertencia" className="confirmar-baja-planta-icono">
        ⚠
      </span>
      <p>
        Vas a dar de baja la planta <strong>{nombrePlanta}</strong>.
      </p>
      <p>
        Las siembras y el material de bodega existentes se conservan, y la planta deja de aparecer en el catálogo
        público.
      </p>
      <CampoAreaTexto
        etiqueta="Motivo de la baja"
        valor={motivo}
        alCambiar={setMotivo}
        error={errorMotivo}
        maxLength={MAXIMO_MOTIVO}
        rows={3}
      />
      <p className="confirmar-baja-planta-contador">{`${motivo.length}/${MAXIMO_MOTIVO}`}</p>
      <AvisoFormulario aviso={aviso} />
    </div>
  )

  const botonConfirmar = (
    <Boton variante="peligro" onClick={() => void confirmar()} disabled={enviando}>
      Sí, dar de baja
    </Boton>
  )

  if (esEscritorio) {
    return (
      <Modal
        abierto={abierto}
        titulo={TITULO}
        alCancelar={cerrar}
        acciones={botonConfirmar}
        className="confirmar-baja-planta"
        cerrarConEscape
        cerrarAlTocarFondo
      >
        {contenido}
      </Modal>
    )
  }

  return (
    <BottomSheet
      abierto={abierto}
      titulo={TITULO}
      alCerrar={cerrar}
      pie={
        <div className="confirmar-baja-planta-botones">
          {botonConfirmar}
          <Boton variante="secundario" onClick={cerrar} disabled={enviando}>
            Cancelar
          </Boton>
        </div>
      }
    >
      {contenido}
    </BottomSheet>
  )
}
