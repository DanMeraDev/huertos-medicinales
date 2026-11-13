import { useState } from 'react'
import { urlFoto } from '../../api/fotos'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import {
  verificarActividad,
  type ActividadRevision,
  type EstadoVerificacion,
  type ResultadoVerificacion,
} from './mspRevisionApi'
import './mspRevision.css'

const OPCIONES_DICTAMEN: OpcionSelector[] = [
  { valor: 'APROBADA', etiqueta: 'Aprobar' },
  { valor: 'OBSERVADA_CORREGIR', etiqueta: 'Observar para corrección' },
  { valor: 'RECHAZADA_DEFINITIVA', etiqueta: 'Rechazar definitivamente' },
]

const MENSAJE_OBSERVACION_REQUERIDA =
  'Debe ingresar una observación detallada para justificar la observación o rechazo'

export interface DetalleAuditoriaActividadProps {
  actividad: ActividadRevision
  alVerificar: () => void
  alCerrar: () => void
}

export function DetalleAuditoriaActividad({
  actividad,
  alVerificar,
  alCerrar,
}: DetalleAuditoriaActividadProps) {
  const [dictamen, setDictamen] = useState<EstadoVerificacion>('APROBADA')
  const [observacion, setObservacion] = useState('')
  const [errorObservacion, setErrorObservacion] = useState('')
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)
  const [enviando, setEnviando] = useState(false)

  const urlEvidencia = urlFoto(actividad.evidenciaFotoUrl)

  async function emitirDictamen() {
    const requiereObservacion =
      dictamen === 'OBSERVADA_CORREGIR' || dictamen === 'RECHAZADA_DEFINITIVA'

    if (requiereObservacion && !observacion.trim()) {
      setErrorObservacion(MENSAJE_OBSERVACION_REQUERIDA)
      return
    }

    setErrorObservacion('')
    setNotificacion(null)
    setEnviando(true)
    try {
      const resultado = await verificarActividad(actividad.id, {
        nuevoEstado: dictamen,
        observacion: observacion.trim() || null,
      })
      setNotificacion({ tipo: 'exito', mensaje: mensajeConfirmacion(resultado) })
      alVerificar()
    } catch (error) {
      const mensaje = error instanceof Error ? error.message : 'Error al emitir el dictamen'
      setNotificacion({ tipo: 'error', mensaje })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="msp-auditoria" aria-label="Auditoría de la actividad ancestral">
      {notificacion && <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />}

      <header className="msp-auditoria-encabezado">
        <div>
          <h2 className="msp-auditoria-titulo">{actividad.tituloSaber}</h2>
          <p className="msp-auditoria-comunidad">{actividad.nombreComunidad}</p>
          <p className="msp-auditoria-fecha">
            Fecha de realización: {actividad.fechaRealizacion} · Intento {actividad.numeroIntento}
          </p>
        </div>
        <Boton variante="secundario" onClick={alCerrar}>
          Cerrar auditoría
        </Boton>
      </header>

      <div className="msp-auditoria-evidencias">
        <figure className="msp-auditoria-fotografia">
          {urlEvidencia ? (
            <img
              className="msp-auditoria-imagen"
              src={urlEvidencia}
              alt={`Evidencia fotográfica de ${actividad.tituloSaber}`}
            />
          ) : (
            <p className="msp-auditoria-sin-foto">La actividad no tiene evidencia fotográfica.</p>
          )}
          <figcaption className="msp-auditoria-pie">
            Evidencia fotográfica de la actividad
          </figcaption>
        </figure>

        <div className="msp-auditoria-detalle">
          <h3 className="msp-auditoria-subtitulo">Bitácora de la actividad</h3>
          <p className="msp-auditoria-bitacora">{actividad.descripcionBitacora}</p>

          {actividad.evidenciaVideoUrl && (
            <a
              className="msp-auditoria-video"
              href={actividad.evidenciaVideoUrl}
              target="_blank"
              rel="noreferrer"
            >
              Ver el video de la evidencia
            </a>
          )}

          <h3 className="msp-auditoria-subtitulo">Voluntarios participantes</h3>
          <ul className="msp-auditoria-participantes">
            {actividad.participantes.map((participante) => (
              <li key={participante.miembroId}>
                {participante.nombreCompleto} · {participante.horasTrabajadas} horas
              </li>
            ))}
          </ul>
        </div>
      </div>

      <form
        className="msp-auditoria-formulario"
        onSubmit={(evento) => {
          evento.preventDefault()
          void emitirDictamen()
        }}
        noValidate
      >
        <Selector
          etiqueta="Dictamen"
          opciones={OPCIONES_DICTAMEN}
          valor={dictamen}
          alCambiar={(valor) => setDictamen(valor as EstadoVerificacion)}
          required
        />

        <CampoTexto
          etiqueta="Observación"
          valor={observacion}
          alCambiar={setObservacion}
          error={errorObservacion}
          maxLength={500}
          placeholder="Justifica la observación o el rechazo de la actividad"
        />

        <Boton type="submit" variante="primario" disabled={enviando}>
          {enviando ? 'Emitiendo dictamen…' : 'Emitir Dictamen'}
        </Boton>
      </form>
    </section>
  )
}

function mensajeConfirmacion(resultado: ResultadoVerificacion): string {
  if (resultado.estado === 'APROBADA') {
    return `Actividad aprobada. Se acreditaron ${resultado.puntosComunidadAcreditados} puntos a la comunidad.`
  }
  if (resultado.estado === 'OBSERVADA_CORREGIR') {
    return 'Actividad observada. La comunidad debe corregir las evidencias y reenviarlas.'
  }
  return 'Actividad rechazada definitivamente.'
}
