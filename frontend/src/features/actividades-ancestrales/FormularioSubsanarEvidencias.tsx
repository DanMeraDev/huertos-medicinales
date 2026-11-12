import { useEffect, useState, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import {
  buscarActividadParaCorregir,
  reenviarEvidencias,
  subirFotografiaActividad,
} from './actividadesAncestralesApi'
import './FormularioSubsanarEvidencias.css'

export interface FormularioSubsanarEvidenciasProps {
  idActividad: number
  alReenviar: () => void
}

export function FormularioSubsanarEvidencias({
  idActividad,
  alReenviar,
}: FormularioSubsanarEvidenciasProps) {
  const [tituloSaber, setTituloSaber] = useState('')
  const [observacionMsp, setObservacionMsp] = useState('')
  const [numeroIntento, setNumeroIntento] = useState(1)
  const [descripcionBitacora, setDescripcionBitacora] = useState('')
  const [evidenciaFotoUrl, setEvidenciaFotoUrl] = useState('')
  const [archivoFoto, setArchivoFoto] = useState<File | null>(null)
  const [evidenciaVideoUrl, setEvidenciaVideoUrl] = useState('')

  const [errores, setErrores] = useState<Record<string, string>>({})
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)
  const [cargando, setCargando] = useState(true)
  const [subiendoFoto, setSubiendoFoto] = useState(false)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    buscarActividadParaCorregir(idActividad)
      .then((actividad) => {
        setTituloSaber(actividad.tituloSaber)
        setObservacionMsp(actividad.observacionMsp ?? '')
        setNumeroIntento(actividad.numeroIntento)
        setDescripcionBitacora(actividad.descripcionBitacora)
        setEvidenciaFotoUrl(actividad.evidenciaFotoUrl)
        setEvidenciaVideoUrl(actividad.evidenciaVideoUrl ?? '')
      })
      .catch((error) =>
        setNotificacion({
          tipo: 'error',
          mensaje:
            error instanceof Error ? error.message : 'Error al cargar la actividad ancestral observada',
        }),
      )
      .finally(() => setCargando(false))
  }, [idActividad])

  async function subirFoto() {
    if (!archivoFoto) {
      setErrores((prev) => ({ ...prev, evidenciaFotoUrl: 'Selecciona una fotografía de evidencia' }))
      return
    }
    setSubiendoFoto(true)
    try {
      const respuesta = await subirFotografiaActividad(archivoFoto)
      setEvidenciaFotoUrl(respuesta.fotografiaUrl)
      setArchivoFoto(null)
      setErrores((prev) => {
        const siguiente = { ...prev }
        delete siguiente.evidenciaFotoUrl
        return siguiente
      })
      setNotificacion({
        tipo: 'exito',
        mensaje: 'Fotografía subida correctamente',
      })
    } catch (error) {
      const mensaje = error instanceof Error ? error.message : 'Error al subir la fotografía'
      setNotificacion({ tipo: 'error', mensaje })
    } finally {
      setSubiendoFoto(false)
    }
  }

  function validar(): boolean {
    const nuevosErrores: Record<string, string> = {}

    if (!descripcionBitacora.trim()) {
      nuevosErrores.descripcionBitacora = 'La descripción de la bitácora es obligatoria'
    }

    if (!evidenciaFotoUrl.trim()) {
      nuevosErrores.evidenciaFotoUrl = 'La fotografía de evidencia es obligatoria para la revisión del MSP'
    }

    if (evidenciaVideoUrl.trim() && !/^https?:\/\/\S+$/.test(evidenciaVideoUrl.trim())) {
      nuevosErrores.evidenciaVideoUrl = 'El video de evidencia debe ser una URL válida'
    }

    setErrores(nuevosErrores)
    return Object.keys(nuevosErrores).length === 0
  }

  async function manejarEnvio(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setNotificacion(null)
    if (!validar()) {
      return
    }

    setEnviando(true)
    try {
      await reenviarEvidencias(idActividad, {
        descripcionBitacora: descripcionBitacora.trim(),
        evidenciaFotoUrl: evidenciaFotoUrl.trim(),
        evidenciaVideoUrl: evidenciaVideoUrl.trim() || null,
      })
      alReenviar()
    } catch (error) {
      const mensaje = error instanceof Error ? error.message : 'Error al reenviar las evidencias corregidas'
      setNotificacion({ tipo: 'error', mensaje })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="formulario-subsanar-contenedor">
      {notificacion && (
        <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
      )}

      {cargando ? (
        <p className="subsanar-cargando">Cargando la actividad observada…</p>
      ) : (
        <section className="subsanar-observacion" aria-label="Observación del Técnico MSP">
          <p className="subsanar-observacion-titulo">{tituloSaber}</p>
          {observacionMsp && <Notificacion tipo="error" mensaje={observacionMsp} />}
          <p className="subsanar-intento">Intento {numeroIntento}</p>
        </section>
      )}

      <form className="formulario-subsanar-grilla" onSubmit={manejarEnvio} noValidate>
        <CampoTexto
          etiqueta="Descripción de la Bitácora"
          valor={descripcionBitacora}
          alCambiar={setDescripcionBitacora}
          error={errores.descripcionBitacora}
          maxLength={2000}
          placeholder="Describe el ritual o faena realizada paso a paso"
          required
        />

        <div className="campo">
          <label className="campo-etiqueta" htmlFor="evidencia-foto-subsanar">
            Evidencia Fotográfica
          </label>
          <div className="campo-foto">
            <input
              id="evidencia-foto-subsanar"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(evento) => {
                const archivos = evento.target.files
                if (archivos && archivos.length > 0) {
                  setArchivoFoto(archivos[0])
                  setErrores((prev) => {
                    const siguiente = { ...prev }
                    delete siguiente.evidenciaFotoUrl
                    return siguiente
                  })
                }
              }}
            />
            <p className="campo-ayuda">
              JPG, PNG o WEBP (máx. 5 MB). La ruta se guarda como {evidenciaFotoUrl || 'pendiente'}
            </p>
          </div>
          {errores.evidenciaFotoUrl && (
            <p id="evidencia-foto-subsanar-error" className="campo-error">
              {errores.evidenciaFotoUrl}
            </p>
          )}
          <Boton
            variante="secundario"
            className="formulario-subsanar-foto-boton"
            onClick={subirFoto}
            disabled={subiendoFoto || !archivoFoto}
          >
            {subiendoFoto ? 'Subiendo…' : 'Reemplazar fotografía'}
          </Boton>
        </div>

        <CampoTexto
          etiqueta="Evidencia de Video (opcional)"
          type="url"
          valor={evidenciaVideoUrl}
          alCambiar={setEvidenciaVideoUrl}
          error={errores.evidenciaVideoUrl}
          maxLength={500}
          placeholder="https://..."
        />

        <div className="formulario-subsanar-acciones">
          <Boton
            type="submit"
            variante="primario"
            className="formulario-subsanar-boton"
            disabled={enviando}
          >
            {enviando ? 'Reenviando…' : 'Reenviar Evidencias Corregidas'}
          </Boton>
        </div>
      </form>
    </div>
  )
}
