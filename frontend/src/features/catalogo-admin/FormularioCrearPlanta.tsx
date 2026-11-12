import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiError } from '../../api/client'
import { Boton } from '../../components/Boton'
import { CargadorImagen } from '../../components/CargadorImagen'
import { Notificacion } from '../../components/Notificacion'
import { Tarjeta } from '../../components/Tarjeta'
import {
  CampoContraindicaciones,
  CampoPreparacion,
  CamposCultivo,
  CamposIdentificacion,
  CamposUsos,
  type CamposPlantaProps,
} from './CamposPlanta'
import { mensajeErrorFotografia, registrarPlanta, subirFotografiaPlanta } from './plantasAdminApi'
import type { CualidadTermica } from './types'
import { useEsEscritorio } from './useEsEscritorio'
import {
  construirSolicitud,
  DATOS_INICIALES,
  erroresDelPaso,
  validarPlanta,
  type DatosPlanta,
  type ErroresPlanta,
} from './validacionPlanta'
import './FormularioCrearPlanta.css'

const RUTA_CATALOGO = '/catalogo-plantas'
const MENSAJE_EXITO = 'Planta registrada exitosamente'
const MENSAJE_DUPLICADO = 'El nombre científico ya existe en el catálogo'
const MENSAJE_SESION = 'Necesitas iniciar sesión como Técnico MSP'
const MENSAJE_ERROR_REGISTRO = 'No se pudo registrar la planta. Intente nuevamente'

const PASOS: { titulo: string; campos: (keyof ErroresPlanta)[] }[] = [
  {
    titulo: 'Identificación y fotografía',
    campos: ['nombreComun', 'nombreCientifico', 'nombreKichwa', 'familiaBotanica', 'fotografiaUrl'],
  },
  {
    titulo: 'Parámetros agronómicos',
    campos: ['cualidadTermica', 'cicloCultivo', 'origen', 'altitudMinMsnm', 'altitudMaxMsnm', 'rangoAltitud',
      'regionesDistribucion'],
  },
  {
    titulo: 'Usos y dosificación',
    campos: ['categorias', 'usosMedicinales', 'preparacionDosificacion', 'contraindicaciones'],
  },
]

interface Aviso {
  mensaje: string
  conEnlaceLogin: boolean
}

function erroresDelServidor(error: ApiError): ErroresPlanta | null {
  if (error.status === 409) return { nombreCientifico: MENSAJE_DUPLICADO }
  if (error.status === 400) return error.errores ?? { rangoAltitud: error.detalle ?? error.message }
  return null
}

function avisoDeError(error: unknown): Aviso {
  const sinPermiso = error instanceof ApiError && (error.status === 401 || error.status === 403)
  return sinPermiso
    ? { mensaje: MENSAJE_SESION, conEnlaceLogin: true }
    : { mensaje: MENSAJE_ERROR_REGISTRO, conEnlaceLogin: false }
}

function AvisoFormulario({ aviso }: { aviso: Aviso | null }) {
  if (!aviso) return null
  return (
    <div className="crear-planta-aviso">
      <Notificacion tipo="error" mensaje={aviso.mensaje} />
      {aviso.conEnlaceLogin && <Link to="/login">Iniciar sesión</Link>}
    </div>
  )
}

function TarjetaFormulario({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <Tarjeta className="crear-planta-tarjeta">
      <h2 className="crear-planta-tarjeta-titulo">{titulo}</h2>
      {children}
    </Tarjeta>
  )
}

export function FormularioCrearPlanta() {
  const esEscritorio = useEsEscritorio()
  const navegar = useNavigate()
  const [datos, setDatos] = useState<DatosPlanta>(DATOS_INICIALES)
  const [fotografia, setFotografia] = useState<File | null>(null)
  const [errores, setErrores] = useState<ErroresPlanta>({})
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const [paso, setPaso] = useState(0)
  const [enviando, setEnviando] = useState(false)
  const esUltimoPaso = paso === PASOS.length - 1

  function actualizar<K extends keyof DatosPlanta>(campo: K, valor: DatosPlanta[K]) {
    setDatos((actuales) => ({ ...actuales, [campo]: valor }))
  }

  function mostrarErrores(nuevos: ErroresPlanta) {
    setErrores(nuevos)
    const pasoConError = PASOS.findIndex((delPaso) => delPaso.campos.some((campo) => nuevos[campo]))
    if (pasoConError >= 0) setPaso(pasoConError)
  }

  function siguiente() {
    const delPaso = erroresDelPaso(validarPlanta(datos), PASOS[paso].campos)
    setErrores(delPaso)
    if (Object.keys(delPaso).length === 0) setPaso(paso + 1)
  }

  async function subirFotografia() {
    return fotografia ? (await subirFotografiaPlanta(fotografia)).fotografiaUrl : null
  }

  async function enviar(cualidadTermica: CualidadTermica) {
    let fotografiaUrl: string | null
    try {
      fotografiaUrl = await subirFotografia()
    } catch (error) {
      setAviso({ mensaje: mensajeErrorFotografia(error), conEnlaceLogin: false })
      return
    }
    try {
      await registrarPlanta(construirSolicitud(datos, cualidadTermica, fotografiaUrl))
      navegar(RUTA_CATALOGO, { state: { mensajeExito: MENSAJE_EXITO } })
    } catch (error) {
      const delServidor = error instanceof ApiError ? erroresDelServidor(error) : null
      if (delServidor) mostrarErrores(delServidor)
      else setAviso(avisoDeError(error))
    }
  }

  async function guardar() {
    const nuevos = validarPlanta(datos)
    const cualidadTermica = datos.cualidadTermica
    if (Object.keys(nuevos).length > 0 || !cualidadTermica) {
      mostrarErrores(nuevos)
      return
    }
    setErrores({})
    setAviso(null)
    setEnviando(true)
    try {
      await enviar(cualidadTermica)
    } finally {
      setEnviando(false)
    }
  }

  function alEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!esEscritorio && !esUltimoPaso) siguiente()
    else void guardar()
  }

  const campos: CamposPlantaProps = { datos, errores, alCambiar: actualizar }
  const cargadorFoto = (
    <CargadorImagen etiqueta="Fotografía de la planta" archivo={fotografia} alCambiar={setFotografia} />
  )

  if (esEscritorio) {
    return (
      <form className="crear-planta" onSubmit={alEnviar} noValidate>
        <div className="crear-planta-columnas">
          <div className="crear-planta-columna">
            <TarjetaFormulario titulo="Taxonomía y nombres">
              <CamposIdentificacion {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Fotografía">{cargadorFoto}</TarjetaFormulario>
            <TarjetaFormulario titulo="Condiciones de cultivo">
              <CamposCultivo {...campos} />
            </TarjetaFormulario>
          </div>
          <div className="crear-planta-columna">
            <TarjetaFormulario titulo="Usos terapéuticos MSP">
              <CamposUsos {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Modos de preparación y dosificación">
              <CampoPreparacion {...campos} />
            </TarjetaFormulario>
            <TarjetaFormulario titulo="Contraindicaciones">
              <CampoContraindicaciones {...campos} />
            </TarjetaFormulario>
          </div>
        </div>
        <AvisoFormulario aviso={aviso} />
        <div className="crear-planta-acciones">
          <Boton variante="secundario" onClick={() => navegar(RUTA_CATALOGO)}>
            Cancelar
          </Boton>
          <Boton type="submit" className="crear-planta-guardar" disabled={enviando}>
            Guardar Planta en Catálogo
          </Boton>
        </div>
      </form>
    )
  }

  function contenidoDelPaso() {
    if (paso === 0) {
      return (
        <>
          <CamposIdentificacion {...campos} />
          {cargadorFoto}
        </>
      )
    }
    if (paso === 1) return <CamposCultivo {...campos} />
    return (
      <>
        <CamposUsos {...campos} />
        <CampoPreparacion {...campos} />
        <CampoContraindicaciones {...campos} />
      </>
    )
  }

  return (
    <form className="crear-planta" onSubmit={alEnviar} noValidate>
      <div
        className="crear-planta-progreso"
        role="progressbar"
        aria-label={`Paso ${paso + 1} de ${PASOS.length}`}
        aria-valuemin={1}
        aria-valuemax={PASOS.length}
        aria-valuenow={paso + 1}
      >
        <div className="crear-planta-progreso-avance" style={{ width: `${((paso + 1) / PASOS.length) * 100}%` }} />
      </div>
      <h2 className="crear-planta-paso-titulo">{PASOS[paso].titulo}</h2>
      <div className="crear-planta-campos">{contenidoDelPaso()}</div>
      <AvisoFormulario aviso={aviso} />
      <div className="crear-planta-barra-inferior">
        {paso > 0 && (
          <Boton variante="secundario" onClick={() => setPaso(paso - 1)}>
            Atrás
          </Boton>
        )}
        {esUltimoPaso ? (
          <Boton type="submit" disabled={enviando}>
            Guardar Planta
          </Boton>
        ) : (
          <Boton onClick={siguiente}>Siguiente</Boton>
        )}
      </div>
    </form>
  )
}
