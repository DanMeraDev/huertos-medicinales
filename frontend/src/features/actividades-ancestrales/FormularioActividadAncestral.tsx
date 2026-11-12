import { useEffect, useState, type FormEvent } from 'react'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Notificacion } from '../../components/Notificacion'
import { Selector, type OpcionSelector } from '../../components/Selector'
import {
  listarMiembrosComunidad,
  listarSaberesAncestrales,
  registrarActividadAncestral,
  subirFotografiaActividad,
  type FaseLunar,
  type MiembroComunidadOpcion,
  type SaberAncestralOpcion,
} from './actividadesAncestralesApi'
import { ListaParticipantesHoras, type ParticipanteNominado } from './ListaParticipantesHoras'
import './FormularioActividadAncestral.css'

const OPCIONES_FASE_LUNAR: OpcionSelector[] = [
  { valor: 'YANA_KILLA_OSCURA', etiqueta: 'Yana Killa - Luna Oscura' },
  { valor: 'WINAK_KILLA_CRECIENTE', etiqueta: 'Winak Killa - Luna Creciente' },
  { valor: 'PURA_KILLA_LLENA', etiqueta: 'Pura Killa - Luna Llena' },
  { valor: 'WANUK_KILLA_MENGUANTE', etiqueta: 'Wanuk Killa - Luna Menguante' },
]

export function FormularioActividadAncestral() {
  const [saberes, setSaberes] = useState<SaberAncestralOpcion[]>([])
  const [miembros, setMiembros] = useState<MiembroComunidadOpcion[]>([])
  const [saberAncestralId, setSaberAncestralId] = useState('')
  const [fechaRealizacion, setFechaRealizacion] = useState('')
  const [faseLunarEjecucion, setFaseLunarEjecucion] = useState<FaseLunar>('' as FaseLunar)
  const [descripcionBitacora, setDescripcionBitacora] = useState('')
  const [evidenciaFotoUrl, setEvidenciaFotoUrl] = useState('')
  const [archivoFoto, setArchivoFoto] = useState<File | null>(null)
  const [evidenciaVideoUrl, setEvidenciaVideoUrl] = useState('')
  const [participantes, setParticipantes] = useState<ParticipanteNominado[]>([])

  const [errores, setErrores] = useState<Record<string, string>>({})
  const [notificacion, setNotificacion] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)
  const [cargando, setCargando] = useState(false)
  const [subiendoFoto, setSubiendoFoto] = useState(false)

  useEffect(() => {
    listarSaberesAncestrales()
      .then(setSaberes)
      .catch(() => setSaberes([]))
    listarMiembrosComunidad()
      .then(setMiembros)
      .catch(() => setMiembros([]))
  }, [])

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

    if (!saberAncestralId) {
      nuevosErrores.saberAncestralId = 'El saber ancestral es obligatorio'
    }

    if (!fechaRealizacion) {
      nuevosErrores.fechaRealizacion = 'La fecha de realización es obligatoria'
    }

    if (!faseLunarEjecucion) {
      nuevosErrores.faseLunarEjecucion = 'La fase lunar de ejecución es obligatoria'
    }

    if (!descripcionBitacora.trim()) {
      nuevosErrores.descripcionBitacora = 'La descripción de la bitácora es obligatoria'
    }

    if (!evidenciaFotoUrl.trim()) {
      nuevosErrores.evidenciaFotoUrl = 'La fotografía de evidencia es obligatoria para la revisión del MSP'
    }

    if (evidenciaVideoUrl.trim() && !/^https?:\/\/\S+$/.test(evidenciaVideoUrl.trim())) {
      nuevosErrores.evidenciaVideoUrl = 'El video de evidencia debe ser una URL válida'
    }

    if (participantes.length === 0) {
      nuevosErrores.participantes = 'Debe registrar al menos un miembro participante en la actividad ancestral'
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

    setCargando(true)
    try {
      await registrarActividadAncestral({
        saberAncestralId: Number(saberAncestralId),
        fechaRealizacion,
        faseLunarEjecucion,
        descripcionBitacora: descripcionBitacora.trim(),
        evidenciaFotoUrl: evidenciaFotoUrl.trim(),
        evidenciaVideoUrl: evidenciaVideoUrl.trim() || null,
        participantes: participantes.map((participante) => ({
          miembroId: participante.miembroId,
          horasTrabajadas: participante.horasTrabajadas,
        })),
      })
      setNotificacion({
        tipo: 'exito',
        mensaje: '¡Actividad ancestral registrada! Quedó en revisión del Técnico MSP',
      })
      setSaberAncestralId('')
      setFechaRealizacion('')
      setFaseLunarEjecucion('' as FaseLunar)
      setDescripcionBitacora('')
      setEvidenciaFotoUrl('')
      setArchivoFoto(null)
      setEvidenciaVideoUrl('')
      setParticipantes([])
      setErrores({})
    } catch (error) {
      const mensaje = error instanceof Error ? error.message : 'Error al registrar la actividad ancestral'
      setNotificacion({ tipo: 'error', mensaje })
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="formulario-actividad-ancestral-contenedor">
      {notificacion && (
        <Notificacion tipo={notificacion.tipo} mensaje={notificacion.mensaje} />
      )}

      <form className="formulario-actividad-ancestral-grilla" onSubmit={manejarEnvio} noValidate>
        <Selector
          etiqueta="Hito / Saber Ancestral"
          opciones={saberes.map((saber) => ({
            valor: String(saber.id),
            etiqueta: `${saber.ordenPaso}. ${saber.titulo}`,
          }))}
          valor={saberAncestralId}
          alCambiar={setSaberAncestralId}
          error={errores.saberAncestralId}
          required
        />

        <CampoTexto
          etiqueta="Fecha de Realización"
          type="date"
          valor={fechaRealizacion}
          alCambiar={setFechaRealizacion}
          error={errores.fechaRealizacion}
          required
        />

        <Selector
          etiqueta="Fase Lunar de Ejecución"
          opciones={OPCIONES_FASE_LUNAR}
          valor={faseLunarEjecucion}
          alCambiar={(valor) => setFaseLunarEjecucion(valor as FaseLunar)}
          error={errores.faseLunarEjecucion}
          required
        />

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
          <label className="campo-etiqueta" htmlFor="evidencia-foto">
            Evidencia Fotográfica
          </label>
          <div className="campo-foto">
            <input
              id="evidencia-foto"
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
            <p id="evidencia-foto-error" className="campo-error">
              {errores.evidenciaFotoUrl}
            </p>
          )}
          <Boton
            variante="secundario"
            className="formulario-actividad-foto-boton"
            onClick={subirFoto}
            disabled={subiendoFoto || !archivoFoto}
          >
            {subiendoFoto ? 'Subiendo…' : 'Subir fotografía'}
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

        <ListaParticipantesHoras
          miembros={miembros}
          participantes={participantes}
          alAgregar={(participante) => setParticipantes([...participantes, participante])}
          alQuitar={(miembroId) =>
            setParticipantes(participantes.filter((participante) => participante.miembroId !== miembroId))
          }
          error={errores.participantes}
        />

        <div className="formulario-actividad-ancestral-acciones">
          <Boton
            type="submit"
            variante="primario"
            className="formulario-actividad-ancestral-boton"
            disabled={cargando}
          >
            Enviar a Revisión MSP
          </Boton>
        </div>
      </form>
    </div>
  )
}
