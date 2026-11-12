import { useEffect, useId, useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CampoTexto } from '../../components/CampoTexto'
import { Insignia } from '../../components/Insignia'
import { KintiGuia } from '../../components/KintiGuia'
import { Notificacion } from '../../components/Notificacion'
import { Selector } from '../../components/Selector'
import {
  crearTalentoComunidad,
  obtenerActividadesParaTalentos,
  type ActividadOpcion,
  type TalentoComunidadResponse,
} from './talentosApi'
import './CrearTalentoForm.css'

export interface CrearTalentoFormProps {
  actividades?: ActividadOpcion[]
  alExito?: (talento: TalentoComunidadResponse) => void
}

const REGEX_NOMBRE_INSIGNIA = /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$/

export function CrearTalentoForm({ actividades, alExito }: CrearTalentoFormProps) {
  const [listaActividades, setListaActividades] = useState<ActividadOpcion[]>(actividades ?? [])
  const [actividadComunidadId, setActividadComunidadId] = useState('')
  const [nombreInsignia, setNombreInsignia] = useState('')
  const [metaRepeticiones, setMetaRepeticiones] = useState('')
  const [descripcion, setDescripcion] = useState('')

  const [errorActividad, setErrorActividad] = useState<string | undefined>()
  const [errorNombre, setErrorNombre] = useState<string | undefined>()
  const [errorMeta, setErrorMeta] = useState<string | undefined>()
  const [errorDescripcion, setErrorDescripcion] = useState<string | undefined>()
  const [mensajeError, setMensajeError] = useState<string | null>(null)
  const [mensajeExito, setMensajeExito] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const idDescripcion = useId()
  const idErrorDescripcion = `${idDescripcion}-error`

  useEffect(() => {
    if (actividades && actividades.length > 0) {
      setListaActividades(actividades)
      return
    }

    obtenerActividadesParaTalentos()
      .then((resultado) => setListaActividades(resultado))
      .catch(() => {
        setMensajeError('No se pudo cargar la lista de actividades de la comunidad')
      })
  }, [actividades])

  function validar(): boolean {
    let esValido = true
    setErrorActividad(undefined)
    setErrorNombre(undefined)
    setErrorMeta(undefined)
    setErrorDescripcion(undefined)
    setMensajeError(null)

    if (!actividadComunidadId) {
      setErrorActividad('Debes seleccionar una actividad asociada')
      esValido = false
    }

    const nombreLimpio = nombreInsignia.trim()
    if (!nombreLimpio) {
      setErrorNombre('El nombre de la insignia es obligatorio')
      esValido = false
    } else if (!REGEX_NOMBRE_INSIGNIA.test(nombreLimpio)) {
      setErrorNombre('El nombre de la insignia solo puede contener letras, números y espacios')
      esValido = false
    } else if (nombreLimpio.length > 120) {
      setErrorNombre('El nombre de la insignia no puede superar 120 caracteres')
      esValido = false
    }

    const metaNum = parseInt(metaRepeticiones, 10)
    if (!metaRepeticiones.trim() || Number.isNaN(metaNum) || metaNum <= 0) {
      setErrorMeta('La meta de repeticiones debe ser un número entero mayor a 0')
      esValido = false
    }

    const descLimpia = descripcion.trim()
    if (!descLimpia) {
      setErrorDescripcion('La descripción del reconocimiento es obligatoria')
      esValido = false
    } else if (descLimpia.length > 255) {
      setErrorDescripcion('La descripción no puede superar 255 caracteres')
      esValido = false
    }

    return esValido
  }

  async function manejarEnvio(evento: FormEvent) {
    evento.preventDefault()
    setMensajeExito(null)

    if (!validar()) {
      return
    }

    setEnviando(true)
    try {
      const nuevoTalento = await crearTalentoComunidad({
        actividadComunidadId: Number(actividadComunidadId),
        nombreInsignia: nombreInsignia.trim(),
        descripcion: descripcion.trim(),
        metaRepeticiones: parseInt(metaRepeticiones, 10),
      })

      setMensajeExito(
        'Medalla registrada con éxito. ¡Tus miembros ya pueden comenzar a acumular repeticiones para obtenerla!',
      )
      setNombreInsignia('')
      setDescripcion('')
      setMetaRepeticiones('')
      setActividadComunidadId('')
      alExito?.(nuevoTalento)
    } catch (err: unknown) {
      const mensaje =
        err instanceof ApiError && err.detail
          ? err.detail
          : 'Error al registrar la medalla de comunidad'
      setMensajeError(mensaje)
    } finally {
      setEnviando(false)
    }
  }

  const opcionesSelector = listaActividades.map((a) => ({
    valor: String(a.id),
    etiqueta: a.nombre,
  }))

  return (
    <div className="crear-talento-contenedor">
      <div className="crear-talento-banner">
        <KintiGuia variante="dia" tamano="sm" />
        <p className="crear-talento-banner-mensaje">
          ¡Fomentar la participación comunitaria es grandioso! Define medallas de talento para reconocer el esfuerzo
          constante de tus voluntarios en cada labor sagrada
        </p>
      </div>

      <div className="crear-talento-tarjeta">
        <h3 className="crear-talento-titulo">Definir nueva medalla o talento</h3>

        {mensajeExito && <Notificacion tipo="exito" mensaje={mensajeExito} />}
        {mensajeError && <Notificacion tipo="error" mensaje={mensajeError} />}

        <form onSubmit={manejarEnvio} className="crear-talento-formulario" noValidate>
          <Selector
            etiqueta="Actividad Asociada"
            opciones={opcionesSelector}
            valor={actividadComunidadId}
            alCambiar={setActividadComunidadId}
            error={errorActividad}
            disabled={enviando}
          />

          <CampoTexto
            etiqueta="Nombre de la Insignia o Talento"
            valor={nombreInsignia}
            alCambiar={setNombreInsignia}
            error={errorNombre}
            maxLength={120}
            placeholder="Ej. Guardián del Riego 1"
            disabled={enviando}
          />

          <CampoTexto
            type="number"
            etiqueta="Meta de Repeticiones"
            valor={metaRepeticiones}
            alCambiar={setMetaRepeticiones}
            error={errorMeta}
            min={1}
            placeholder="Ej. 50"
            disabled={enviando}
          />

          <div className="campo crear-talento-col-completa">
            <label htmlFor={idDescripcion} className="campo-etiqueta">
              Descripción del Reconocimiento
            </label>
            <textarea
              id={idDescripcion}
              className="campo-control"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              maxLength={255}
              rows={3}
              placeholder="Describe el logro que reconoce esta medalla comunitaria..."
              aria-invalid={Boolean(errorDescripcion)}
              aria-describedby={errorDescripcion ? idErrorDescripcion : undefined}
              disabled={enviando}
            />
            {errorDescripcion && (
              <p id={idErrorDescripcion} className="campo-error">
                {errorDescripcion}
              </p>
            )}
          </div>

          <div className="crear-talento-previsualizacion-bloque crear-talento-col-completa">
            <p className="crear-talento-previsualizacion-etiqueta">Previsualización de Medalla</p>
            <div data-testid="previsualizacion-medalla" className="crear-talento-previsualizacion-cuerpo">
              <svg
                className="crear-talento-icono-semilla"
                viewBox="0 0 48 48"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
              >
                <circle cx="24" cy="24" r="22" stroke="currentColor" strokeWidth="2" />
                <path
                  d="M24 12C24 12 16 20 16 28C16 32.4 19.6 36 24 36C28.4 36 32 32.4 32 28C32 20 24 12 24 12Z"
                  fill="currentColor"
                  fillOpacity="0.2"
                  stroke="currentColor"
                  strokeWidth="2"
                />
                <path d="M24 18V30" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                <path d="M24 24L20 20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                <path d="M24 26L28 22" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
              </svg>
              <Insignia texto={nombreInsignia.trim() || 'Nombre de la medalla'} tono="exito" />
              <small className="ficha-miembro-kinti-mensaje">
                {metaRepeticiones && parseInt(metaRepeticiones, 10) > 0
                  ? `Meta: ${metaRepeticiones} repeticiones`
                  : 'Meta por definir'}
              </small>
            </div>
          </div>

          <div className="crear-talento-acciones crear-talento-col-completa">
            <Boton type="submit" disabled={enviando}>
              {enviando ? 'Registrando medalla…' : 'Registrar Medalla de Comunidad'}
            </Boton>
          </div>
        </form>
      </div>
    </div>
  )
}
